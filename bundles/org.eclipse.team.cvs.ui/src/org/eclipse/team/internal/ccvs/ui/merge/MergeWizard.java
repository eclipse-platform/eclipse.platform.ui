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


import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.MergeSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipantDialog;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeDialog;
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
		MergeSynchronizeParticipant participant = (MergeSynchronizeParticipant)SubscriberParticipant.find(s);
		if(participant == null) {
			participant = new MergeSynchronizeParticipant(s);
		}
			
		SubscriberParticipantDialog compareAction = new SubscriberParticipantDialog(activePage.getWorkbenchWindow().getShell(), CVSMergeSubscriber.ID_MODAL, participant, s.roots()) {
			protected SynchronizeDialog createCompareDialog(Shell shell, String title, CompareEditorInput input) {
				return new SynchronizeDialog(shell, title, input) {
					public boolean close() {
						final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
						if(! isRememberParticipant() && store.getBoolean(ICVSUIConstants.PREF_PROMPT_ON_MIXED_TAGS)) {						
									AvoidableMessageDialog dialog = new AvoidableMessageDialog(
											getShell(),
											"Remember the Merge",  //$NON-NLS-1$
											null,	// accept the default window icon
											"Do you want to remember this merge session for later? If you plan on merging changes into your workspace from this merge configuration later on you should save this session. This will make it easier to incrementally merge changes.",  //$NON-NLS-1$
											MessageDialog.WARNING, 
											new String[] {IDialogConstants.OK_LABEL, IDialogConstants.NO_LABEL}, 
											0);
										
									if(dialog.open() == IDialogConstants.OK_ID) {
										rememberParticipant();
									}
									if(dialog.isDontShowAgain()) {
										store.setValue(ICVSUIConstants.PREF_WARN_REMEMBERING_MERGES, false);
									}																				
								}
						return super.close();
					}
				};
			}
		};
		compareAction.run();
		return true;
	}
	
	/*
	 * Set the resources that should be merged.
	 */
	public void setResources(IResource[] resources) {
		this.resources = resources;
	}
}