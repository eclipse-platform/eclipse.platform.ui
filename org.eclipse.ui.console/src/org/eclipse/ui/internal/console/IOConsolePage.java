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
package org.eclipse.ui.internal.console;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;

/**
 * A page for an IOConsole
 * 
 * @since 3.1
 *  
 */
public class IOConsolePage extends TextConsolePage {

    private ScrollLockAction fScrollLockAction;

    private boolean fReadOnly;

    private IPropertyChangeListener fPropertyChangeListener;

    public IOConsolePage(TextConsole console, IConsoleView view) {
        super(console, view);

        fPropertyChangeListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String property = event.getProperty();
                if (property.equals(IConsoleConstants.P_CONSOLE_OUTPUT_COMPLETE)) {
                    setReadOnly();
                }
            }
        };
        console.addPropertyChangeListener(fPropertyChangeListener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        super.createControl(parent);
        if (fReadOnly) {
            IOConsoleViewer viewer = (IOConsoleViewer) getViewer();
            viewer.setReadOnly();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.console.TextConsolePage#createViewer(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.ui.console.TextConsole)
     */
    protected TextConsoleViewer createViewer(Composite parent) {
        return new IOConsoleViewer(parent, (TextConsole)getConsole());
    }

    public void setAutoScroll(boolean scroll) {
        IOConsoleViewer viewer = (IOConsoleViewer) getViewer();
        if (viewer != null) {
            viewer.setAutoScroll(scroll);
            fScrollLockAction.setChecked(!scroll);
        }
    }

    /**
     * Informs the viewer that it's text widget should not be editable.
     */
    public void setReadOnly() {
        fReadOnly = true;
        IOConsoleViewer viewer = (IOConsoleViewer) getViewer();
        if (viewer != null) {
            viewer.setReadOnly();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.TextConsolePage#createActions()
     */
    protected void createActions() {
        super.createActions();
        fScrollLockAction = new ScrollLockAction(getConsoleView());
        setAutoScroll(!fScrollLockAction.isChecked());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.TextConsolePage#contextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    protected void contextMenuAboutToShow(IMenuManager menuManager) {
        super.contextMenuAboutToShow(menuManager);
        menuManager.add(fScrollLockAction);
        IOConsoleViewer viewer = (IOConsoleViewer) getViewer();
        if (!viewer.isReadOnly()) {
            menuManager.remove(ActionFactory.CUT.getId());
            menuManager.remove(ActionFactory.PASTE.getId());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.console.TextConsolePage#configureToolBar(org.eclipse.jface.action.IToolBarManager)
     */
    protected void configureToolBar(IToolBarManager mgr) {
        super.configureToolBar(mgr);
        mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fScrollLockAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.IPage#dispose()
     */
    public void dispose() {
        if (fScrollLockAction != null) {
            fScrollLockAction.dispose();
            fScrollLockAction = null;
        }
        getConsole().removePropertyChangeListener(fPropertyChangeListener);
        super.dispose();
    }

}
