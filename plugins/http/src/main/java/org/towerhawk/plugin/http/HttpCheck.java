package org.towerhawk.plugin.http;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Slf4j
@Extension
@TowerhawkType("http")
public class HttpCheck implements CheckExecutor {
	protected transient HttpClient client;
	protected transient RequestParamCache requestParamCache = new RequestParamCache();
	protected String endpoint;
	protected String method;
	protected String body;
	protected Map<String, String> headers = new LinkedHashMap<>();
	protected Auth auth = new Auth();
	protected boolean includeResponseInResult = true;

	protected CloseableHttpClient configureHttpClient() {
		HttpClientBuilder builder = HttpClients.custom();

		if (auth.getUsername() != null && !auth.getUsername().isEmpty()) {
			Credentials creds = new UsernamePasswordCredentials(auth.getUsername(), auth.getPassword());
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY, creds);
			builder.setDefaultCredentialsProvider(provider);
		}

		return builder.build();
	}

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception {
		HttpResponse response = null;
		try {
			HttpUriRequest request = createRequest();
			ExecutionResult result = ExecutionResult.startTimer();
			response = client.execute(request);
			result.complete();
			String asString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			result.setResult(asString);
			result.addResult("status", response.getStatusLine().getStatusCode());
			result.addResult("headers", response.getAllHeaders());
			if (includeResponseInResult) {
				builder.addContext("httpResponse", asString);
				builder.addContext("connection", String.format("Connection to %s successful", endpoint));
			}
			return result;
		} finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
					if (response instanceof AutoCloseable) {
						((AutoCloseable) response).close();
					}
				} catch (Exception e) {
					log.warn("Caught exception while closing response", e);
				}
			}
		}
	}

	@Override
	public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {
		if (endpoint == null || endpoint.isEmpty()) {
			endpoint = config.getString("defaultHost","localhost");
		}
		if (!endpoint.contains("://")) {
			endpoint = "http://" + endpoint;
		}

		if (method == null || method.isEmpty()) {
			if (body == null || body.isEmpty()) {
				method = "GET";
			} else {
				method = "POST";
			}
		}

		requestParamCache.setUri(endpoint);
		requestParamCache.setHttpMethod(method);
		requestParamCache.setHeaderArray(headers);

		client = configureHttpClient();
	}

	protected HttpUriRequest createRequest() {
		HttpUriRequest request = createHttpUriRequest(requestParamCache.getUri(), requestParamCache.getHttpMethod());
		request.setHeaders(requestParamCache.getHeaderArray());
		if (request instanceof HttpEntityEnclosingRequest) {
			((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(body, StandardCharsets.UTF_8));
		}
		return request;
	}

	protected HttpUriRequest createHttpUriRequest(URI uri, String httpMethod) {
		switch (httpMethod) {
			case "GET":
				return new HttpGet(uri);
			case "HEAD":
				return new HttpHead(uri);
			case "POST":
				return new HttpPost(uri);
			case "PUT":
				return new HttpPut(uri);
			case "PATCH":
				return new HttpPatch(uri);
			case "DELETE":
				return new HttpDelete(uri);
			case "OPTIONS":
				return new HttpOptions(uri);
			case "TRACE":
				return new HttpTrace(uri);
			default:
				throw new IllegalArgumentException("Invalid HTTP method: " + httpMethod);
		}
	}

	@Getter
	protected class RequestParamCache {
		private URI uri;
		private String httpMethod;
		private Header[] headerArray;

		public void setUri(String endpoint) {
			try {
				uri = new URI(endpoint);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("endpoint '" + endpoint + "' cannot be converted to URI", e);
			}
		}

		public void setHttpMethod(String method) {
			httpMethod = method.toUpperCase();
		}

		public void setHeaderArray(Map<String, String> headers) {
			headerArray = headers.entrySet().stream().map(
					e -> new BasicHeader(e.getKey(), e.getValue()))
					.collect(Collectors.toList())
					.toArray(new Header[headers.size()]);
		}
	}

	@Override
	public void close() throws Exception {
		if (client instanceof Closeable) {
			((Closeable) client).close();
		}
	}

	@Getter
	@Setter
	public class Auth {
		protected String username;
		protected String password;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers.putAll(headers);
	}
}
