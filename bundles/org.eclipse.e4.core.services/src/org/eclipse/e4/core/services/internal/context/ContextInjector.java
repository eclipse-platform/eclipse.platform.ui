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
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;

/**
 * Reflection-based context injector.
 */
public class ContextInjector {

	private class Processor {

		final private String name;
		final private IEclipseContext context;
		protected boolean addition;

		protected boolean shouldProcessPostConstruct = false;
		protected boolean isInDispose = false;
		protected Object userObject;

		protected boolean injectWithNulls = false;

		private List postConstructMethods;

		public ArrayList classHierarchy = new ArrayList(5);

		public Processor(String name, IEclipseContext context, boolean addition, boolean isInDispose) {
			this.name = name;
			this.context = context;
			this.addition = addition;
			this.isInDispose = isInDispose;
		}

		public void setObject(Object userObject) {
			this.userObject = userObject;
		}

		public void setInjectNulls(boolean injectWithNulls) {
			this.injectWithNulls = injectWithNulls;
		}

		/**
		 * The method assumes injection is needed for this field, from the context property named
		 * injectName.
		 */
		public void processField(final Field field, InjectionProperties properties) {
			String injectName = properties.getPropertyName();
			if ((name != null) && !name.equals(injectName)) // filter if name is specified
				return;
			Object value = null;
			if (addition) {
				IContextProvider provider = properties.getProvider();
				if (provider != null) {
					provider.setContext(context);
					value = provider;
				} else if (context.containsKey(injectName)) {
					value = context.get(injectName);
				} else {
					if (!properties.isOptional())
						throw new IllegalStateException("Could not set " + field //$NON-NLS-1$
								+ " because of missing: " + injectName); //$NON-NLS-1$
					return;
				}
			}
			setField(userObject, field, value);
		}

		public void processMethod(final Method method, boolean optional) {
			// we only get here if we are injecting
			InjectionProperties[] properties = InjectionPropertyResolver
					.getInjectionParamProperties(method);
			if (name != null) {
				// is it one of the arguments of this method?
				boolean found = false;
				for (int i = 0; i < properties.length; i++) {
					if (name.equals(properties[i].getPropertyName())) {
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
				InjectionProperties[] properties = InjectionPropertyResolver
						.getInjectionParamProperties(method);
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

		private EclipseContext oldParent;

		public ReparentProcessor(IEclipseContext oldParent, IEclipseContext context) {
			super(null, context, true /* set */, false);
			this.oldParent = (EclipseContext) oldParent;
		}

		/**
		 * Returns whether the value associated with the given key is affected by the parent change.
		 */
		private boolean hasChanged(String key) {
			// if value is local then parent change has no effect
			if (context.getLocal(key) != null)
				return false;
			Object oldValue = oldParent == null ? null : oldParent.internalGet(
					(EclipseContext) context, key, null, false);
			Object newValue = context == null ? null : ((EclipseContext) context).internalGet(
					(EclipseContext) context, key, null, false);
			return oldValue != newValue;
		}

		public void processField(final Field field, InjectionProperties properties) {
			if (hasChanged(properties.getPropertyName()))
				super.processField(field, properties);
		}

		public void processMethod(final Method method, boolean optional) {
			// any argument changed?
			InjectionProperties[] properties = InjectionPropertyResolver
					.getInjectionParamProperties(method);

			boolean changed = false;
			for (int i = 0; i < properties.length; i++) {
				String key = properties[i].getPropertyName();
				if (hasChanged(key)) {
					changed = true;
					break;
				}
			}
			if (changed)
				super.processMethod(method, optional);
		}

	}

	final static private String JAVA_OBJECT = "java.lang.Object"; //$NON-NLS-1$

	final protected IEclipseContext context;

	final protected String fieldPrefix;

	final protected int fieldPrefixLength;

	final protected String setMethodPrefix;

	public ContextInjector(IEclipseContext context, String fieldPrefix, String setMethodPrefix) {
		this.context = context;
		this.fieldPrefix = (fieldPrefix != null) ? fieldPrefix
				: IContextConstants.INJECTION_FIELD_PREFIX;
		this.setMethodPrefix = (setMethodPrefix != null) ? setMethodPrefix
				: IContextConstants.INJECTION_SET_METHOD_PREFIX;

		fieldPrefixLength = this.fieldPrefix.length();
	}

	public void inject(String name, Object userObject) {
		// final ContextChangeEvent event
		// event.getName(), event.getContext()
		Processor processor = new Processor(name, context, true, false);
		processClassHierarchy(userObject, processor);
	}

	public void inject(Object userObject) {
		Processor processor = new Processor(null, context, true, false);
		processor.shouldProcessPostConstruct = true;
		processClassHierarchy(userObject, processor);
	}

	public void uninject(String name, Object userObject) {
		Processor processor = new Processor(name, context, false, false);
		processClassHierarchy(userObject, processor);
	}

	public void uninject(Object userObject) {
		Processor processor = new Processor(null, context, false, false);
		processor.setInjectNulls(true);
		processClassHierarchy(userObject, processor);
	}

	public void dispose(Object userObject) {
		Processor processor = new Processor(null, context, false, true);
		processor.setInjectNulls(true);
		processClassHierarchy(userObject, processor);
	}

	public void reparent(Object userObject, EclipseContext oldParent, EclipseContext newParent) {
		if (oldParent == newParent)
			return;
		Processor processor = new ReparentProcessor(oldParent, newParent);
		processClassHierarchy(userObject, processor);
	}

	// TBD this is way to many ways; leave one annotation and one non-annotation
	private void findAndCallDispose(Class objectsClass, Processor processor) {
		// 1. Try a method with dispose annotation
		Method[] methods = objectsClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getParameterTypes().length > 0)
				continue;
			if (!InjectionPropertyResolver.isPreDestory(method))
				continue;
			if (!isOverridden(method, processor))
				callMethod(processor.userObject, method, null);
		}
		// 2. Try IEclipseContextAware#contextDisposed(IEclipseContext)
		try {
			Method dispose = objectsClass.getDeclaredMethod(
					IContextConstants.INJECTION_DISPOSE_CONTEXT_METHOD,
					new Class[] { IEclipseContext.class });
			// only call this method if we haven't found any other dispose methods yet
			if (!isOverridden(dispose, processor))
				callMethod(processor.userObject, dispose, new Object[] { context });
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		// 3. Try contextDisposed()
		try {
			Method dispose = objectsClass.getDeclaredMethod(
					IContextConstants.INJECTION_DISPOSE_CONTEXT_METHOD, new Class[0]);
			// only call this method if we haven't found any other dispose methods yet
			if (!isOverridden(dispose, processor))
				callMethod(processor.userObject, dispose, null);
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}

		// 4. Try dispose()
		try {
			Method dispose = objectsClass.getDeclaredMethod("dispose", null); //$NON-NLS-1$
			// only call this method if we haven't found any other dispose methods yet
			if (!isOverridden(dispose, processor))
				callMethod(processor.userObject, dispose, null);
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
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
			Class superClass = objectsClass.getSuperclass();
			if (!superClass.getName().equals(JAVA_OBJECT)) {
				processor.classHierarchy.add(objectsClass);
				processClass(superClass, processor);
				processor.classHierarchy.remove(objectsClass);
			}
			processFields(objectsClass, processor);
			processMethods(objectsClass, processor);
		} else {
			// order: methods, fields, superclass
			processMethods(objectsClass, processor);
			processFields(objectsClass, processor);
			Class superClass = objectsClass.getSuperclass();
			if (!superClass.getName().equals(JAVA_OBJECT)) {
				processor.classHierarchy.add(objectsClass);
				processClass(superClass, processor);
				processor.classHierarchy.remove(objectsClass);
			}
		}
	}

	private void processClassHierarchy(Object userObject, Processor processor) {
		processor.setObject(userObject);
		processClass(userObject.getClass(), processor);
		processor.processPostConstructMethod();
	}

	/**
	 * Make the processor visit all declared fields on the given class.
	 */
	private void processFields(Class objectsClass, Processor processor) {
		Field[] fields = objectsClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];

			InjectionProperties properties = InjectionPropertyResolver.getInjectionProperties(
					field, fieldPrefix);
			if (properties.shouldInject())
				processor.processField(field, properties);
		}
	}

	/**
	 * Make the processor visit all declared methods on the given class.
	 */
	private void processMethods(Class objectsClass, Processor processor) {
		if (processor.isInDispose) {
			findAndCallDispose(objectsClass, processor);
		}
		Method[] methods = objectsClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			// is this method overridden?
			if (isOverridden(method, processor))
				continue; // process in the subclass
			if (processor.shouldProcessPostConstruct) {
				if (InjectionPropertyResolver.isPostConstruct(method)) {
					processor.addPostConstructMethod(method);
					continue;
				}
			}
			InjectionProperties properties = InjectionPropertyResolver.getInjectionProperties(
					method, setMethodPrefix);
			if (properties.shouldInject())
				processor.processMethod(method, properties.isOptional());
		}
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

			InjectionProperties[] properties = InjectionPropertyResolver
					.getInjectionParamProperties(method);
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
				InjectionProperties properties = InjectionPropertyResolver
						.getInjectionProperties(constructor);
				if (!properties.shouldInject())
					continue;
			}

			InjectionProperties[] properties = InjectionPropertyResolver
					.getInjectionParamsProperties(constructor);
			Object[] actualParams = processParams(properties, constructor.getParameterTypes(),
					false, false);
			if (actualParams == null)
				continue;
			Object newInstance = callConstructor(constructor, actualParams);
			if (newInstance == null)
				return null;
			ContextInjectionFactory.inject(newInstance, context, null, null);
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
			IContextProvider provider = properties[i].getProvider();
			if (provider != null) {
				provider.setContext(context);
				actualParams[i] = provider;
				continue;
			}
			// 2) must have a key defined by now
			String key = properties[i].getPropertyName();
			if (key == null)
				return null;
			// 3) if we have the key in the context
			if (context.containsKey(key)) {
				if (injectWithNulls) {
					actualParams[i] = null;
					continue;
				} else {
					Object candidate = context.get(key);
					if (candidate != null
							&& parameterTypes[i].isAssignableFrom(candidate.getClass())) {
						actualParams[i] = candidate;
						continue;
					}
				}
			}
			// 4) special case: context as the argument
			if (key.equals(IEclipseContext.class.getName())) {
				actualParams[i] = context;
				continue;
			}
			// 5) can we ignore this argument?
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
