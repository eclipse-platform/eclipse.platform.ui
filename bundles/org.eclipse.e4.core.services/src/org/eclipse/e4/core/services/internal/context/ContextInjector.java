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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
		protected Set seenMethods = new HashSet(5);

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
		public void processField(final Field field, String injectName, boolean optional) {
			if ((name != null) && !name.equals(injectName)) // filter if name is specified
				return;
			if (!context.containsKey(injectName) && addition) {
				if (!optional)
					throw new IllegalStateException("Could not set " + field //$NON-NLS-1$
							+ " because of missing: " + injectName); //$NON-NLS-1$
				return;
			}
			setField(userObject, field, addition ? context.get(injectName) : null);
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

			Object[] actualParams = processParams(properties, !addition, injectWithNulls);
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
				Object[] actualParams = processParams(properties, !addition, injectWithNulls);
				if (actualParams != null)
					callMethod(userObject, method, actualParams);
				else
					logWarning(userObject, new IllegalArgumentException());
			}
		}

		boolean seen(Method method) {
			// uniquely identify methods by name+parameter types
			StringBuffer sig = new StringBuffer();
			sig.append(method.getName());
			Class[] parms = method.getParameterTypes();
			for (int i = 0; i < parms.length; i++) {
				sig.append(parms[i]);
				sig.append(',');
			}
			return !seenMethods.add(sig.toString());
		}

	}

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

		public void processField(final Field field, String injectName, boolean optional) {
			if (hasChanged(injectName))
				super.processField(field, injectName, optional);
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
			if (!processor.seen(method))
				callMethod(processor.userObject, method, null);
		}
		// 2. Try IEclipseContextAware#contextDisposed(IEclipseContext)
		try {
			Method dispose = objectsClass.getDeclaredMethod(
					IContextConstants.INJECTION_DISPOSE_CONTEXT_METHOD,
					new Class[] { IEclipseContext.class });
			// only call this method if we haven't found any other dispose methods yet
			if (!processor.seen(dispose))
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
			if (!processor.seen(dispose))
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
			if (!processor.seen(dispose))
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
			if (!superClass.getName().equals(JAVA_OBJECT))
				processClass(superClass, processor);
			processFields(objectsClass, processor);
			processMethods(objectsClass, processor);
		} else {
			// order: methods, fields, superclass
			processMethods(objectsClass, processor);
			processFields(objectsClass, processor);
			Class superClass = objectsClass.getSuperclass();
			if (!superClass.getName().equals(JAVA_OBJECT))
				processClass(superClass, processor);
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
				processor
						.processField(field, properties.getPropertyName(), properties.isOptional());
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
			// don't process methods already visited in subclasses
			if (processor.seen(method))
				continue;
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

	public Object invoke(Object userObject, String methodName, Object defaultValue) {
		Method[] methods = userObject.getClass().getDeclaredMethods();
		for (int j = 0; j < methods.length; j++) {
			Method method = methods[j];
			if (!method.getName().equals(methodName))
				continue;

			InjectionProperties[] properties = InjectionPropertyResolver
					.getInjectionParamProperties(method);
			Object[] actualParams = processParams(properties, false, false);
			if (actualParams != null)
				return callMethod(userObject, method, actualParams);
		}
		return defaultValue;
	}

	// TBD code from ReflectionContributionFactory#createObject()
	public Object make(Class clazz, IEclipseContext context) {
		Constructor[] constructors = clazz.getConstructors();

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

			// skip non-public constructors
			if ((constructor.getModifiers() & Modifier.PUBLIC) == 0)
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
			Object[] actualParams = processParams(properties, false, false);
			if (actualParams == null)
				continue;
			Object newInstance;
			try {
				newInstance = constructor.newInstance(actualParams);
			} catch (IllegalArgumentException e) {
				logWarning(clazz, e);
				return null;
			} catch (InstantiationException e) {
				logWarning(clazz, e);
				return null;
			} catch (IllegalAccessException e) {
				logWarning(clazz, e);
				return null;
			} catch (InvocationTargetException e) {
				logWarning(clazz, e);
				return null;
			}
			ContextInjectionFactory.inject(newInstance, context, null, null);
			return newInstance;
		}

		String message = "could not find satisfiable constructor in class " + clazz.getName(); //$NON-NLS-1$
		logWarning(clazz, new RuntimeException(message));
		return null;
	}

	private Object[] processParams(InjectionProperties[] properties, boolean ignoreMissing,
			boolean injectWithNulls) {
		Object[] actualParams = new Object[properties.length];
		for (int i = 0; i < actualParams.length; i++) {
			String key = properties[i].getPropertyName();
			if (key == null)
				return null;
			if (context.containsKey(key))
				actualParams[i] = (injectWithNulls) ? null : context.get(key);
			else if (key.equals("IEclipseContext")) // TBD constant IEclipseContext.getClass().getName()
				actualParams[i] = context;
			else {
				if (ignoreMissing || properties[i].isOptional())
					actualParams[i] = null;
				else
					return null;
			}
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
		if (args != null) { // make sure args are assignable
			Class[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != args.length) {
				logWarning(method, new IllegalArgumentException());
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

}
