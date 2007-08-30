/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.eclipse.ui.internal.ViewStackTrimToolBar;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * @since 3.0
 */
public class SystemMenuFastViewOrientation extends ContributionItem {

    private PartPane viewPane;

    private IntModel currentOrientation = new IntModel(SWT.VERTICAL);

    private ViewStackTrimToolBar minimizedStack = null;
    
    public SystemMenuFastViewOrientation(PartPane newViewPane) {
    	this(newViewPane, null);
    }

    /**
	 * @param pane
	 * @param vstt
	 */
	public SystemMenuFastViewOrientation(PartPane newViewPane,
			final ViewStackTrimToolBar vstt) {
        this.viewPane = newViewPane;
        this.minimizedStack = vstt;
        
        currentOrientation.addChangeListener(new IChangeListener() {
            public void update(boolean changed) {
                if (changed) {
                    WorkbenchWindow workbenchWindow = (WorkbenchWindow) viewPane
                            .getPage().getWorkbenchWindow();
                    
                    if (vstt == null) {
	                    FastViewBar bar = workbenchWindow.getFastViewBar();
	                    if (bar != null && viewPane != null) {
	                        IWorkbenchPartReference ref = viewPane.getPartReference();
	                        
	                        if (ref instanceof IViewReference) {
	                            bar.setOrientation((IViewReference)ref,
	                                    currentOrientation.get());
	                        }
	                    }
                    }
                    else {
                    	vstt.setOrientation(currentOrientation.get(), workbenchWindow);
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

        IWorkbenchPartReference ref = viewPane.getPartReference();
        if (!(ref instanceof IViewReference))
        	return;
        
        if (minimizedStack == null) {
	        FastViewBar bar = workbenchWindow.getFastViewBar();
	        if (bar != null && viewPane != null) {
                currentOrientation.set(bar.getOrientation((IViewReference)ref));
	        }
        }
        else {
        	currentOrientation.set(minimizedStack.getPaneOrientation());
        }
        
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

    public boolean isDynamic() {
        return true;
    }
}
