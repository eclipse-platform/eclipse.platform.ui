/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ChangeToPerspectiveMenu;
import org.eclipse.ui.internal.PinEditorAction;
import org.eclipse.ui.internal.ReopenEditorMenu;
import org.eclipse.ui.internal.ShowInMenu;
import org.eclipse.ui.internal.ShowViewMenu;
import org.eclipse.ui.internal.SwitchToWindowMenu;
import org.eclipse.ui.internal.actions.HelpSearchContributionItem;
import org.eclipse.ui.internal.actions.PinEditorContributionItem;

/**
 * Access to standard contribution items provided by the workbench.
 * <p>
 * Most of the functionality of this class is provided by
 * static methods and fields.
 * Example usage:
 * <pre>
 * MenuManager menu = ...;
 * IContributionItem reEdit
 * 	  = ContributionItemFactory.REOPEN_EDITORS.create(window);
 * menu.add(reEdit);
 * </pre>
 * </p>
 * <p>
 * Clients may declare subclasses that provide additional application-specific
 * contribution item factories.
 * </p>
 * 
 * @since 3.0
 */
public abstract class ContributionItemFactory {

    /**
     * Id of contribution items created by this factory.
     */
    private final String contributionItemId;

    /**
     * Creates a new workbench contribution item factory with the given id.
     * 
     * @param contributionItemId the id of contribution items created by this factory
     */
    protected ContributionItemFactory(String contributionItemId) {
        this.contributionItemId = contributionItemId;
    }

    /**
     * Creates a new standard contribution item for the given workbench window.
     * <p>
     * A typical contribution item automatically registers listeners against the
     * workbench window so that it can keep its enablement state up to date.
     * Ordinarily, the window's references to these listeners will be dropped
     * automatically when the window closes. However, if the client needs to get
     * rid of a contribution item while the window is still open, the client must
     * call IContributionItem#dispose to give the item an
     * opportunity to deregister its listeners and to perform any other cleanup.
     * </p>
     * 
     * @param window the workbench window
     * @return the workbench contribution item
     */
    public abstract IContributionItem create(IWorkbenchWindow window);

    /**
     * Returns the id of this contribution item factory.
     * 
     * @return the id of contribution items created by this factory
     */
    public String getId() {
        return contributionItemId;
    }

    /**
     * Workbench action (id "pinEditor"): Toggle whether the editor is pinned.
     * This action maintains its enablement state.
     */
    public static final ContributionItemFactory PIN_EDITOR = new ContributionItemFactory(
            "pinEditor") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ContributionItemFactory */
        public IContributionItem create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            PinEditorAction action = new PinEditorAction(window);
            action.setId(getId());
            return new PinEditorContributionItem(action, window);
        }
    };

    /**
     * Workbench contribution item (id "openWindows"): A list of windows
     * currently open in the workbench. Selecting one of the items makes the
     * corresponding window the active window.
     * This action dynamically maintains the list of windows.
     */
    public static final ContributionItemFactory OPEN_WINDOWS = new ContributionItemFactory(
            "openWindows") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ContributionItemFactory */
        public IContributionItem create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new SwitchToWindowMenu(window, getId(), true);
        }
    };

    /**
     * Workbench contribution item (id "viewsShortlist"): A list of views
     * available to be opened in the window, arranged as a shortlist of 
     * promising views and an "Other" subitem. Selecting
     * one of the items opens the corresponding view in the active window.
     * This action dynamically maintains the view shortlist.
     */
    public static final ContributionItemFactory VIEWS_SHORTLIST = new ContributionItemFactory(
            "viewsShortlist") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ContributionItemFactory */
        public IContributionItem create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new ShowViewMenu(window, getId());
        }
    };

    /**
     * Workbench contribution item (id "viewsShowIn"): A list of views
     * available to be opened in the window, arranged as a list of 
     * alternate views to show the same item currently selected. Selecting
     * one of the items opens the corresponding view in the active window.
     * This action dynamically maintains the view list.
     */
    public static final ContributionItemFactory VIEWS_SHOW_IN = new ContributionItemFactory(
            "viewsShowIn") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ContributionItemFactory */
        public IContributionItem create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            
            ShowInMenu showInMenu = new ShowInMenu();
            showInMenu.setId(getId());
            showInMenu.initialize(window);
			return showInMenu;
        }
    };

    /**
     * Workbench contribution item (id "reopenEditors"): A list of recent
     * editors (with inputs) available to be reopened in the window. Selecting
     * one of the items reopens the corresponding editor on its input in the
     * active window. This action dynamically maintains the list of editors.
     */
    public static final ContributionItemFactory REOPEN_EDITORS = new ContributionItemFactory(
            "reopenEditors") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ContributionItemFactory */
        public IContributionItem create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new ReopenEditorMenu(window, getId(), true);
        }
    };

    /**
     * Workbench contribution item (id "perspectivesShortlist"): A list of
     * perspectives available to be opened, arranged as a shortlist of 
     * promising perspectives and an "Other" subitem. Selecting
     * one of the items makes the corresponding perspective active. Should a 
     * new perspective need to be opened, a workbench user preference controls
     * whether the prespective is opened in the active window or a new window.
     * This action dynamically maintains the perspectives shortlist.
     */
    public static final ContributionItemFactory PERSPECTIVES_SHORTLIST = new ContributionItemFactory(
            "perspectivesShortlist") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ContributionItemFactory */
        public IContributionItem create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new ChangeToPerspectiveMenu(window, getId());
        }
    };
    
    /**
     * Workbench contribution item (id "newWizardShortlist"): A list of
     * new item wizards available to be opened, arranged as a shortlist of 
     * promising new item wizards and an "Other" subitem. Selecting
     * one of the items invokes the corresponding new item wizard. 
     * This action dynamically maintains the new item wizard shortlist.
     * @since 3.1
     */
    public static final ContributionItemFactory NEW_WIZARD_SHORTLIST = new ContributionItemFactory(
            "newWizardShortlist") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ContributionItemFactory */
        public IContributionItem create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new BaseNewWizardMenu(window, getId());
        }
    };
    
    /**
     * Workbench contribution item (id "helpSearch"): An editable field
     * for entering help search queries.
     * @since 3.1
     */
    public static final ContributionItemFactory HELP_SEARCH = new ContributionItemFactory(
            "helpSearch") {//$NON-NLS-1$
        public IContributionItem create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new HelpSearchContributionItem(window, getId());
        }
    };

    
}
