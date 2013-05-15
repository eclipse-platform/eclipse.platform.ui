/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
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
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 *
 */
public class ActiveChildLookupFunction extends ContextFunction {

	private String localVar;
	private String var;

	public ActiveChildLookupFunction(String var, String localVar) {
		this.var = var;
		this.localVar = localVar;
	}

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		IEclipseContext childContext = context.getActiveChild();
		if (childContext != null) {
			return childContext.get(var);
		}
		return context.get(localVar);
	}

}
