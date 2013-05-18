/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Dirk Fauth <dirk.fauth@gmail.com> - modifications to instance creation
 ******************************************************************************/
package org.eclipse.e4.tools.services.impl;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.e4.tools.services.IMessageFactoryService;
import org.eclipse.e4.tools.services.Message;
import org.eclipse.e4.tools.services.Message.ReferenceType;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class MessageFactoryServiceImpl implements IMessageFactoryService {

	// Cache so when multiple instance use the same message class
	private Map<Object, Reference<Object>> SOFT_CACHE = Collections
			.synchronizedMap(new HashMap<Object, Reference<Object>>());

	private Map<Object, Reference<Object>> WEAK_CACHE = Collections
			.synchronizedMap(new HashMap<Object, Reference<Object>>());

	private int CLEANUPCOUNT = 0;

	@Override
	public <M> M getMessageInstance(final Locale locale, final Class<M> messages, final BundleLocalization localization)
			throws InstantiationException, IllegalAccessException {
		String key = messages.getName() + "_" + locale; //$NON-NLS-1$

		final Message annotation = messages.getAnnotation(Message.class);
		Map<Object, Reference<Object>> cache = null;
		ReferenceType type = ReferenceType.NONE;

		if (++CLEANUPCOUNT > 1000) {
			Iterator<Entry<Object, Reference<Object>>> it = WEAK_CACHE
					.entrySet().iterator();
			while (it.hasNext()) {
				if (it.next().getValue().get() == null) {
					it.remove();
				}
			}

			it = SOFT_CACHE.entrySet().iterator();
			while (it.hasNext()) {
				if (it.next().getValue().get() == null) {
					it.remove();
				}
			}
			CLEANUPCOUNT = 0;
		}

		if (annotation == null
				|| annotation.referenceType() == ReferenceType.SOFT) {
			cache = SOFT_CACHE;
			type = ReferenceType.SOFT;
		} else if (annotation.referenceType() == ReferenceType.WEAK) {
			cache = WEAK_CACHE;
			type = ReferenceType.WEAK;
		}

		if (cache != null && cache.containsKey(key)) {
			@SuppressWarnings("unchecked")
			Reference<M> ref = (Reference<M>) cache.get(key);
			M o = ref.get();
			if (o != null) {
				return o;
			}
			cache.remove(key);
		}

		M instance;

		if (System.getSecurityManager() == null) {
			instance = createInstance(locale, messages, annotation, localization);
		} else {
			instance = AccessController.doPrivileged(new PrivilegedAction<M>() {

				public M run() {
					try {
						return createInstance(locale, messages, annotation, localization);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					return null;
				}

			});
		}

		if (cache != null) {
			if (type == ReferenceType.SOFT) {
				cache.put(key, new SoftReference<Object>(instance));
			} else if (type == ReferenceType.WEAK) {
				cache.put(key, new WeakReference<Object>(instance));
			}
		}

		return instance;
	}

	/**
	 * Creates and returns an instance of the of a given messages class for the given {@link Locale}.
	 * The message class gets instantiated and the fields are initialized with values out of a {@link ResourceBundle}.
	 * As there are several options to specify the location of the {@link ResourceBundle} to load, the 
	 * following search order is used:
	 * <ol>
	 * <li>URI location<br/>
	 * 		If the message class is annotated with <code>@Message</code> and the <i>contributorURI</i>
	 * 		attribute is set, the {@link ResourceBundle} is searched at the specified location</li> 
	 * <li>Relative location<br/>
	 * 		If the message class is not annotated with <code>@Message</code> and a contributorURI
	 * 		attribute value or there is no {@link ResourceBundle} found at the specified location, a 
	 * 		{@link ResourceBundle} with the same name in the same package as the message class is searched.</li> 
	 * <li>Bundle localization<br/>
	 * 		If there is no {@link ResourceBundle} found by URI or relative location, the OSGi {@link ResourceBundle}
	 * 		configured in the MANIFEST.MF is tried to load.</li> 
	 * </ol>
	 * Note: Even if there is no {@link ResourceBundle} found in any of the mentioned locations, this method will
	 * 		 not break. In this case the fields of the message class will get initialized with values that look 
	 * 		 like <code>!key!</code> to indicate that there is no translation value found for that key.
	 * 
	 * @param locale The {@link Locale} for which the message class instance is requested.
	 * @param messages The type of the message class whose instance is requested.
	 * @param annotation The annotation that is used in the message class. If specified it is needed
	 * 			to retrieve the URI of the location to search for the {@link ResourceBundle}.
	 * @param localization The service that is needed to retrieve {@link ResourceBundle} objects from a bundle 
	 * 			with a given locale.
	 * @return The created instance of the given messages class and {@link Locale}.
	 * 
	 * @throws InstantiationException if the requested message class represents an abstract class, an interface, 
	 * 			an array class, a primitive type, or void; 
	 * 			or if the class has no nullary constructor; 
	 * 			or if the instantiation fails for some other reason.
	 * @throws IllegalAccessException if the requested message class or its nullary constructor is not accessible.
	 */
	private static <M> M createInstance(Locale locale, Class<M> messages,
			Message annotation, BundleLocalization localization) throws InstantiationException,
			IllegalAccessException {

		ResourceBundle resourceBundle = null;
		if (annotation != null && annotation.contributorURI().length() > 0) {
			resourceBundle = ResourceBundleHelper.getResourceBundleForUri(annotation.contributorURI(), locale, localization);
		}
		
		if (resourceBundle == null) {
			//check for the resource bundle relative to the messages class
			String baseName = messages.getName().replace('.', '/');
			
			try {
				resourceBundle = ResourceBundleHelper.getEquinoxResourceBundle(baseName, locale, messages.getClassLoader());
			}
			catch (MissingResourceException e) {
				//do nothing as this just means there is no resource bundle named
				//like the messages class in the same package
				//therefore we will go on and search for the OSGi resource bundle
			}
		}
		
		if (resourceBundle == null) {
			//retrieve the OSGi resource bundle
			Bundle bundle = FrameworkUtil.getBundle(messages);
			resourceBundle = localization.getLocalization(bundle, locale.toString());
		}
		
		//always create a provider, if there is no resource bundle found, simply the modified keys will
		//be returned by this provider to show that there is something wrong on loading it
		ResourceBundleTranslationProvider provider = new ResourceBundleTranslationProvider(resourceBundle);
		
		M instance = messages.newInstance();
		Field[] fields = messages.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			if (!fields[i].isAccessible()) {
				fields[i].setAccessible(true);
			}

			fields[i].set(instance,
					provider.translate(fields[i].getName()));
		}

		return instance;
	}
}