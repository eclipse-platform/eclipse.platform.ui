/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;

public class ContextHelpProviderInput {
	private IContext context;
	private IContextProvider provider;
	private Control control;
	private IWorkbenchPart part;
	public ContextHelpProviderInput(IContextProvider provider, IContext context, Control control, IWorkbenchPart part) {
		this.provider = provider;
		this.context = context;
		this.control =control;
		this.part = part;
	}
	
	public IContextProvider getProvider() {
		return provider;
	}
	public IContext getContext() {
		return context;
	}
	public Control getControl() {
		return control;
	}
	public IWorkbenchPart getPart() {
		return part;
	}
}
