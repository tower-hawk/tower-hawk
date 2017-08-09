package org.towerhawk.monitor.check.type;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.Status;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.monitor.check.threshold.SimpleRegexThreshold;
import org.towerhawk.serde.resolver.CheckType;

import java.io.OutputStream;
import java.net.Socket;

@Slf4j
@Getter
@Setter
@CheckType("portResponse")
public class PortResponseCheck extends PortCheck {

	private String send = null;
	private String outputCharset = "UTF8";
	private SimpleRegexThreshold response;

	@Override
	protected Status extension(CheckRun.Builder builder, RunContext context) {
		try {
			if (send == null || send.isEmpty()) {
				throw new IllegalStateException("send and expectRegex must not be empty or null");
			}
			Socket socket = (Socket) context.get(SOCKET);
			socket.setSoTimeout(getMsRemaining(true));
			OutputStream os = socket.getOutputStream();
			os.write(send.getBytes(outputCharset));
			os.flush();
			String result = transformInputStream(socket.getInputStream());
			return response.evaluate(builder, result);
		} catch (Exception e) {
			builder.critical().error(new RuntimeException("Exception caught when trying to send or read from socket", e));
			log.warn("Exception caught on check {} when trying to send or read from socket", getFullName(), e);
			return builder.getStatus();
		}
	}
}
