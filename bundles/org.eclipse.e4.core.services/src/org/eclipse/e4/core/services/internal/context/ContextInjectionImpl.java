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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;

// TBD this class should be merged into ContextInjector
/**
 * Context injection implementation. See the class comment of {@link ContextInjectionFactory} for
 * details on the injection algorithm.
 */
public class ContextInjectionImpl implements IContextConstants {

	/**
	 * We keep one injector per context.
	 */
	private Map injectors = new HashMap(); // IEclipseContext -> injector

	public ContextInjectionImpl() {
		// placeholder
	}

	synchronized public void injectInto(final Object userObject, final IEclipseContext context) {
		ContextToObjectLink link;
		synchronized (injectors) {
			if (injectors.containsKey(context))
				link = (ContextToObjectLink) injectors.get(context);
			else {
				link = new ContextToObjectLink(context);
				injectors.put(context, link);
			}
		}
		context.runAndTrack(link, new Object[] { userObject });
	}

	public static Object invoke(final Object userObject, final String methodName,
			final IEclipseContext context, final Object defaultValue) {
		ContextInjector injector = new ContextInjector(context);
		return injector.invoke(userObject, methodName, defaultValue);
	}

	public static Object make(Class clazz, final IEclipseContext context) {
		ContextInjector injector = new ContextInjector(context);
		return injector.make(clazz);
	}
}
