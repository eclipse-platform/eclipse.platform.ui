/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.team.internal.ui.sync.views.SubscriberInput;
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;

/**
 * This class acts as the superclass fo all action groups that appear in the SyncViewer
 */
public abstract class SyncViewerActionGroup extends ActionGroup {
	
	private SyncViewer syncView;
	
	protected SyncViewerActionGroup(SyncViewer syncView) {
		this.syncView = syncView;
	}

	/**
	 * Return the SyncViewer for this action group
	 * @return
	 */
	public SyncViewer getSyncView() {
		return syncView;
	}
	
	/**
	 * Save the state of the action group into the given IMemento
	 * @param memento
	 */
	public void save(IMemento memento) {
	}
	
	/**
	 * Restore the state of the action group from the IMemento
	 * @param memento
	 */
	public void restore(IMemento memento) {
	}

	public void setContext(ActionContext context) {
		super.setContext(context);
		initializeActions();
	}

	public void addContext(ActionContext context) {
	}

	public void removeContext(ActionContext context) {
	}

	protected void initializeActions() {
	}

	protected SubscriberInput getSubscriberContext() {
		ActionContext input = getContext();
		if(input != null) {
			return (SubscriberInput)input.getInput();
		}
		return null;
	}

	/**
	 * Method invoked from a SyncViewerToolbarDropDownAction
	 * 
	 * @param menu
	 */
	public void fillMenu(SyncViewerToolbarDropDownAction action) {
	}
}
