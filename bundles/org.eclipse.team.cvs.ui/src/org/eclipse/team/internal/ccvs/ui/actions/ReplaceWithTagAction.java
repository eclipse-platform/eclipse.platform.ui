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

import org.eclipse.core.internal.resources.mapping.ResourceMapping;
import org.eclipse.core.internal.resources.mapping.ResourceMappingContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.operations.ReplaceOperation;
import org.eclipse.team.internal.ccvs.ui.tags.TagSelectionDialog;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;
import org.eclipse.team.internal.ui.dialogs.ResourceMappingResourceDisplayArea;
import org.eclipse.ui.PlatformUI;

/**
 * Action for replace with tag.
 */
public class ReplaceWithTagAction extends WorkspaceTraversalAction {
    
    /* package*/ static UncommittedChangesDialog getPromptingDialog(final Shell shell, final ResourceMapping[] mappings) {
        final UncommittedChangesDialog[] dialog = new UncommittedChangesDialog[] { null };
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    dialog[0] = new UncommittedChangesDialog(shell, CVSUIMessages.ReplaceWithTagAction_4, mappings, monitor) { 
                        protected String getSingleMappingMessage(ResourceMapping mapping) {
                            String label = ResourceMappingResourceDisplayArea.getLabel(mapping);
                            if (getAllMappings().length == 1) {
                                return NLS.bind(CVSUIMessages.ReplaceWithTagAction_2, new String[] { label }); 
                            }
                            return NLS.bind(CVSUIMessages.ReplaceWithTagAction_0, new String[] { label }); 
                        }
            
                        protected String getMultipleMappingsMessage() {
                            return CVSUIMessages.ReplaceWithTagAction_1; 
                        }
                        protected String getHelpContextId() {
                            return IHelpContextIds.REPLACE_OVERWRITE_PROMPT;
                        }
                    };
                }
            });
        } catch (InvocationTargetException e) {
            CVSUIPlugin.openError(shell, null, null, e);
            return null;
        } catch (InterruptedException e) {
            return null;
        }
        return dialog[0];
    }
    
    protected static ResourceMapping[] checkOverwriteOfDirtyResources(Shell shell, ResourceMapping[] mappings, IProgressMonitor monitor)  {
        // Prompt for any uncommitted changes
        UncommittedChangesDialog dialog = getPromptingDialog(shell, mappings);
        if (dialog == null) return null;
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
					CVSUIMessages.ReplaceWithTagAction_message, 
					CVSUIMessages.TagSelectionDialog_Select_a_Tag_1, 
					TagSelectionDialog.INCLUDE_ALL_TAGS, 
					false, /*show recurse*/
					IHelpContextIds.REPLACE_TAG_SELECTION_DIALOG); 
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Dialog.CANCEL) {
					return;
				}
				tag[0] = dialog.getResult();
				
				// For non-projects determine if the tag being loaded is the same as the resource's parent
				// If it's not, warn the user that they will have strange sync behavior
				try {
					if(!CVSAction.checkForMixingTags(getShell(), getRootTraversalResources(resourceMappings[0], ResourceMappingContext.LOCAL_CONTEXT, null), tag[0])) {
						tag[0] = null;
						return;
					}
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);			 
		
		if (resourceMappings[0] == null || resourceMappings[0].length == 0 || tag[0] == null) return;
		
		// Peform the replace in the background
		new ReplaceOperation(getTargetPart(), resourceMappings[0], tag[0]).run();
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return CVSUIMessages.ReplaceWithTagAction_replace; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
	
}
