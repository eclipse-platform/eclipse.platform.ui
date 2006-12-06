/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.io.IOException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.BaseCompareAction;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.patch.ApplyPatchOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class CompareWithPatchAction extends BaseCompareAction implements IObjectActionDelegate {

	
	private IWorkbenchPart targetPart;

	protected boolean isEnabled(ISelection selection) {
		return Utilities.getResources(selection).length == 1;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.compare.internal.BaseCompareAction#run(org.eclipse.jface.viewers.ISelection)
	 */
	protected void run(ISelection selection) {
		IResource firstResource = Utilities.getFirstResource(selection);
		
		boolean isPatch = false;
		if (firstResource instanceof IFile) {
			IFile file = (IFile)firstResource;
			if (file.exists()) {
				WorkspacePatcher patch = new WorkspacePatcher(null);
				try {
					patch.parse(file);
					if (patch.getDiffs().length > 0) {
						isPatch = true;
					}
				} catch (IOException e) {
					// Ignore
				} catch (CoreException e) {
					// Ignore
				}
			}
		}
		
		final ApplyPatchOperation patchOp;
		if (isPatch) {
			patchOp= new ApplyPatchOperation(targetPart, (IFile)firstResource, null, new CompareConfiguration());
		} else {
			patchOp= new ApplyPatchOperation(targetPart, firstResource);
		}
	
		targetPart.getSite().getShell().getDisplay().asyncExec(new Runnable(){
			public void run() {
				BusyIndicator.showWhile(targetPart.getSite().getShell().getDisplay(), patchOp); 
			}
		});

	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}
}
