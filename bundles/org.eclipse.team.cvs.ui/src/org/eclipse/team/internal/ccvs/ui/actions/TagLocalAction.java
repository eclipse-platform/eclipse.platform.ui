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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.operations.ITagOperation;
import org.eclipse.team.internal.ccvs.ui.operations.TagOperation;
import org.eclipse.team.internal.ui.dialogs.ResourceMappingResourceDisplayArea;
import org.eclipse.ui.PlatformUI;


public class TagLocalAction extends TagAction {
    
    ResourceMapping[] mappings;
	
	protected boolean performPrompting()  {
		// Prompt for any uncommitted changes
        mappings = getCVSResourceMappings();
        final UncommittedChangesDialog[] dialog = new UncommittedChangesDialog[] { null };
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    dialog[0] = new UncommittedChangesDialog(getShell(), CVSUIMessages.TagLocalAction_4, mappings, monitor) { //$NON-NLS-1$
                        protected String getSingleMappingMessage(ResourceMapping mapping) {
                            String label = ResourceMappingResourceDisplayArea.getLabel(mapping);
                            if (getAllMappings().length == 1) {
                                return NLS.bind(CVSUIMessages.TagLocalAction_2, new String[] { label }); //$NON-NLS-1$
                            }
                            return NLS.bind(CVSUIMessages.TagLocalAction_0, new String[] { label }); //$NON-NLS-1$
                        }
            
                        protected String getMultipleMappingsMessage() {
                            return CVSUIMessages.TagLocalAction_1; //$NON-NLS-1$
                        }
                        protected String getHelpContextId() {
                            return IHelpContextIds.TAG_UNCOMMITED_PROMPT;
                        }
                    };
                }
            });
        } catch (InvocationTargetException e) {
            handle(e);
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        if (dialog[0] == null) return false;
        mappings = dialog[0].promptToSelectMappings();
		if(mappings.length == 0) {
			// nothing to do
			return false;						
		}
		
		return true;
	}

    protected ITagOperation createTagOperation() {
        if (mappings == null)
            mappings = getCVSResourceMappings();
		return new TagOperation(getTargetPart(), mappings);
	}
	
		/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getId()
	 */
	public String getId() {
		return ICVSUIConstants.CMD_TAGASVERSION;
	}
}
