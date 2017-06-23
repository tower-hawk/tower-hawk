package org.towerhawk.check.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.check.AbstractCheck;
import org.towerhawk.check.run.CheckRun;
import org.towerhawk.spring.Configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class PortCheck extends AbstractCheck {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private int port;
	private String host = Configuration.get().getDefaultHost();
	private int localPort = -1;
	private String localHost = Configuration.get().getDefaultLocalHost();
	private String send = null;
	private String expectRegex = null;
	private String outputCharset = "UTF8";
	private boolean matchIsCritical = true;
	private boolean includeOutputInContext = false;

	@Override
	protected void doRun(CheckRun.Builder builder) {
		log.info("Running PortCheck on port {}", port);
		Socket socket = null;
		try {
			if (localPort <= 0 || Configuration.DEFAULT_LOCAL_HOST.equals(localHost)) {
				socket = new Socket(host, port);
			} else {
				InetAddress address = InetAddress.getByName(localHost);
				socket = new Socket(host, port, address, localPort);
			}
			if (send != null && !send.isEmpty() && expectRegex != null && !expectRegex.isEmpty()) {
				OutputStream os = socket.getOutputStream();
				os.write(send.getBytes(outputCharset));
				os.flush();
				String result = transformInputStream(socket.getInputStream());
				if (includeOutputInContext) {
					builder.addContext("output", result);
				}
				if (result.matches(expectRegex)) {
					builder.succeeded();
					builder.addContext("expectRegex", "found");
				} else {
					builder.addContext("expectRegex", "not found");
					if (matchIsCritical) {
						builder.critical();
					} else {
						builder.warning();
					}
				}
			} else {
				builder.succeeded();
				builder.addContext("connection", String.format("Connection to %s:%d successful.", host, port));
			}
		} catch (Exception e) {
			builder.addContext("connection", String.format("Connection to %s:%d failed", host, port));
			builder.critical();
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				log.error("Unable to close connection", e);
			}
		}
	}

	private int getPort() {
		return port;
	}

	private void setPort(int port) {
		this.port = port;
	}

	private String getHost() {
		return host;
	}

	private void setHost(String host) {
		this.host = host;
	}

	private int getLocalPort() {
		return localPort;
	}

	private void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	private String getLocalHost() {
		return localHost;
	}

	private void setLocalHost(String localHost) {
		this.localHost = localHost;
	}

	private String getSend() {
		return send;
	}

	private void setSend(String send) {
		this.send = send;
	}

	private String getExpectRegex() {
		return expectRegex;
	}

	private void setExpectRegex(String expectRegex) {
		this.expectRegex = expectRegex;
	}

	private String getOutputCharset() {
		return outputCharset;
	}

	private void setOutputCharset(String outputCharset) {
		this.outputCharset = outputCharset;
	}

	private boolean isMatchIsCritical() {
		return matchIsCritical;
	}

	private void setMatchIsCritical(boolean matchIsCritical) {
		this.matchIsCritical = matchIsCritical;
	}
}
