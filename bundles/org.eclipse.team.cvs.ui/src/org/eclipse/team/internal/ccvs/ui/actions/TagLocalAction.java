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

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.ITagOperation;
import org.eclipse.team.internal.ccvs.ui.operations.TagOperation;
import org.eclipse.team.internal.ui.dialogs.ResourceMappingResourceDisplayArea;


public class TagLocalAction extends TagAction {
    
    ResourceMapping[] mappings;
	
	protected boolean performPrompting()  {
		// Prompt for any uncommitted changes
        mappings = getCVSResourceMappings();
        UncommittedChangesDialog dialog = new UncommittedChangesDialog(getShell(), Policy.bind("TagLocalAction.4"), mappings) { //$NON-NLS-1$
            protected String getSingleMappingMessage(ResourceMapping mapping) {
                String label = ResourceMappingResourceDisplayArea.getLabel(mapping);
                if (getAllMappings().length == 1) {
                    return Policy.bind("TagLocalAction.2", label); //$NON-NLS-1$
                }
                return Policy.bind("TagLocalAction.0", label); //$NON-NLS-1$
            }

            protected String getMultipleMappingsMessage() {
                return Policy.bind("TagLocalAction.1"); //$NON-NLS-1$
            }
        };
		mappings = dialog.promptToSelectMappings();
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
