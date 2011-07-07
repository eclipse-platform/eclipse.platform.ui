/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.modeling;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;

public class MenuServiceCreationFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context) {
		try {
			return ContextInjectionFactory.make(MenuService.class, context);
		} catch (InjectionException ie) {
			// we won't report this at the moment.
			System.err.println("MenuService: " + context + ": " + ie);
		}
		return null;
	}

}
