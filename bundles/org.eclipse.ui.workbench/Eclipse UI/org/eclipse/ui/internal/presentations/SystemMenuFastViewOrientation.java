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

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.FastViewBar;
import org.eclipse.ui.internal.IChangeListener;
import org.eclipse.ui.internal.IntModel;
import org.eclipse.ui.internal.RadioMenu;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * @since 3.0
 */
public class SystemMenuFastViewOrientation extends ContributionItem {

    private ViewPane viewPane;

    private IntModel currentOrientation = new IntModel(SWT.VERTICAL);

    public SystemMenuFastViewOrientation(ViewPane newViewPane) {
        this.viewPane = newViewPane;

        currentOrientation.addChangeListener(new IChangeListener() {
            public void update(boolean changed) {
                if (changed) {
                    WorkbenchWindow workbenchWindow = (WorkbenchWindow) viewPane
                            .getPage().getWorkbenchWindow();
                    FastViewBar bar = workbenchWindow.getFastViewBar();
                    if (bar != null && viewPane != null) {
                        bar.setOrientation(viewPane.getViewReference(),
                                currentOrientation.get());
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

            currentOrientation.set(bar.getOrientation(viewPane
                    .getViewReference()));
            MenuItem orientationItem = new MenuItem(menu, SWT.CASCADE, index);
            {
                orientationItem.setText(WorkbenchMessages
                        .getString("FastViewBar.view_orientation")); //$NON-NLS-1$

                Menu orientationSwtMenu = new Menu(orientationItem);
                RadioMenu orientationMenu = new RadioMenu(orientationSwtMenu,
                        currentOrientation);
                orientationMenu
                        .addMenuItem(
                                WorkbenchMessages
                                        .getString("FastViewBar.horizontal"), new Integer(SWT.HORIZONTAL)); //$NON-NLS-1$
                orientationMenu
                        .addMenuItem(
                                WorkbenchMessages
                                        .getString("FastViewBar.vertical"), new Integer(SWT.VERTICAL)); //$NON-NLS-1$

                orientationItem.setMenu(orientationSwtMenu);
            }
        }
    }

    public boolean isDynamic() {
        return true;
    }
}