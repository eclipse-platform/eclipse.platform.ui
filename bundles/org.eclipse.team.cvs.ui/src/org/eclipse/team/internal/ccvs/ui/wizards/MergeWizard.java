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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation;
import org.eclipse.team.internal.ccvs.ui.subscriber.MergeSynchronizeParticipant;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.ui.IWorkbenchPart;

public class MergeWizard extends Wizard {
    MergeWizardPage page;
	IResource[] resources;
    private final IWorkbenchPart part;
    
	public MergeWizard(IWorkbenchPart part, IResource[] resources) {
        this.part = part;
        this.resources = resources;
    }

	public void addPages() {
	    setNeedsProgressMonitor(true);
	    TagSource tagSource = TagSource.create(resources);
		setWindowTitle(Policy.bind("MergeWizard.title")); //$NON-NLS-1$
		ImageDescriptor mergeImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_MERGE);
		page = new MergeWizardPage("mergePage", Policy.bind("MergeWizard.0"), mergeImage, Policy.bind("MergeWizard.1"), tagSource); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addPage(page);
	}

	/*
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		
		CVSTag startTag = page.getStartTag();
		CVSTag endTag = page.getEndTag();			
		
		if (startTag == null || !page.isPreview()) {
		    // Perform the update (merge) in the background
		    UpdateOperation op = new UpdateOperation(getPart(), resources, getLocalOptions(startTag, endTag), null);
		    try {
                op.run();
            } catch (InvocationTargetException e) {
                CVSUIPlugin.openError(getShell(), null, null, e);
            } catch (InterruptedException e) {
                // Ignore
            }
		} else {
			// First check if there is an existing matching participant, if so then re-use it
			MergeSynchronizeParticipant participant = MergeSynchronizeParticipant.getMatchingParticipant(resources, startTag, endTag);
			if(participant == null) {
				CVSMergeSubscriber s = new CVSMergeSubscriber(resources, startTag, endTag);
				participant = new MergeSynchronizeParticipant(s);
				TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
			}
			participant.refresh(resources, null, null, null);
		}
		return true;
	}
	
    private Command.LocalOption[] getLocalOptions(CVSTag startTag, CVSTag endTag) {
        List options = new ArrayList();
        if (startTag != null) {
            options.add(Command.makeArgumentOption(Update.JOIN, startTag.getName()));
        }
        options.add(Command.makeArgumentOption(Update.JOIN, endTag.getName()));
        return (Command.LocalOption[]) options.toArray(new Command.LocalOption[options.size()]);
    }

    private IWorkbenchPart getPart() {
        return part;
    }
}
