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
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ViewThatCachesItsShell {
    
    private Dialog cachedDialog;
    private Composite parent;
    
    public ViewThatCachesItsShell(Composite parent) {
        this.parent = parent;
        parent.setLayout(new FillLayout());
        
        Button pushButton = new Button(parent, SWT.PUSH);
        pushButton.setText("Cache now");
        pushButton.addSelectionListener(new SelectionAdapter() {
           public void widgetSelected(SelectionEvent e) {
               cacheNow();
           }
        });
        cacheNow();
        
        Button openDialog = new Button(parent, SWT.PUSH);
        openDialog.setText("Open Dialog");
        openDialog.addSelectionListener(new SelectionAdapter() {
           public void widgetSelected(SelectionEvent e) {
               cachedDialog.open();
           }
        });
    }
    
    private void cacheNow() {
        cachedDialog = new MessageDialog(parent.getShell(), "test", null, "hello world", MessageDialog.INFORMATION,
                new String[] {"Okay"}, 0);            
        
    }
}
