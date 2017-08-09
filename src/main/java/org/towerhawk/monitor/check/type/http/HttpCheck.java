package org.towerhawk.monitor.check.type.http;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.Status;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.monitor.check.threshold.Threshold;
import org.towerhawk.serde.resolver.CheckType;
import org.towerhawk.spring.config.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@Slf4j
@CheckType("http")
@Setter
public class HttpCheck extends AbstractCheck {
	protected String endpoint;
	protected String method = "GET";
	protected String body;
	protected Auth auth = new Auth();
	protected boolean includeResponseInResult = true;

	protected HttpComponentsClientHttpRequestFactory requestFactory;

	protected CloseableHttpClient configureHttpClient() {
		HttpClientBuilder builder = HttpClients.custom();

		if ( StringUtils.hasText(auth.getUsername()) ) {
			Credentials creds = new UsernamePasswordCredentials(auth.getUsername(), auth.getPassword());
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY, creds);
			builder.setDefaultCredentialsProvider(provider);
		}

		return builder.build();
	}

	protected ClientHttpRequest getClientHttpRequest() {
		try {
			return requestFactory.createRequest(new URI(endpoint), HttpMethod.resolve(method));
		} catch (IOException | URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void doRun(CheckRun.Builder builder, RunContext context) throws InterruptedException {
		try (ClientHttpResponse response = getClientHttpRequest().execute(); ){
			Threshold t = getThreshold();
			String asString = IOUtils.toString(response.getBody(), StandardCharsets.UTF_8);
			if (includeResponseInResult) {
				builder.addContext("httpResponse", asString);
			}
			Object processed = processResponse(asString);
			Status status = t.evaluate(builder, processed);
			if ( status == Status.SUCCEEDED ) {
				builder.succeeded().addContext("connection", String.format("Connection to %s successful", endpoint));
			}
		} catch ( IOException e ) {
			throw new IllegalStateException(e);
		}
	}

	// If someone wants to work on this. We should be able to specify a
	// response processor, which converts the http response, or at least
	// the body, into something stronger than a string
	protected Object processResponse(String response) {
		return response;
	}

	@Override
	public void init(Check check, Configuration configuration, App app, String id) {
		if ( !StringUtils.hasText(endpoint) ) {
			endpoint = new StringBuilder("http://").append(configuration.getDefaultHost()).toString();
		}

		HttpClient client = configureHttpClient();
		requestFactory = new HttpComponentsClientHttpRequestFactory(client) {
			@Override
			protected void postProcessHttpRequest(HttpUriRequest request) {
				if ( request instanceof HttpEntityEnclosingRequest) {
					((HttpEntityEnclosingRequest) request).setEntity(new ByteArrayEntity(body.getBytes(StandardCharsets.UTF_8)));
				}
			}
		};

		super.init(check, configuration, app, id);
	}

	@Getter
	@Setter
	public class Auth {
		protected String username;
		protected String password;
	}
}
