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

import java.lang.reflect.Constructor;
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
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IContributionFactorySpi;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.Bundle;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.IContributionFactory#call(java.lang.Object,
	 * java.lang.String, java.lang.String,
	 * org.eclipse.e4.core.services.context.IEclipseContext, java.lang.Object)
	 */
	public Object call(Object object, String uriString, String methodName,
			IEclipseContext context, Object defaultValue) {
		if (uriString != null) {
			URI uri = URI.createURI(uriString);
			if (uri.segmentCount() > 3) {
				String prefix = uri.segment(2);
				IContributionFactorySpi factory = (IContributionFactorySpi) languages
						.get(prefix);
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
				 * @see java.util.Comparator#compare(java.lang.Object,
				 * java.lang.Object)
				 */
				public int compare(Method m1, Method m2) {
					int l1 = m1.getParameterTypes().length;
					int l2 = m2.getParameterTypes().length;

					return l1 - l2;
				}
			});

			// Find the first satisfiable method
			for (Iterator<Method> iter = toSort.iterator(); iter.hasNext()
					&& targetMethod == null;) {
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
			System.err.println("calling: " + methodName); //$NON-NLS-1$
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.IContributionFactory#create(java.lang.String
	 * , org.eclipse.e4.core.services.context.IEclipseContext)
	 */
	public Object create(String uriString, IEclipseContext context) {
		URI uri = URI.createURI(uriString);
		Bundle bundle = getBundle(uri);
		if (bundle != null) {
			if (uri.segmentCount() > 3) {
				String prefix = uri.segment(2);
				IContributionFactorySpi factory = (IContributionFactorySpi) languages
						.get(prefix);
				StringBuffer resource = new StringBuffer(uri.segment(3));
				for (int i = 4; i < uri.segmentCount(); i++) {
					resource.append('/');
					resource.append(uri.segment(i));
				}
				return factory.create(bundle, resource.toString(), context);
			}
			try {
				Class<?> targetClass = bundle.loadClass(uri.segment(2));
				return createObject(targetClass, context);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private void processLanguages() {
		languages = new HashMap<String, Object>();
		String extId = "org.eclipse.e4.languages"; //$NON-NLS-1$
		IConfigurationElement[] languageElements = registry
				.getConfigurationElementsFor(extId);
		for (int i = 0; i < languageElements.length; i++) {
			IConfigurationElement languageElement = languageElements[i];
			try {
				languages
						.put(
								languageElement.getAttribute("name"), //$NON-NLS-1$
								languageElement
										.createExecutableExtension("contributionFactory")); //$NON-NLS-1$
			} catch (InvalidRegistryObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Object createObject(Class<?> targetClass, IEclipseContext context) {

		Constructor<?> targetConstructor = null;

		Constructor<?>[] constructors = targetClass.getConstructors();

		// Optimization: if there's only one constructor, use it.
		if (constructors.length == 1) {
			targetConstructor = constructors[0];
		} else {
			ArrayList<Constructor<?>> toSort = new ArrayList<Constructor<?>>();

			for (int i = 0; i < constructors.length; i++) {
				Constructor<?> constructor = constructors[i];

				// Filter out non-public constructors
				if ((constructor.getModifiers() & Modifier.PUBLIC) != 0) {
					toSort.add(constructor);
				}
			}

			// Sort the constructors by descending number of constructor
			// arguments
			Collections.sort(toSort, new Comparator<Constructor<?>>() {
				public int compare(Constructor<?> c1, Constructor<?> c2) {

					int l1 = c1.getParameterTypes().length;
					int l2 = c2.getParameterTypes().length;

					return l1 - l2;
				}
			});

			// Find the first satisfiable constructor
			for (Constructor<?> next : toSort) {
				boolean satisfiable = true;

				Class<?>[] params = next.getParameterTypes();
				for (int i = 0; i < params.length && satisfiable; i++) {
					Class<?> clazz = params[i];

					if (!context.containsKey(clazz.getName())) {
						satisfiable = false;
					}
				}

				if (satisfiable) {
					targetConstructor = next;
				}
			}
		}

		if (targetConstructor == null) {
			throw new RuntimeException(
					"could not find satisfiable constructor in class " + targetClass); //$NON-NLS-1$
		}

		Class<?>[] paramKeys = targetConstructor.getParameterTypes();

		try {
			Object[] params = new Object[paramKeys.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = context.get(paramKeys[i].getName());
			}

			Object newInstance = targetConstructor.newInstance(params);
			ContextInjectionFactory.inject(newInstance, context, null, null);
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Bundle getBundle(URI platformURI) {
		return Activator.getDefault().getBundleForName(platformURI.segment(1));
	}

}
