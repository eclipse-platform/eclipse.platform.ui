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

/**
 * The first character of the service name is not case-sensitive; rest is case-sensitive:
 * <default_prefix>Log <-> Log <-> log
 */
public class ContextInjectionImpl implements IJavaInjection {

	abstract private class Processor {

		protected boolean isSetter;
		
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

	final protected int fieldPrefixLength;

	public ContextInjectionImpl() {
		this(FIELD_PREFIX, SET_METHOD_PREFIX);
	}

	public ContextInjectionImpl(String fieldPrefix, String setMethodPrefix) {
		this.fieldPrefix = (fieldPrefix != null) ? fieldPrefix : FIELD_PREFIX;
		this.setMethodPrefix = (setMethodPrefix != null) ? setMethodPrefix : SET_METHOD_PREFIX;

		fieldPrefixLength = this.fieldPrefix.length();
	}

	public void injectInto(final Object userObject, final IEclipseContext context) {

		final Processor processor = new Processor(true /*setter*/) {
			public void processField(final Field field) {
				final String candidateName = field.getName();
				if (!candidateName.startsWith(fieldPrefix))
					return;
				FieldLink link = new FieldLink(userObject, context, candidateName.substring(fieldPrefixLength), field);
				context.runAndTrack(link, "Java Field Reflection Injection");
			}

			public void processMethod(final Method method) {
				final String candidateName = method.getName();
				if (!candidateName.startsWith(SET_METHOD_PREFIX))
					return;
				MethodLink link = new MethodLink(userObject, context, candidateName.substring(SET_METHOD_PREFIX.length()), method);
				context.runAndTrack(link, "Java Method Reflection Injection");
			}
		};
		
		walkClassHierarchy(userObject.getClass(), processor);
		
		// trigger post-injection processing
		notifyUserMethod(CONTEXT_SET_METHOD, userObject, context);
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
