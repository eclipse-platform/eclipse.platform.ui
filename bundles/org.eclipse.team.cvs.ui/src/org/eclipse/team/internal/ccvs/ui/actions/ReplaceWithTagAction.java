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

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.ReplaceOperation;
import org.eclipse.team.internal.ccvs.ui.tags.TagSelectionDialog;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;
import org.eclipse.team.internal.ui.dialogs.ResourceMappingResourceDisplayArea;

/**
 * Action for replace with tag.
 */
public class ReplaceWithTagAction extends WorkspaceTraversalAction {
    
    /* package*/ static UncommittedChangesDialog getPromptingDialog(Shell shell, ResourceMapping[] mappings) {
        return new UncommittedChangesDialog(shell, Policy.bind("ReplaceWithTagAction.4"), mappings) { //$NON-NLS-1$
            protected String getSingleMappingMessage(ResourceMapping mapping) {
                String label = ResourceMappingResourceDisplayArea.getLabel(mapping);
                if (getAllMappings().length == 1) {
                    return Policy.bind("ReplaceWithTagAction.2", label); //$NON-NLS-1$
                }
                return Policy.bind("ReplaceWithTagAction.0", label); //$NON-NLS-1$
            }

            protected String getMultipleMappingsMessage() {
                return Policy.bind("ReplaceWithTagAction.1"); //$NON-NLS-1$
            }
        };
    }
    
    protected static ResourceMapping[] checkOverwriteOfDirtyResources(Shell shell, ResourceMapping[] mappings, IProgressMonitor monitor)  {
        // Prompt for any uncommitted changes
        UncommittedChangesDialog dialog = getPromptingDialog(shell, mappings);
        mappings = dialog.promptToSelectMappings();
        if(mappings.length == 0) {
            // nothing to do
            return null;                       
        }
        
        return mappings;
    }

	/*
	 * Method declared on IActionDelegate.
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		
		// Setup the holders
		final ResourceMapping[][] resourceMappings = new ResourceMapping[][] {null};
		final CVSTag[] tag = new CVSTag[] {null};
		final boolean[] recurse = new boolean[] {true};
		
		// Show a busy cursor while display the tag selection dialog
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
                monitor = Policy.monitorFor(monitor);
				resourceMappings[0] = checkOverwriteOfDirtyResources(getShell(), getCVSResourceMappings(), new InfiniteSubProgressMonitor(monitor, 100));
				if(resourceMappings[0] == null || resourceMappings[0].length == 0) {
					// nothing to do
					return;
				}
				TagSelectionDialog dialog = new TagSelectionDialog(getShell(), TagSource.create(resourceMappings[0]), 
					Policy.bind("ReplaceWithTagAction.message"), //$NON-NLS-1$
					Policy.bind("TagSelectionDialog.Select_a_Tag_1"), //$NON-NLS-1$
					TagSelectionDialog.INCLUDE_ALL_TAGS, 
					!isLogicalModel(getCVSResourceMappings()), /*show recurse*/
					IHelpContextIds.REPLACE_TAG_SELECTION_DIALOG); //$NON-NLS-1$
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Dialog.CANCEL) {
					return;
				}
				tag[0] = dialog.getResult();
				recurse[0] = dialog.getRecursive();
				
				// For non-projects determine if the tag being loaded is the same as the resource's parent
				// If it's not, warn the user that they will have strange sync behavior
				try {
					if(!CVSAction.checkForMixingTags(getShell(), getRootTraversalResources(resourceMappings[0], null, null), tag[0])) {
						tag[0] = null;
						return;
					}
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);			 //$NON-NLS-1$
		
		if (resourceMappings[0] == null || resourceMappings[0].length == 0 || tag[0] == null) return;
		
		// Peform the replace in the background
		new ReplaceOperation(getTargetPart(), resourceMappings[0], tag[0] /*, recurse[0]*/).run();
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("ReplaceWithTagAction.replace"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
	
}
