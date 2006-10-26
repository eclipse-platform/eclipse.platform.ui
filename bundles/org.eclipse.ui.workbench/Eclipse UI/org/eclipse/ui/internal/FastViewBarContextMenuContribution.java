/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewReference;

public class FastViewBarContextMenuContribution extends ContributionItem {
    private MenuItem orientationItem;
    private MenuItem restoreItem;
    private MenuItem closeItem;
    private FastViewBar bar;
    private RadioMenu radioButtons;
    private IViewReference selectedView;
    private IntModel currentOrientation = new IntModel(SWT.VERTICAL);
    
    private IChangeListener orientationChangeListener = new IChangeListener() {
        public void update(boolean changed) {
            if (changed && selectedView != null) {
                bar.setOrientation(selectedView, currentOrientation.get());
            }
        }
    };
    
    public FastViewBarContextMenuContribution(FastViewBar bar) {
        this.bar = bar;
        currentOrientation.addChangeListener(orientationChangeListener);
    }

    public void fill(Menu menu, int index) {
        // TODO Auto-generated method stub
        super.fill(menu, index);
        

        orientationItem = new MenuItem(menu, SWT.CASCADE, index++);
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

        restoreItem = new MenuItem(menu, SWT.CHECK, index++);
        restoreItem.setText(WorkbenchMessages.ViewPane_fastView);
        restoreItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                bar.restoreView(selectedView);
            }
        });

        closeItem = new MenuItem(menu, SWT.NONE, index++);
        closeItem.setText(WorkbenchMessages.WorkbenchWindow_close); 
        closeItem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (selectedView != null) {
                    WorkbenchPage page = bar.getWindow().getActiveWorkbenchPage();
                    if (page != null) {
                        page.hideView(selectedView);
                    }
                }
            }
        });

       
        // Set menu item enablement etc based on whether a view is selected
        WorkbenchPage page = bar.getWindow().getActiveWorkbenchPage();
        
        if (selectedView != null) {
        	restoreItem.setEnabled(page!=null && page.isMoveable(selectedView));
        } else {
        	restoreItem.setEnabled(false);
        }
        restoreItem.setSelection(true);
        
        if (selectedView != null) {
			closeItem
					.setEnabled(page != null && page.isCloseable(selectedView));
		} else {
			closeItem.setEnabled(false);
		}
        
        orientationItem.setEnabled(selectedView != null);
        if (selectedView != null) {
            // Set the new orientation, but avoid re-sending the event to our own
            // listener
            currentOrientation.set(bar.getOrientation(selectedView),
                    orientationChangeListener);
        }
    }
    
    public void setTarget(IViewReference selectedView) {
        this.selectedView = selectedView;
    }

    public boolean isDynamic() {
        return true;
    }
    
    public void dispose() {
        if (radioButtons != null) {
            radioButtons.dispose();
        }
        super.dispose();
    }
}
