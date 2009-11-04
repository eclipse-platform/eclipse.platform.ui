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

package org.eclipse.e4.ui.bindings;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.bindings.internal.BindingServiceCreationFunction;

/**
 * Utility methods for setting up an IEclipseContext with support for key bindings.
 */
public class ContextUtil {
	/**
	 * This should be called once on the application context.
	 * 
	 * @param context
	 */
	public static void bindingSetup(IEclipseContext context) {
		context.set(IContextConstants.ROOT_CONTEXT, context);
		context.set(EBindingService.class.getName(), new BindingServiceCreationFunction());
	}
}
