/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.AvoidableMessageDialog;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * Contains helper methods for CVS actions.
 * 
 * [Note: it would be nice to have common CVS error handling
 * placed here and have all CVS actions subclass. For example
 * error handling UI could provide a retry facility for actions
 * if they have failed.]
 */
abstract public class CVSAction extends TeamAction {
	/**
	 * Answers <code>true</code> if the current selection contains resource that don't
	 * have overlapping paths and <code>false</code> otherwise. 
	 */
	protected boolean isSelectionNonOverlapping() throws TeamException {
		IResource[] resources = getSelectedResources();
		// allow operation for non-overlapping resource selections
		if(resources.length>0) {
			List validPaths = new ArrayList(2);
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				
				// only allow cvs resources to be selected
				if(RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId()) == null) {
					return false;
				}
				
				// check if this resource overlaps other selections		
				IPath resourceFullPath = resource.getFullPath();
				if(!validPaths.isEmpty()) {
					for (Iterator it = validPaths.iterator(); it.hasNext();) {
						IPath path = (IPath) it.next();
						if(path.isPrefixOf(resourceFullPath) || 
					       resourceFullPath.isPrefixOf(path)) {
							return false;
						}
					}
				}
				validPaths.add(resourceFullPath);
				
				// ensure that resources are managed
				ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
				if(cvsResource.isFolder()) {
					if( ! ((ICVSFolder)cvsResource).isCVSFolder()) return false;
				} else {
					if( ! cvsResource.isManaged()) return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if a the resources' parent's tags are different then the given tag. 
	 * Prompts the user that they are adding mixed tags and returns <code>true</code> if 
	 * the user wants to continue or <code>false</code> otherwise.
	 */
	public static boolean checkForMixingTags(final Shell shell, IResource[] resources, final CVSTag tag) throws CVSException {
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		if(!store.getBoolean(ICVSUIConstants.PREF_PROMPT_ON_MIXED_TAGS)) {
			return true;
		};
		
		final boolean[] result = new boolean[] { true };
		
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() != IResource.PROJECT) {
				ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
				CVSTag parentTag = cvsResource.getParent().getFolderSyncInfo().getTag();
				if (!CVSTag.equalTags(tag, parentTag)) {
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {							
							AvoidableMessageDialog dialog = new AvoidableMessageDialog(
									shell,
									Policy.bind("CVSAction.mixingTagsTitle"),  //$NON-NLS-1$
									null,	// accept the default window icon
									Policy.bind("CVSAction.mixingTags", tag.getName()),  //$NON-NLS-1$
									MessageDialog.QUESTION, 
									new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
									0);
									
							result[0] = dialog.open() == 0;
							if(result[0] && dialog.isDontShowAgain()) {
								store.setValue(ICVSUIConstants.PREF_PROMPT_ON_MIXED_TAGS, false);
							}																				
						}
					});
					// only prompt once
					break;										
				}
			}
		}
		return result[0];
	}
}
