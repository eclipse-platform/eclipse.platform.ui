/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.ui.operations.ResourceMappingMergeOperation;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractModelMergeOperation extends ResourceMappingMergeOperation {

	private ResourceMappingContext context;
	
	public AbstractModelMergeOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings, ResourceMappingContext resourceMappingContext) {
		super(part, selectedMappings);
		this.context = resourceMappingContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingOperation#getResourceMappingContext()
	 */
	protected ResourceMappingContext getResourceMappingContext() {;
		return context;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.TeamOperation#canRunAsJob()
	 */
	protected boolean canRunAsJob() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#isPreviewInDialog()
	 */
	protected boolean isPreviewInDialog() {
		return CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_UPDATE_PREVIEW).equals(ICVSUIConstants.PREF_UPDATE_PREVIEW_IN_DIALOG);
	}

}
