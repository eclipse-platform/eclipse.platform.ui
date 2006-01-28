/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class ShowModelProviderAction extends Action {

	private final ISynchronizePageConfiguration configuration;
	private final ModelProvider provider;

	public ShowModelProviderAction(ISynchronizePageConfiguration configuration, ModelProvider provider) {
		super(provider.getDescriptor().getLabel(), IAction.AS_RADIO_BUTTON);
		this.configuration = configuration;
		this.provider = provider;
	}
	
	public void run() {
		Viewer v = configuration.getPage().getViewer();
		v.setInput(provider);
	}

}
