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
import org.eclipse.ui.actions.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
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

    private FormToolkit toolkit;
    private ScrolledPageBook mainPageBook;
    private PageForm pageForm;
    // cache model instance for reuse.
    private IntroModelRoot model = getModel();
    private SharedStyleManager sharedStyleManager;

    // static SWT Intro. This is the link shown on the center of a page in a
    // static SWT intro.
    private Hyperlink welcomeLink;

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


    protected void updateNavigationActionsState() {
        if (getModel().isDynamic()) {
            forwardAction.setEnabled(canNavigateForward());
            backAction.setEnabled(canNavigateBackward());
            return;
        }
    }


    public FormIntroPartImplementation() {
        // Shared style manager
        sharedStyleManager = new SharedStyleManager(getModel());
    }

    public void createPartControl(Composite container) {

        if (getModel().isDynamic())
            dynamicCreatePartControl(container);
        else {
            staticCreatePartControl(container);
        }
    }



    /*
     * create dynamic UI forms Intro, ie: swt intro.
     */
    private void dynamicCreatePartControl(Composite container) {
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
        getModel().addPropertyListener(this);

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

        // Create root page in root page layout form, only if needed.
        if (sharedStyleManager.useCustomHomePagelayout()) {
            // if we do not have a root page form, create one
            RootPageForm rootPageForm = new RootPageForm(toolkit, model, form);
            rootPageForm.createPartControl(pageBook, sharedStyleManager);
        }

        // Create the Page form.
        pageForm = new PageForm(toolkit, model, form);
        pageForm.createPartControl(pageBook, sharedStyleManager);

        // now determine which page to show. Show it and ad it to history.
        // if the cached page is a URL ignore it. We do not want to launch a
        // browser on startup.
        String cachedPage = getCachedCurrentPage();
        if (cachedPage != null & !isURL(cachedPage))
            // this will create the page in the page form.
            model.setCurrentPageId(cachedPage);

        AbstractIntroPage pageToShow = getModel().getCurrentPage();
        if (pageToShow != null) {
            if (pageBook.hasPage(pageToShow.getId()))
                // we are showing Home Page.
                pageBook.showPage(pageToShow.getId());
            else {
                // if we are showing a regular intro page, or if the Home Page
                // has a regular page layout, set the page id to the static
                // PageForm id. first create the correct content.
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
            String pageId = getModel().getCurrentPageId();
            if (pageId == null | pageId.equals("")) //$NON-NLS-1$
                // If page ID was not set properly. exit.
                return;

            // if we are showing a regular intro page, or if the Home Page
            // has a regular page layout, set the page id to the static PageForm
            // id.
            if (!mainPageBook.hasPage(pageId))
                pageId = PageForm.PAGE_FORM_ID;
            mainPageBook.showPage(pageId);
        }
    }

    protected void addToolBarActions() {
        // Handle menus:
        IActionBars actionBars = getIntroPart().getIntroSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        actionBars.setGlobalActionHandler(ActionFactory.FORWARD.getId(),
                forwardAction);
        actionBars.setGlobalActionHandler(ActionFactory.BACK.getId(),
                backAction);
        toolBarManager.add(homeAction);
        toolBarManager.add(backAction);
        toolBarManager.add(forwardAction);
        toolBarManager.update(true);
        actionBars.updateActionBars();
        updateNavigationActionsState();
    }



    public void standbyStateChanged(boolean standby, boolean isStandbyPartNeeded) {
        if (getModel().isDynamic())
            dynamicStandbyStateChanged(standby, isStandbyPartNeeded);
        else
            staticStandbyStateChanged(standby);
    }


    public void dynamicStandbyStateChanged(boolean standby,
            boolean isStandbyPartNeeded) {
        // handle action enablement first
        if (isStandbyPartNeeded | standby) {
            homeAction.setEnabled(false);
            forwardAction.setEnabled(false);
            backAction.setEnabled(false);
        } else {
            homeAction.setEnabled(true);
            updateNavigationActionsState();
        }

        if (isStandbyPartNeeded)
            // we have a standby part, nothing more to do in presentation.
            return;


        if (standby) {
            // we are in standby. Show standby page, in PageForm.
            String standbyPageId = getModel().getCurrentPageId();
            AbstractIntroPage standbyPage = getModel().getStandbyPage();
            if (standbyPage != null)
                standbyPageId = standbyPage.getId();
            pageForm.showPage(standbyPageId);
            mainPageBook.showPage(PageForm.PAGE_FORM_ID);
        } else {
            // if we are showing a regular intro page, or if the Home Page
            // has a regular page layout, set the page id to the static PageForm
            // id.
            AbstractIntroPage pageToShow = getModel().getCurrentPage();
            String pageId = pageToShow.getId();
            if (!mainPageBook.hasPage(pageId))
                pageId = PageForm.PAGE_FORM_ID;
            // show it in page form first.
            pageForm.showPage(pageToShow.getId());
            // now show the main page book.
            mainPageBook.showPage(pageId);
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
        if (getModel().isDynamic()) {
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
                    success = getModel().setCurrentPageId(getCurrentLocation());
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

        if (getModel().isDynamic()) {
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
                    success = getModel().setCurrentPageId(getCurrentLocation());
                    currentIntroPart.getControl().setRedraw(true);
                }
            }
        }
        updateNavigationActionsState();
        return success;
    }

    public boolean navigateHome() {
        IntroHomePage rootPage = getModel().getHomePage();
        if (getModel().isDynamic()) {
            CustomizableIntroPart currentIntroPart = (CustomizableIntroPart) IntroPlugin
                    .getIntro();
            currentIntroPart.getControl().setRedraw(false);
            boolean success = false;
            success = getModel().setCurrentPageId(rootPage.getId());
            updateHistory(rootPage.getId());
            currentIntroPart.getControl().setRedraw(true);
            return success;
        } else
            return false;

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#handleRegistryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    protected void handleRegistryChanged(IRegistryChangeEvent event) {
        if (getModel().isDynamic()) {
            IntroPlugin.closeIntro();
            IntroPlugin.showIntro(false);
        }
    }



    // *********** Static case ******************
    /*
     * create static UI forms Intro. For this, we only launch the url of the
     * root page.
     */
    private void staticCreatePartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
                HyperlinkGroup.UNDERLINE_HOVER);

        // create a page that has only one link. The URL and tooltip will be set
        // by the standby listener.
        welcomeLink = createStaticPage(parent);
    }


    private Hyperlink createStaticPage(Composite parent) {
        Form mainForm = toolkit.createForm(parent);
        Composite body = mainForm.getBody();

        GridLayout gl = new GridLayout();
        body.setLayout(gl);
        String label = IntroPlugin.getString("StaticHTML.welcome");
        Hyperlink link = toolkit.createHyperlink(body, label, SWT.WRAP);
        link.setFont(PageStyleManager.getHeaderFont());
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        gd.horizontalAlignment = GridData.CENTER;
        gd.verticalAlignment = GridData.CENTER;
        link.setLayoutData(gd);
        link.addHyperlinkListener(new HyperlinkAdapter() {

            public void linkActivated(HyperlinkEvent e) {
                Hyperlink link = (Hyperlink) e.getSource();
                Util.openBrowser((String) link.getHref());
                return;
            }
        });

        return link;
    }

    public void staticStandbyStateChanged(boolean standby) {
        IntroHomePage homePage = getModel().getHomePage();
        IntroHomePage standbyPage = getModel().getStandbyPage();
        if (standbyPage == null)
            standbyPage = homePage;

        if (standby) {
            welcomeLink.setHref(standbyPage.getUrl());
            welcomeLink.setToolTipText(standbyPage.getUrl());
        } else {
            welcomeLink.setHref(homePage.getUrl());
            welcomeLink.setToolTipText(homePage.getUrl());
        }
    }
}