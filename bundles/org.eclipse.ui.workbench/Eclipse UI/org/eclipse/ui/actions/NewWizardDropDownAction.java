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

package org.eclipse.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Action which, when run, will open the new wizard dialog.
 * In addition, it has a drop-down showing the new wizard shortcuts
 * associated with the current perspective.
 *
 * @since 3.1
 */
public class NewWizardDropDownAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    private ActionFactory.IWorkbenchAction showDlgAction;

    private IContributionItem newWizardMenu;
    
    private IMenuCreator menuCreator = new IMenuCreator() {

        private MenuManager dropDownMenuMgr;

        protected void createDropDownMenuMgr() {
            if (dropDownMenuMgr == null) {
                dropDownMenuMgr = new MenuManager();
                dropDownMenuMgr.add(newWizardMenu);
            }
        }

        public Menu getMenu(Control parent) {
            createDropDownMenuMgr();
            return dropDownMenuMgr.createContextMenu(parent);
        }

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

        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IMenuCreator#dispose()
         */
        public void dispose() {
            if (dropDownMenuMgr != null) {
                dropDownMenuMgr.dispose();
                dropDownMenuMgr = null;
            }
        }
    };

    /**
     * Create a new <code>NewWizardDropDownAction</code>, with the default
     * action for opening the new wizard dialog, and the default contribution item
     * for populating the drop-down menu.
     * 
     * @param window the window in which this action appears
     */
    public NewWizardDropDownAction(IWorkbenchWindow window) {
        this(window, ActionFactory.NEW.create(window), ContributionItemFactory.NEW_WIZARD_SHORTLIST.create(window));
    }
    
    /**
     * Create a new <code>NewWizardDropDownAction</code>.
     * 
     * @param window the window in which this action appears
     * @param showDlgAction the action to delegate to when this action is run directly, 
     *   rather than being dropped down
     * @param newWizardMenu the contribution item that adds the contents to the drop-down menu
     */
    public NewWizardDropDownAction(IWorkbenchWindow window,
            ActionFactory.IWorkbenchAction showDlgAction, 
            IContributionItem newWizardMenu) {
        super(WorkbenchMessages.getString("NewWizardDropDown.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        this.showDlgAction = showDlgAction;
        this.newWizardMenu = newWizardMenu;
        
        setToolTipText(showDlgAction.getToolTipText());

        ISharedImages sharedImages = window.getWorkbench()
                .getSharedImages();
        setImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
        setDisabledImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));

        setMenuCreator(menuCreator);
    }

    /**
     * Disposes the action.
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // action has already been disposed
            return;
        }
        showDlgAction.dispose();
        newWizardMenu.dispose();
        menuCreator.dispose();
        workbenchWindow = null;
    }

    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        showDlgAction.run();
    }

}