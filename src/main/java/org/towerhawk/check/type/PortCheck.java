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
				sendAndReadFromSocket(builder, socket);
			} else {
				builder.succeeded();
				builder.addContext("connection", String.format("Connection to %s:%d successful.", host, port));
			}

		} catch (Exception e) {
			builder.addContext("connection", String.format("Connection to %s:%d failed", host, port));
			builder.error(e);
			builder.critical();
			log.warn("Failing getCheck {} due to exception {}", getId(), e);
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				log.error("Unable to close connection for {}", getId(), e);
			}
		}
	}

	protected void sendAndReadFromSocket(CheckRun.Builder builder, Socket socket) {
		try {
			socket.setSoTimeout(getTimeRemaining());
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
		} catch (Exception e) {
			builder.addContext("send", String.format("Exception caught when trying to send or read from socket %s", e.getMessage()));
			log.warn("Exception caught on getCheck {} when trying to send or read from socket {}", getId(), e);
		}
	}

	protected int getTimeRemaining() {
		long timeRemaining = getTimeoutMs() - (System.currentTimeMillis() - runningStartTimeMs);
		if (timeRemaining < 0) {
			throw new IllegalStateException("Check is timed out before running expected output");
		}
		return (int) timeRemaining;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public String getLocalHost() {
		return localHost;
	}

	public void setLocalHost(String localHost) {
		this.localHost = localHost;
	}

	public String getSend() {
		return send;
	}

	public void setSend(String send) {
		this.send = send;
	}

	public String getExpectRegex() {
		return expectRegex;
	}

	public void setExpectRegex(String expectRegex) {
		this.expectRegex = expectRegex;
	}

	public String getOutputCharset() {
		return outputCharset;
	}

	public void setOutputCharset(String outputCharset) {
		this.outputCharset = outputCharset;
	}

	public boolean isMatchIsCritical() {
		return matchIsCritical;
	}

	public void setMatchIsCritical(boolean matchIsCritical) {
		this.matchIsCritical = matchIsCritical;
	}

	public boolean isIncludeOutputInContext() {
		return includeOutputInContext;
	}

	public void setIncludeOutputInContext(boolean includeOutputInContext) {
		this.includeOutputInContext = includeOutputInContext;
	}
}
