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
package org.eclipse.ui.intro.internal.model;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.w3c.dom.*;

/**
 * Base class for all Intro pages., inlcuding HomePage.
 */
public abstract class AbstractIntroPage extends AbstractIntroContainer {

    protected static final String TAG_PAGE = "page";

    private static final String ATT_TITLE = "title";
    private static final String ATT_STYLE = "style";
    private static final String ATT_ALT_STYLE = "alt-style";

    private String title;
    private String style;
    private String altStyle;

    /**
     * The vectors to hold all inhertied styles and alt styles from included
     * elements. They are lazily created when children are resolved (ie:
     * includes are resolved) OR when extensions are resolved and styles need to
     * be added to the target page.
     * <p>
     * Style Rules:
     * <ul>
     * <li>For includes, merge-style controls wether or not the enclosing page
     * inherits the styles of the target.
     * <li>If a page is including a shared div, merging target styles into this
     * page is ignored. Shared divs do not have styles.</li>
     * <li>For extensions, if the style or alt-style is not defined, that means
     * that no style inheritence is needed, and the style of the target page are
     * not updated.
     * <li>If an extension is extending a shared div, merging the styles of
     * this extension into the target is ignored. Shared divs do not have
     * styles.</li>
     * <li>Shared hastable has alt-styles as keys, the plugin descriptors as
     * values.</li>
     * </ul>
     */
    private Vector styles;

    private Hashtable altStyles;

    /**
     * @param element
     */
    AbstractIntroPage(Element element, IPluginDescriptor pd) {
        super(element, pd);
        title = getAttribute(element, ATT_TITLE);
        style = getAttribute(element, ATT_STYLE);
        altStyle = getAttribute(element, ATT_ALT_STYLE);

        // Resolve.
        style = IntroModelRoot.getPluginLocation(style, pd);
        altStyle = IntroModelRoot.getPluginLocation(altStyle, pd);
    }



    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return Returns the style.
     */
    public String getStyle() {
        return style;
    }

    /**
     * @return Returns the alt_style.
     */
    public String getAltStyle() {
        return altStyle;
    }

    /**
     * Gets all the inherited styles of this page. Styles can be inherited from
     * includes or from configExtensions.
     * <p>
     * Note: this call needs to get all the children of this page, and so it
     * will resolve this page. might be expensive.
     * 
     * @return Returns all the inherited styles of this page. Returns an empty
     *         array if page is not expandable or does not have inherited
     *         styles.
     */
    public String[] getStyles() {
        // call get children first to resolve includes and populate styles
        // vector. Resolving children will initialize the style vectors.
        getChildren();
        String[] stylesArray = new String[styles.size()];
        styles.copyInto(stylesArray);
        return stylesArray;
    }

    /**
     * Gets all the inherited alt-styles of this page (ie: styles for the SWT
     * presentation). A hashtable is returned that has inhertied alt-styles as
     * keys, and plugin descriptors as values. This is needed to be able to load
     * resources from the inherited target plugin. Note: this call needs to get
     * all the children of this page, and so its will resolve this page. might
     * be expensive.
     * 
     * @return Returns all the inherited styles of this page. Returns an empty
     *         hashtable if page is not expandable, does not have any includes,
     *         or has includes that do not merge styles.
     */
    public Hashtable getAltStyles() {
        // call get children first to resolve includes and populate hashtable.
        // Resolving children will initialize the style vectors.
        getChildren();
        return altStyles;
    }

    /**
     * Adds the given style to the list. Style is not added if it already exists
     * in the list.
     * 
     * @param style
     */
    protected void addStyle(String style) {
        initStylesVectors();
        if (styles.contains(style))
            return;
        styles.add(style);
    }

    /**
     * Adds the given style to the list.Style is not added if it already exists
     * in the list.
     * 
     * @param altStyle
     */
    protected void addAltStyle(String altStyle, IPluginDescriptor pd) {
        initStylesVectors();
        if (altStyles.containsKey(altStyle))
            return;
        altStyles.put(altStyle, pd);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.ABSTRACT_PAGE;
    }

    /*
     * Override parent behavior to lazily initialize styles vectors. This will
     * only be called once, if resolved == false.
     * 
     * @see org.eclipse.ui.intro.internal.model.AbstractIntroContainer#resolveChildren()
     */
    protected void resolveChildren() {
        initStylesVectors();
        super.resolveChildren();
    }

    private void initStylesVectors() {
        if (styles == null)
            // delay creation until needed.
            styles = new Vector();
        if (altStyles == null)
            // delay creation until needed.
            altStyles = new Hashtable();
    }

    /**
     * Override parent behavior to add support for HEAD & Title element in pages
     * only, and not in divs.
     * 
     * @see org.eclipse.ui.intro.internal.model.AbstractIntroContainer#getModelChild(org.eclipse.core.runtime.IConfigurationElement)
     */
    protected AbstractIntroElement getModelChild(Element childElement,
            IPluginDescriptor pd) {
        AbstractIntroElement child = null;
        if (childElement.getNodeName().equalsIgnoreCase(IntroHead.TAG_HEAD)) {
            child = new IntroHead(childElement, pd);
        } else if (childElement.getNodeName().equalsIgnoreCase(
                IntroPageTitle.TAG_TITLE)) {
            child = new IntroPageTitle(childElement, pd);
        }
        if (child != null)
            return child;

        return super.getModelChild(childElement, pd);
    }

    /**
     * Returns all head contributions in this page. There can be more than one
     * head contribution in the page;
     * 
     * @return
     */
    public IntroHead[] getHTMLHeads() {
        return (IntroHead[]) getChildrenOfType(AbstractIntroElement.HEAD);
    }

    // THESE METHODS MIGHT BE REMOVED. ADDED HERE FOR BACKWARD COMPATIBILITY.
    public IntroLink[] getLinks() {
        return (IntroLink[]) getChildrenOfType(AbstractIntroElement.LINK);
    }

    /**
     * Returns the divs in this page. HTML presentation divs and Navigation
     * include divs are filtered out, for now.
     */
    public IntroDiv[] getDivs() {
        // get real page divs.
        IntroDiv[] divs = (IntroDiv[]) getChildrenOfType(AbstractIntroElement.DIV);

        // filter bad stuff for now.
        Vector vectorDivs = new Vector(Arrays.asList(divs));
        for (int i = 0; i < vectorDivs.size(); i++) {
            IntroDiv aDiv = (IntroDiv) vectorDivs.elementAt(i);
            if (aDiv.getId().equals("navigation-links")
                    || aDiv.getId().equals("background-image")
                    || aDiv.getId().equals("root-background")) {
                vectorDivs.remove(aDiv);
                i--;
            }
        }

        // return proper object type.
        IntroDiv[] filteredDivs = new IntroDiv[vectorDivs.size()];
        vectorDivs.copyInto(filteredDivs);
        return filteredDivs;
    }

    /**
     * Returns the first child with the given class-id.
     * 
     * @return
     */
    public String getText() {
        String text = doGetText(this);
        if (text != null)
            return text;

        AbstractIntroContainer[] containers = (AbstractIntroContainer[]) getChildrenOfType(AbstractIntroElement.ABSTRACT_CONTAINER);
        for (int i = 0; i < containers.length; i++) {
            text = doGetText(containers[i]);
            if (text != null)
                return text;
        }
        return null;
    }

    /**
     * Returns the first child with the given class-id.
     * 
     * @return
     */
    private String doGetText(AbstractIntroContainer container) {
        IntroText[] allText = (IntroText[]) container
                .getChildrenOfType(AbstractIntroElement.TEXT);
        for (int i = 0; i < allText.length; i++) {
            if (allText[i].getClassId().equals("page-description"))
                return allText[i].getText();
        }
        return null;
    }


}