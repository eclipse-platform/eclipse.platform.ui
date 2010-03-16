/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.internal.services.ServicesActivator;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.injector.IInjector;
import org.eclipse.e4.core.services.injector.IObjectProvider;
import org.eclipse.e4.core.services.injector.ObjectDescriptor;
import org.eclipse.e4.core.services.internal.annotations.AnnotationsSupport;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Reflection-based dependency injector.
 */
public class InjectorImpl implements IInjector {

	final static private String DEBUG_INJECTOR = "org.eclipse.e4.core.services/debug/injector"; //$NON-NLS-1$
	final static private boolean shouldTrace = ServicesActivator.getDefault()
			.getBooleanDebugOption(DEBUG_INJECTOR, false);
	final static private String JAVA_OBJECT = "java.lang.Object"; //$NON-NLS-1$

	// plug-in class that gets replaced in Java 1.5+
	final private static AnnotationsSupport annotationSupport = new AnnotationsSupport(); // XXX
																							// remove;

	public InjectorImpl() {
		// placeholder
	}

	public boolean inject(Object userObject, IObjectProvider objectProvider) {
		boolean result = false;
		try {
			result = processClassHierarchy(userObject, false /* process static */, objectProvider);
		} catch (InvocationTargetException e) {
			logExternalError("Exception occured while processing injecting", userObject, e);
		}
		objectProvider.runAndTrack(new InjectionClass(userObject, objectProvider), null);
		return result;
	}

	// TBD use null object to inject statics
	public boolean injectStatic(Class clazz, IObjectProvider objectProvider) {
		try {
			Object object = make(clazz, objectProvider);
			return processClassHierarchy(object, true /* process static */, objectProvider);
		} catch (InvocationTargetException e) {
			// try-catch won't be necessary once we stop creating an object
			e.printStackTrace();
		} catch (InstantiationException e) {
			// try-catch won't be necessary once we stop creating an object
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Make the processor visit all declared members on the given class and all superclasses
	 * 
	 * @throws InvocationTargetException
	 */
	private boolean processClass(Object userObject, Class objectsClass, ArrayList classHierarchy,
			boolean processStatic, IObjectProvider objectProvider) throws InvocationTargetException {
		// order: superclass, fields, methods
		if (objectsClass != null) {
			Class superClass = objectsClass.getSuperclass();
			if (!superClass.getName().equals(JAVA_OBJECT)) {
				classHierarchy.add(objectsClass);
				if (!processClass(userObject, superClass, classHierarchy, processStatic,
						objectProvider))
					return false;
				classHierarchy.remove(objectsClass);
			}
		}
		if (!processFields(userObject, objectsClass, processStatic, objectProvider))
			return false;
		if (!processMethods(userObject, objectsClass, classHierarchy, processStatic, objectProvider))
			return false;
		return true;
	}

	private boolean processClassHierarchy(Object userObject, boolean processStatic,
			IObjectProvider objectProvider) throws InvocationTargetException {
		if (!processClass(userObject, (userObject == null) ? null : userObject.getClass(),
				new ArrayList(5), processStatic, objectProvider))
			return false;
		return true;
	}

	/**
	 * Make the processor visit all declared fields on the given class.
	 */
	private boolean processFields(Object userObject, Class objectsClass, boolean processStatic,
			IObjectProvider objectProvider) {
		Field[] fields = objectsClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (Modifier.isStatic(field.getModifiers()) != processStatic)
				continue;
			InjectionProperties properties = annotationSupport.getInjectProperties(field,
					objectProvider);
			if (field.getName().startsWith(IContextConstants.INJECTION_PREFIX))
				properties.setInject(true);

			if (!properties.shouldInject())
				continue;
			objectProvider.runAndTrack(new InjectionField(userObject, objectProvider, field,
					properties.groupUpdates()), null);
		}
		return true;
	}

	/**
	 * Make the processor visit all declared methods on the given class.
	 * 
	 * @throws InvocationTargetException
	 */
	private boolean processMethods(final Object userObject, Class objectsClass,
			ArrayList classHierarchy, boolean processStatic, IObjectProvider objectProvider)
			throws InvocationTargetException {
		Method[] methods = objectsClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			final Method method = methods[i];
			if (isOverridden(method, classHierarchy))
				continue; // process in the subclass
			if (Modifier.isStatic(method.getModifiers()) != processStatic)
				continue;
			InjectionProperties properties = annotationSupport.getInjectProperties(method,
					objectProvider);
			if (method.getName().startsWith(IContextConstants.INJECTION_PREFIX))
				properties.setInject(true);

			if (properties.getHandlesEvent() != null) {
				// XXX this is wrong, but it will be removed anyway
				ObjectDescriptor desc = ObjectDescriptor.make(IEventBroker.class);
				IEventBroker eventBroker = (IEventBroker) objectProvider.get(desc);
				eventBroker.subscribe(properties.getHandlesEvent(), null, new EventHandler() {
					public void handleEvent(Event event) {
						Object data = event.getProperty(IEventBroker.DATA);
						boolean wasAccessible = method.isAccessible();
						if (!wasAccessible) {
							method.setAccessible(true);
						}
						try {
							method.invoke(userObject, data);
						} catch (Exception e) {
							throw new RuntimeException(e);
						} finally {
							if (!wasAccessible) {
								method.setAccessible(false);
							}
						}
					}
				}, properties.getEventHeadless());
				continue;
			}

			if (!properties.shouldInject())
				continue;

			objectProvider.runAndTrack(new InjectionMethod(userObject, objectProvider, method,
					properties.groupUpdates()), null);
		}
		return true;
	}

	// TBD this is the same method as in the class injector
	/**
	 * Checks if a given method is overridden with an injectable method.
	 */
	private boolean isOverridden(Method method, ArrayList classHierarchy) {
		int modifiers = method.getModifiers();
		if (Modifier.isPrivate(modifiers))
			return false;
		if (Modifier.isStatic(modifiers))
			return false;
		// method is not private if we reached this line, check not(public OR protected)
		boolean isDefault = !(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers));

		for (Iterator i = classHierarchy.iterator(); i.hasNext();) {
			Class subClass = (Class) i.next();
			Method override = null;
			try {
				override = subClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
			} catch (SecurityException e) {
				continue;
			} catch (NoSuchMethodException e) {
				continue; // this is the desired outcome
			}
			if (override != null) {
				if (isDefault) { // must be in the same package to override
					Package originalPackage = method.getDeclaringClass().getPackage();
					Package overridePackage = subClass.getPackage();

					if (originalPackage == null && overridePackage == null)
						return true;
					if (originalPackage == null || overridePackage == null)
						return false;
					if (originalPackage.equals(overridePackage))
						return true;
				} else
					return true;
			}
		}
		return false;
	}

	public Object invoke(Object userObject, String methodName, IObjectProvider objectProvider)
			throws InvocationTargetException, CoreException {
		Method[] methods = userObject.getClass().getDeclaredMethods();
		for (int j = 0; j < methods.length; j++) {
			Method method = methods[j];
			if (!method.getName().equals(methodName))
				continue;

			InjectionMethod injectMethod = new InjectionMethod(userObject, objectProvider, method,
					false);
			try {
				return injectMethod.invoke(false, false);
			} catch (InjectionException e) {
				IStatus status = new Status(IStatus.ERROR, "org.eclipse.e4.core.services",
						"Unable to invoke method");
				throw new CoreException(status);
			}
		}
		IStatus status = new Status(IStatus.ERROR, "org.eclipse.e4.core.services",
				"Unable to find matching method to invoke");
		throw new CoreException(status);
	}

	public Object invoke(Object userObject, String methodName, Object defaultValue,
			IObjectProvider objectProvider) throws InvocationTargetException {
		return invokeUsingClass(userObject, userObject.getClass(), methodName, defaultValue,
				objectProvider);
	}

	public Object invokeUsingClass(Object userObject, Class currentClass, String methodName,
			Object defaultValue, IObjectProvider objectProvider) throws InvocationTargetException {
		Method[] methods = currentClass.getDeclaredMethods();
		for (int j = 0; j < methods.length; j++) {
			Method method = methods[j];
			if (!method.getName().equals(methodName))
				continue;

			InjectionMethod injectMethod = new InjectionMethod(userObject, objectProvider, method,
					false);
			try {
				return injectMethod.invoke(false, false);
			} catch (InjectionException e) {
				// TBD?
			}
		}
		Class superClass = currentClass.getSuperclass();
		if (superClass == null) {
			return defaultValue;
		}
		return invokeUsingClass(userObject, superClass, methodName, defaultValue, objectProvider);
	}

	public Object make(Class clazz, IObjectProvider objectProvider)
			throws InvocationTargetException, InstantiationException {
		Constructor[] constructors = clazz.getDeclaredConstructors();

		// Sort the constructors by descending number of constructor arguments
		ArrayList sortedConstructors = new ArrayList(constructors.length);
		for (int i = 0; i < constructors.length; i++)
			sortedConstructors.add(constructors[i]);
		Collections.sort(sortedConstructors, new Comparator() {
			public int compare(Object c1, Object c2) {
				int l1 = ((Constructor) c1).getParameterTypes().length;
				int l2 = ((Constructor) c2).getParameterTypes().length;
				return l2 - l1;
			}
		});

		for (Iterator i = sortedConstructors.iterator(); i.hasNext();) {
			Constructor constructor = (Constructor) i.next();

			// skip private and protected constructors; allow public and package visibility
			if (((constructor.getModifiers() & Modifier.PRIVATE) != 0)
					|| ((constructor.getModifiers() & Modifier.PROTECTED) != 0))
				continue;

			// unless this is the default constructor, it has to be tagged
			InjectionProperties cProps = annotationSupport.getInjectProperties(constructor,
					objectProvider);
			if (!cProps.shouldInject() && constructor.getParameterTypes().length != 0)
				continue;

			InjectionConstructor injectedConstructor = new InjectionConstructor(null,
					objectProvider, constructor);
			Object newInstance = injectedConstructor.make();
			if (newInstance != null) {
				inject(newInstance, objectProvider);
				return newInstance;
			}
		}

		if (shouldTrace)
			System.out
					.println("Could not find satisfiable constructor in class " + clazz.getName());
		return null;
	}

	private void logExternalError(String msg, Object destination, Exception e) {
		System.out.println(msg + " " + destination.toString()); //$NON-NLS-1$
		if (e != null)
			e.printStackTrace();
		// TBD convert this into real logging
		// String msg = NLS.bind("Injection failed", destination.toString());
		// RuntimeLog.log(new Status(IStatus.WARNING,
		// IRuntimeConstants.PI_COMMON, 0, msg, e));
	}

}
