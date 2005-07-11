/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.FastViewBar;
import org.eclipse.ui.internal.IChangeListener;
import org.eclipse.ui.internal.IntModel;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.RadioMenu;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * @since 3.0
 */
public class SystemMenuFastViewOrientation extends ContributionItem {

    private PartPane viewPane;

    private IntModel currentOrientation = new IntModel(SWT.VERTICAL);

    public SystemMenuFastViewOrientation(PartPane newViewPane) {
        this.viewPane = newViewPane;

        currentOrientation.addChangeListener(new IChangeListener() {
            public void update(boolean changed) {
                if (changed) {
                    WorkbenchWindow workbenchWindow = (WorkbenchWindow) viewPane
                            .getPage().getWorkbenchWindow();
                    FastViewBar bar = workbenchWindow.getFastViewBar();
                    if (bar != null && viewPane != null) {
                        IWorkbenchPartReference ref = viewPane.getPartReference();
                        
                        if (ref instanceof IViewReference) {
                            bar.setOrientation((IViewReference)ref,
                                    currentOrientation.get());
                        }
                    }
                }
            }
        });
    }

    public void dispose() {
        viewPane = null;
    }

    public void fill(Menu menu, int index) {
        WorkbenchWindow workbenchWindow = (WorkbenchWindow) viewPane.getPage()
                .getWorkbenchWindow();
        
        
        FastViewBar bar = workbenchWindow.getFastViewBar();
        if (bar != null && viewPane != null) {
            IWorkbenchPartReference ref = viewPane.getPartReference();
            if (ref instanceof IViewReference) {
                
                currentOrientation.set(bar.getOrientation((IViewReference)ref));
                MenuItem orientationItem = new MenuItem(menu, SWT.CASCADE, index);
                {
                    orientationItem.setText(WorkbenchMessages.FastViewBar_view_orientation);
    
                    Menu orientationSwtMenu = new Menu(orientationItem);
                    RadioMenu orientationMenu = new RadioMenu(orientationSwtMenu,
                            currentOrientation);
                    orientationMenu
                            .addMenuItem(
                                    WorkbenchMessages.FastViewBar_horizontal, new Integer(SWT.HORIZONTAL)); 
                    orientationMenu
                            .addMenuItem(
                                    WorkbenchMessages.FastViewBar_vertical, new Integer(SWT.VERTICAL)); 
    
                    orientationItem.setMenu(orientationSwtMenu);
                }
            }
        }
    }

    public boolean isDynamic() {
        return true;
    }
}
