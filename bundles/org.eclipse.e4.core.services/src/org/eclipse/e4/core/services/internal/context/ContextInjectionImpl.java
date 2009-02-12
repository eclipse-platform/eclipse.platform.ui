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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IJavaInjection;

/**
 * The first character of the service name is not case-sensitive; rest is case-sensitive:
 * <default_prefix>Log <-> Log <-> log
 */
public class ContextInjectionImpl implements IJavaInjection {
	
	/**
	 * We keep one injector per context.
	 */
	private Map injectors = new HashMap(); // IEclipseContext -> injector

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

	synchronized public void injectInto(final Object userObject, final IEclipseContext context) {
		ContextToObjectLink link;
		synchronized (injectors) {
			if (injectors.containsKey(context))
				link = (ContextToObjectLink) injectors.get(context);
			else {
				link = new ContextToObjectLink(context, fieldPrefix, setMethodPrefix);
				injectors.put(context, link);
			}
		}
		context.runAndTrack(link, new Object[] {userObject});

		// trigger post-injection processing
		notifyUserMethod(CONTEXT_SET_METHOD, userObject, context);
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
