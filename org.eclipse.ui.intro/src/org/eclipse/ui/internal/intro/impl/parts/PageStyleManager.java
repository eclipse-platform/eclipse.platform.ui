/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.parts;

import java.util.*;

import org.eclipse.ui.internal.intro.impl.model.*;
import org.osgi.framework.*;

public class PageStyleManager extends SharedStyleManager {

    private Hashtable altStyleProperties = new Hashtable();
    private IntroModelRoot root;


    /**
     * Constructor used when page styles need to be loaded. The plugin's bundle
     * is retrieved from the page model class. The default properties are
     * assumed to be the presentation shared properties. The inherrited
     * properties are properties that we got from included and extension styles.
     * 
     * @param modelRoot
     */
    public PageStyleManager(AbstractIntroPage page, Properties sharedProperties) {
        this.page = page;
        bundle = page.getBundle();
        pageProperties = new Properties(sharedProperties);
        String altStyle = page.getAltStyle();
        if (altStyle != null)
                load(pageProperties, altStyle);

        // AltStyles Hashtable has alt-styles as keys, the bundles as
        // values.
        Hashtable altStyles = page.getAltStyles();
        Enumeration styles = altStyles.keys();
        while (styles.hasMoreElements()) {
            String style = (String) styles.nextElement();
            Properties inheritedProperties = new Properties();
            Bundle bundle = (Bundle) altStyles.get(style);
            load(inheritedProperties, style);
            altStyleProperties.put(inheritedProperties, bundle);
        }

        // cache root
        root = (IntroModelRoot) page.getParentPage().getParent();
    }

    // Override parent method to include alt styles.
    public String getProperty(String key) {
        Properties aProperties = findProperty(key);
        return aProperties.getProperty(key);
    }


    /**
     * Finds a Properties that represents an inherited shared style, or this
     * current pages style.
     * 
     * @param key
     * @return
     */
    private Properties findProperty(String key) {
        // search inherited properties first.
        Enumeration inheritedPageProperties = altStyleProperties.keys();
        while (inheritedPageProperties.hasMoreElements()) {
            Properties aProperties = (Properties) inheritedPageProperties
                    .nextElement();
            if (aProperties.containsKey(key))
                    return aProperties;
        }
        // search the page and shared properties last.
        return pageProperties;
    }

    /**
     * Finds the bundle from which as shared style was loaded.
     * 
     * @param key
     * @return
     */
    private Bundle getAltStyleBundle(String key) {
        Properties aProperties = findProperty(key);
        return (Bundle) altStyleProperties.get(aProperties);
    }

    /**
     * Finds the bundle from which this key was loaded. If the key is not from
     * an inherited alt style, then use the bundle corresponding to this page.
     * 
     * @param key
     * @return
     */
    public Bundle getAssociatedBundle(String key) {
        Properties aProperties = findProperty(key);
        Bundle bundle = (Bundle) altStyleProperties.get(aProperties);
        if (bundle != null)
            return bundle;
        else
            return super.getAssociatedBundle(key);
    }



    public int getPageNumberOfColumns() {
        String key = page.getId() + ".layout.ncolumns"; //$NON-NLS-1$
        int ncolumns = 0;
        String value = getProperty(key);
        try {
            ncolumns = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return ncolumns;
    }


    public int getNumberOfColumns(IntroDiv group) {
        String key = createGroupKey(group) + ".layout.ncolumns";
        return getIntProperty(key);
    }

    private int getIntProperty(String key) {
        int ncolumns = 0;
        String value = getProperty(key);
        try {
            ncolumns = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return ncolumns;
    }

    public int getVerticalLinkSpacing() {
        String key = page.getId() + ".layout.link-vspacing"; //$NON-NLS-1$
        int vspacing = 5;
        String value = getProperty(key);
        try {
            vspacing = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return vspacing;
    }



    /**
     * Finds the description text of the given group. Looks for the Text child
     * element whos id is specified as follows:
     * <p>
     * <pageId>. <path_to_group>.description-id= <id of child description Text
     * element>
     * </p>
     * If not found, use the default description style.
     * 
     * @param group
     * @return
     */
    public String getDescription(IntroDiv group) {
        String key = createGroupKey(group) + ".description-id";
        return doGetDescription(group, key);
    }

    private String createGroupKey(IntroDiv group) {
        StringBuffer buffer = new StringBuffer(group.getId());
        AbstractBaseIntroElement parent = (AbstractBaseIntroElement) group
                .getParent();
        while (parent != null
                && !parent.isOfType(AbstractIntroElement.MODEL_ROOT)) {
            buffer.insert(0, parent.getId() + ".");
            parent = (AbstractBaseIntroElement) parent.getParent();
        }
        return buffer.toString();
    }


    /**
     * Finds the description text of the associated page. Looks for the Text
     * child element whos id is specified as follows:
     * <p>
     * <pageId>.description-id= <id of child description Text element>
     * </p>
     * If not found, use the default description style.
     * 
     * @param group
     * @return
     */
    public String getPageDescription() {
        String key = page.getId() + ".description-id";
        return doGetDescription(page, key);
    }

    private String doGetDescription(AbstractIntroContainer parent, String key) {
        String path = getProperty(key);
        String description = null;
        if (path != null)
                description = findTextFromPath(parent, path);
        if (description != null)
                return description;
        return findTextFromStyleId(parent, getDescriptionStyleId());
    }

    private String getDescriptionStyleId() {
        String key = "description-style-id";
        return getProperty(key);
    }

    /**
     * Finds the subtitle of the associated page. Looks for the Text child
     * element whose id is specified as follows:
     * <p>
     * <pageId>.description-id= <id of child description Text element>
     * </p>
     * If not found, use the default description style.
     * 
     * @param group
     * @return
     */
    public String getPageSubTitle() {
        String key = page.getId() + ".subtitle-id";
        String path = getProperty(key);
        String description = null;
        if (path != null)
                description = findTextFromPath(page, path);
        if (description != null)
                return description;
        return findTextFromStyleId(page, getPageSubTitleStyleId());
    }

    private String getPageSubTitleStyleId() {
        String key = "subtitle-style-id";
        return getProperty(key);
    }

    private String findTextFromPath(AbstractIntroContainer parent, String path) {
        AbstractIntroElement child = parent.findTarget(root, path);
        if (child != null && child.isOfType(AbstractIntroElement.TEXT)) {
            makeFiltered(child);
            return ((IntroText) child).getText();
        }
        return null;
    }

    /**
     * Returns the first direct child text element with the given style-id.
     * 
     * @return
     */
    private String findTextFromStyleId(AbstractIntroContainer parent,
            String styleId) {
        IntroText[] allText = (IntroText[]) parent
                .getChildrenOfType(AbstractIntroElement.TEXT);
        for (int i = 0; i < allText.length; i++) {
            if (allText[i].getClassId() == null)
                    // not all elements have style id.
                    continue;
            if (allText[i].getClassId().equals(styleId)) {
                makeFiltered(allText[i]);
                return allText[i].getText();
            }
        }
        return null;
    }

    /**
     * Util method to check model type, and filter model element out if it is of
     * the correct type.
     * 
     * @param element
     */
    private AbstractIntroElement makeFiltered(AbstractIntroElement element) {
        if (element.isOfType(AbstractIntroElement.BASE_ELEMENT))
                ((AbstractBaseIntroElement) element).setFilterState(true);
        return element;
    }



    public boolean getShowLinkDescription() {
        String key = page.getId() + ".show-link-description"; //$NON-NLS-1$
        String value = getProperty(key);
        if (value == null)
                value = "true"; //$NON-NLS-1$
        return value.toLowerCase().equals("true"); //$NON-NLS-1$
    }



    //********* remove later ***********

    private String findTextFromIdXX(AbstractIntroContainer parent,
            String childId) {
        AbstractIntroElement child = parent.findChild(childId);
        if (child != null && child.isOfType(AbstractIntroElement.TEXT))
                return ((IntroText) child).getText();
        return null;
    }

    /**
     * Returns the first child text element with the given style-id. This search
     * is deep and it will look past first level children.
     * 
     * @return
     */
    private String findTextFromStyleIdXX(AbstractIntroContainer parent,
            String styleId) {
        String text = doFindTextWithClassIdXXX(parent, styleId);
        if (text != null)
                return text;
        AbstractIntroContainer[] containers = (AbstractIntroContainer[]) parent
                .getChildrenOfType(AbstractIntroElement.ABSTRACT_CONTAINER);
        for (int i = 0; i < containers.length; i++) {
            text = findTextFromStyleId(containers[i], styleId);
            if (text != null)
                    return text;
        }
        return null;
    }

    /**
     * Returns the first direct child text element with the given style-id.
     * 
     * @return
     */
    private String doFindTextWithClassIdXXX(AbstractIntroContainer container,
            String styleId) {
        IntroText[] allText = (IntroText[]) container
                .getChildrenOfType(AbstractIntroElement.TEXT);
        for (int i = 0; i < allText.length; i++) {
            if (allText[i].getClassId().equals(styleId))
                    return allText[i].getText();
        }
        return null;
    }



}

