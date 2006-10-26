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
package org.eclipse.team.internal.ui.actions;

import org.eclipse.compare.patch.ApplyPatchOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;

public class ApplyPatchAction extends TeamAction {

	protected boolean isEnabled() throws TeamException {
		return true;
	}
	
	public void run(IAction action) {
		IResource[] resources = getSelectedResources();
		IResource resource = null;
		if (resources.length > 0) {
			resource = resources[0];
		}
		ApplyPatchOperation op = new ApplyPatchOperation(getTargetPart(), resource);
		BusyIndicator.showWhile(Display.getDefault(), op); 
	}

}
