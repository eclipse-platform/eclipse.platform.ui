/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CVSWorkingSetWizard extends Wizard {
	
	private CVSWorkingSetFolderSelectionPage folderSelectionPage;
	
	private CVSWorkingSet selection;
	private CVSWorkingSet editedSet;

	/**
	 * Constructor for CVSWorkingSetWizard.
	 */
	public CVSWorkingSetWizard() {
		super();
	}

	public CVSWorkingSetWizard(CVSWorkingSet editedSet) {
		super();
		this.editedSet = editedSet;
	}
	
	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);
		ImageDescriptor substImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_NEW_LOCATION);
		folderSelectionPage = new CVSWorkingSetFolderSelectionPage(
			"FolderSelectionPage", //$NON-NLS-1$
			Policy.bind("CVSWorkingSetFolderSelectionPage.projectSelectionPageTitle"), //$NON-NLS-1$
			substImage,
			Policy.bind("CVSWorkingSetFolderSelectionPage.projectSelectionPageDescription")); //$NON-NLS-1$
		folderSelectionPage.setOriginalWorkingSet(editedSet);
		addPage(folderSelectionPage);
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			CVSWorkingSet set = new CVSWorkingSet(folderSelectionPage.getWorkingSetName());
			set.setFolders(folderSelectionPage.getSelectedFolders());
			selection = set;
			if (editedSet != null) {
				editedSet.mutate(set);
				selection = editedSet;
			}
			return true;
		} catch (CVSException e) {
			CVSUIPlugin.openError(getShell(), null, null, e, CVSUIPlugin.PERFORM_SYNC_EXEC);
		}
		return false;
	}
	
	/**
	 * Method getSelection.
	 * @return CVSWorkingSet
	 */
	public CVSWorkingSet getSelection() {
		return selection;
	}
	
}
