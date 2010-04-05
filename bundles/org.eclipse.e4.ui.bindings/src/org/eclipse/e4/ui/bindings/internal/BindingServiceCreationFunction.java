/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.bindings.internal;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.Logger;

/**
 *
 */
public class BindingServiceCreationFunction extends ContextFunction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.context.spi.ContextFunction#compute(org.eclipse.e4.core.services
	 * .context.IEclipseContext, java.lang.Object[])
	 */
	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		try {
			return ContextInjectionFactory.make(BindingServiceImpl.class, context);
		} catch (InvocationTargetException e) {
			Logger logger = (Logger) context.get(Logger.class.getName());
			if (logger != null) {
				logger.error(e);
			}
		} catch (InstantiationException e) {
			Logger logger = (Logger) context.get(Logger.class.getName());
			if (logger != null) {
				logger.error(e);
			}
		}
		return null;
	}

}
