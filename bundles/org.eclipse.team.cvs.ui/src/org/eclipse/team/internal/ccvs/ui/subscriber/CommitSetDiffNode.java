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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ui.synchronize.SynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;

/**
 * This diff node represents an outgoing commit set when it appears in the 
 * synchronize view.
 */
public class CommitSetDiffNode extends SynchronizeModelElement {

    private final CommitSet set;

    public CommitSetDiffNode(ISynchronizeModelElement parent, CommitSet set) {
        super(parent);
        this.set = set;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.ISynchronizeModelElement#getResource()
     */
    public IResource getResource() {
        return null;
    }

    public CommitSet getSet() {
        return set;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_CHANGELOG); // TODO: Custom image needed
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getName()
	 */
	public String getName() {
	    String name = set.getTitle();
	    if (CommitSetManager.getInstance().isDefault(set)) {
	        name = Policy.bind("CommitSetDiffNode.0", name); //$NON-NLS-1$
	    }
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SyncInfoModelElement#toString()
	 */
	public String toString() {
		return getName();
	}
}
