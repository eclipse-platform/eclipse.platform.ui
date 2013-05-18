package org.eclipse.e4.tools.services;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.osgi.service.localization.BundleLocalization;

/**
 * Service that is responsible for creating and managing message class instances.
 */
public interface IMessageFactoryService {
	
	/**
	 * Returns an instance of the of a given messages class for the given {@link Locale}.
	 * If configured it caches the created instances and return the already created instances.
	 * Otherwise a new instance will be created.
	 * 
	 * @param locale The {@link Locale} for which the message class instance is requested.
	 * @param messages The type of the message class whose instance is requested.
	 * @param localization The service that is needed to retrieve {@link ResourceBundle} objects from a bundle 
	 * 			with a given locale.
	 * @return An instance of the given messages class and {@link Locale}.
	 * 
	 * @throws InstantiationException if the requested message class represents an abstract class, an interface, 
	 * 			an array class, a primitive type, or void; 
	 * 			or if the class has no nullary constructor; 
	 * 			or if the instantiation fails for some other reason.
	 * @throws IllegalAccessException if the requested message class or its nullary constructor is not accessible.
	 */
	public <M> M getMessageInstance(final Locale locale, final Class<M> messages, BundleLocalization localization)
			throws InstantiationException, IllegalAccessException;
}
