/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro.internal.presentations;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.intro.internal.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.parts.*;
import org.eclipse.ui.intro.internal.util.*;

/**
 * This is a UI Forms based implementation of an Intro Part Presentation.
 */
public class FormIntroPartImplementation extends AbstractIntroPart implements
        IPropertyListener {

    private FormToolkit toolkit = null;

    //private Form mainForm = null;
    private ScrolledPageBook mainPageBook = null;

    // cache model instance for reuse.
    private IntroModelRoot model = getModelRoot();

    private FormStyleManager styleManager;

    static {
        // REVISIT: register all common images here. Even if this part implementation is
        // created again, the images will remain in plugin registry.
        ImageUtil.registerImage(ImageUtil.ROOT_LINK, "overview_96.gif");
        ImageUtil.registerImage(ImageUtil.ROOT_LINK_SMALL, "overview_64.gif");
        ImageUtil.registerImage(ImageUtil.FORM_BG, "form_banner.gif");
        ImageUtil.registerImage(ImageUtil.LINK, "bpel_16.gif");
    }

    private Action homeAction = new Action() {

        {
            setToolTipText(IntroPlugin
                    .getResourceString("Browser_home_tooltip"));
            setImageDescriptor(ImageUtil.createImageDescriptor("home_nav.gif"));
        }

        public void run() {
            IntroHomePage rootPage = getModelRoot().getHomePage();
            if (rootPage.isDynamic())
                    getModelRoot().setCurrentPageId(rootPage.getId());
        }
    };

    public FormIntroPartImplementation() {
        styleManager = new FormStyleManager(getModelRoot());

    }

    public void createPartControl(Composite container) {
        toolkit = new FormToolkit(container.getDisplay());
        Color bg = styleManager.getColor(toolkit, "bg");
        if (bg != null) {
            toolkit.setBackground(bg);
        }
        toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
                HyperlinkGroup.UNDERLINE_ROLLOVER);
        Form mainForm = toolkit.createForm(container);
        Color fg = styleManager.getColor(toolkit, "title.fg");
        if (fg != null) mainForm.setForeground(fg);
        IPluginDescriptor pd = getModelRoot().getPresentation()
                .getConfigurationElement().getDeclaringExtension()
                .getDeclaringPluginDescriptor();
        Image bgImage = styleManager.getImage(pd, "title.image", null, null);
        mainForm.setBackgroundImage(bgImage);
        String repeat = styleManager.getProperty("title.image.repeat");
        if (repeat != null && repeat.toLowerCase().equals("true"))
                mainForm.setBackgroundImageTiled(true);
        mainForm.setText(getModelRoot().getPresentation().getTitle());
        mainPageBook = createMainPageBook(toolkit, mainForm);
        // Add this presentation as a listener to model.
        getModelRoot().addPropertyListener(this);

        addToolBarActions();
    }

    private ScrolledPageBook createMainPageBook(FormToolkit toolkit, Form form) {

        // get body and create page book in it. Body has GridLayout.
        Composite body = form.getBody();
        //Util.printLayout("Initial Form body layout", body.getLayout());
        body.setLayout(new GridLayout());

        // make sure page book expands h and v.
        ScrolledPageBook pageBook = toolkit.createPageBook(body, SWT.V_SCROLL
                | SWT.H_SCROLL);
        pageBook.setLayoutData(new GridData(GridData.FILL_BOTH));
        // creating root page form.
        if (!pageBook.hasPage(model.getHomePage().getId())) {
            // if we do not have a root page form. create one.
            RootPageForm rootPageForm = new RootPageForm(toolkit, model);
            rootPageForm.createPartControl(pageBook, styleManager);
            pageBook.showPage(model.getHomePage().getId());
        }

        // creating static page form.
        if (!pageBook.hasPage(PageForm.PAGE_FORM_ID)) {
            // if we do not have this page create one in main page book and
            // show it.
            PageForm pageForm = new PageForm(toolkit, model);
            pageForm.createPartControl(pageBook, styleManager);
        }

        return pageBook;
    }

    public void dispose() {
        toolkit.dispose();
    }

    /**
     * Handle model property changes. The UI is notified here of a change to
     * the current page in the model. This happens if an intro URL showPage
     * method is executed.
     * 
     * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
     *      int)
     */
    public void propertyChanged(Object source, int propId) {
        if (propId == IntroModelRoot.CURRENT_PAGE_PROPERTY_ID) {
            String pageId = getModelRoot().getCurrentPageId();
            if (pageId == null | pageId.equals(""))
            // If page ID was not set properly. exit.
                    return;

            String rootPageId = getModelRoot().getHomePage().getId();

            // if we are showing a regular intro page, set the page id to the
            // static PageForm id.
            if (!pageId.equals(rootPageId)) pageId = PageForm.PAGE_FORM_ID;

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
        super.addToolBarActions();
    }

    public void setFocus() {
        mainPageBook.getCurrentPage().setFocus();
    }

}
