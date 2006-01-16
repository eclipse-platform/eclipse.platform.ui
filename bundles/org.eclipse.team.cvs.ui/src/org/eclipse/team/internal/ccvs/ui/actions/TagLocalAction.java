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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.ui.PlatformUI;

/**
 * Action that tags the local workspace with a version tag.
 */
public class TagLocalAction extends TagAction {
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.TagAction#performPrompting(org.eclipse.team.internal.ccvs.ui.operations.ITagOperation)
	 */
	protected boolean performPrompting(ITagOperation operation)  {
		if (operation instanceof TagOperation) {
			final TagOperation tagOperation = (TagOperation) operation;
			try {
				if (hasOutgoingChanges(tagOperation)) {
					final boolean[] keepGoing = new boolean[] { true };
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							OutgoingChangesDialog dialog = new OutgoingChangesDialog(getShell(), tagOperation.getScope(), 
									CVSUIMessages.TagLocalAction_2, 
									CVSUIMessages.TagLocalAction_0, 
									CVSUIMessages.TagLocalAction_1);
							int result = dialog.open();
							keepGoing[0] = result == Window.OK;
						}
					});
					return keepGoing[0];
				}
				return true;
			} catch (InterruptedException e) {
				// Ignore
			} catch (InvocationTargetException e) {
				handle(e);
			}
		}
		return false;
	}
	
	private boolean hasOutgoingChanges(final RepositoryProviderOperation operation) throws InvocationTargetException, InterruptedException {
		final boolean[] hasChange = new boolean[] { false };
		PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				try {
					monitor.beginTask("Looking for uncommitted changes", 100);
					operation.buildScope(Policy.subMonitorFor(monitor, 50));
					hasChange[0] = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().hasLocalChanges(
							operation.getScope().getTraversals(), 
							Policy.subMonitorFor(monitor, 50));
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		});
		return hasChange[0];
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.actions.TagAction#createTagOperation()
     */
    protected ITagOperation createTagOperation() {
		return new TagOperation(getTargetPart(), getCVSResourceMappings());
	}
	
		/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getId()
	 */
	public String getId() {
		return ICVSUIConstants.CMD_TAGASVERSION;
	}
}
