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

import java.util.ArrayList;
import java.util.HashSet;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.bindings.Binding;

/**
 *
 */
public class PartialMatchCommandLookupFunction extends ContextFunction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.context.spi.ContextFunction#compute(org.eclipse.e4.core.services
	 * .context.IEclipseContext, java.lang.Object[])
	 */
	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		if (arguments == null || arguments.length == 0) {
			return this;
		}
		String bindingPrefixId = (String) arguments[0];
		IEclipseContext current = context;
		IEclipseContext child = (IEclipseContext) current.getLocal(IContextConstants.ACTIVE_CHILD);
		while (child != null) {
			current = child;
			child = (IEclipseContext) current.getLocal(IContextConstants.ACTIVE_CHILD);
		}

		HashSet<Binding> set = new HashSet<Binding>();
		while (current != null) {
			ArrayList<Binding> tmp = (ArrayList<Binding>) current.getLocal(bindingPrefixId);
			if (tmp != null) {
				for (Binding binding : tmp) {
					set.add(binding);
				}
			}
			current = (IEclipseContext) current.getLocal(IContextConstants.PARENT);
		}
		return set;
	}

}
