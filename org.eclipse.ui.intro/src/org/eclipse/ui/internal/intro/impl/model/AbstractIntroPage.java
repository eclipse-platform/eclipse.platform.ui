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
package org.eclipse.ui.internal.intro.impl.model;

import java.util.*;

import org.eclipse.ui.internal.intro.impl.model.loader.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.osgi.framework.*;
import org.w3c.dom.*;

/**
 * Base class for all Intro pages., inlcuding HomePage.
 */
public abstract class AbstractIntroPage extends AbstractIntroContainer {

    protected static final String TAG_PAGE = "page"; //$NON-NLS-1$
    private static final String ATT_STYLE = "style"; //$NON-NLS-1$
    private static final String ATT_ALT_STYLE = "alt-style"; //$NON-NLS-1$
    private static final String ATT_CONTENT = "content"; //$NON-NLS-1$

    private String style;
    private String altStyle;
    private IntroPageTitle title;
    private String content;

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
     * <li>Shared hashtable has alt-styles as keys and bundles as values.</li>
     * </ul>
     */
    private Vector styles;
    private Hashtable altStyles;

    /**
     * @param element
     */
    AbstractIntroPage(Element element, Bundle bundle) {
        super(element, bundle);
        content = getAttribute(element, ATT_CONTENT);
        if (content == null)
            init(element, bundle);
        else
            // Content is not null. Resolve it. Other attributes will be loaded
            // when xml content file is loaded
            content = IntroModelRoot.getPluginLocation(content, bundle);
    }

    private void init(Element element, Bundle bundle) {
        style = getAttribute(element, ATT_STYLE);
        altStyle = getAttribute(element, ATT_ALT_STYLE);

        // Resolve.
        style = IntroModelRoot.getPluginLocation(style, bundle);
        altStyle = IntroModelRoot.getPluginLocation(altStyle, bundle);

    }


    /**
     * The page's title. Each page can have one title.
     * 
     * @return Returns the title of this page.
     */
    public String getTitle() {
        // title is a child of the page, and so we have to load children first.
        // We also have to resolve children because someone might be including a
        // title. Update title instance after all includes and extensions have
        // been resolved.
        getChildren();
        if (title == null) {
            // there should only be one title child per page. safe to cast.
            IntroPageTitle[] titles = (IntroPageTitle[]) getChildrenOfType(AbstractIntroElement.PAGE_TITLE);
            if (titles.length > 0)
                title = titles[0];
        }

        if (title == null)
            // still null. no title.
            return null;
        else
            return title.getTitle();
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

    public void insertStyle(String style, int location) {
        initStylesVectors();
        if (styles.contains(style))
            return;
        styles.add(location, style);
    }

    /**
     * Adds the given style to the list.Style is not added if it already exists
     * in the list.
     * 
     * @param altStyle
     */
    protected void addAltStyle(String altStyle, Bundle bundle) {
        initStylesVectors();
        if (altStyles.containsKey(altStyle))
            return;
        altStyles.put(altStyle, bundle);
    }


    /**
     * Util method to add given styles to the list.
     *  
     */
    protected void addStyles(String[] styles) {
        for (int i = 0; i < styles.length; i++)
            addStyle(styles[i]);
    }

    /**
     * Util method to add map of altstyles to list.
     */
    protected void addAltStyles(Hashtable altStyles) {
        // 
        this.altStyles.putAll(altStyles);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.ABSTRACT_PAGE;
    }

    /*
     * Override parent behavior to lazily initialize styles vectors. This will
     * only be called once, if resolved == false.
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroContainer#resolveChildren()
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
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroContainer#getModelChild(org.eclipse.core.runtime.IConfigurationElement)
     */
    protected AbstractIntroElement getModelChild(Element childElement,
            Bundle bundle) {
        AbstractIntroElement child = null;
        if (childElement.getNodeName().equalsIgnoreCase(IntroHead.TAG_HEAD)) {
            child = new IntroHead(childElement, bundle);
        } else if (childElement.getNodeName().equalsIgnoreCase(
                IntroPageTitle.TAG_TITLE)) {
            // if we have a title, only add it as a child if we did not load one
            // before. A page can only have one title.
            if (title == null) {
                child = new IntroPageTitle(childElement, bundle);
            }
        }
        if (child != null)
            return child;
        return super.getModelChild(childElement, bundle);
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


    /**
     * load the children of this container. Override parent behavior because we
     * want to support loading content from other xml files. The design is that
     * only the id and content from the existing page are honored. all other
     * attributes are what they are defined in the external page.
     *  
     */
    protected void loadChildren() {
        if (content == null) {
            // no content. do regular loading.
            super.loadChildren();
            return;
        }

        // load the first page with correct id, from content xml file.
        Document dom = new IntroContentParser(content).getDocument();
        if (dom == null)
            // bad xml. Parser would have logged fact.
            return;

        Element[] pages = ModelLoaderUtil.getElementsByTagName(dom,
                IntroPage.TAG_PAGE);
        if (pages.length == 0) {
            Log.warning("Content file has no pages."); //$NON-NLS-1$
            return;
        }
        // point the element of this page to the new element. Pick first page
        // with matching id. Make sure to disable loading of children of current
        // element if a matching page in the external content file is not found.
        boolean foundMatchingPage = false;
        for (int i = 0; i < pages.length; i++) {
            Element pageElement = pages[i];
            if (pageElement.getAttribute(IntroPage.ATT_ID).equals(getId())) {
                this.element = pageElement;
                // call init on the new element. the filtering and the style-id
                // are loaded by the parent class.
                init(pageElement, getBundle());
                // TODO: revisit. Special processing here should be made more
                // general.
                style_id = element
                        .getAttribute(AbstractBaseIntroElement.ATT_STYLE_ID);
                filteredFrom = element
                        .getAttribute(AbstractBaseIntroElement.ATT_FIlTERED_FROM);
                foundMatchingPage = true;
            }
        }
        if (foundMatchingPage)
            // now do children loading as usual.
            super.loadChildren();
        else {
            // page was not found in content file. Perform load actions, and log
            // fact.
            // init the children vector.
            children = new Vector();
            loaded = true;
            // free DOM model for memory performance.
            element = null;
            Log.warning("Content file does not have page with id= " + getId()); //$NON-NLS-1$
        }
    }

    /**
     * Deep copy since class has mutable objects.
     */
    public Object clone() throws CloneNotSupportedException {
        AbstractIntroPage clone = (AbstractIntroPage) super.clone();
        if (title != null) {
            IntroPageTitle clonedTitle = (IntroPageTitle) title.clone();
            clonedTitle.setParent(clone);
            clone.title = clonedTitle;
        }
        // styles are safe for a shallow copy.
        if (styles != null)
            clone.styles = (Vector) styles.clone();
        if (altStyles != null)
            clone.altStyles = (Hashtable) altStyles.clone();
        return clone;
    }

}



