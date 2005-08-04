/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.tags;

import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;

/**
 * A tag source for multiple folders.
 * 
 * TODO: Temporarily a subclass of single folder until I 
 * can figure out how to handle the multi-folder case.
 */
public class MultiFolderTagSource extends SingleFolderTagSource {

    private final ICVSFolder[] folders;

    /* package */ MultiFolderTagSource(ICVSFolder[] folders) {
        super(folders[0]);
        this.folders = folders;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.merge.SingleFolderTagSource#getShortDescription()
     */
    public String getShortDescription() {
        return NLS.bind(CVSUIMessages.MultiFolderTagSource_0, new String[] { Integer.toString(folders.length) }); 
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.tags.SingleFolderTagSource#getCVSResources()
     */
    public ICVSResource[] getCVSResources() {
        return folders;
    }
}
