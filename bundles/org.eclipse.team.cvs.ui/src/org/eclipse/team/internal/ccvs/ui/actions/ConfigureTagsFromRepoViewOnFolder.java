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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.model.RemoteModule;
import org.eclipse.team.internal.ccvs.ui.tags.TagConfigurationDialog;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;
;

/**
 * DefineTagAction remembers a tag by name
 */
public class ConfigureTagsFromRepoViewOnFolder extends CVSAction {
	
	/**
	 * Returns the selected remote folders
	 */
	protected ICVSRemoteFolder[] getSelectedRemoteFolders() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = selection.iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof RemoteModule) {
					next = ((RemoteModule)next).getCVSResource();
				}
				if (next instanceof ICVSRemoteFolder) {
					ICVSRemoteFolder folder = (ICVSRemoteFolder)next;
					if (folder.isDefinedModule()) {
						resources.add(next);
					} else if(new Path(null, ((ICVSRemoteFolder)next).getRepositoryRelativePath()).segmentCount()==1) {
						resources.add(next);
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			return (ICVSRemoteFolder[])resources.toArray(new ICVSRemoteFolder[resources.size()]);
		}
		return new ICVSRemoteFolder[0];
	}

	/*
	 * @see CVSAction@execute(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				final ICVSRemoteFolder[] roots = getSelectedRemoteFolders();
				final Shell shell = getShell();
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						ICVSFolder[] cvsFolders = new ICVSFolder[roots.length];
						for (int i = 0; i < roots.length; i++) {
							cvsFolders[i] = roots[i];
						}
						TagConfigurationDialog d = new TagConfigurationDialog(shell, TagSource.create(cvsFolders));
						d.open();
					}
				});
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}

	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteFolder[] roots = getSelectedRemoteFolders();
		if (roots.length != 1) return false;
		return true;
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("ConfigureTagsFromRepoViewConfigure_Tag_Error_1"); //$NON-NLS-1$
	}

}
