/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
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
import org.eclipse.e4.tools.services.ToolsServicesActivator;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.LogService;

public class MessageFactoryServiceImpl implements IMessageFactoryService {

	private static LogService logService = ToolsServicesActivator.getDefault().getLogService();

	// Cache so when multiple instance use the same message class
	private Map<Object, Reference<Object>> SOFT_CACHE = Collections
			.synchronizedMap(new HashMap<Object, Reference<Object>>());

	private Map<Object, Reference<Object>> WEAK_CACHE = Collections
			.synchronizedMap(new HashMap<Object, Reference<Object>>());

	private int CLEANUPCOUNT = 0;

	public <M> M createInstance(final String locale, final Class<M> messages, final BundleLocalization localization)
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
			instance = doCreateInstance(locale, messages, annotation, localization);
		} else {
			instance = AccessController.doPrivileged(new PrivilegedAction<M>() {

				public M run() {
					try {
						return doCreateInstance(locale, messages, annotation, localization);
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

	private static <M> M doCreateInstance(String locale, Class<M> messages,
			Message annotation, BundleLocalization localization) throws InstantiationException,
			IllegalAccessException {

		Locale loc = null;
		try {
			loc = locale == null ? Locale.getDefault() : ResourceBundleHelper.toLocale(locale);
		}
		catch (Exception e) {
			//parsing the locale String to a Locale failed, so we use the default Locale
			if (logService != null)
				logService.log(LogService.LOG_ERROR, "Invalid locale", e); //$NON-NLS-1$
			loc = Locale.getDefault();
		}
		
		ResourceBundle resourceBundle = null;
		if (annotation != null && annotation.contributorURI().length() > 0) {
			resourceBundle = ResourceBundleHelper.getResourceBundleForUri(annotation.contributorURI(), loc, localization);
		}
		
		if (resourceBundle == null) {
			//check for the resource bundle relative to the messages class
			String baseName = messages.getName().replace('.', '/');
			
			try {
				resourceBundle = ResourceBundleHelper.getEquinoxResourceBundle(baseName, loc, messages.getClassLoader());
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
			resourceBundle = localization.getLocalization(bundle, locale);
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