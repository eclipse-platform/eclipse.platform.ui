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
package org.eclipse.team.internal.ccvs.ui.merge;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.MergeSynchronizeParticipant;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.*;

public class MergeWizard extends Wizard {
	MergeWizardStartPage startPage;
	MergeWizardEndPage endPage;
	IResource[] resources;

	public void addPages() {
		// when merging multiple resources, use the tags found on the first selected
		// resource. This makes sense because you would typically merge resources that
		// have a common context and are versioned and branched together.
		IProject projectForTagRetrieval = resources[0].getProject();
				
		setWindowTitle(Policy.bind("MergeWizard.title")); //$NON-NLS-1$
		ImageDescriptor mergeImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_MERGE);
		startPage = new MergeWizardStartPage("startPage", Policy.bind("MergeWizard.start"), mergeImage); //$NON-NLS-1$ //$NON-NLS-2$
		startPage.setProject(projectForTagRetrieval);
		addPage(startPage);
		endPage = new MergeWizardEndPage("endPage", Policy.bind("MergeWizard.end"), mergeImage, startPage); //$NON-NLS-1$ //$NON-NLS-2$
		endPage.setProject(projectForTagRetrieval);
		addPage(endPage);
	}

	/*
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		
		IWorkbenchWindow wWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = null;
		if(wWindow != null) {
			activePage = wWindow.getActivePage();
		}
		
		CVSTag startTag = startPage.getTag();
		CVSTag endTag = endPage.getTag();				
		
		CVSMergeSubscriber s = new CVSMergeSubscriber(resources, startTag, endTag);
		MergeSynchronizeParticipant participant = new MergeSynchronizeParticipant(s);
		
		ISynchronizePageConfiguration configuration = participant.createPageConfiguration();
		configuration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] { 
				ISynchronizePageConfiguration.NAVIGATE_GROUP, 
				ISynchronizePageConfiguration.MODE_GROUP, 
				ISynchronizePageConfiguration.LAYOUT_GROUP });
		configuration.setProperty(ISynchronizePageConfiguration.P_OBJECT_CONTRIBUTION_ID, CVSMergeSubscriber.ID_MODAL);	
		participant.refreshInDialog(Utils.findShell(), s.roots(), Policy.bind("Participant.merging"), configuration, null); //$NON-NLS-1$
		return true;
	}
	
	/*
	 * Set the resources that should be merged.
	 */
	public void setResources(IResource[] resources) {
		this.resources = resources;
	}
}