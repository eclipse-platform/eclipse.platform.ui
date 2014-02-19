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
package org.eclipse.e4.core.internal.services;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import org.eclipse.e4.core.services.nls.IMessageFactoryService;
import org.eclipse.e4.core.services.nls.Message;
import org.eclipse.e4.core.services.nls.Message.ReferenceType;
import org.eclipse.e4.core.services.translation.ResourceBundleProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.LogService;

public class MessageFactoryServiceImpl implements IMessageFactoryService {

	private static LogService logService = ServicesActivator.getDefault().getLogService();

	// Cache so when multiple instance use the same message class
	private Map<Object, Reference<Object>> SOFT_CACHE = Collections
			.synchronizedMap(new HashMap<Object, Reference<Object>>());

	private Map<Object, Reference<Object>> WEAK_CACHE = Collections
			.synchronizedMap(new HashMap<Object, Reference<Object>>());

	private int CLEANUPCOUNT = 0;

	@Override
	public <M> M getMessageInstance(final Locale locale, final Class<M> messages,
			final ResourceBundleProvider provider) {
		String key = messages.getName() + "_" + locale; //$NON-NLS-1$

		final Message annotation = messages.getAnnotation(Message.class);
		Map<Object, Reference<Object>> cache = null;
		ReferenceType type = ReferenceType.NONE;

		if (++CLEANUPCOUNT > 1000) {
			Iterator<Entry<Object, Reference<Object>>> it = WEAK_CACHE.entrySet().iterator();
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

		if (annotation == null || annotation.referenceType() == ReferenceType.SOFT) {
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
			instance = createInstance(locale, messages, annotation, provider);
		} else {
			instance = AccessController.doPrivileged(new PrivilegedAction<M>() {

				public M run() {
					return createInstance(locale, messages, annotation, provider);
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
	 * Creates and returns an instance of the of a given messages class for the given {@link Locale}
	 * . The message class gets instantiated and the fields are initialized with values out of a
	 * {@link ResourceBundle}. As there are several options to specify the location of the
	 * {@link ResourceBundle} to load, the following search order is used:
	 * <ol>
	 * <li>URI location<br/>
	 * If the message class is annotated with <code>@Message</code> and the <i>contributorURI</i>
	 * attribute is set, the {@link ResourceBundle} is searched at the specified location</li>
	 * <li>Relative location<br/>
	 * If the message class is not annotated with <code>@Message</code> and a contributorURI
	 * attribute value or there is no {@link ResourceBundle} found at the specified location, a
	 * {@link ResourceBundle} with the same name in the same package as the message class is
	 * searched.</li>
	 * <li>Bundle localization<br/>
	 * If there is no {@link ResourceBundle} found by URI or relative location, the OSGi
	 * {@link ResourceBundle} configured in the MANIFEST.MF is tried to load.</li>
	 * </ol>
	 * Note: Even if there is no {@link ResourceBundle} found in any of the mentioned locations,
	 * this method will not break. In this case the fields of the message class will get initialized
	 * with values that look like <code>!key!</code> to indicate that there is no translation value
	 * found for that key.
	 * 
	 * @param locale
	 *            The {@link Locale} for which the message class instance is requested.
	 * @param messages
	 *            The type of the message class whose instance is requested.
	 * @param annotation
	 *            The annotation that is used in the message class. If specified it is needed to
	 *            retrieve the URI of the location to search for the {@link ResourceBundle}.
	 * @param rbProvider
	 *            The service that is needed to retrieve {@link ResourceBundle} objects from a
	 *            bundle with a given locale.
	 * 
	 * @return The created instance of the given messages class and {@link Locale} or
	 *         <code>null</code> if an error occured on creating the instance.
	 */
	private static <M> M createInstance(Locale locale, Class<M> messages, Message annotation,
			ResourceBundleProvider rbProvider) {

		ResourceBundle resourceBundle = null;
		if (annotation != null && annotation.contributorURI().length() > 0) {
			resourceBundle = ResourceBundleHelper.getResourceBundleForUri(
					annotation.contributorURI(), locale, rbProvider);
		}

		if (resourceBundle == null) {
			// check for the resource bundle relative to the messages class
			String baseName = messages.getName().replace('.', '/');

			resourceBundle = ResourceBundleHelper.getEquinoxResourceBundle(baseName, locale,
					messages.getClassLoader());

			if (resourceBundle == null) {
				// check for the resource bundle relative to the messages class by searching
				// the properties file lower case
				// this is a fix for Linux environments
				resourceBundle = ResourceBundleHelper.getEquinoxResourceBundle(
						baseName.toLowerCase(), locale, messages.getClassLoader());
			}
		}

		if (resourceBundle == null) {
			// retrieve the OSGi resource bundle
			Bundle bundle = FrameworkUtil.getBundle(messages);
			resourceBundle = rbProvider.getResourceBundle(bundle, locale.toString());
		}

		// always create a provider, if there is no resource bundle found, simply the modified keys
		// will
		// be returned by this provider to show that there is something wrong on loading it
		ResourceBundleTranslationProvider provider = new ResourceBundleTranslationProvider(
				resourceBundle);

		M instance = null;
		try {
			instance = messages.newInstance();
			Field[] fields = messages.getDeclaredFields();

			for (int i = 0; i < fields.length; i++) {
				if (!fields[i].isAccessible()) {
					fields[i].setAccessible(true);
				}

				fields[i].set(instance, provider.translate(fields[i].getName()));
			}
		} catch (InstantiationException e) {
			if (logService != null)
				logService.log(LogService.LOG_ERROR, "Instantiation of messages class failed", e); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			if (logService != null)
				logService.log(LogService.LOG_ERROR, "Failed to access messages class", e); //$NON-NLS-1$
		}

		// invoke the method annotated with @PostConstruct
		processPostConstruct(instance, messages);

		return instance;
	}

	/**
	 * Searches for the method annotated {@link PostConstruct} in the messages class. If there is
	 * one found it will be executed.
	 * <p>
	 * Note: The method annotated with {@link PostConstruct} does not support method injection
	 * because we are not using the injection mechanism to call.
	 * 
	 * @param messageObject
	 *            The message instance of the given class where the method annotated with
	 *            {@link PostConstruct} should be called
	 * @param messageClass
	 *            The type of the message class whose instance is requested.
	 */
	private static void processPostConstruct(Object messageObject, Class<?> messageClass) {
		if (messageObject != null) {
			Method[] methods = messageClass.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				if (!method.isAnnotationPresent(PostConstruct.class)) {
					continue;
				} else {
					try {
						method.invoke(messageObject);
					} catch (Exception e) {
						if (logService != null)
							logService
									.log(LogService.LOG_ERROR,
											"Exception on trying to execute the @PostConstruct annotated method in " + messageClass, e); //$NON-NLS-1$
					}
				}
			}
		}
	}

}