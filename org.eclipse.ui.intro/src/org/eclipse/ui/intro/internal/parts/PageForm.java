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
package org.eclipse.ui.intro.internal.parts;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.intro.internal.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.util.*;

/**
 * A Composite that represents an Intro Page. It is swapped in the main page
 * book in the FormIntroPartImplementation class. It has the navigation toolbar
 * UI and it has a page book for swapping in categories of Intro Pages.
 */
public class PageForm implements IIntroConstants, IPropertyListener {

    private FormToolkit toolkit = null;

    private ScrolledPageBook mainPageBook = null;

    private ScrolledPageBook categoryPageBook = null;

    private FormStyleManager styleManager;

    private IntroModelRoot model = null;

    private Form formContent;

    // Id to this page. There is only a single instance of this page in the
    // main page book.
    public static String PAGE_FORM_ID = "pageFormId";

    private HyperlinkAdapter hyperlinkAdapter = new HyperlinkAdapter() {

        public void linkActivated(HyperlinkEvent e) {
            ImageHyperlink imageLink = (ImageHyperlink) e.getSource();
            IntroLink introLink = (IntroLink) imageLink.getData(INTRO_LINK);
            IntroURLParser parser = new IntroURLParser(introLink.getUrl());
            if (parser.hasIntroUrl()) {
                // execute the action embedded in the IntroURL
                parser.getIntroURL().execute();
                return;
            } else if (parser.hasProtocol()) {
                Program.launch(introLink.getUrl());
                return;
            }
            DialogUtil.displayInfoMessage(imageLink.getShell(), "URL is: "
                    + introLink.getUrl());
        }

        public void linkEntered(HyperlinkEvent e) {
        }

        public void linkExited(HyperlinkEvent e) {
        }
    };

    /**
     *  
     */
    public PageForm(FormToolkit toolkit, IntroModelRoot modelRoot) {
        this.toolkit = toolkit;
        this.model = modelRoot;
        // add this PageForm as a listener to model changes to update the title
        // of this form.
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
            FormStyleManager sharedStyleManager) {

        // Create a *root* page style manager from shared style manager
        // because we need it for the UI navigation composite. And we need to
        // pass it around to category forms. So, do not null it.
        this.styleManager = new FormStyleManager(model.getHomePage(),
                sharedStyleManager.getProperties());

        this.mainPageBook = mainPageBook;
        // creating page in Main page book.
        formContent = toolkit.createForm(mainPageBook.getContainer());
        mainPageBook.registerPage(PAGE_FORM_ID, formContent);
        GridLayout layout = new GridLayout();
        formContent.getBody().setLayout(layout);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        Util.highlight(formContent.getBody(), SWT.COLOR_RED);

        // Get form body. Form body is one column grid layout. Add page book
        // and navigation UI to it.
        categoryPageBook = toolkit.createPageBook(formContent.getBody(),
                SWT.H_SCROLL | SWT.V_SCROLL);
        categoryPageBook.setLayoutData(new GridData(GridData.FILL_BOTH));

        // adding navigation UI.
        Composite navigationComposite = toolkit.createComposite(formContent
                .getBody());
        navigationComposite.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_CENTER));
        int numberOfLinks = model.getHomePage().getLinks().length;
        layout = new GridLayout();
        layout.numColumns = numberOfLinks;
        navigationComposite.setLayout(layout);
        // add image hyperlinks for all links.
        createSmallNavigator(navigationComposite, model.getHomePage()
                .getLinks());
        formContent.setText(model.getCurrentPage().getTitle());
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
     * Creates an Image Hyperlink from an IntroLink. Model object is cached.
     * 
     * @param body
     * @param link
     */
    private Control createImageHyperlink(Composite body, IntroLink link) {
        ImageHyperlink imageLink = toolkit.createImageHyperlink(body, SWT.NULL);

        Image image = styleManager.getImage("rootPage.small-icon."
                + link.getId(), "rootPage.small-icon",
                ImageUtil.ROOT_LINK_SMALL);
        imageLink.setImage(image);
        imageLink.setToolTipText(link.getLabel());
        // each link is centered in cell.
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        imageLink.setLayoutData(gd);
        // cache the intro link model object for description and URL.
        imageLink.setData(INTRO_LINK, link);
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
            // update Form title.
            String pageId = model.getCurrentPageId();
            formContent.setText(model.getCurrentPage().getTitle());

            // update page book with a Category Form.
            if (!categoryPageBook.hasPage(pageId)) {
                // if we do not have a category form for this page create one.
                CategoryForm categoryForm = new CategoryForm(toolkit, model);
                categoryForm.createPartControl(categoryPageBook, styleManager);
            }
            categoryPageBook.showPage(model.getCurrentPage().getId());
            //TODO need to transfer focus to the first link in
            // the page somehow; we may need IIntroPage interface with
            // a few methods like 'setFocus()' etc.
            // DG
        }
    }
}