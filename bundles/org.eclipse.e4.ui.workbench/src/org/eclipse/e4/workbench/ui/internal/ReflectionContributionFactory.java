/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.contributions.IContributionFactorySpi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

/**
 * Create the contribution factory.
 */
public class ReflectionContributionFactory implements IContributionFactory {

	private IExtensionRegistry registry;
	private Map<String, Object> languages;

	/**
	 * Create a reflection factory.
	 * 
	 * @param registry
	 *            to read languages.
	 */
	public ReflectionContributionFactory(IExtensionRegistry registry) {
		this.registry = registry;
		processLanguages();
	}

	public Object call(Object object, String uriString, String methodName, IEclipseContext context,
			Object defaultValue) {
		if (uriString != null) {
			URI uri = URI.createURI(uriString);
			if (uri.segmentCount() > 3) {
				String prefix = uri.segment(2);
				IContributionFactorySpi factory = (IContributionFactorySpi) languages.get(prefix);
				return factory.call(object, methodName, context, defaultValue);
			}
		}

		Method targetMethod = null;

		Method[] methods = object.getClass().getMethods();

		// Optimization: if there's only one method, use it.
		if (methods.length == 1) {
			targetMethod = methods[0];
		} else {
			ArrayList<Method> toSort = new ArrayList<Method>();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				// Filter out non-public constructors
				if ((method.getModifiers() & Modifier.PUBLIC) != 0
						&& method.getName().equals(methodName)) {
					toSort.add(method);
				}
			}

			// Sort the methods by descending number of method
			// arguments
			Collections.sort(toSort, new Comparator<Method>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
				 */
				public int compare(Method m1, Method m2) {
					int l1 = m1.getParameterTypes().length;
					int l2 = m2.getParameterTypes().length;

					return l1 - l2;
				}
			});

			// Find the first satisfiable method
			for (Iterator<Method> iter = toSort.iterator(); iter.hasNext() && targetMethod == null;) {
				Method next = iter.next();

				boolean satisfiable = true;

				Class<?>[] params = next.getParameterTypes();
				for (int i = 0; i < params.length && satisfiable; i++) {
					Class<?> clazz = params[i];

					if (!context.containsKey(clazz.getName())
							&& !IEclipseContext.class.equals(clazz)) {
						satisfiable = false;
					}
				}

				if (satisfiable) {
					targetMethod = next;
				}
			}
		}

		if (targetMethod == null) {
			if (defaultValue != null) {
				return defaultValue;
			}
			throw new RuntimeException(
					"could not find satisfiable method " + methodName + " in class " + object.getClass()); //$NON-NLS-1$//$NON-NLS-2$
		}

		Class<?>[] paramKeys = targetMethod.getParameterTypes();

		try {
			Activator.trace(Policy.DEBUG_CMDS, "calling: " + methodName, null); //$NON-NLS-1$
			Object[] params = new Object[paramKeys.length];
			for (int i = 0; i < params.length; i++) {
				if (IEclipseContext.class.equals(paramKeys[i])) {
					params[i] = context;
				} else {
					params[i] = context.get(paramKeys[i].getName());
				}
			}

			return targetMethod.invoke(object, params);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Object create(String uriString, IEclipseContext context) {
		if (uriString == null) {
			return null;
		}
		URI uri = URI.createURI(uriString);
		Bundle bundle = getBundle(uri);
		Object contribution;
		if (bundle != null) {
			contribution = createFromBundle(bundle, context, uri);
		} else {
			contribution = null;
			Activator.log(LogService.LOG_ERROR, "Unable to retrive the bundle from the URI: " //$NON-NLS-1$
					+ uriString);
		}
		return contribution;
	}

	protected Object createFromBundle(Bundle bundle, IEclipseContext context, URI uri) {
		Object contribution;
		if (uri.segmentCount() > 3) {
			String prefix = uri.segment(2);
			IContributionFactorySpi factory = (IContributionFactorySpi) languages.get(prefix);
			StringBuffer resource = new StringBuffer(uri.segment(3));
			for (int i = 4; i < uri.segmentCount(); i++) {
				resource.append('/');
				resource.append(uri.segment(i));
			}
			contribution = factory.create(bundle, resource.toString(), context);
		} else {
			String clazz = uri.segment(2);
			try {
				Class<?> targetClass = bundle.loadClass(clazz);
				contribution = ContextInjectionFactory.make(targetClass, context);
			} catch (ClassNotFoundException e) {
				contribution = null;
				String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
						+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				Activator.log(LogService.LOG_ERROR, message, e);
			} catch (InvocationTargetException e) {
				contribution = null;
				String message = "Unable to instantiate class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
						+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				Activator.log(LogService.LOG_ERROR, message, e);
			} catch (InstantiationException e) {
				contribution = null;
				String message = "Unable to instantiate class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
						+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				Activator.log(LogService.LOG_ERROR, message, e);
			}
		}
		return contribution;
	}

	protected void processLanguages() {
		languages = new HashMap<String, Object>();
		String extId = "org.eclipse.e4.languages"; //$NON-NLS-1$
		IConfigurationElement[] languageElements = registry.getConfigurationElementsFor(extId);
		for (int i = 0; i < languageElements.length; i++) {
			IConfigurationElement languageElement = languageElements[i];
			try {
				languages.put(languageElement.getAttribute("name"), //$NON-NLS-1$
						languageElement.createExecutableExtension("contributionFactory")); //$NON-NLS-1$
			} catch (InvalidRegistryObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Bundle getBundle(URI platformURI) {
		return Activator.getDefault().getBundleForName(platformURI.segment(1));
	}

	public Bundle getBundle(String uriString) {
		URI uri = URI.createURI(uriString);
		return getBundle(uri);
	}

}
