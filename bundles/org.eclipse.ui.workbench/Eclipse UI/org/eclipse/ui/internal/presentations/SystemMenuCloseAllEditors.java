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
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.WorkbenchMessages;

public class SystemMenuCloseAllEditors extends Action implements ISelfUpdatingAction {

    private EditorPane editorPane;

    public SystemMenuCloseAllEditors(EditorPane editorPane) {
        this.editorPane = editorPane;
        setText(WorkbenchMessages.getString("PartPane.closeAll")); //$NON-NLS-1$
    }
    
    public void setPane(EditorPane pane) {
    	editorPane = pane;
    }
    
    public void dispose() {
        editorPane = null;
    }

    public void run() {
        editorPane.doHideAll();
    }
    
    public void update() {
    	if (editorPane == null) {
    		setEnabled(false);
    		return;
    	}
    	
    	setEnabled(editorPane.getPage().getEditorReferences().length >= 1);
    }
    
    public boolean shouldBeVisible() {
    	return true;
    }

}