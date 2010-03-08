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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.injector.IObjectProvider;

public class InjectionClass extends InjectionAbstract {

	final static private String JAVA_OBJECT = "java.lang.Object"; //$NON-NLS-1$

	public InjectionClass(Object userObject, IObjectProvider primarySupplier) {
		super(userObject, primarySupplier, false);
	}

	public boolean notify(ContextChangeEvent event) {
		Object object = getObject();
		if (object == null)
			return false;
		int eventType = event.getEventType();
		if (eventType == ContextChangeEvent.DISPOSE) {
			if (object instanceof IDisposable)
				((IDisposable) object).dispose();
			processPreDestory(object, object.getClass(), new ArrayList(5));
			return false;
		}
		if (eventType == ContextChangeEvent.INITIAL) {
			processPostConstruct(object, object.getClass(), new ArrayList(5));
		}
		IObjectProvider context = event.getContext();
		context.get(new InjectionProperties(true, "e4_valid_context", true, null)); // pseudo-dependency
																					// to create a
																					// link
		return true;
	}

	private void processPostConstruct(Object userObject, Class objectClass, ArrayList classHierarchy) {
		Class superClass = objectClass.getSuperclass();
		if (superClass != null && !superClass.getName().equals(JAVA_OBJECT)) {
			classHierarchy.add(objectClass);
			processPostConstruct(userObject, superClass, classHierarchy);
			classHierarchy.remove(objectClass);
		}
		Method[] methods = objectClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (!isPostConstruct(method))
				continue;
			if (!isOverridden(method, classHierarchy)) {
				InjectionMethod methodInvoke = new InjectionMethod(getObject(), primarySupplier,
						method, false);
				try {
					methodInvoke.invoke(false, false);
				} catch (InjectionException e) {
					// TBD log
					e.printStackTrace();
				}
			}
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

	private void processPreDestory(Object userObject, Class objectClass, ArrayList classHierarchy) {
		Class superClass = objectClass.getSuperclass();
		if (superClass != null && !superClass.getName().equals(JAVA_OBJECT)) {
			classHierarchy.add(objectClass);
			processPreDestory(userObject, superClass, classHierarchy);
			classHierarchy.remove(objectClass);
		}
		Method[] methods = objectClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getParameterTypes().length > 0) // TBD why?
				continue;
			if (!annotationSupport.isPreDestory(method))
				continue;
			if (!isOverridden(method, classHierarchy)) {
				InjectionMethod methodInvoke = new InjectionMethod(getObject(), primarySupplier,
						method, false);
				try {
					methodInvoke.invoke(false, false);
				} catch (InjectionException e) {
					// TBD log
					e.printStackTrace();
				}
			}
		}
	}

	// TBD move into the base class? For @PostConstruct?
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

}
