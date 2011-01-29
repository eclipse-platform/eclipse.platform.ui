package org.eclipse.e4.tools.services;

public interface IMessageFactoryService {
	public <M> M createInstance(final String locale, final Class<M> messages)
			throws InstantiationException, IllegalAccessException;
}
