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

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.IContextConstants;

/**
 *
 */
public class BindingLookupFinction extends ContextFunction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.context.spi.ContextFunction#compute(org.eclipse.e4.core.services
	 * .context.IEclipseContext, java.lang.Object[])
	 */
	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		if (arguments == null) {
			return this;
		}
		if (arguments.length > 1) {
			if (BindingServiceImpl.LOOKUP_BINDING.equals(arguments[0])) {
				String bindingId = (String) arguments[1];
				IEclipseContext current = context;
				IEclipseContext child = (IEclipseContext) current
						.getLocal(IContextConstants.ACTIVE_CHILD);
				while (child != null) {
					current = child;
					child = (IEclipseContext) current.getLocal(IContextConstants.ACTIVE_CHILD);
				}
				return current.get(bindingId);
			} else if (BindingServiceImpl.LOOKUP_CMD.equals(arguments[0])) {
				String cmdBindingId = (String) arguments[1];
				IEclipseContext current = context;
				IEclipseContext child = (IEclipseContext) current
						.getLocal(IContextConstants.ACTIVE_CHILD);
				while (child != null) {
					current = child;
					child = (IEclipseContext) current.getLocal(IContextConstants.ACTIVE_CHILD);
				}
				return current.get(cmdBindingId);
			}
		}
		return null;
	}

}
