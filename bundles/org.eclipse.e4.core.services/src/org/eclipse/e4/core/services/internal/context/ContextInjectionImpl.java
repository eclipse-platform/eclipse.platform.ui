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

import java.lang.reflect.*;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IJavaInjection;

// TBD userObjects bound with this context are stored as weak references.
/**
 * The first character of the service name is not case-sensitive; rest is case-sensitive:
 * iSetLog <-> Log <-> log
 */
public class ContextInjectionImpl implements IJavaInjection {

	abstract private class Processor {

		public boolean isSetter;

		public Processor(boolean isSetter) {
			this.isSetter = isSetter;
		}

		// true if a field was set 
		abstract void processMethod(Method method);

		// true if a field was set 
		abstract void processField(Field field);
	}

	final static private String JAVA_OBJECT = "java.lang.Object"; //$NON-NLS-1$
	final static private Class[] contextNotifySignature = new Class[] {IEclipseContext.class};

	final protected String fieldPrefix;
	final protected String setMethodPrefix;
	final protected String setRemovePrefix;

	final protected int fieldPrefixLength;

	public ContextInjectionImpl() {
		this(FIELD_PREFIX, SET_METHOD_PREFIX, REMOVE_METHOD_PREFIX);
	}

	public ContextInjectionImpl(String fieldPrefix, String setMethodPrefix, String setRemovePrefix) {
		this.fieldPrefix = (fieldPrefix != null) ? fieldPrefix : FIELD_PREFIX;
		this.setMethodPrefix = (setMethodPrefix != null) ? setMethodPrefix : SET_METHOD_PREFIX;
		this.setRemovePrefix = (setRemovePrefix != null) ? setRemovePrefix : REMOVE_METHOD_PREFIX;

		fieldPrefixLength = this.fieldPrefix.length();
	}

	public void injectInto(final Object userObject, final IEclipseContext context) {

		final Processor processor = new Processor(true /*setter*/) {
			public void processField(Field field) {
				String candidateName = field.getName();
				if (!candidateName.startsWith(fieldPrefix))
					return;
				String key = internalCase(candidateName.substring(fieldPrefixLength));
				if (!context.containsKey(key)) // check explicitly to differentiate from null's
					return;
				Object value = context.get(key);
				setField(userObject, field, value);
			}

			public void processMethod(Method method) {
				String candidateName = method.getName();
				if (!candidateName.startsWith(SET_METHOD_PREFIX))
					return;
				String key = internalCase(candidateName.substring(SET_METHOD_PREFIX.length()));
				Class[] parameterTypes = method.getParameterTypes();
				Object value = context.get(key, parameterTypes);
				setMethod(userObject, method, value);
			}
		};
		context.runAndTrack(new Runnable() {

			public void run() {
				walkClassHierarchy(userObject.getClass(), processor);
			}
		}, "Java reflection injection");

		// trigger post-injection processing
		notifyUserMethod(CONTEXT_SET_METHOD, userObject, context);
	}

	protected void notifyUserObject(final IEclipseContext context, final Object userObject, final String serviceName, final Object value, boolean isSetter) {
		final String methodPrefix = (isSetter) ? setMethodPrefix : setRemovePrefix;

		Processor processor = new Processor(isSetter) {
			public void processField(Field field) {
				String candidateName = field.getName();
				if (!candidateName.startsWith(fieldPrefix))
					return;
				String key = internalCase(candidateName.substring(fieldPrefix.length()));
				if (!key.equals(serviceName))
					return;
				setField(userObject, field, (isSetter) ? value : null);
			}

			public void processMethod(Method method) {
				String candidateName = method.getName();
				if (!candidateName.startsWith(methodPrefix))
					return;
				String key = internalCase(candidateName.substring(methodPrefix.length()));
				if (!key.equals(serviceName))
					return;
				setMethod(userObject, method, value);
			}
		};

		walkClassHierarchy(userObject.getClass(), processor);
	}

	/**
	 * For setters: we set fields first, them methods.
	 * Otherwise, clear methods first, fields next 
	 */
	private void walkClassHierarchy(Class objectsClass, Processor processor) {
		// process superclass first
		Class superClass = objectsClass.getSuperclass();
		if (!superClass.getName().equals(JAVA_OBJECT))
			walkClassHierarchy(superClass, processor);
		if (processor.isSetter) {
			// fields second
			Field[] fields = objectsClass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				processor.processField(fields[i]);
			}
			// methods last
			Method[] methods = objectsClass.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				processor.processMethod(methods[i]);
			}
		} else {
			// methods second
			Method[] methods = objectsClass.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				processor.processMethod(methods[i]);
			}
			// fields last
			Field[] fields = objectsClass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				processor.processField(fields[i]);
			}
		}
	}

	protected String internalCase(String name) {
		char firstChar = name.charAt(0);
		if (!Character.isUpperCase(firstChar))
			return name;

		// convert first char to the lower case for internal use
		firstChar = Character.toLowerCase(firstChar);
		if (name.length() == 1)
			return Character.toString(firstChar);
		return Character.toString(firstChar) + name.substring(1);
	}

	protected boolean setField(Object userObject, Field field, Object value) {
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

	protected boolean setMethod(Object userObject, Method method, Object value) {
		Class[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1)
			return false;
		if ((value != null) && !parameterTypes[0].isAssignableFrom(value.getClass()))
			return false;

		boolean wasAccessible = true;
		if (!method.isAccessible()) {
			method.setAccessible(true);
			wasAccessible = false;
		}
		try {
			method.invoke(userObject, new Object[] {value});
		} catch (IllegalArgumentException e) {
			logWarning(method, e);
			return false;
		} catch (IllegalAccessException e) {
			logWarning(method, e);
			return false;
		} catch (InvocationTargetException e) {
			logWarning(method, e);
			return false;
		} finally {
			if (!wasAccessible)
				method.setAccessible(false);
		}
		return true;
	}

	private void notifyUserMethod(String methodName, Object userObject, IEclipseContext newContext) {
		// perform post-injection processing
		Class objectClass = userObject.getClass();
		boolean wasAccessible = true;
		Method method = null;
		try {
			method = objectClass.getMethod(methodName, contextNotifySignature);
			if (!method.isAccessible()) {
				method.setAccessible(true);
				wasAccessible = false;
			}
			method.invoke(userObject, new Object[] {newContext});
		} catch (SecurityException e) {
			logWarning(userObject, e);
		} catch (NoSuchMethodException e) {
			// fine - nothing to call
		} catch (IllegalArgumentException e) {
			logWarning(userObject, e);
		} catch (IllegalAccessException e) {
			logWarning(userObject, e);
		} catch (InvocationTargetException e) {
			logWarning(userObject, e);
		} finally {
			if (!wasAccessible && method != null)
				method.setAccessible(false);
		}
	}

	private void logWarning(Object destination, Exception e) {
		System.out.println("Injection failed " + destination.toString());
		if (e != null)
			e.printStackTrace();
		// TBD convert this into real logging
		//		String msg = NLS.bind("Injection failed", destination.toString());
		//		RuntimeLog.log(new Status(IStatus.WARNING, IRuntimeConstants.PI_COMMON, 0, msg, e));
	}
}
