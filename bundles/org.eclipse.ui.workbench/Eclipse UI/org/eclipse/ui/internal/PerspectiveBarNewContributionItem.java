/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ContributionItemFactory;

public class PerspectiveBarNewContributionItem extends ContributionItem {

    private MenuManager menuManager = null;

    private ToolBar toolBar = null;

    private ToolItem toolItem = null;

    public PerspectiveBarNewContributionItem(IWorkbenchWindow workbenchWindow) {
        super(PerspectiveBarNewContributionItem.class.getName());
        menuManager = new MenuManager();
        menuManager.add(ContributionItemFactory.PERSPECTIVES_SHORTLIST
                .create(workbenchWindow));
    }

    public void fill(ToolBar parent, int index) {
        if (toolItem == null && parent != null) {
            toolBar = parent;
            toolItem = new ToolItem(parent, SWT.PUSH);
            toolItem.setImage(WorkbenchImages.getImageDescriptor(
                    IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE)
                    .createImage());
            toolItem.setText(""); //$NON-NLS-1$
            toolItem.setToolTipText(WorkbenchMessages
                    .getString("PerspectiveBarNewContributionItem.toolTip")); //$NON-NLS-1$
            toolItem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent event) {
                    menuManager.update(true);
                    Point point = new Point(event.x, event.y);
                    if (event.widget instanceof ToolItem) {
                        ToolItem toolItem = (ToolItem) event.widget;
                        Rectangle rectangle = toolItem.getBounds();
                        point = new Point(rectangle.x, rectangle.y
                                + rectangle.height);
                    }
                    Menu menu = menuManager.createContextMenu(toolBar);
                    point = toolBar.toDisplay(point);
                    menu.setLocation(point.x, point.y);
                    menu.setVisible(true);
                }
            });
        }
    }
}
