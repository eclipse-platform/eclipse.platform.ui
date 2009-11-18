/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import org.eclipse.e4.core.services.injector.IObjectProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.internal.annotations.AnnotationsSupport;

/**
 * Reflection-based context injector.
 */
public class ContextInjector {

	private class Processor {

		final private String name;
		protected boolean addition;

		protected boolean shouldProcessPostConstruct = false;
		protected boolean isInDispose = false;
		protected Object userObject;

		protected boolean injectWithNulls = false;

		protected boolean processStatic = false;

		private List postConstructMethods;

		public ArrayList classHierarchy = new ArrayList(5);

		public Processor(String name, boolean addition, boolean isInDispose) {
			this.name = name;
			this.addition = addition;
			this.isInDispose = isInDispose;
		}

		public void setObject(Object userObject) {
			this.userObject = userObject;
		}

		public void setInjectNulls(boolean injectWithNulls) {
			this.injectWithNulls = injectWithNulls;
		}

		public void setProcessStatic(boolean processStatic) {
			this.processStatic = processStatic;
		}

		/**
		 * The method assumes injection is needed for this field, from the context property named
		 * injectName.
		 */
		public void processField(final Field field, InjectionProperties properties) {
			if (Modifier.isStatic(field.getModifiers()) != processStatic)
				return;
			// filter if name is specified
			if ((name != null) && !name.equals(context.getKey(properties)))
				return;
			Object value = null;
			if (addition) {
				Object provider = properties.getProvider();
				if (provider != null)
					value = provider;
				else if (context.containsKey(properties))
					value = context.get(properties);
				else {
					if (!properties.isOptional())
						throw new IllegalStateException("Could not set " + field //$NON-NLS-1$
								+ " because of missing: " + context.getKey(properties)); //$NON-NLS-1$
					return;
				}
			}
			setField(userObject, field, value);
		}

		public void processMethod(final Method method, boolean optional) {
			if (Modifier.isStatic(method.getModifiers()) != processStatic)
				return;
			// we only get here if we are injecting
			InjectionProperties[] properties = annotationSupport.getInjectParamProperties(method);
			if (name != null) {
				// is it one of the arguments of this method?
				boolean found = false;
				for (int i = 0; i < properties.length; i++) {
					if (name.equals(context.getKey(properties[i]))) {
						found = true;
						break;
					}
				}
				if (!found)
					return;
			}

			Object[] actualParams = processParams(properties, method.getParameterTypes(),
					!addition, injectWithNulls);
			if (actualParams != null)
				callMethod(userObject, method, actualParams);
			else if (!optional)
				throw new IllegalStateException("Could not invoke " + method //$NON-NLS-1$
						+ ": no matching context elements"); //$NON-NLS-1$
		}

		public void addPostConstructMethod(Method method) {
			if (postConstructMethods == null)
				postConstructMethods = new ArrayList(1);
			postConstructMethods.add(method);
		}

		public void processPostConstructMethod() {
			if (!shouldProcessPostConstruct)
				return;
			if (postConstructMethods == null)
				return;
			for (Iterator it = postConstructMethods.iterator(); it.hasNext();) {
				Method method = (Method) it.next();
				InjectionProperties[] properties = annotationSupport
						.getInjectParamProperties(method);
				Object[] actualParams = processParams(properties, method.getParameterTypes(),
						!addition, injectWithNulls);
				if (actualParams == null)
					logWarning(userObject, new IllegalArgumentException());
				else
					callMethod(userObject, method, actualParams);
			}
		}

	}

	// TBD investigate if this approach to reparenting works with calculated values and providers
	private class ReparentProcessor extends Processor {

		private IObjectProvider oldParent;

		public ReparentProcessor(IObjectProvider oldParent) {
			super(null, true /* set */, false);
			this.oldParent = oldParent;
		}

		/**
		 * Returns whether the value associated with the given key is affected by the parent change.
		 */
		private boolean hasChanged(InjectionProperties key) {
			// if value is local then parent change has no effect
			// XXX this is incorrect
			// if (context.getLocal(key) != null)
			// return false;
			// XXX this is incorrect: different parents, same grandparent
			// Object oldValue = oldParent == null ? null : oldParent.internalGet(
			// (EclipseContext) context, key, null, false);
			// Object newValue = context == null ? null : ((EclipseContext) context).internalGet(
			// (EclipseContext) context, key, null, false);
			// return oldValue != newValue;

			// XXX for now, check if values are different
			Object oldValue = oldParent.get(key);
			Object newValue = context.get(key);
			return (oldValue != newValue); // use pointer comparison, not #equals()
		}

		public void processField(final Field field, InjectionProperties properties) {
			if (hasChanged(properties))
				super.processField(field, properties);
		}

		public void processMethod(final Method method, boolean optional) {
			// any argument changed?
			InjectionProperties[] properties = annotationSupport.getInjectParamProperties(method);

			boolean changed = false;
			for (int i = 0; i < properties.length; i++) {
				if (hasChanged(properties[i])) {
					changed = true;
					break;
				}
			}
			if (changed)
				super.processMethod(method, optional);
		}

	}

	final static private String JAVA_OBJECT = "java.lang.Object"; //$NON-NLS-1$

	final protected IObjectProvider context;
	final private AnnotationsSupport annotationSupport;

	public ContextInjector(IObjectProvider context) {
		this.context = context;
		// plug-in class that gets replaced in Java 1.5+
		annotationSupport = new AnnotationsSupport(context);
	}

	public void inject(String name, Object userObject) {
		Processor processor = new Processor(name, true, false);
		processClassHierarchy(userObject, processor);
	}

	public void inject(Object userObject) {
		Processor processor = new Processor(null, true, false);
		processor.shouldProcessPostConstruct = true;
		processClassHierarchy(userObject, processor);
	}

	// TBD use null object to inject statics
	public void injectStatic(Class clazz) {
		Processor processor = new Processor(null, true, false);
		processor.shouldProcessPostConstruct = true;
		processor.setProcessStatic(true);
		Object object = make(clazz);
		processClassHierarchy(object, processor);
	}

	public void uninject(String name, Object userObject) {
		Processor processor = new Processor(name, false, false);
		processClassHierarchy(userObject, processor);
	}

	public void uninject(Object userObject) {
		Processor processor = new Processor(null, false, false);
		processor.setInjectNulls(true);
		processClassHierarchy(userObject, processor);
	}

	public void dispose(Object userObject) {
		if (userObject instanceof IDisposable)
			((IDisposable) userObject).dispose();

		Processor processor = new Processor(null, false, true);
		processor.setInjectNulls(true);
		processClassHierarchy(userObject, processor);
	}

	public void reparent(Object userObject, IObjectProvider oldParent) {
		if (oldParent == context)
			return;
		Processor processor = new ReparentProcessor(oldParent);
		processClassHierarchy(userObject, processor);
	}

	static void logWarning(Object destination, Exception e) {
		System.out.println("Injection failed " + destination.toString()); //$NON-NLS-1$
		if (e != null)
			e.printStackTrace();
		// TBD convert this into real logging
		// String msg = NLS.bind("Injection failed", destination.toString());
		// RuntimeLog.log(new Status(IStatus.WARNING,
		// IRuntimeConstants.PI_COMMON, 0, msg, e));
	}

	/**
	 * Make the processor visit all declared members on the given class and all superclasses
	 */
	private void processClass(Class objectsClass, Processor processor) {
		if (processor.addition) {
			// order: superclass, fields, methods
			if (objectsClass != null) {
				Class superClass = objectsClass.getSuperclass();
				if (!superClass.getName().equals(JAVA_OBJECT)) {
					processor.classHierarchy.add(objectsClass);
					processClass(superClass, processor);
					processor.classHierarchy.remove(objectsClass);
				}
			}
			processFields(objectsClass, processor);
			processMethods(objectsClass, processor);
		} else {
			// order: methods, fields, superclass
			processMethods(objectsClass, processor);
			processFields(objectsClass, processor);
			if (objectsClass != null) {
				Class superClass = objectsClass.getSuperclass();
				if (!superClass.getName().equals(JAVA_OBJECT)) {
					processor.classHierarchy.add(objectsClass);
					processClass(superClass, processor);
					processor.classHierarchy.remove(objectsClass);
				}
			}
		}
	}

	private void processClassHierarchy(Object userObject, Processor processor) {
		processor.setObject(userObject);
		processClass((userObject == null) ? null : userObject.getClass(), processor);
		processor.processPostConstructMethod();
	}

	/**
	 * Make the processor visit all declared fields on the given class.
	 */
	private void processFields(Class objectsClass, Processor processor) {
		Field[] fields = objectsClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];

			InjectionProperties properties = annotationSupport.getInjectProperties(field);
			if (field.getName().startsWith(IContextConstants.INJECTION_PREFIX))
				properties.setInject(true);

			if (properties.shouldInject())
				processor.processField(field, properties);
		}
	}

	/**
	 * Make the processor visit all declared methods on the given class.
	 */
	private void processMethods(Class objectsClass, Processor processor) {
		Method[] methods = objectsClass.getDeclaredMethods();
		if (processor.isInDispose) {
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				if (method.getParameterTypes().length > 0) // TBD why?
					continue;
				if (!annotationSupport.isPreDestory(method))
					continue;
				if (!isOverridden(method, processor))
					callMethod(processor.userObject, method, null);
			}
		}
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (isOverridden(method, processor))
				continue; // process in the subclass
			if (processor.shouldProcessPostConstruct) {
				if (isPostConstruct(method)) {
					processor.addPostConstructMethod(method);
					continue;
				}
			}

			InjectionProperties properties = annotationSupport.getInjectProperties(method);
			if (method.getName().startsWith(IContextConstants.INJECTION_PREFIX))
				properties.setInject(true);

			if (properties.shouldInject())
				processor.processMethod(method, properties.isOptional());
		}
	}

	// TBD simplify this: only one non-annotation and one "implements IInitializable"?
	/**
	 * Returns whether the given method is a post-construction process method, as defined by the
	 * class comment of {@link ContextInjectionFactory}.
	 */
	private boolean isPostConstruct(Method method) {
		boolean isPostConstruct = annotationSupport.isPostConstruct(method);
		if (isPostConstruct)
			return true;
		if (!method.getName().equals(IContextConstants.INJECTION_SET_CONTEXT_METHOD))
			return false;
		Class[] parms = method.getParameterTypes();
		if (parms.length == 0)
			return true;
		if (parms.length == 1 && parms[0].equals(IEclipseContext.class))
			return true;
		return false;
	}

	/**
	 * Checks if a given method is overridden with an injectable method.
	 */
	private boolean isOverridden(Method method, Processor processor) {
		int modifiers = method.getModifiers();
		if (Modifier.isPrivate(modifiers))
			return false;
		if (Modifier.isStatic(modifiers))
			return false;
		// method is not private if we reached this line, check not(public OR protected)
		boolean isDefault = !(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers));

		for (Iterator i = processor.classHierarchy.iterator(); i.hasNext();) {
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

	public Object invoke(Object userObject, String methodName, Object defaultValue) {
		Method[] methods = userObject.getClass().getDeclaredMethods();
		for (int j = 0; j < methods.length; j++) {
			Method method = methods[j];
			if (!method.getName().equals(methodName))
				continue;

			InjectionProperties[] properties = annotationSupport.getInjectParamProperties(method);
			Object[] actualParams = processParams(properties, method.getParameterTypes(), false,
					false);
			if (actualParams != null)
				return callMethod(userObject, method, actualParams);
		}
		return defaultValue;
	}

	public Object make(Class clazz) {
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

			// unless this is the last constructor, it has to be tagged
			if (i.hasNext()) {
				InjectionProperties properties = annotationSupport.getInjectProperties(constructor);
				if (!properties.shouldInject())
					continue;
			}

			InjectionProperties[] properties = annotationSupport
					.getInjectParamsProperties(constructor);
			Object[] actualParams = processParams(properties, constructor.getParameterTypes(),
					false, false);
			if (actualParams == null)
				continue;
			Object newInstance = callConstructor(constructor, actualParams);
			return newInstance;
		}

		String message = "could not find satisfiable constructor in class " + clazz.getName(); //$NON-NLS-1$
		logWarning(clazz, new RuntimeException(message));
		return null;
	}

	private Object[] processParams(InjectionProperties[] properties, Class[] parameterTypes,
			boolean ignoreMissing, boolean injectWithNulls) {
		Object[] actualParams = new Object[properties.length];
		for (int i = 0; i < actualParams.length; i++) {
			// 1) if we have a provider, use it
			Object provider = properties[i].getProvider();
			if (provider != null) {
				actualParams[i] = provider;
				continue;
			}
			// 2) if we have the key in the context
			if (context.containsKey(properties[i])) {
				if (injectWithNulls) {
					actualParams[i] = null;
					continue;
				} else {
					Object candidate = context.get(properties[i]);
					if (candidate != null
							&& parameterTypes[i].isAssignableFrom(candidate.getClass())) {
						actualParams[i] = candidate;
						continue;
					}
				}
			}
			// 3) can we ignore this argument?
			if (ignoreMissing || properties[i].isOptional()) {
				actualParams[i] = null;
				continue;
			}
			return null;
		}
		return actualParams;
	}

	private boolean setField(Object userObject, Field field, Object value) {
		if ((value != null) && !field.getType().isAssignableFrom(value.getClass())) {
			// TBD add debug option
			return false;
		}

		boolean wasAccessible = true;
		if (!field.isAccessible()) {
			field.setAccessible(true);
			wasAccessible = false;
		}
		try {
			field.set(userObject, value);
		} catch (IllegalArgumentException e) {
			logWarning(field, e);
			return false;
		} catch (IllegalAccessException e) {
			logWarning(field, e);
			return false;
		} finally {
			if (!wasAccessible)
				field.setAccessible(false);
		}
		return true;
	}

	private Object callMethod(Object userObject, Method method, Object[] args) {
		Object result = null;
		boolean wasAccessible = true;
		if (!method.isAccessible()) {
			method.setAccessible(true);
			wasAccessible = false;
		}
		try {
			result = method.invoke(userObject, args);
		} catch (IllegalArgumentException e) {
			logWarning(method, e);
			return null;
		} catch (IllegalAccessException e) {
			logWarning(method, e);
			return null;
		} catch (InvocationTargetException e) {
			logWarning(method, e);
			return null;
		} finally {
			if (!wasAccessible)
				method.setAccessible(false);
		}
		return result;
	}

	private Object callConstructor(Constructor constructor, Object[] args) {
		if (args != null) { // make sure args are assignable
			Class[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length != args.length) {
				logWarning(constructor, new IllegalArgumentException());
				return null;
			}
			for (int i = 0; i < args.length; i++) {
				if ((args[i] != null) && !parameterTypes[i].isAssignableFrom(args[i].getClass())) {
					// TBD consider when do we need to log
					// logWarning(method, new IllegalArgumentException());
					return null;
				}
			}
		}

		Object result = null;
		boolean wasAccessible = true;
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
			wasAccessible = false;
		}
		try {
			result = constructor.newInstance(args);
		} catch (IllegalArgumentException e) {
			logWarning(constructor, e);
			return null;
		} catch (IllegalAccessException e) {
			logWarning(constructor, e);
			return null;
		} catch (InvocationTargetException e) {
			logWarning(constructor, e);
			return null;
		} catch (InstantiationException e) {
			logWarning(constructor, e);
			return null;
		} finally {
			if (!wasAccessible)
				constructor.setAccessible(false);
		}
		return result;
	}

}
