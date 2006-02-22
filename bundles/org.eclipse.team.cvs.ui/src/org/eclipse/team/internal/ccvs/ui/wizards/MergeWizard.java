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
package org.eclipse.team.internal.ccvs.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.mappings.ModelMergeOperation;
import org.eclipse.team.internal.ccvs.ui.mappings.ModelMergeParticipant;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;
import org.eclipse.ui.IWorkbenchPart;

public class MergeWizard extends Wizard {
    MergeWizardPage page;
	IResource[] resources;
    private final IWorkbenchPart part;
    private final ResourceMapping[] mappings;
    
	public MergeWizard(IWorkbenchPart part, IResource[] resources, ResourceMapping[] mappings) {
        this.part = part;
        this.resources = resources;
        this.mappings = mappings;
    }

	public void addPages() {
	    setNeedsProgressMonitor(true);
	    TagSource tagSource = TagSource.create(resources);
		setWindowTitle(CVSUIMessages.MergeWizard_title); 
		ImageDescriptor mergeImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_MERGE);
		page = new MergeWizardPage("mergePage", CVSUIMessages.MergeWizard_0, mergeImage, CVSUIMessages.MergeWizard_1, tagSource); //$NON-NLS-1$  
		addPage(page);
	}

	public boolean performFinish() {
		
		CVSTag startTag = page.getStartTag();
		CVSTag endTag = page.getEndTag();			
		
		if (startTag == null || !page.isPreview()) {
		    // Perform the update (merge) in the background
		    UpdateOperation op = new UpdateOperation(getPart(), mappings, getLocalOptions(startTag, endTag), null);
		    try {
                op.run();
            } catch (InvocationTargetException e) {
                CVSUIPlugin.openError(getShell(), null, null, e);
            } catch (InterruptedException e) {
                // Ignore
            }
		} else {
			ModelMergeParticipant participant = ModelMergeParticipant.getMatchingParticipant(mappings, startTag, endTag);
			if(participant == null) {
		    	CVSMergeSubscriber s = new CVSMergeSubscriber(getProjects(resources), startTag, endTag);
		    	try {
					new ModelMergeOperation(getPart(), mappings, s, page.isOnlyPreviewConflicts()).run();
				} catch (InvocationTargetException e) {
					CVSUIPlugin.log(IStatus.ERROR, "Internal error", e.getTargetException()); //$NON-NLS-1$
				} catch (InterruptedException e) {
					// Ignore
				}
			} else {
				participant.refresh(null, mappings);
			}
		}
		return true;
	}

	private IResource[] getProjects(IResource[] resources) {
		Set projects = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			projects.add(resource.getProject());
		}
		return (IResource[]) projects.toArray(new IResource[projects.size()]);
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
