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
package org.eclipse.ui.internal.intro.impl.parts;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;

/**
 * A Form that represents an Intro Page categories are. It is swapped in the
 * categories page book in the PageForm class. It has the navigation toolbar UI
 * and it has a page book for swapping in categories of Intro Pages.
 */
public class CategoryForm implements IIntroConstants {

    private FormToolkit toolkit = null;
    private ScrolledPageBook categoryPageBook = null;
    private IntroModelRoot model = null;
    private ScrolledForm pageForm = null;
    private FormStyleManager styleManager;

    private HyperlinkAdapter hyperlinkAdapter = new HyperlinkAdapter() {

        public void linkActivated(HyperlinkEvent e) {
            Hyperlink imageLink = (Hyperlink) e.getSource();
            IntroLink introLink = (IntroLink) imageLink.getData(INTRO_LINK);
            IntroURLParser parser = new IntroURLParser(introLink.getUrl());
            if (parser.hasIntroUrl()) {
                // execute the action embedded in the IntroURL
                parser.getIntroURL().execute();
                return;
            } else if (parser.hasProtocol()) {
                Util.openBrowser(introLink.getUrl());
                return;
            }
            DialogUtil.displayInfoMessage(imageLink.getShell(), IntroPlugin
                    .getString("HyperlinkAdapter.urlIs") //$NON-NLS-1$
                    + " " + introLink.getUrl()); //$NON-NLS-1$
        }

        public void linkEntered(HyperlinkEvent e) {
        }

        public void linkExited(HyperlinkEvent e) {
        }
    };

    /**
     *  
     */
    public CategoryForm(FormToolkit toolkit, IntroModelRoot modelRoot) {
        this.toolkit = toolkit;
        this.model = modelRoot;
    }

    /**
     * Create the form for the root page. Number of columns there is equal to
     * the number of links. Every image link does not cache a model object for
     * data retrieval..
     * 
     * @param pageBook
     */
    public void createPartControl(ScrolledPageBook categoryPageBook,
            FormStyleManager sharedStyleManager) {
        this.categoryPageBook = categoryPageBook;
        // first, create a page style manager from shared style manager
        // because we need it for the UI navigation composite.
        AbstractIntroPage page = model.getCurrentPage();
        styleManager = new FormStyleManager(page, sharedStyleManager
                .getProperties());
        String pageId = model.getCurrentPageId();

        // categoriesComposite has Table Layout with one col. Holds page
        // description and composite with all otehr children.
        Composite categoriesComposite = categoryPageBook.createPage(pageId);
        //Util.highlight(categoriesComposite, SWT.COLOR_GREEN);
        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 15;
        layout.leftMargin = 15;
        layout.rightMargin = 15;
        layout.bottomMargin = 15;
        layout.verticalSpacing = 15;
        categoriesComposite.setLayout(layout);
        
        if (page.getPageDescription() != null) {
            Label label = toolkit.createLabel(categoriesComposite, page
                    .getPageDescription(), SWT.WRAP);
            label.setFont(DEFAULT_FONT);
            TableWrapData td = new TableWrapData();
            td.align = TableWrapData.FILL;
            label.setLayoutData(td);
        }

        createPageChildren(page, categoriesComposite);

        // Clear memory. No need for style manager any more.
        sharedStyleManager = null;
    }

    private void createPageChildren(AbstractIntroPage page, Composite parent) {
        // setup page composite/layout
        Composite pageComposite = createPageTableComposite(page, parent);
        // now add all children
        AbstractIntroElement[] children = page.getChildren();
        for (int i = 0; i < children.length; i++)
            createIntroElement(children[i], pageComposite);
    }

    private void createIntroElement(AbstractIntroElement element,
            Composite parent) {
        switch (element.getType()) {
        case AbstractIntroElement.DIV:
            // DONOW:
            IntroDiv group = (IntroDiv) element;
            if (AbstractIntroPage.isFilteredDiv(group))
                break;
            Control c = createGroup(group, parent);
            updateLayoutData(c);
            // c must be a composite.
            Composite newParent = (Composite) c;
            if (c instanceof Section)
                // client is a composite also.
                newParent = (Composite) ((Section) newParent).getClient();
            AbstractIntroElement[] children = group.getChildren();
            for (int i = 0; i < children.length; i++)
                createIntroElement(children[i], newParent);
            break;
        case AbstractIntroElement.LINK:
            IntroLink link = (IntroLink) element;
            c = createImageHyperlink(parent, link);
            updateLayoutData(c);
            break;
        case AbstractIntroElement.TEXT:

            break;

        default:
            break;
        }
    }

    private void updateLayoutData(Control c) {
        if (c instanceof Hyperlink)
            return;
        TableWrapData td = new TableWrapData(TableWrapData.FILL,
                TableWrapData.TOP);
        td.grabHorizontal = true;
        c.setLayoutData(td);
    }

    private Composite createGroup(IntroDiv group, Composite parent) {
        String label = group.getLabel();
        String description = group.getText();
        int numColumns = styleManager.getNumberOfColumns(group);
        numColumns = numColumns < 1 ? 1 : numColumns;
        int vspacing = styleManager.getVerticalLinkSpacing();
        boolean showLinkDescription = styleManager.getShowLinkDescription();
        int style = description != null ? Section.DESCRIPTION : SWT.NULL;
        Composite client = null;
        Composite control = null;
        if (description != null || label != null) {
            Section section = toolkit.createSection(parent, style);
            if (label != null)
                section.setText(label);
            if (description != null)
                section.setDescription(description);
            client = toolkit.createComposite(section, SWT.WRAP);
            section.setClient(client);
            control = section;
        } else {
            client = toolkit.createComposite(parent, SWT.WRAP);
            control = client;
        }
        TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = numColumns;
        layout.verticalSpacing = vspacing;
        client.setLayout(layout);
        //Util.highlight(client, SWT.COLOR_YELLOW);
        return control;
    }


    /**
     * Creates an Image Hyperlink from an IntroLink. Model object is cached.
     * 
     * @param body
     * @param link
     */
    private Control createImageHyperlink(Composite body, IntroLink link) {
        Control control;
        Hyperlink linkControl;
        boolean showLinkDescription = styleManager.getShowLinkDescription();
        Image linkImage = styleManager.getImage(link, "link-icon", //$NON-NLS-1$
                ImageUtil.DEFAULT_LINK);
        if (showLinkDescription && link.getText() != null) {
            Composite container = toolkit.createComposite(body);
            TableWrapLayout layout = new TableWrapLayout();
            layout.leftMargin = layout.rightMargin = 0;
            layout.topMargin = layout.bottomMargin = 0;
            layout.verticalSpacing = 0;
            layout.numColumns = 2;
            container.setLayout(layout);
            Label ilabel = toolkit.createLabel(container, null);
            ilabel.setImage(linkImage);
            TableWrapData td = new TableWrapData();
            td.valign = TableWrapData.TOP;
            td.rowspan = 2;
            ilabel.setLayoutData(td);
            linkControl = toolkit.createHyperlink(container, null, SWT.WRAP);
            td = new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM);
            td.grabVertical = true;
            linkControl.setLayoutData(td);
            //Util.highlight(linkControl, SWT.COLOR_RED);
            //Util.highlight(container, SWT.COLOR_DARK_YELLOW);
            Label desc = toolkit.createLabel(container, link.getText(),
                    SWT.WRAP);
            td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
            td.grabHorizontal = true;
            td.grabVertical = true;
            desc.setLayoutData(td);
            control = container;
        } else {
            ImageHyperlink imageLink = toolkit.createImageHyperlink(body,
                    SWT.WRAP | SWT.CENTER);
            imageLink.setImage(linkImage);
            linkControl = imageLink;
            control = linkControl;
        }
        linkControl.setText(link.getLabel());
        linkControl.setFont(DEFAULT_FONT);
        // cache the intro link model object for description and URL.
        linkControl.setData(INTRO_LINK, link);
        linkControl.addHyperlinkListener(hyperlinkAdapter);
        return control;
    }

    /**
     * Creates a composite with TableWrapLayout to hold all page children. If
     * number of columns is zero, the number of child groups is used.
     * 
     * @param page
     * @param parent
     * @return
     */
    private Composite createPageTableComposite(AbstractIntroPage page,
            Composite parent) {
        int childDivCount = page.getChildrenOfType(AbstractIntroElement.DIV).length;
        Composite client = toolkit.createComposite(parent);
        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 0;
        layout.bottomMargin = 0;
        layout.leftMargin = 0;
        layout.rightMargin = 0;
        int numColumns = styleManager.getPageNumberOfColumns();
        layout.numColumns = numColumns == 0 ? childDivCount : numColumns;
        client.setLayout(layout);

        // parent has TableWrapLayout, and so update layout of this child.
        TableWrapData td = new TableWrapData();
        td.align = TableWrapData.FILL;
        td.grabHorizontal = true;
        client.setLayoutData(td);
        return client;
    }


}