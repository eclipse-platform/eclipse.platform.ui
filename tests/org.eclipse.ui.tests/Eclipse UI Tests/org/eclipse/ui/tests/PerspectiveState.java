/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests;

import java.util.ArrayList;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;

/**
 * Represents the state of a perspective (layout, etc).
 * 
 * @since 3.1
 */
public class PerspectiveState {

    private IMemento memento;
    
    /**
     *  
     */
    public PerspectiveState(IWorkbenchPage page) {
        WorkbenchPage wbPage = (WorkbenchPage) page;
        Perspective persp = wbPage.getActivePerspective();
        XMLMemento mem = XMLMemento.createWriteRoot("perspectiveState");
        persp.saveState(mem);
        this.memento = mem;
    }

    /**
     * Returns the part ids in the given folder (specify <code>null</code> for top level).
     *  
     * @param folderId the folder id, or <code>null</code>
     * @return the part ids in the given folder (an <code>ArrayList</code> of <code>String</code>)
     */
    public ArrayList getPartIds(String folderId) {
        ArrayList result = new ArrayList();
        IMemento[] infos = memento.getChild("layout").getChild("mainWindow").getChildren("info");
        for (int i = 0; i < infos.length; i++) {
            IMemento info = infos[i];
            String partId = info.getString("part");
            if ("true".equals(info.getString("folder"))) {
                if (partId.equals(folderId)) {
	                IMemento[] pages = info.getChild("folder").getChildren("page");
	                for (int j = 0; j < pages.length; j++) {
	                    IMemento page = pages[j];
	                    result.add(page.getString("content"));
	                }
                }
            }
            else {
                if (folderId == null) {
                    result.add(partId);
                }
            }
        }
        return result;
    }
    
}
