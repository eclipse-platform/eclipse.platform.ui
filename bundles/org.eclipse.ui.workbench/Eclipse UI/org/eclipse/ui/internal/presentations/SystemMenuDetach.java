/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuDetach extends Action implements ISelfUpdatingAction {

    private ViewPane viewPane;
    private IStackPresentationSite site;
    private WorkbenchPage page;

    public SystemMenuDetach(IStackPresentationSite site) {
        this.site = site;
        setText(WorkbenchMessages.PartPane_detach);
        update();
    }
    
    public void update() {
    	IPresentablePart presentablePart = site.getSelectedPart();
    	setEnabled(presentablePart != null && site.isPartMoveable(presentablePart));
    	if(viewPane != null){
    		setChecked(!viewPane.isDocked());
        	page = viewPane.getPage();
    	}
    }
    
    public boolean shouldBeVisible() {
    	if(page != null) {
			return page.getActivePerspective().getPresentation().canDetach();
		}
    	return false;
    }
    
    public void dispose() {
        site = null;
    }

    public void setPane(ViewPane current){
    	viewPane = current;
    	update();
    }
    	
    public void run() {
    	if(site != null){
    		if(!isChecked()){
	    		viewPane.doDetach();			
    		}
    		else{
    			viewPane.doAttach();
    		}
    	}
    }

}
