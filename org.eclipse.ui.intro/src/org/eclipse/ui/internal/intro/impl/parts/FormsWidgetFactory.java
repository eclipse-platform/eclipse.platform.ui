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



    private FormToolkit toolkit;
    private PageStyleManager styleManager;


    /*
     * protect bad creation.
     */
    protected FormsWidgetFactory(FormToolkit toolkit,
            PageStyleManager styleManager) {
        this.toolkit = toolkit;
        this.styleManager = styleManager;
    }

    /**
     * Check the filter state of the element. Only base elements have the filter
     * attribute.
     * 
     * @param element
     * @return
     */
    private boolean getFilterState(AbstractIntroElement element) {
        if (element.isOfType(AbstractIntroElement.BASE_ELEMENT))
            return ((AbstractBaseIntroElement) element).isFiltered();
        else
            return false;
    }



    public void createIntroElement(Composite parent,
            AbstractIntroElement element) {
        // check if this element is filtered, and if yes, do not create it.
        boolean isFiltered = getFilterState(element);
        if (isFiltered)
                return;

        switch (element.getType()) {
        case AbstractIntroElement.DIV:
            IntroDiv group = (IntroDiv) element;
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
            IntroText text = (IntroText) element;
            c = createText(parent, text);
            updateLayoutData(c);
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
     * Creates an Image Hyperlink from an IntroLink. Model object is NOT cached.
     * 
     * @param body
     * @param link
     */
    private Control createImageHyperlink(Composite parent, IntroLink link) {
        Control control;
        Hyperlink linkControl;
        boolean showLinkDescription = styleManager.getShowLinkDescription();
        Image linkImage = styleManager.getImage(link, "link-icon", //$NON-NLS-1$
                ImageUtil.DEFAULT_LINK);
        if (showLinkDescription && link.getText() != null) {
            Composite container = toolkit.createComposite(parent);
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

            Control desc = createText(container, link.getIntroText());
            td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
            td.grabHorizontal = true;
            td.grabVertical = true;
            desc.setLayoutData(td);
            control = container;
        } else {
            ImageHyperlink imageLink = toolkit.createImageHyperlink(parent,
                    SWT.WRAP | SWT.CENTER);
            imageLink.setImage(linkImage);
            TableWrapData td = new TableWrapData(TableWrapData.FILL,
                    TableWrapData.CENTER);
            td.grabHorizontal = true;
            imageLink.setLayoutData(td);
            linkControl = imageLink;
            control = linkControl;
        }
        linkControl.setText(link.getLabel());
        linkControl.setFont(IIntroConstants.DEFAULT_FONT);
        linkControl.addHyperlinkListener(hyperlinkAdapter);
        //Util.highlight(linkControl, SWT.COLOR_DARK_YELLOW);
        return control;
    }

    /**
     * Creates a forms Text or FormattedText.
     * 
     * @param body
     * @param link
     */
    private Control createText(Composite parent, IntroText text) {
        if (text.isFormatted()) {

            FormText formText = toolkit.createFormText(parent, true);
            formText.addHyperlinkListener(hyperlinkAdapter);
            formText.setLayoutData(new TableWrapData(TableWrapData.FILL,
                    TableWrapData.FILL));
            formText.setText(generateFormText(text.getText()), true, true);
            return formText;
        } else {
            Label label = toolkit.createLabel(parent, text.getText(), SWT.WRAP);
            return label;
        }
    }


    private String generateFormText(String text) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<form>"); //$NON-NLS-1$
        sbuf.append(text);
        sbuf.append("</form>"); //$NON-NLS-1$
        return sbuf.toString();
    }

}



