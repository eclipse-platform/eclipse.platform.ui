/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 *
 */
public class PartServiceCreationFunction extends ContextFunction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.context.spi.ContextFunction#compute(org.eclipse.e4.core.services
	 * .context.IEclipseContext, java.lang.Object[])
	 */
	@Override
	public Object compute(IEclipseContext context) {
		// look for the top-most MWindow in the context chain:

		// 1st: go up the tree to find topmost MWindow
		MWindow window = null;
		IEclipseContext current = context;
		do {
			MContext model = current.get(MContext.class);
			if (model instanceof MWindow)
				window = (MWindow) model;
			current = current.getParent();
		} while (current != null);

		if (window == null) {
			if (context.get(MApplication.class) != null) {
				// called from Application scope
				return ContextInjectionFactory.make(ApplicationPartServiceImpl.class, context);
			}
			return IInjector.NOT_A_VALUE;
		}

		IEclipseContext windowContext = window.getContext();
		PartServiceImpl service = windowContext.getLocal(PartServiceImpl.class);
		if (service == null) {
			service = ContextInjectionFactory.make(PartServiceImpl.class, windowContext);
			windowContext.set(PartServiceImpl.class, service);
		}
		return service;
	}

}
