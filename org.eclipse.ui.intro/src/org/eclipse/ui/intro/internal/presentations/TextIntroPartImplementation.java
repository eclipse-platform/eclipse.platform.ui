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

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.intro.internal.*;
import org.eclipse.ui.intro.internal.model.*;

/**
 * This is an Text based implementation of an Intro Part. It simply walks the
 * model and prints the content of pages. It is used for debugging.
 */
public class TextIntroPartImplementation extends AbstractIntroPart {

    public void createPartControl(Composite container) {
        Text textUI = new Text(container, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL);
        StringBuffer text = new StringBuffer();
        IntroModelRoot model = IntroPlugin.getDefault().getIntroModelRoot();
        printModelRootInfo(model, text);

        // Root Page
        IntroHomePage rootPage = model.getHomePage();
        printHomePage(rootPage, text);
        printPageChildren(rootPage, text);

        IntroPage[] pages = model.getPages();
        printPages(pages, text);

        text.append("\n\n");
        printModelFlagTests(model, text);

        textUI.setText(text.toString());

        addToolBarActions();
    }

    private void printModelRootInfo(IntroModelRoot model, StringBuffer text) {
        text.append("\nIntro Model Content:");
        text.append("\n======================");
        text.append("\n\nModel has valid config = " + model.hasValidConfig());
        text.append("\nPresentation Title = "
                + model.getPresentation().getTitle());
        text.append("\nPresentation Shared Style = "
                + model.getPresentation().getStyle());
        text.append("\nPresentation id = " + model.getPresentation().getId());
        text.append("\nHome page id = "
                + model.getPresentation().getHomePageId());
        IntroHead headContent = model.getPresentation().getHead();
        if (headContent != null)
            text.append("\nPresentation Shared Head = " + headContent.getSrc());
        text.append("\nNumber of pages (not including Root Page) = "
                + model.getPages().length);
        text.append("\nNumber of shared divs = "
                + model.getChildrenOfType(IntroElement.DIV).length);
        text
                .append("\nNumber of unresolved extensions = "
                        + model
                                .getChildrenOfType(IntroElement.CONTAINER_EXTENSION).length);
    }

    /**
     * @param text
     * @param root
     */
    private void printHomePage(IntroHomePage rootPage, StringBuffer text) {
        text.append("\n\nHOME PAGE: ");
        text.append("\n--------------");
        text.append("\n\tis dynamic= " + rootPage.isDynamic());

        text.append("\n\tid = " + rootPage.getId());
        text.append("\n\ttitle = " + rootPage.getTitle());
        text.append("\n\tstyle = " + rootPage.getStyle());
        text.append("\n\talt-style = " + rootPage.getAltStyle());
        text.append("\n\tstandby style = " + rootPage.getStandbyStyle());
        text.append("\n\tstandby alt-style = " + rootPage.getStandbyAltStyle());
        text.append("\n\ttext = " + rootPage.getText());
        text.append("\n\turl = " + rootPage.getUrl());
        text.append("\n\tstandby-url = " + rootPage.getStandbyUrl());
        printPageStyles(rootPage, text);
    }

    private void printPageStyles(AbstractIntroPage page, StringBuffer text) {
        text.append("\n\tpage styles are = ");
        String[] styles = page.getStyles();
        for (int i = 0; i < styles.length; i++)
            text.append(styles[i] + "\n\t\t\t");
        text.append("\n\tpage alt-styles are = ");

        Hashtable altStylesHashtable = page.getAltStyles();
        Enumeration altStyles = altStylesHashtable.keys();
        while (altStyles.hasMoreElements()) {
            String altStyle = (String) altStyles.nextElement();

            IPluginDescriptor pd = (IPluginDescriptor) altStylesHashtable
                    .get(altStyle);
            text.append(altStyle + " from " + pd.getUniqueIdentifier());
            text.append("\n\t\t");
        }
    }

    private void printPageChildren(AbstractIntroPage page, StringBuffer text) {

        text.append("\n\tpage children = " + page.getChildren().length);
        text.append("\n");
        printContainerChildren(page, text, "\n\t\t");

    }

    private void printContainerChildren(AbstractIntroContainer container,
            StringBuffer text, String indent) {

        IntroElement[] children = container.getChildren();
        for (int i = 0; i < children.length; i++) {
            int childType = children[i].getType();
            switch (childType) {
            case IntroElement.BASE_ELEMENT:
                text.append("SHOULD NEVER BE HERE");
                break;
            case IntroElement.DIV:
                printDiv(text, (IntroDiv) children[i], indent);
                break;
            case IntroElement.LINK:
                printLink(text, (IntroLink) children[i], indent);
                break;
            case IntroElement.TEXT:
                printText(text, (IntroText) children[i], indent);
                break;
            case IntroElement.IMAGE:
                printImage(text, (IntroImage) children[i], indent);
                break;
            case IntroElement.HTML:
                printHtml(text, (IntroHTML) children[i], indent);
                break;
            case IntroElement.INCLUDE:
                printInclude(text, (IntroInclude) children[i], indent);
                break;
            case IntroElement.HEAD:
                printHead(text, (IntroHead) children[i], indent);
                break;

            }

        }
    }

    private void printDiv(StringBuffer text, IntroDiv div, String indent) {
        text.append(indent + "DIV: id = " + div.getId());
        indent = indent + "\t\t";
        text.append(indent + "label = " + div.getLabel());
        text.append(indent + "text = " + div.getText());
        text.append(indent + "children = " + div.getChildren().length);
        printContainerChildren(div, text, indent + "\t\t");
    }

    private void printLink(StringBuffer text, IntroLink link, String indent) {
        text.append(indent + "LINK: id = " + link.getId());
        indent = indent + "\t\t";
        text.append(indent + "label = " + link.getLabel());
        text.append(indent + "text = " + link.getText());
    }

    private void printText(StringBuffer text, IntroText introText, String indent) {
        text.append(indent + "TEXT: id = " + introText.getId());
        indent = indent + "\t\t";
        text.append(indent + "text = " + introText.getText());
    }

    private void printImage(StringBuffer text, IntroImage image, String indent) {
        text.append(indent + "IMAGE: id = " + image.getId());
        indent = indent + "\t\t";
        text.append(indent + "src = " + image.getSrc());
        text.append(indent + "alt = " + image.getAlt());
    }

    private void printHtml(StringBuffer text, IntroHTML html, String indent) {
        text.append(indent + "HTML: id = " + html.getId());
        indent = indent + "\t\t";
        text.append(indent + "src = " + html.getSrc());
        text.append(indent + "isInlined = " + html.isInlined());
        if (html.getIntroImage() != null)
            printImage(text, html.getIntroImage(), indent + "\t\t");
        if (html.getIntroText() != null)
            printText(text, html.getIntroText(), indent + "\t\t");

    }

    private void printInclude(StringBuffer text, IntroInclude include,
            String indent) {
        text.append(indent + "INCLUDE: id = " + include.getId());
        indent = indent + "\t\t";
        text.append(indent + "configId = " + include.getConfigId());
        text.append(indent + "path = " + include.getPath());
        text.append(indent + "merge-style = " + include.getMergeStyle());
    }

    private void printHead(StringBuffer text, IntroHead head, String indent) {
        text.append(indent + "HEAD: id = " + head.getId());
        indent = indent + "\t\t";
        text.append(indent + "src = " + head.getSrc());
    }

    /**
     * Appends a given page's categories to the Text buffer.
     * 
     * @param text
     */
    private void printPages(IntroPage[] pages, StringBuffer text) {
        for (int i = 0; i < pages.length; i++) {
            text.append("\n\nPAGE id = " + pages[i].getId());
            text.append("\n----------");
            text.append("\n\ttitle = " + pages[i].getTitle());
            text.append("\n\tstyle = " + pages[i].getStyle());
            text.append("\n\talt-style = " + pages[i].getAltStyle());
            text.append("\n\ttext = " + pages[i].getText());
            printPageStyles(pages[i], text);
            printPageChildren(pages[i], text);
        }
    }

    private void printModelFlagTests(IntroModelRoot model, StringBuffer text) {
        text.append("Model Flag Tests: ");
        text.append("\n----------------");
        if (model.getPages().length == 0) {
            text.append("\nNo first page in model\n\n");
            return;
        }
        IntroPage firstPage = model.getPages()[0];
        text.append("\n\t\tFirst page children are: ");
        text.append("\n\t\t\tDivs: "
                + firstPage.getChildrenOfType(IntroElement.DIV).length);
        text.append("\n\t\t\tLinks: "
                + firstPage.getChildrenOfType(IntroElement.LINK).length);
        text.append("\n\t\t\tTexts: "
                + firstPage.getChildrenOfType(IntroElement.TEXT).length);
        text.append("\n\t\t\tHTMLs: "
                + firstPage.getChildrenOfType(IntroElement.HTML).length);
        text.append("\n\t\t\tImages: "
                + firstPage.getChildrenOfType(IntroElement.IMAGE).length);
        text.append("\n\t\t\tIncludes: "
                + firstPage.getChildrenOfType(IntroElement.INCLUDE).length);
        text
                .append("\n\t\t\tModel Elements: "
                        + firstPage
                                .getChildrenOfType(IntroElement.BASE_ELEMENT).length);
        text
                .append("\n\t\t\tContainers: "
                        + firstPage
                                .getChildrenOfType(IntroElement.ABSTRACT_CONTAINER).length);
        text
                .append("\n\t\t\tAll Pages: "
                        + firstPage
                                .getChildrenOfType(IntroElement.ABSTRACT_PAGE).length);
        text
                .append("\n\t\t\tElements with Text child(AbstractTextElemets): "
                        + firstPage
                                .getChildrenOfType(IntroElement.ABSTRACT_TEXT).length);

        IntroElement[] linksAndDivs = (IntroElement[]) firstPage
                .getChildrenOfType(IntroElement.DIV | IntroElement.LINK);
        text.append("\n\t\t\tDivs and Links: " + linksAndDivs.length);
    }

}
