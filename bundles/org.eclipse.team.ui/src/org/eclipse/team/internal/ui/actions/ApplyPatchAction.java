/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.patch.ApplyPatchOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

public class ApplyPatchAction extends TeamAction {

	public boolean isEnabled() {
		return true;
	}
	
	protected void execute(IAction action) throws InvocationTargetException,
			InterruptedException {
		IResource[] resources = getSelectedResources();
		IResource resource = null;
		if (resources.length > 0) {
			resource = resources[0];
		}
		ApplyPatchOperation op = new ApplyPatchOperation(getTargetPart(), resource);
		BusyIndicator.showWhile(Display.getDefault(), op); 
	}

}
