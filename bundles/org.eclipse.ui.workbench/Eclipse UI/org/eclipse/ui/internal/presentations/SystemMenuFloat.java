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
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.DetachedViewStack;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchMessages;

public class SystemMenuFloat extends Action implements ISelfUpdatingAction {

    private DetachedViewStack stack;

    public SystemMenuFloat(DetachedViewStack stack) {
        this.stack = stack;
        setText(WorkbenchMessages.getString("PartPane.float")); //$NON-NLS-1$
        update();
    }
    
    public void update() {
    	setChecked(stack.isFloating());
    }
    
    public boolean shouldBeVisible() {
    	return true;
    }
    
    public void dispose() {
        stack = null;
    }

    public void setPane(ViewPane current){
    	
    }
    	
    public void run() {
		if(!isChecked()){
    		stack.setFloatingState(true);
		}
		else{
			stack.setFloatingState(false);
		}
    }

}
