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
package org.eclipse.debug.internal.ui.actions;

 
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
 
public class SelectAllVariablesAction extends SelectAllAction {

	protected void update() {
		if (!(getView() instanceof IDebugView)) {
			return;
		}
		Viewer viewer= ((IDebugView)getView()).getViewer();
		getAction().setEnabled(((TreeViewer)viewer).getTree().getItemCount() != 0);
	}
	
	protected String getActionId() {
		return IDebugView.SELECT_ALL_ACTION + ".Variables"; //$NON-NLS-1$
	}
}
