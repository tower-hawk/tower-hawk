package org.towerhawk.monitor.check.type;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.CheckContext;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.threshold.SimpleNumericThreshold;
import org.towerhawk.serde.resolver.CheckType;
import org.towerhawk.spring.config.Configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

@Slf4j
@Getter
@Setter
@CheckType("port")
public class PortCheck extends AbstractCheck {

	private int port;
	private String host;
	private int localPort = -1;
	private String localHost;
	private String send = null;
	private String expectRegex = null;
	private String outputCharset = "UTF8";
	private boolean matchIsCritical = true;
	private boolean includeOutputInContext = false;

	public PortCheck() {
		setThreshold(SimpleNumericThreshold.builder().warnUpper(1000).critUpper(3000).build());
	}

	@Override
	public void init(Check check, Configuration configuration, App app, String id) {
		super.init(check, configuration, app, id);
		if (host == null) {
			host = configuration.getDefaultHost();
		}
		if (localHost == null) {
			localHost = configuration.getDefaultLocalHost();
		}
	}

	@Override
	protected void doRun(CheckRun.Builder builder, CheckContext checkContext) throws InterruptedException {
		Socket socket = null;
		long start = java.lang.System.currentTimeMillis();
		try {
			if (localPort <= 0 || Configuration.DEFAULT_LOCAL_HOST.equals(localHost)) {
				log.info("Running port check on {}:{}", host, port);
				socket = new Socket(host, port);
			} else {
				log.info("Running port check on {}:{} from {}:{}", host, port, localHost, localPort);
				InetAddress address = InetAddress.getByName(localHost);
				socket = new Socket(host, port, address, localPort);
			}
			getThreshold().evaluate(builder, java.lang.System.currentTimeMillis() - start);
			if (send != null && !send.isEmpty() && expectRegex != null && !expectRegex.isEmpty()) {
				sendAndReadFromSocket(builder, socket);
			} else {
				builder.succeeded().addContext("connection", String.format("Connection to %s:%d successful", host, port));
			}

		} catch (Exception e) {
			builder.critical().error(e).addContext("connection", String.format("Connection to %s:%d failed", host, port));
			log.warn("Failing port check {} due to exception", getFullName(), e);
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				log.error("Unable to close connection for {} to {}:{}", getFullName(), socket.getInetAddress().getHostName(), socket.getPort(), e);
			}
		}
	}

	protected void sendAndReadFromSocket(CheckRun.Builder builder, Socket socket) {
		try {
			socket.setSoTimeout(getMsRemaining(true));
			OutputStream os = socket.getOutputStream();
			os.write(send.getBytes(outputCharset));
			os.flush();
			String result = transformInputStream(socket.getInputStream());
			if (includeOutputInContext) {
				builder.addContext("output", result);
			}
			if (result.matches(expectRegex)) {
				builder.succeeded().addContext("expectRegex", "found");
			} else {
				builder.addContext("expectRegex", "not found");
				if (matchIsCritical) {
					builder.critical();
				} else {
					builder.warning();
				}
			}
		} catch (Exception e) {
			builder.critical().error(new RuntimeException("Exception caught when trying to send or read from socket", e));
			log.warn("Exception caught on getCheck {} when trying to send or read from socket {}", getFullName(), e);
		}
	}
}
