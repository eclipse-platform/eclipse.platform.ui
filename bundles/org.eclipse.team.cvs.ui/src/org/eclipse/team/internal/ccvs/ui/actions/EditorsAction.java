/*******************************************************************************
 * Copyright (c) 2003 CSC SoftwareConsult GmbH & Co. OHG, Germany and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * 	CSC - Intial implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.Team;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.EditorsInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.EditorsDialog;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction.IProviderAction;
import org.eclipse.team.internal.ccvs.ui.Policy;


/**
 * This Action gets the <code>EditorsInfo[]</code>
 * It is used by the <code>ShowEditorAction</code> 
 * and <code>ShowEditorAction</code>.
 * 
 * @author <a href="mailto:gregor.kohlwes@csc.com,kohlwes@gmx.net">Gregor
 * Kohlwes</a>
 * 
 */
public class EditorsAction implements IProviderAction, IRunnableWithProgress {
	EditorsInfo[] f_editorsInfo = new EditorsInfo[0];
	CVSTeamProvider f_provider;
	IResource[] f_resources;

	public EditorsAction() {
	}
	
	public EditorsAction(CVSTeamProvider provider, IResource[] resources) {
		f_provider = provider;
		f_resources = resources;
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction.IProviderAction#execute(org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus execute(CVSTeamProvider provider, IResource[] resources, IProgressMonitor monitor)
		throws CVSException {
			f_editorsInfo = provider.editors(resources, monitor);
			return Team.OK_STATUS;
	}

	public boolean isPerformEdit() {
		return CVSUIPlugin.EDIT.equals(CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_EDIT_ACTION));
	}
	
	
	public boolean promptToEdit(Shell shell) {
	
		if (!isPerformEdit()) return true;
		
		if (f_editorsInfo.length > 0) {
			final EditorsDialog view = new EditorsDialog(shell, f_editorsInfo);
			// Open the dialog using a sync exec (there are no guarentees that we
			// were called from the UI thread
			CVSUIPlugin.openDialog(shell, new CVSUIPlugin.IOpenableInShell() {
				public void open(Shell shell) {
					view.open();
				}
			}, CVSUIPlugin.PERFORM_SYNC_EXEC);
			return (view.getReturnCode() == EditorsDialog.OK);
		}
		return true;


		
	}

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)
		throws InvocationTargetException, InterruptedException {
			if (f_provider == null || f_resources == null) {
				throw new InvocationTargetException(new RuntimeException(Policy.bind("EditorsAction.classNotInitialized", this.getClass().getName()))); //$NON-NLS-1$
			}
			try {
				execute(f_provider,f_resources,monitor);
			} catch (CVSException e) {
				throw new InvocationTargetException(e);
			}
	}

	/**
	 * Returns the f_editorsInfo.
	 * @return EditorsInfo[]
	 */
	public EditorsInfo[] getEditorsInfo() {
		return f_editorsInfo;
	}

}
