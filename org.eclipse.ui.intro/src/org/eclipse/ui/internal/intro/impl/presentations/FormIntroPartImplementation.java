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

import org.eclipse.core.runtime.*;
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
import org.eclipse.ui.intro.config.*;

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
            setImageDescriptor(ImageUtil
                    .createImageDescriptor("full/elcl16/home_nav.gif")); //$NON-NLS-1$
            setDisabledImageDescriptor(ImageUtil
                    .createImageDescriptor("full/dlcl16/home_nav.gif")); //$NON-NLS-1$
        }

        public void run() {
            IntroHomePage rootPage = getModelRoot().getHomePage();
            if (getModelRoot().isDynamic()) {
                CustomizableIntroPart currentIntroPart = (CustomizableIntroPart) IntroPlugin
                        .getIntro();
                currentIntroPart.getControl().setRedraw(false);
                getModelRoot().setCurrentPageId(rootPage.getId());
                updateHistory(rootPage.getId());
                currentIntroPart.getControl().setRedraw(true);
            }
        }
    };


    protected void updateNavigationActionsState() {
        if (getModelRoot().isDynamic()) {
            forwardAction.setEnabled(canNavigateForward());
            backAction.setEnabled(canNavigateBackward());
            return;
        }
    }


    public FormIntroPartImplementation() {
        // Shared style manager
        sharedStyleManager = new SharedStyleManager(getModelRoot());
    }

    public void createPartControl(Composite container) {

        if (getModelRoot().isDynamic())
            handleDynamicIntro(container);
        else {
            // create just a dummy composite for now, to enable...
            Composite composite = new Composite(container, SWT.NULL);
            handleStaticIntro();
        }
    }


    /*
     * createyet3agaian static UI forms Intro. For this, we only kaunch the url
     * of the root page.
     */
    private void handleStaticIntro() {
        String rootPageUrl = getModelRoot().getHomePage().getUrl();
        Util.openBrowser(rootPageUrl);
    }


    /*
     * create dynamic UI forms Intro, ie: swt intro.
     */
    private void handleDynamicIntro(Composite container) {
        // Create single toolkit instance, which is disposed of on dispose of
        // intro part. also define background of all presentation.
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
        Image bgImage = sharedStyleManager.getImage("title.image", null, null); //$NON-NLS-1$
        if (bgImage != null) {
            mainForm.setBackgroundImage(bgImage);
            String repeat = sharedStyleManager
                    .getProperty("title.image.repeat"); //$NON-NLS-1$
            if (repeat != null && repeat.equalsIgnoreCase("true")) //$NON-NLS-1$

                mainForm.setBackgroundImageTiled(true);
        }

        mainPageBook = createMainPageBook(toolkit, mainForm);
        // Add this presentation as a listener to model.
        getModelRoot().addPropertyListener(this);

        // Clear memory. No need for style manager any more.
        sharedStyleManager = null;

        addToolBarActions();
    }


    /**
     * The main page book that holds Intro pages. It has two pages, one that
     * holds the home page, and one that holds all other pages. If the
     * presentation is configured to not show the home page with the Home Page
     * layout, then this page book will only have one page.
     * 
     * @param toolkit
     * @param form
     * @return
     */
    private ScrolledPageBook createMainPageBook(FormToolkit toolkit, Form form) {
        // get body and create page book in it. Body has GridLayout.
        Composite body = form.getBody();
        body.setLayout(new GridLayout());
        // make sure page book expands h and v.
        ScrolledPageBook pageBook = toolkit.createPageBook(body, SWT.V_SCROLL
                | SWT.H_SCROLL);
        pageBook.setLayoutData(new GridData(GridData.FILL_BOTH));
        // Create root page form, only if needed.
        if (sharedStyleManager.useCustomHomePagelayout()) {
            // if we do not have a root page form, create one
            RootPageForm rootPageForm = new RootPageForm(toolkit, model, form);
            rootPageForm.createPartControl(pageBook, sharedStyleManager);
        }

        // Create the Page form.
        PageForm pageForm = new PageForm(toolkit, model, form);
        pageForm.createPartControl(pageBook, sharedStyleManager);

        // now determine which page to show. Show it and ad it to history.
        // if the cached page is a URL ignore it. We do not want to launch a
        // browser on startup.
        String cachedPage = getCachedCurrentPage();
        if (cachedPage != null & !isURL(cachedPage))
            model.setCurrentPageId(cachedPage);
        AbstractIntroPage pageToShow = getModelRoot().getCurrentPage();

        if (pageToShow != null) {
            if (pageBook.hasPage(pageToShow.getId()))
                // we are showing Home Page.
                pageBook.showPage(pageToShow.getId());
            else {
                // if we are showing a regular intro page, or if the Home Page
                // has a regular page layout, set the page id to the static
                // PageForm id. first create the correct content
                pageForm.showPage(pageToShow.getId());
                // then show the page
                pageBook.showPage(PageForm.PAGE_FORM_ID);
            }
            updateHistory(pageToShow.getId());
        }

        return pageBook;
    }

    public void dispose() {
        if (toolkit != null)
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
            try {
                Log.info("entering Property change.");
                String pageId = getModelRoot().getCurrentPageId();
                Log.info("current page id is: " + pageId);
                if (pageId == null | pageId.equals("")) //$NON-NLS-1$
                    // If page ID was not set properly. exit.
                    return;

                // if we are showing a regular intro page, or if the Home Page
                // has a
                // regular page layout, set the page id to the static PageForm
                // id.
                if (!mainPageBook.hasPage(pageId))
                    pageId = PageForm.PAGE_FORM_ID;
                Log.info("before show page");
                mainPageBook.showPage(pageId);
                Log.info("after show page. ");
            } catch (Exception e) {
                Log.error("Property change failed.", e);
            }
        }
    }

    protected void addToolBarActions() {
        // Handle menus:
        IActionBars actionBars = getIntroPart().getIntroSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(homeAction);
        toolBarManager.add(backAction);
        toolBarManager.add(forwardAction);
        toolBarManager.update(true);
        actionBars.updateActionBars();
        updateNavigationActionsState();

    }



    public void standbyStateChanged(boolean standby) {
        if (standby) {
            homeAction.setEnabled(false);
            forwardAction.setEnabled(false);
            backAction.setEnabled(false);
        } else {
            homeAction.setEnabled(true);
            updateNavigationActionsState();
        }
    }

    public void setFocus() {
        if (model.isDynamic()) {
            if (mainPageBook.getCurrentPage() != null)
                mainPageBook.getCurrentPage().setFocus();
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateBackward()
     */
    public boolean navigateBackward() {
        boolean success = false;
        if (getModelRoot().isDynamic()) {
            // dynamic case. Uses navigation history.
            if (canNavigateBackward()) {
                navigateHistoryBackward();
                if (isURL(getCurrentLocation()))
                    success = Util.openBrowser(getCurrentLocation());
                else {
                    // Set current page, and this will triger regen.
                    CustomizableIntroPart currentIntroPart = (CustomizableIntroPart) IntroPlugin
                            .getIntro();
                    currentIntroPart.getControl().setRedraw(false);
                    success = getModelRoot().setCurrentPageId(
                            getCurrentLocation());
                    currentIntroPart.getControl().setRedraw(true);
                }
            }
        }

        updateNavigationActionsState();
        return success;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateForward()
     */
    public boolean navigateForward() {
        boolean success = false;

        if (getModelRoot().isDynamic()) {
            // dynamic case. Uses navigation history.
            if (canNavigateForward()) {
                navigateHistoryForward();
                if (isURL(getCurrentLocation()))
                    success = Util.openBrowser(getCurrentLocation());
                else {
                    // Set current page, and this will triger regen.
                    CustomizableIntroPart currentIntroPart = (CustomizableIntroPart) IntroPlugin
                            .getIntro();
                    currentIntroPart.getControl().setRedraw(false);
                    success = getModelRoot().setCurrentPageId(
                            getCurrentLocation());
                    currentIntroPart.getControl().setRedraw(true);
                }
            }
        }
        updateNavigationActionsState();
        return success;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#handleRegistryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    protected void handleRegistryChanged(IRegistryChangeEvent event) {
        // TODO Auto-generated method stub

    }



}