/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;

 
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation;
import org.eclipse.team.internal.ccvs.ui.tags.TagSelectionWizardPage;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;
import org.eclipse.team.internal.ccvs.ui.tags.TagSourceWorkbenchAdapter;
import org.eclipse.ui.IWorkbenchPart;

public class UpdateWizard extends Wizard {
	
	private static final String UPDATE_WIZARD_SECTION = "UpdateWizard"; //$NON-NLS-1$

	private IResource[] resources;
	private final IWorkbenchPart part;
	private final WizardSizeSaver fSizeSaver;
	private TagSelectionWizardPage tagSelectionPage;
	
	protected UpdateWizard(IWorkbenchPart part, IResource[] resources) {
		this.part = part;
		this.resources = resources;
		fSizeSaver= new WizardSizeSaver(this, UPDATE_WIZARD_SECTION);
		setDialogSettings(CVSUIPlugin.getPlugin().getDialogSettings());
		setWindowTitle(Policy.bind("UpdateWizard.title")); //$NON-NLS-1$
	}
	
    public static void run(IWorkbenchPart part, IResource [] resources) {
        UpdateWizard wizard = new UpdateWizard(part, resources);
		final WizardDialog dialog= new WizardDialog(part.getSite().getShell(), wizard);
		dialog.setMinimumPageSize(wizard.loadSize());
		dialog.open();
    }
    
	public void addPages() {
		ImageDescriptor substImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_CHECKOUT);
        tagSelectionPage = new TagSelectionWizardPage("tagPage", Policy.bind("UpdateWizard.0"), substImage, Policy.bind("UpdateWizard.1"), TagSource.create(resources), TagSourceWorkbenchAdapter.INCLUDE_ALL_TAGS); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		tagSelectionPage.setAllowNoTag(true);
		tagSelectionPage.setHelpContxtId(IHelpContextIds.UPDATE_TAG_SELETION_PAGE);
		ICVSFolder[] folders = getCVSFolders();
		if (folders.length > 0) {
			try {
				CVSTag selectedTag = folders[0].getFolderSyncInfo().getTag();
				tagSelectionPage.setSelection(selectedTag);
			} catch (CVSException e) {
				CVSUIPlugin.log(e);
			}
		}
		addPage(tagSelectionPage);
	}
	
	private ICVSFolder[] getCVSFolders() {
		Set projects = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			projects.add(resource.getProject());
		}
		ICVSFolder[] folders = new ICVSFolder[projects.size()];
		int i = 0;
		for (Iterator iter = projects.iterator(); iter.hasNext();) {
			IProject project = (IProject) iter.next();
			folders[i++] = CVSWorkspaceRoot.getCVSFolderFor(project);
		}
		return folders;
	}

	/*
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			new UpdateOperation(part, resources, Command.NO_LOCAL_OPTIONS, tagSelectionPage.getSelectedTag()).run();
		} catch (InvocationTargetException e) {
			CVSUIPlugin.openError(getShell(), null, null, e);
			return false;
		} catch (InterruptedException e) {
			return false;
		}

		fSizeSaver.saveSize();
		return true;
	}
	
    public Point loadSize() {
        return fSizeSaver.getSize();
    }
}
