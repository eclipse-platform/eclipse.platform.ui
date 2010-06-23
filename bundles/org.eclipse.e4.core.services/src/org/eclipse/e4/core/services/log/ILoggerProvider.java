package org.eclipse.e4.core.services.log;

public interface ILoggerProvider {
	public Logger getClassLogger(Class<?> clazz);
}
