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
package org.eclipse.ui.internal.intro.impl.presentations;

import org.eclipse.jface.action.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.swt.*;
import org.eclipse.ui.internal.intro.impl.util.*;

/**
 * This is a UI Forms based implementation of an Intro Part Presentation.
 */
public class FormIntroPartImplementation extends
        AbstractIntroPartImplementation implements IPropertyListener {

    private FormToolkit toolkit = null;
    private ScrolledPageBook mainPageBook = null;
    // cache model instance for reuse.
    private IntroModelRoot model = getModelRoot();
    private SharedStyleManager sharedStyleManager;

    static {
        // REVISIT: register all common images here. Even if this part
        // implementation is created again, the images will remain in plugin
        // registry.
        ImageUtil.registerImage(ImageUtil.DEFAULT_ROOT_LINK, "overview_48.gif"); //$NON-NLS-1$
        ImageUtil.registerImage(ImageUtil.DEFAULT_SMALL_ROOT_LINK,
                "overview_32.gif"); //$NON-NLS-1$
        ImageUtil.registerImage(ImageUtil.DEFAULT_FORM_BG, "form_banner.gif"); //$NON-NLS-1$
        ImageUtil.registerImage(ImageUtil.DEFAULT_LINK, "welcome_item.gif"); //$NON-NLS-1$
    }

    private Action homeAction = new Action() {

        {
            setToolTipText(IntroPlugin.getString("Browser.homeButton_tooltip")); //$NON-NLS-1$
            setImageDescriptor(ImageUtil.createImageDescriptor("home_nav.gif")); //$NON-NLS-1$
        }

        public void run() {
            IntroHomePage rootPage = getModelRoot().getHomePage();
            if (rootPage.isDynamic())
                getModelRoot().setCurrentPageId(rootPage.getId());
        }
    };

    public FormIntroPartImplementation() {
        // Shared style manager
        sharedStyleManager = new SharedStyleManager(getModelRoot());
    }

    public void createPartControl(Composite container) {
        // Create single toolkit instance, which is disposed of on dispose of
        // intro part.
        // also define bacjgrounf of all presentation.
        toolkit = new FormToolkit(container.getDisplay());
        // Define presentation title color
        Color bg = sharedStyleManager.getColor(toolkit, "bg"); //$NON-NLS-1$
        if (bg != null) {
            toolkit.setBackground(bg);
        }
        toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
                HyperlinkGroup.UNDERLINE_HOVER);

        // Define presentation title color and image.
        Form mainForm = toolkit.createForm(container);
        Color fg = sharedStyleManager.getColor(toolkit, "title.fg"); //$NON-NLS-1$
        if (fg != null)
            mainForm.setForeground(fg);
        // Image bgImage = sharedStyleManager.getImage("title.image", null,
        // null); //$NON-NLS-1$
        // if (bgImage != null) {
        //    mainForm.setBackgroundImage(bgImage);
        //    String repeat = sharedStyleManager
        //            .getProperty("title.image.repeat"); //$NON-NLS-1$
        //    if (repeat != null && repeat.toLowerCase().equals("true"))
        // //$NON-NLS-1$
        //           mainForm.setBackgroundImageTiled(true);
        //}


        mainPageBook = createMainPageBook(toolkit, mainForm);
        // Add this presentation as a listener to model.
        getModelRoot().addPropertyListener(this);

        // Clear memory. No need for style manager any more.
        sharedStyleManager = null;

        addToolBarActions();
    }

    private ScrolledPageBook createMainPageBook(FormToolkit toolkit, Form form) {

        // get body and create page book in it. Body has GridLayout.
        Composite body = form.getBody();
        body.setLayout(new GridLayout());
        // make sure page book expands h and v.
        ScrolledPageBook pageBook = toolkit.createPageBook(body, SWT.V_SCROLL
                | SWT.H_SCROLL);
        pageBook.setLayoutData(new GridData(GridData.FILL_BOTH));
        // creating root page form.
        if (!pageBook.hasPage(model.getHomePage().getId())) {
            // if we do not have a root page form. create one and show it.
            RootPageForm rootPageForm = new RootPageForm(toolkit, model, form);
            rootPageForm.createPartControl(pageBook, sharedStyleManager);
            pageBook.showPage(model.getHomePage().getId());
        }

        // creating static page form.
        if (!pageBook.hasPage(PageForm.PAGE_FORM_ID)) {
            // if we do not have this page create one in main page book.
            PageForm pageForm = new PageForm(toolkit, model, form);
            pageForm.createPartControl(pageBook, sharedStyleManager);
        }

        return pageBook;
    }

    public void dispose() {
        toolkit.dispose();
    }

    /**
     * Handle model property changes. The UI is notified here of a change to the
     * current page in the model. This happens if an intro URL showPage method
     * is executed.
     * 
     * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
     *      int)
     */
    public void propertyChanged(Object source, int propId) {
        if (propId == IntroModelRoot.CURRENT_PAGE_PROPERTY_ID) {
            String pageId = getModelRoot().getCurrentPageId();
            if (pageId == null | pageId.equals("")) //$NON-NLS-1$
                // If page ID was not set properly. exit.
                return;

            String rootPageId = getModelRoot().getHomePage().getId();

            // if we are showing a regular intro page, set the page id to the
            // static PageForm id.
            if (!pageId.equals(rootPageId))
                pageId = PageForm.PAGE_FORM_ID;

            mainPageBook.showPage(pageId);
        }
    }

    protected void addToolBarActions() {
        // Handle menus:
        IActionBars actionBars = getIntroPart().getIntroSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(homeAction);
        toolBarManager.update(true);
        actionBars.updateActionBars();
        //super.addToolBarActions();
    }

    public void setFocus() {
        mainPageBook.getCurrentPage().setFocus();
    }

}