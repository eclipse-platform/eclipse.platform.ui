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

		protected IEclipseContext context;
		protected boolean isSetter;

		public Processor(IEclipseContext context, boolean isSetter) {
			this.isSetter = isSetter;
			this.context = context;
		}

		// true if a field was set 
		abstract void processMethod(Method method);

		// true if a field was set 
		abstract void processField(Field field);
		
		protected String findKey(String key) {
			if (context.containsKey(key)) // priority goes to exact match
				return key; 
			// alternate capitalization of the first char if possible
			String candidate = altKey(key);
			if (candidate == null) // no alternative spellings
				return null; 
			if (context.containsKey(candidate))
				return candidate; 
			return null; // means "not set"; differentiate from null values
		}
		
		protected boolean keyMatches(String key1, String key2) {
			if (key1 == null && key2 == null)
				return true;
			if (key1 == null || key2 == null)
				return false;
			if (key1.equals(key2))
				return true;
			String candidate = altKey(key2);
			if (candidate == null) // no alternative spellings
				return false; 
			return key1.equals(candidate);
		}
		
		/**
		 * Calculates alternative spelling of the key: "log" <-> "Log", if any.
		 * Returns null if there is no alternate.
		 */
		protected String altKey(String key) {
			if (key.length() == 0)
				return null;
			char firstChar = key.charAt(0);
			String candidate = null;
			if (Character.isUpperCase(firstChar)) {
				firstChar = Character.toLowerCase(firstChar);
				if (key.length() == 1)
					candidate = Character.toString(firstChar);
				else
					candidate = Character.toString(firstChar) + key.substring(1);
			} else if (Character.isLowerCase(firstChar)) {
				firstChar = Character.toUpperCase(firstChar);
					if (key.length() == 1)
						candidate = Character.toString(firstChar);
					else
						candidate = Character.toString(firstChar) + key.substring(1);
			}
			return candidate;
		}
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

		final Processor processor = new Processor(context, true /*setter*/) {
			public void processField(final Field field) {
				final String candidateName = field.getName();
				if (!candidateName.startsWith(fieldPrefix))
					return;
				context.runAndTrack(new Runnable() {
					public void run() {
						String key = findKey(candidateName.substring(fieldPrefixLength));
						if (key == null) // value not set in the context
							return;
						Object value = context.get(key);
						setField(userObject, field, value);
					}
				}, "Java reflection injection");
			}

			public void processMethod(final Method method) {
				final String candidateName = method.getName();
				if (!candidateName.startsWith(SET_METHOD_PREFIX))
					return;
				context.runAndTrack(new Runnable() {
					public void run() {
						final String key = findKey(candidateName.substring(SET_METHOD_PREFIX.length()));
						if (key == null) // value not set in the context
							return; 
						Class[] parameterTypes = method.getParameterTypes();
						Object value = context.get(key, parameterTypes);
						setMethod(userObject, method, value);
					}
				}, "Java reflection injection");
			}
		};
		
		walkClassHierarchy(userObject.getClass(), processor);

		// trigger post-injection processing
		notifyUserMethod(CONTEXT_SET_METHOD, userObject, context);
	}

	protected void notifyUserObject(final IEclipseContext context, final Object userObject, final String serviceName, final Object value, boolean isSetter) {
		final String methodPrefix = (isSetter) ? setMethodPrefix : setRemovePrefix;

		Processor processor = new Processor(context, isSetter) {
			public void processField(Field field) {
				String candidateName = field.getName();
				if (!candidateName.startsWith(fieldPrefix))
					return;
				String candidate = candidateName.substring(fieldPrefix.length());
				if (!keyMatches(serviceName, candidate))
					return;
				setField(userObject, field, (isSetter) ? value : null);
			}

			public void processMethod(Method method) {
				String candidateName = method.getName();
				if (!candidateName.startsWith(methodPrefix))
					return;
				String candidate = candidateName.substring(methodPrefix.length());
				if (!keyMatches(serviceName, candidate))
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
