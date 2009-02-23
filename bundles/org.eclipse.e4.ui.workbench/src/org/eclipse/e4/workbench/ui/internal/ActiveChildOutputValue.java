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
package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ComputedValue;
import org.eclipse.e4.ui.services.IServiceConstants;

public final class ActiveChildOutputValue extends ComputedValue {
	private final String attr;

	public ActiveChildOutputValue(String attr) {
		this.attr = attr;
	}

	public Object compute(IEclipseContext context, Object[] arguments) {
		IEclipseContext childContext = (IEclipseContext) context
				.getLocal(IServiceConstants.ACTIVE_CHILD);
		if (childContext != null) {
			return childContext.get(attr);
		} else if (context.containsKey(IServiceConstants.OUTPUTS)) {
			IEclipseContext outputs = (IEclipseContext) context
					.get(IServiceConstants.OUTPUTS);
			return outputs.get(attr);
		}
		return null;
	}
}