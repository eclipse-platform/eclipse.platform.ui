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
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ccvs.ui.CVSLightweightDecorator;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.ITagOperation;
import org.eclipse.team.internal.ccvs.ui.operations.TagOperation;
import org.eclipse.team.internal.ui.dialogs.IPromptCondition;
import org.eclipse.team.internal.ui.dialogs.PromptingDialog;

public class TagLocalAction extends TagAction {

	IResource[] resources;
	
	protected boolean performPrompting()  {
		// Prompt for any uncommitted changes
		PromptingDialog prompt = new PromptingDialog(getShell(), getSelectedResources(),
			getPromptCondition(), Policy.bind("TagAction.uncommittedChangesTitle"));//$NON-NLS-1$
		try {
			 resources = prompt.promptForMultiple();
		} catch(InterruptedException e) {
			return false;
		}
		if(resources.length == 0) {
			// nothing to do
			return false;						
		}
		
		return true;
	}

	protected ITagOperation getTagOperation() {
		return new TagOperation(getShell(), resources);
	}
	
	/**
	 * Note: This method is designed to be overridden by test cases.
	 */
	protected IPromptCondition getPromptCondition() {
		return new IPromptCondition() {
			public boolean needsPrompt(IResource resource) {
				return CVSLightweightDecorator.isDirty(resource);
			}
			public String promptMessage(IResource resource) {
				return Policy.bind("TagAction.uncommittedChanges", resource.getName());//$NON-NLS-1$
			}
		};
	}
}
