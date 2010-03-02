/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ContributionItemFactory;

public class PerspectiveBarNewContributionItem extends ContributionItem {

    private MenuManager menuManager = null;

    private Image image;

    private ToolItem toolItem = null;

    public PerspectiveBarNewContributionItem(IWorkbenchWindow workbenchWindow) {
        super(PerspectiveBarNewContributionItem.class.getName());
        menuManager = new MenuManager();
        menuManager.add(ContributionItemFactory.PERSPECTIVES_SHORTLIST
                .create(workbenchWindow));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.ContributionItem#dispose()
     */
    public void dispose() {
        super.dispose();
        if (image != null && !image.isDisposed()) {
            image.dispose();
            image = null;
        }
    }

    public void fill(final ToolBar parent, int index) {
        if (toolItem == null && parent != null) {
            parent.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    //toolItem.getImage().dispose();
                    toolItem.dispose();
                    toolItem = null;
                }
            });

            toolItem = new ToolItem(parent, SWT.PUSH);
            if (image == null || image.isDisposed()) {
                image = WorkbenchImages.getImageDescriptor(
                        IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE)
                        .createImage();
            }
            toolItem.setImage(image);

            toolItem.setText(""); //$NON-NLS-1$
            toolItem.setToolTipText(WorkbenchMessages.PerspectiveBarNewContributionItem_toolTip); 
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
                    Menu menu = menuManager.createContextMenu(parent);
                    point = parent.toDisplay(point);
                    menu.setLocation(point.x, point.y);
                    menu.setVisible(true);
                }
            });
        }
    }
}
