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

package org.eclipse.ui.internal.ide;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.NewWizardMenu;

/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
public class NewWizardDropDownAction extends Action implements
        ActionFactory.IWorkbenchAction, IMenuCreator,
        IWorkbenchWindowPulldownDelegate2 {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    private IAction newWizardAction;

    private MenuManager dropDownMenuMgr;

    /**
     *	Create a new instance of this class
     */
    public NewWizardDropDownAction(IWorkbenchWindow window,
            IAction newWizardAction) {
        super(IDEWorkbenchMessages.getString("NewWizardDropDown.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        this.newWizardAction = newWizardAction;
        setToolTipText(newWizardAction.getToolTipText());

        // @issues should be IDE-specific images
        ISharedImages sharedImages = PlatformUI.getWorkbench()
                .getSharedImages();
        setImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
        setDisabledImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));

        setMenuCreator(this);
    }

    /**
     * create the menu manager for the drop down menu.
     */
    protected void createDropDownMenuMgr() {
        if (dropDownMenuMgr == null) {
            dropDownMenuMgr = new MenuManager();
            dropDownMenuMgr.add(new NewWizardMenu(workbenchWindow));
        }
    }

    /**
     * dispose method comment.
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // action has already been disposed
            return;
        }
        if (dropDownMenuMgr != null) {
            dropDownMenuMgr.dispose();
            dropDownMenuMgr = null;
        }
        workbenchWindow = null;
    }

    /**
     * getMenu method comment.
     */
    public Menu getMenu(Control parent) {
        createDropDownMenuMgr();
        return dropDownMenuMgr.createContextMenu(parent);
    }

    /**
     * Create the drop down menu as a submenu of parent.  Necessary
     * for CoolBar support.
     */
    public Menu getMenu(Menu parent) {
        createDropDownMenuMgr();
        Menu menu = new Menu(parent);
        IContributionItem[] items = dropDownMenuMgr.getItems();
        for (int i = 0; i < items.length; i++) {
            IContributionItem item = items[i];
            IContributionItem newItem = item;
            if (item instanceof ActionContributionItem) {
                newItem = new ActionContributionItem(
                        ((ActionContributionItem) item).getAction());
            }
            newItem.fill(menu, -1);
        }
        return menu;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
    }

    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        newWizardAction.run();
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }
}