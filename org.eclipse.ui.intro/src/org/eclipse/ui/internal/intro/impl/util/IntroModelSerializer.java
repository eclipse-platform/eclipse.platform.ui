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
package org.eclipse.ui.internal.intro.impl.util;

import java.util.*;

import org.eclipse.ui.internal.intro.impl.model.*;
import org.osgi.framework.*;

/**
 * Print the model to a string buffer (only) for debugging.
 */
public class IntroModelSerializer {

    private StringBuffer buffer;

    public IntroModelSerializer(IntroModelRoot root) {
        this.buffer = new StringBuffer();
        printModelRootInfo(root, buffer);

        // Root Page
        IntroHomePage rootPage = root.getHomePage();
        printHomePage(rootPage, buffer);
        printPageChildren(rootPage, buffer);

        IntroPage[] pages = root.getPages();
        printPages(pages, buffer);

        buffer.append("\n\n"); //$NON-NLS-1$
        printModelFlagTests(root, buffer);
    }

    private void printModelRootInfo(IntroModelRoot model, StringBuffer text) {
        text.append("\nIntro Model Content:"); //$NON-NLS-1$
        text.append("\n======================"); //$NON-NLS-1$
        text.append("\n\nModel has valid config = " + model.hasValidConfig()); //$NON-NLS-1$
        text.append("\nPresentation Title = " //$NON-NLS-1$
                + model.getPresentation().getTitle());
        text.append("\nPresentation Shared Style = " //$NON-NLS-1$
                + model.getPresentation().getImplementationStyle());
        text.append("\nPresentation type = " + model.getPresentation().getType()); //$NON-NLS-1$
        text.append("\nHome page id = " //$NON-NLS-1$
                + model.getPresentation().getHomePageId());
        IntroHead headContent = model.getPresentation().getHead();
        if (headContent != null)
            text.append("\nPresentation Shared Head = " + headContent.getSrc()); //$NON-NLS-1$
        text.append("\nNumber of pages (not including Root Page) = " //$NON-NLS-1$
                + model.getPages().length);
        text.append("\nNumber of shared divs = " //$NON-NLS-1$
                + model.getChildrenOfType(AbstractIntroElement.DIV).length);
        text
                .append("\nNumber of unresolved extensions = " //$NON-NLS-1$
                        + model
                                .getChildrenOfType(AbstractIntroElement.CONTAINER_EXTENSION).length);
    }

    /**
     * @param text
     * @param root
     */
    private void printHomePage(IntroHomePage rootPage, StringBuffer text) {
        text.append("\n\nHOME PAGE: "); //$NON-NLS-1$
        text.append("\n--------------"); //$NON-NLS-1$
        text.append("\n\tis dynamic= " + rootPage.isDynamic()); //$NON-NLS-1$

        text.append("\n\tid = " + rootPage.getId()); //$NON-NLS-1$
        text.append("\n\ttitle = " + rootPage.getTitle()); //$NON-NLS-1$
        text.append("\n\tstyle = " + rootPage.getStyle()); //$NON-NLS-1$
        text.append("\n\talt-style = " + rootPage.getAltStyle()); //$NON-NLS-1$
        text.append("\n\tstandby style = " + rootPage.getStandbyStyle()); //$NON-NLS-1$
        text.append("\n\tstandby alt-style = " + rootPage.getStandbyAltStyle()); //$NON-NLS-1$
        text.append("\n\ttext = " + rootPage.getText()); //$NON-NLS-1$
        text.append("\n\turl = " + rootPage.getUrl()); //$NON-NLS-1$
        text.append("\n\tstandby-url = " + rootPage.getStandbyUrl()); //$NON-NLS-1$
        text.append("\n\tclass-id = " + rootPage.getClassId()); //$NON-NLS-1$
        printPageStyles(rootPage, text);
    }

    private void printPageStyles(AbstractIntroPage page, StringBuffer text) {
        text.append("\n\tpage styles are = "); //$NON-NLS-1$
        String[] styles = page.getStyles();
        for (int i = 0; i < styles.length; i++)
            text.append(styles[i] + "\n\t\t\t"); //$NON-NLS-1$
        text.append("\n\tpage alt-styles are = "); //$NON-NLS-1$

        Hashtable altStylesHashtable = page.getAltStyles();
        Enumeration altStyles = altStylesHashtable.keys();
        while (altStyles.hasMoreElements()) {
            String altStyle = (String) altStyles.nextElement();

            Bundle bundle = (Bundle) altStylesHashtable.get(altStyle);
            text.append(altStyle + " from " + bundle.getSymbolicName()); //$NON-NLS-1$
            text.append("\n\t\t"); //$NON-NLS-1$
        }
    }

    private void printPageChildren(AbstractIntroPage page, StringBuffer text) {

        text.append("\n\tpage children = " + page.getChildren().length); //$NON-NLS-1$
        text.append("\n"); //$NON-NLS-1$
        printContainerChildren(page, text, "\n\t\t"); //$NON-NLS-1$

    }

    private void printContainerChildren(AbstractIntroContainer container,
            StringBuffer text, String indent) {

        AbstractIntroElement[] children = container.getChildren();
        for (int i = 0; i < children.length; i++) {
            int childType = children[i].getType();
            switch (childType) {
            case AbstractIntroElement.ELEMENT:
                text.append("SHOULD NEVER BE HERE"); //$NON-NLS-1$
                break;
            case AbstractIntroElement.DIV:
                printDiv(text, (IntroDiv) children[i], indent);
                break;
            case AbstractIntroElement.LINK:
                printLink(text, (IntroLink) children[i], indent);
                break;
            case AbstractIntroElement.TEXT:
                printText(text, (IntroText) children[i], indent);
                break;
            case AbstractIntroElement.IMAGE:
                printImage(text, (IntroImage) children[i], indent);
                break;
            case AbstractIntroElement.HTML:
                printHtml(text, (IntroHTML) children[i], indent);
                break;
            case AbstractIntroElement.INCLUDE:
                printInclude(text, (IntroInclude) children[i], indent);
                break;
            case AbstractIntroElement.HEAD:
                printHead(text, (IntroHead) children[i], indent);
                break;
            case AbstractIntroElement.PAGE_TITLE:
                printPageTitle(text, (IntroPageTitle) children[i], indent);
                break;

            }

        }
    }

    private void printDiv(StringBuffer text, IntroDiv div, String indent) {
        text.append(indent + "DIV: id = " + div.getId()); //$NON-NLS-1$
        indent = indent + "\t\t"; //$NON-NLS-1$
        text.append(indent + "label = " + div.getLabel()); //$NON-NLS-1$
        text.append(indent + "text = " + div.getText()); //$NON-NLS-1$
        text.append(indent + "children = " + div.getChildren().length); //$NON-NLS-1$
        text.append(indent + "class-id = " + div.getClassId()); //$NON-NLS-1$
        printContainerChildren(div, text, indent + "\t\t"); //$NON-NLS-1$
    }

    private void printLink(StringBuffer text, IntroLink link, String indent) {
        text.append(indent + "DEFAULT_LINK: id = " + link.getId()); //$NON-NLS-1$
        indent = indent + "\t\t"; //$NON-NLS-1$
        text.append(indent + "label = " + link.getLabel()); //$NON-NLS-1$
        text.append(indent + "text = " + link.getText()); //$NON-NLS-1$
        text.append(indent + "class-id = " + link.getClassId()); //$NON-NLS-1$
    }

    private void printText(StringBuffer text, IntroText introText, String indent) {
        text.append(indent + "TEXT: id = " + introText.getId()); //$NON-NLS-1$
        indent = indent + "\t\t"; //$NON-NLS-1$
        text.append(indent + "text = " + introText.getText()); //$NON-NLS-1$
        text.append(indent + "class-id = " + introText.getClassId()); //$NON-NLS-1$
    }

    private void printImage(StringBuffer text, IntroImage image, String indent) {
        text.append(indent + "IMAGE: id = " + image.getId()); //$NON-NLS-1$
        indent = indent + "\t\t"; //$NON-NLS-1$
        text.append(indent + "src = " + image.getSrc()); //$NON-NLS-1$
        text.append(indent + "alt = " + image.getAlt()); //$NON-NLS-1$
        text.append(indent + "class-id = " + image.getClassId()); //$NON-NLS-1$
    }

    private void printHtml(StringBuffer text, IntroHTML html, String indent) {
        text.append(indent + "HTML: id = " + html.getId()); //$NON-NLS-1$
        indent = indent + "\t\t"; //$NON-NLS-1$
        text.append(indent + "src = " + html.getSrc()); //$NON-NLS-1$
        text.append(indent + "isInlined = " + html.isInlined()); //$NON-NLS-1$
        text.append(indent + "class-id = " + html.getClassId()); //$NON-NLS-1$
        if (html.getIntroImage() != null)
            printImage(text, html.getIntroImage(), indent + "\t\t"); //$NON-NLS-1$
        if (html.getIntroText() != null)
            printText(text, html.getIntroText(), indent + "\t\t"); //$NON-NLS-1$

    }

    private void printInclude(StringBuffer text, IntroInclude include,
            String indent) {
        text.append(indent + "INCLUDE: configId = " + include.getConfigId()); //$NON-NLS-1$
        indent = indent + "\t\t"; //$NON-NLS-1$
        text.append(indent + "path = " + include.getPath()); //$NON-NLS-1$
        text.append(indent + "merge-style = " + include.getMergeStyle()); //$NON-NLS-1$
    }

    private void printHead(StringBuffer text, IntroHead head, String indent) {
        text.append(indent + "HEAD: src = " + head.getSrc()); //$NON-NLS-1$
    }

    private void printPageTitle(StringBuffer text, IntroPageTitle title,
            String indent) {
        text.append(indent + "TITLE: id = " + title.getId()); //$NON-NLS-1$
        indent = indent + "\t\t"; //$NON-NLS-1$
        text.append(indent + "title = " + title.getTitle()); //$NON-NLS-1$
        text.append(indent + "class-id = " + title.getClassId()); //$NON-NLS-1$
    }

    /**
     * Appends a given page's categories to the Text buffer.
     * 
     * @param text
     */
    private void printPages(IntroPage[] pages, StringBuffer text) {
        for (int i = 0; i < pages.length; i++) {
            text.append("\n\nPAGE id = " + pages[i].getId()); //$NON-NLS-1$
            text.append("\n----------"); //$NON-NLS-1$
            text.append("\n\ttitle = " + pages[i].getTitle()); //$NON-NLS-1$
            text.append("\n\tstyle = " + pages[i].getStyle()); //$NON-NLS-1$
            text.append("\n\talt-style = " + pages[i].getAltStyle()); //$NON-NLS-1$
            text.append("\n\ttext = " + pages[i].getText()); //$NON-NLS-1$
            text.append("\n\tclass-id = " + pages[i].getClassId()); //$NON-NLS-1$
            printPageStyles(pages[i], text);
            printPageChildren(pages[i], text);
        }
    }

    private void printModelFlagTests(IntroModelRoot model, StringBuffer text) {
        text.append("Model Flag Tests: "); //$NON-NLS-1$
        text.append("\n----------------"); //$NON-NLS-1$
        if (model.getPages().length == 0) {
            text.append("\nNo first page in model\n\n"); //$NON-NLS-1$
            return;
        }
        IntroPage firstPage = model.getPages()[0];
        text.append("\n\t\tFirst page children are: "); //$NON-NLS-1$
        text.append("\n\t\t\tDivs: " //$NON-NLS-1$
                + firstPage.getChildrenOfType(AbstractIntroElement.DIV).length);
        text
                .append("\n\t\t\tLinks: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.LINK).length);
        text
                .append("\n\t\t\tTexts: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.TEXT).length);
        text
                .append("\n\t\t\tHTMLs: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.HTML).length);
        text
                .append("\n\t\t\tImages: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.IMAGE).length);
        text
                .append("\n\t\t\tIncludes: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.INCLUDE).length);
        text
                .append("\n\t\t\tPage Titles: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.PAGE_TITLE).length);
        text
                .append("\n\t\t\tPage Heads: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.HEAD).length);
        text
                .append("\n\t\t\tModel Elements: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.ELEMENT).length);
        text
                .append("\n\t\t\tContainers: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.ABSTRACT_CONTAINER).length);
        text
                .append("\n\t\t\tAll Pages: " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.ABSTRACT_PAGE).length);
        text
                .append("\n\t\t\tElements with Text child(AbstractTextElemets): " //$NON-NLS-1$
                        + firstPage
                                .getChildrenOfType(AbstractIntroElement.ABSTRACT_TEXT).length);

        AbstractIntroElement[] linksAndDivs = (AbstractIntroElement[]) firstPage
                .getChildrenOfType(AbstractIntroElement.DIV
                        | AbstractIntroElement.LINK);
        text.append("\n\t\t\tDivs and Links: " + linksAndDivs.length); //$NON-NLS-1$
    }

    /**
     * @return Returns the textUI.
     */
    public String toString() {
        return buffer.toString();
    }
}