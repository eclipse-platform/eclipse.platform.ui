/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Model provider actions for use with a {@link ModelSynchronizeParticipant}.
 * 
 * @since 3.2
 */
public abstract class ModelProviderAction extends BaseSelectionListenerAction {

	private final ISynchronizePageConfiguration configuration;

	public ModelProviderAction(String text, ISynchronizePageConfiguration configuration) {
		super(text);
		this.configuration = configuration;
		initialize(configuration);
	}

	/**
	 * @param configuration
	 */
	private void initialize(ISynchronizePageConfiguration configuration) {
		configuration.getSite().getSelectionProvider().addSelectionChangedListener(this);
		configuration.getPage().getViewer().getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				getConfiguration().getSite().getSelectionProvider().removeSelectionChangedListener(ModelProviderAction.this);
			}
		});
	}

	protected ISynchronizePageConfiguration getConfiguration() {
		return configuration;
	}
	
	/**
	 * Set the selection of this action to the given selection
	 * 
	 * @param selection the selection
	 */
	public void selectionChanged(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			super.selectionChanged((IStructuredSelection)selection);
		} else {
			super.selectionChanged(StructuredSelection.EMPTY);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		return isEnabledForSelection(selection);
	}
	
	/**
	 * @param selection
	 * @return
	 */
	protected abstract boolean isEnabledForSelection(IStructuredSelection selection);

}
