package org.eclipse.e4.tools.services;

import org.eclipse.osgi.service.localization.BundleLocalization;

public interface IMessageFactoryService {
	public <M> M createInstance(final String locale, final Class<M> messages, BundleLocalization localization)
			throws InstantiationException, IllegalAccessException;
}
