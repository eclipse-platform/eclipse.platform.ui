/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
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
 * Factory to create all UI forms widgets for the Forms intro presentation.
 */
public class FormsWidgetFactory {

    private HyperlinkAdapter hyperlinkAdapter = new HyperlinkAdapter() {

        public void linkActivated(HyperlinkEvent e) {
            Hyperlink imageLink = (Hyperlink) e.getSource();
            IntroLink introLink = (IntroLink) imageLink
                    .getData(IIntroConstants.INTRO_LINK);
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

    private FormToolkit toolkit;
    private PageStyleManager styleManager;


    /*
     * protect bad creation.
     */
    protected FormsWidgetFactory(FormToolkit toolkit, PageStyleManager styleManager) {
        this.toolkit = toolkit;
        this.styleManager = styleManager;
    }

    public void createIntroElement(Composite parent,
            AbstractIntroElement element) {
        switch (element.getType()) {
        case AbstractIntroElement.DIV:
            // DONOW:
            IntroDiv group = (IntroDiv) element;
            if (AbstractIntroPage.isFilteredDiv(group))
                    break;
            Control c = createGroup(parent, group);
            updateLayoutData(c);
            // c must be a composite.
            Composite newParent = (Composite) c;
            if (c instanceof Section)
                    // client is a composite also.
                    newParent = (Composite) ((Section) newParent).getClient();
            AbstractIntroElement[] children = group.getChildren();
            for (int i = 0; i < children.length; i++)
                createIntroElement(newParent, children[i]);
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

    private Composite createGroup(Composite parent, IntroDiv group) {
        String label = group.getLabel();
        String description = styleManager.getDescription(group);
        int numColumns = styleManager.getNumberOfColumns(group);
        numColumns = numColumns < 1 ? 1 : numColumns;
        int vspacing = styleManager.getVerticalLinkSpacing();
        Composite client = null;
        Composite control = null;
        if (description != null || label != null) {
            int style = description != null ? Section.DESCRIPTION : SWT.NULL;
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
        linkControl.setFont(IIntroConstants.DEFAULT_FONT);
        // cache the intro link model object for description and URL.
        linkControl.setData(IIntroConstants.INTRO_LINK, link);
        linkControl.addHyperlinkListener(hyperlinkAdapter);
        return control;
    }

}