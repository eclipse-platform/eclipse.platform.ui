/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.parts;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;

/**
 * A Composite that represents the Root Page. It is swapped in the main page
 * book in the FormIntroPartImplementation class.
 */
public class RootPageForm implements IIntroConstants {

    private FormToolkit toolkit;
    private IntroHomePage rootPage;
    private Form parentForm;
    private Label descriptionLabel;

    class PageComposite extends Composite {

        public PageComposite(Composite parent, int style) {
            super(parent, style);
        }

        // Do not allow composite to take wHint as-is - layout manager
        // can reject the hint and compute larger width.
        public Point computeSize(int wHint, int hHint, boolean changed) {
            return ((RootPageLayout) getLayout()).computeSize(this, wHint,
                    hHint, changed);
        }
    }

    class RootPageLayout extends Layout implements ILayoutExtension {

        // gap between link composite and description label.
        private int VERTICAL_SPACING = 20;

        private int LABEL_MARGIN_WIDTH = 5;

        /*
         * Custom layout for Root Page Composite.
         */
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            int innerWHint = wHint;
            if (wHint != SWT.DEFAULT)
                    innerWHint -= LABEL_MARGIN_WIDTH + LABEL_MARGIN_WIDTH;
            Control[] children = composite.getChildren();
            Point s1 = children[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
            Point s2 = children[1].computeSize(innerWHint, SWT.DEFAULT);
            s2.x += LABEL_MARGIN_WIDTH;
            int height = 2 * (s2.y + VERTICAL_SPACING + s1.y / 2);
            Point size = new Point(Math.max(s1.x, s2.x), height + 5);
            return size;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
         *      boolean)
         */
        protected void layout(Composite composite, boolean flushCache) {
            Control[] children = composite.getChildren();
            Rectangle carea = composite.getClientArea();
            Control links = children[0];
            Control label = children[1];
            Point linksSize = links.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            Point labelSize = label.computeSize(carea.width - 2
                    - LABEL_MARGIN_WIDTH * 2, SWT.DEFAULT);
            links.setBounds(carea.width / 2 - linksSize.x / 2, carea.height / 2
                    - linksSize.y / 2, linksSize.x, linksSize.y);
            label.setBounds(LABEL_MARGIN_WIDTH, links.getLocation().y
                    + linksSize.y + VERTICAL_SPACING, carea.width
                    - LABEL_MARGIN_WIDTH - LABEL_MARGIN_WIDTH, labelSize.y);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.forms.widgets.ILayoutExtension#computeMaximumWidth(org.eclipse.swt.widgets.Composite,
         *      boolean)
         */
        public int computeMaximumWidth(Composite parent, boolean changed) {
            return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.forms.widgets.ILayoutExtension#computeMinimumWidth(org.eclipse.swt.widgets.Composite,
         *      boolean)
         */
        public int computeMinimumWidth(Composite parent, boolean changed) {
            return computeSize(parent, 0, SWT.DEFAULT, changed).x;
        }
    }

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
                Util.openBrowser(introLink.getUrl());
                return;
            }
            DialogUtil.displayInfoMessage(imageLink.getShell(), IntroPlugin
                    .getString("HyperlinkAdapter.urlIs") //$NON-NLS-1$
                    + introLink.getUrl());
        }

        public void linkEntered(HyperlinkEvent e) {
            ImageHyperlink imageLink = (ImageHyperlink) e.getSource();
            IntroLink introLink = (IntroLink) imageLink.getData(INTRO_LINK);
            updateDescription(introLink.getText());
        }

        public void linkExited(HyperlinkEvent e) {
            // empty text on exit.
            updateDescription(""); //$NON-NLS-1$
        }

        private void updateDescription(String text) {
            descriptionLabel.setText(text);
            descriptionLabel.getParent().layout();
        }
    };

    /**
     *  
     */
    public RootPageForm(FormToolkit toolkit, IntroModelRoot modelRoot,
            Form parentForm) {
        this.toolkit = toolkit;
        this.rootPage = modelRoot.getHomePage();
        this.parentForm = parentForm;
    }

    /**
     * Create the form for the root page. Number of columns there is equal to
     * the number of links.
     * 
     * @param pageBook
     */
    public void createPartControl(ScrolledPageBook mainPageBook,
            SharedStyleManager shardStyleManager) {
        // first, create the root page style manager from shared style manager.
        PageStyleManager rootPageStyleManager = new PageStyleManager(rootPage,
                shardStyleManager.getProperties());

        // Set title of Main form from root page title.
        parentForm.setText(rootPage.getTitle());

        // Composite for full root page. It has custom layout, and two
        // children: the links composite and the description label.
        Composite pageComposite = new PageComposite(
                mainPageBook.getContainer(), SWT.NULL);
        toolkit.adapt(pageComposite);
        mainPageBook.registerPage(rootPage.getId(), pageComposite);
        pageComposite.setLayout(new RootPageLayout());
        // Util.highlight(pageComposite, SWT.COLOR_DARK_CYAN);

        // create the links composite in the center of the root page.
        createRootPageLinks(rootPageStyleManager, pageComposite);

        // create description label for links description. Instance var for
        // reuse.
        descriptionLabel = createHoverLabel(rootPageStyleManager, pageComposite);

        // Clear memory. No need for style manager any more.
        rootPageStyleManager = null;
    }

    /**
     * Creates links in the root page assuming that root page only has links. If
     * not use for non-empty div links.
     */
    private void createRootPageLinks(PageStyleManager rootPageStyleManager,
            Composite pageComposite) {

        Composite linkComposite = toolkit.createComposite(pageComposite);

        // DONOW: If the root page does not have only links, take the links of
        // the first non-filtered div for now.
        int numberOfLinks = rootPage.getLinks().length;
        if (numberOfLinks > 0) {
            // assume root page only has links, for now. populate the link
            // composite. Number of columns there is equal to the number of
            // links.
            doCreateRootPageLinks(rootPageStyleManager, linkComposite, rootPage
                    .getLinks());
            return;
        }
    }

    /**
     * Creates the given links in the root page with the root page style.
     */
    private void doCreateRootPageLinks(PageStyleManager rootPageStyleManager,
            Composite linkComposite, IntroLink[] links) {

        int numberOfLinks = links.length;
        GridLayout layout = new GridLayout();
        // separate links a bit more.
        layout.horizontalSpacing = 20;
        layout.numColumns = numberOfLinks;
        linkComposite.setLayout(layout);
        // Util.highlight(linkComposite, SWT.COLOR_CYAN);
        // add image hyperlinks for all links.
        for (int i = 0; i < numberOfLinks; i++)
            createImageHyperlink(linkComposite, links[i], rootPageStyleManager);
        // add labels for all links, after adding all links.
        for (int i = 0; i < numberOfLinks; i++)
            createLinkLabel(linkComposite, links[i]);

    }

    /**
     * Creates an Image Hyperlink from an IntroLink. Model object is cached in
     * link.
     * 
     * @param body
     * @param link
     */
    private void createImageHyperlink(Composite body, IntroLink link,
            PageStyleManager styleManager) {
        ImageHyperlink imageLink = toolkit.createImageHyperlink(body, SWT.NULL);
        imageLink.setImage(styleManager.getImage(link, "link-icon", //$NON-NLS-1$
                ImageUtil.DEFAULT_ROOT_LINK));
        imageLink
                .setHoverImage(styleManager.getImage(link, "hover-icon", null)); //$NON-NLS-1$
        // each link is centered in cell.
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        imageLink.setLayoutData(gd);
        // cache the intro link model object for description and URL.
        imageLink.setData(INTRO_LINK, link);
        imageLink.addHyperlinkListener(hyperlinkAdapter);
    }

    private void createLinkLabel(Composite body, IntroLink link) {
        Label linkLabel = toolkit.createLabel(body, link.getLabel());
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        linkLabel.setFont(DEFAULT_FONT);
        linkLabel.setLayoutData(gd);
    }

    /**
     * Creates a label to display the link description when you hover over a
     * hyperlink.
     * 
     * @param body
     */
    private Label createHoverLabel(PageStyleManager styleManager, Composite body) {
        Label label = toolkit.createLabel(body, "", SWT.WRAP); //$NON-NLS-1$
        Color fg = styleManager.getColor(toolkit, "hover-text.fg"); //$NON-NLS-1$
        if (fg == null)
                fg = toolkit.getColors().getColor(FormColors.TITLE);
        label.setForeground(fg);
        label.setAlignment(SWT.CENTER);
        label.setFont(DEFAULT_FONT);
        return label;
    }
}