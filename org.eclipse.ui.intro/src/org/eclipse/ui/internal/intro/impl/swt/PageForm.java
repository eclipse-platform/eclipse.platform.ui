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
package org.eclipse.ui.internal.intro.impl.swt;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;

/**
 * A Form that represents an Intro Page. It is swapped in the main page book in
 * the FormIntroPartImplementation class. It has the navigation toolbar UI and
 * it has a page book for swapping in categories of Intro Pages.
 */
public class PageForm implements IIntroConstants, IPropertyListener {

    private FormToolkit toolkit;
    private ScrolledPageBook categoryPageBook;
    private SharedStyleManager sharedStyleManager;
    private PageStyleManager rootPageStyleManager;
    private IntroModelRoot model;
    private Form parentForm;
    private Form pageForm;

    // Id to this page. There is only a single instance of this page in the
    // main page book.
    public static String PAGE_FORM_ID = "pageFormId"; //$NON-NLS-1$

    private HyperlinkAdapter hyperlinkAdapter = new HyperlinkAdapter() {

        public void linkActivated(HyperlinkEvent e) {
            String url = (String) e.getHref();
            IntroURLParser parser = new IntroURLParser(url);
            if (parser.hasIntroUrl()) {
                // execute the action embedded in the IntroURL
                parser.getIntroURL().execute();
                return;
            } else if (parser.hasProtocol()) {
                Util.openBrowser(url);
                return;
            }
            DialogUtil.displayInfoMessage(((Control) e.getSource()).getShell(),
                    IntroPlugin.getString("HyperlinkAdapter.urlIs") //$NON-NLS-1$
                            + " " + url); //$NON-NLS-1$
        }

        public void linkEntered(HyperlinkEvent e) {
        }

        public void linkExited(HyperlinkEvent e) {
        }
    };

    /**
     *  
     */
    public PageForm(FormToolkit toolkit, IntroModelRoot modelRoot,
            Form parentForm) {
        this.toolkit = toolkit;
        this.model = modelRoot;
        this.parentForm = parentForm;
        // add this PageForm as a listener to model changes to switch the page
        // content of pageBook.
        model.addPropertyListener(this);
    }

    /**
     * Create the form for the root page. Number of columns there is equal to
     * the number of links. Every image link does not cache a model object for
     * data retrieval.
     * 
     * @param pageBook
     */
    public void createPartControl(ScrolledPageBook mainPageBook,
            SharedStyleManager sharedStyleManager) {

        // Create a style manager from shared style manager. We only need it
        // for the UI navigation composite.
        rootPageStyleManager = new PageStyleManager(model.getHomePage(),
                sharedStyleManager.getProperties());

        // Cash the shared style manager. We need to pass it around to category
        // forms. So, do not null it!
        this.sharedStyleManager = sharedStyleManager;

        // creating page in Main page book.
        pageForm = toolkit.createForm(mainPageBook.getContainer());
        mainPageBook.registerPage(PAGE_FORM_ID, pageForm);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        pageForm.getBody().setLayout(layout);
        //Util.highlight(pageForm.getBody(), SWT.COLOR_RED);

        // Get form body. Form body is one column grid layout. Add page book
        // and navigation UI to it.
        categoryPageBook = toolkit.createPageBook(pageForm.getBody(),
                SWT.H_SCROLL | SWT.V_SCROLL);
        categoryPageBook.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create Navigation bar if needed.
        if (sharedStyleManager.showHomePageNavigation()) {
            Composite navigationComposite = toolkit.createComposite(pageForm
                    .getBody());
            navigationComposite.setLayoutData(new GridData(
                    GridData.HORIZONTAL_ALIGN_CENTER));
            int numberOfLinks = model.getHomePage().getLinks().length;
            layout = new GridLayout();
            layout.numColumns = numberOfLinks;
            navigationComposite.setLayout(layout);
            createSmallNavigator(navigationComposite, model.getHomePage()
                    .getLinks());
        }

        pageForm.setText(rootPageStyleManager.getPageSubTitle());

        // Clear memory. No need for root style manager any more.
        rootPageStyleManager = null;

    }

    private void createSmallNavigator(Composite parent, IntroLink[] links) {
        for (int i = 0; i < links.length; i++) {
            Control c = createImageHyperlink(parent, links[i]);
            c.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        }
        for (int i = 0; i < links.length; i++) {
            Label text = toolkit.createLabel(parent, links[i].getLabel());
            text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
            text.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
        }
    }

    /**
     * Creates an Image Hyperlink from an IntroLink. Model object is NOT cached.
     * 
     * @param body
     * @param link
     */
    private Control createImageHyperlink(Composite body, IntroLink link) {
        ImageHyperlink imageLink = toolkit.createImageHyperlink(body, SWT.NULL);

        // set link image.
        Image image = rootPageStyleManager.getImage(link, "small-link-icon", //$NON-NLS-1$
                ImageUtil.DEFAULT_SMALL_ROOT_LINK);
        imageLink.setImage(image);

        // set link hover image.
        image = rootPageStyleManager.getImage(link, "small-hover-icon", null); //$NON-NLS-1$
        imageLink.setHoverImage(image);
        imageLink.setToolTipText(link.getLabel());
        // each link is centered in cell.
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        imageLink.setLayoutData(gd);
        imageLink.setHref(link.getUrl());
        imageLink.addHyperlinkListener(hyperlinkAdapter);
        return imageLink;
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
            // update page book with correct PageContentForm composite.
            String pageId = model.getCurrentPageId();
            showPage(pageId);
        }
    }

    /**
     * This method is called when the current page changes. It creates the
     * PageContentForm if necessary, and handles showing the page in the Page
     * Book. It creates a model PageContentForm for the current page.
     * 
     * @param pageID
     */
    public void showPage(String pageID) {
        if (!categoryPageBook.hasPage(pageID)) {
            // if we do not have a category form for this page create one.
            PageContentForm categoryForm = new PageContentForm(toolkit, model,
                    pageID);
            categoryForm
                    .createPartControl(categoryPageBook, sharedStyleManager);
        }
        categoryPageBook.showPage(pageID);

        // Get cached page subtitle from control data.
        Composite page = (Composite) categoryPageBook.getCurrentPage();
        // update main Form title.
        parentForm.setText(model.getCurrentPage().getTitle());
        // update this page form's title, ie: Page subtitle, if it exists.
        pageForm.setText((String) page.getData(PAGE_SUBTITLE));

        //TODO need to transfer focus to the first link in
        // the page somehow; we may need IIntroPage interface with
        // a few methods like 'setFocus()' etc.
        // DG
    }
}