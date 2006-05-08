/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * An ElementViewPart shows a bunch of elements in a list
 * viewer.
 */
public class ListView extends MockViewPart implements IMenuListener {

    ListViewer viewer;

    ArrayList input;

    MenuManager menuMgr;

    Menu menu;

    Action addAction;

    String ADD_ACTION_ID = "addAction";

    /**
     * Constructor for ElementViewPart
     */
    public ListView() {
        super();
        input = new ArrayList();
    }

    /**
     * @see IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {
        callTrace.add("createPartControl");

        // Create viewer.
        viewer = new ListViewer(parent);
        viewer.setLabelProvider(new LabelProvider());
        viewer.setContentProvider(new ListContentProvider());
        viewer.setInput(input);

        // Create popup menu.
        createPopupMenu();

        // Register stuff.
        getSite().setSelectionProvider(viewer);
    }

    /**
     * Creates a popup menu.
     */
    public void createPopupMenu() {
        // Create actions.
        addAction = new Action("Add Standard Items") {
            public void run() {
                addStandardItems();
            }
        };
        addAction.setId(ADD_ACTION_ID);

        // Create popup menu.
        if (useStaticMenu())
            createStaticPopupMenu();
        else
            createDynamicPopupMenu();
    }

    /**
     * Creates a dynamic popup menu.
     */
    public void createDynamicPopupMenu() {
        menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(this);
        menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    /**
     * Creates a static popup menu.
     */
    public void createStaticPopupMenu() {
        menuMgr = new MenuManager();
        menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
        menuAboutToShow(menuMgr);
    }

    public void addElement(ListElement el) {
        input.add(el);
        viewer.refresh();
        viewer.getControl().update();
    }

    public void selectElement(ListElement el) {
        if (el == null)
            viewer.setSelection(new StructuredSelection());
        else
            viewer.setSelection(new StructuredSelection(el));
    }

    public MenuManager getMenuManager() {
        return menuMgr;
    }

    /**
     * @see IMenuListener#menuAboutToShow(IMenuManager)
     */
    public void menuAboutToShow(IMenuManager menuMgr) {
        menuMgr.add(addAction);
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Tests that the menu mgr contains the expected actions.
     */
    public void verifyActions(TestCase test, IMenuManager menuMgr) {
        Assert.assertNotNull(menuMgr.find(ADD_ACTION_ID));
    }

    public void addStandardItems() {
        addElement(new ListElement("red"));
        addElement(new ListElement("blue"));
        addElement(new ListElement("green"));
        addElement(new ListElement("red", true));
    }

    /**
     * Returns <code>true</code> to indicate that a static menu should be used,
     * <code>false</code> to indicate a dynamic menu.
     */
    private boolean useStaticMenu() {
        Object data = getData();
        if (data instanceof String) {
            String arg = (String) data;
            return arg.indexOf("-staticMenu") >= 0; //$NON-NLS-1$
        }
        return false;
    }
}

