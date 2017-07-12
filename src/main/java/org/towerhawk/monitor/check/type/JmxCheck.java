package org.towerhawk.monitor.check.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.serde.resolver.CheckType;
import org.towerhawk.spring.config.Configuration;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@CheckType("jmx")
public class JmxCheck extends AbstractCheck {

	/**
	 * Takes precedences over host and port
	 */
	private String url;
	private String host;
	private int port = -1;
	private transient String connectionString;
	private transient JMXServiceURL serviceUrl;
	private transient JMXConnector jmxConnector = null;
	private transient MBeanServerConnection mbeanConn = null;
	private transient long connectionCreation = 0;

	/**
	 * Takes precedence over mbean, attribute, and path if that many separators are available
	 */
	private String mbeanPath;
	private ObjectName mbean;
	private String attribute;
	private String path;

	/**
	 * Takes precedence over baseMbean, baseAttribute, and basePath if that many separators are available
	 */
	private String baseMbeanPath;
	private ObjectName baseMbean;
	private String baseAttribute;
	private String basePath;

	private Class resultClass = Long.class;

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		try {
			if (System.currentTimeMillis() - connectionCreation > getConfiguration().getJMXConnectionRefreshMs()) {
				refreshConnection();
			}
			// now query to get the beans or whatever
			Object attributeResult = getValueFromPath(mbeanConn.getAttribute(mbean, attribute), path);
			Object baseAttributeResult = null;
			if (baseMbean != null && baseAttribute != null) {
				baseAttributeResult = getValueFromPath(mbeanConn.getAttribute(baseMbean, baseAttribute), basePath);
			}
			try {
				if (baseAttributeResult == null) {
					getThreshold().evaluate(builder, attributeResult);
				} else {
					Number numericAttributeResult = (Number) attributeResult;
					Number numericBaseAttributeResult = (Number) baseAttributeResult;
					//TODO figure out what to do if numericBaseAttributeResult is 0
					getThreshold().evaluate(builder, numericAttributeResult.doubleValue() / numericBaseAttributeResult.doubleValue());
				}
			} catch (Exception e) {
				builder.unknown().error(e);
				log.error("Unable to evaluate threshold for {} of class {}", attributeResult, attributeResult.getClass(), e);
			}


		} catch (Exception e) {
			log.error("Error while communicating with server {}", url, e);
			builder.critical().error(e);
		}
	}

	private Object getValueFromPath(Object attributeResult, String path) {
		if (path == null) {
			return attributeResult;
		}
		Object attributePathResult = null;
		if (attributeResult instanceof CompositeDataSupport) {
			CompositeDataSupport cds = (CompositeDataSupport) attributeResult;
			attributePathResult = cds.get(path);
		} else if (attributeResult instanceof Map) {
			Map map = (Map) attributeResult;
			attributePathResult = map.get(path);
		}
		if (attributePathResult == null) {
			throw new RuntimeException("Path " + path + " could not be found");
		}
		return attributePathResult;
	}

	@Override
	@SneakyThrows
	public void init(Check check, Configuration configuration, App app, String id) {
		super.init(check, configuration, app, id);
		resolveUrl(configuration);
		resolveMbeanPath(configuration);
		resolveBaseMbeanPath(configuration);
		serviceUrl = new JMXServiceURL(connectionString);
		refreshConnection();
	}

	protected final void refreshConnection() {
			closeConnection();
			createConnection();
	}

	protected final void closeConnection() {
		try {
			if (jmxConnector != null) {
				jmxConnector.close();
			}
		} catch (Exception e) {
			log.error("Unable to close jmx connection to {}", url, e);
		}
	}

	@SneakyThrows
	protected final void createConnection() {
		jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
		mbeanConn = jmxConnector.getMBeanServerConnection();
		connectionCreation = System.currentTimeMillis();
	}

	private void resolveUrl(Configuration configuration) {
		if (url != null && !url.isEmpty()) {
			// do nothing - keep the url as is
		} else if (host != null && !host.isEmpty()) {
			if (port != -1) {
				url = host + ":" + port;
			} else {
				throw new IllegalStateException("port must be set if host is set");
			}
		} else if (port != -1) {
			url = "localhost:" + port;
		}
		connectionString = "service:jmx:rmi:///jndi/rmi://" + url + "/jmxrmi";
	}

	private void resolveMbeanPath(Configuration configuration) {
		if (mbean == null && mbeanPath != null) {
			String[] pathSplit = mbeanPath.split(configuration.getMbeanPathSeparator());
			setMbean(pathSplit[0]);
			if (pathSplit.length > 1) {
				setAttribute(pathSplit[1]);
			}
			if (pathSplit.length > 2) {
				setPath(pathSplit[2]);
			}
		}
	}

	//This must be called after resolveMbeanPath()
	private void resolveBaseMbeanPath(Configuration configuration) {
		if (baseMbean == null && baseMbeanPath != null) {
			String[] pathSplit = baseMbeanPath.split(configuration.getMbeanPathSeparator());
			setBaseMbean(pathSplit[0]);
			if (pathSplit.length > 1) {
				setBaseAttribute(pathSplit[1]);
			}
			if (pathSplit.length > 2) {
				setBasePath(pathSplit[2]);
			}
		}
		if (baseMbean == null && (basePath != null || baseAttribute != null)) {
			baseMbean = mbean;
		}
		if (baseAttribute == null && basePath != null) {
			baseAttribute = attribute;
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (jmxConnector != null) {
			try {
				jmxConnector.close();
			} catch (IOException e) {
				log.error("Unable to close connection to {}", url, e);
			}
		}
	}

	@SneakyThrows
	private void setMbean(String name) {
		this.mbean = new ObjectName(name);
	}

	@SneakyThrows
	private void setBaseMbean(String name) {
		this.baseMbean = new ObjectName(name);
	}

}
