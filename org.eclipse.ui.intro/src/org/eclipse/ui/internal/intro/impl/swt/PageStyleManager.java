/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.swt;

import java.util.*;

import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.osgi.framework.*;

public class PageStyleManager extends SharedStyleManager {

    private AbstractIntroPage page;
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
        properties = new Properties(sharedProperties);
        String altStyle = page.getAltStyle();
        if (altStyle != null)
            load(properties, altStyle);

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
        return properties;
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
    protected Bundle getAssociatedBundle(String key) {
        Properties aProperties = findProperty(key);
        Bundle bundle = (Bundle) altStyleProperties.get(aProperties);
        if (bundle != null)
            return bundle;
        else
            return super.getAssociatedBundle(key);
    }



    public int getPageNumberOfColumns() {
        String key = page.getId() + ".layout.ncolumns"; //$NON-NLS-1$
        return getIntProperty(key);
    }


    public int getNumberOfColumns(IntroGroup group) {
        StringBuffer buff = createPathToElementKey(group);
        if (buff == null)
            // must return 0.
            return 0;
        String key = buff.append(".layout.ncolumns").toString(); //$NON-NLS-1$
        return getIntProperty(key);
    }

    public int getColSpan(AbstractBaseIntroElement element) {
        StringBuffer buff = createPathToElementKey(element);
        if (buff == null)
            return 1;
        String key = buff.append(".layout.colspan").toString(); //$NON-NLS-1$
        int colspan = getIntProperty(key);
        if (colspan != 0)
            return colspan;
        else
            return 1;
    }

    public int getRowSpan(AbstractBaseIntroElement element) {
        StringBuffer buff = createPathToElementKey(element);
        if (buff == null)
            return 1;
        String key = buff.append(".layout.rowspan").toString(); //$NON-NLS-1$
        int rowspan = getIntProperty(key);
        if (rowspan != 0)
            return rowspan;
        else
            return 1;
    }

    private int getIntProperty(String key) {
        int intValue = 0;
        String value = getProperty(key);
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return intValue;
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
     * Returns null if no default style found, or any id in path is null.
     * 
     * @param group
     * @return
     */
    public String getDescription(IntroGroup group) {
        StringBuffer buff = createPathToElementKey(group);
        if (buff == null)
            return null;
        String key = buff.append(".description-id").toString(); //$NON-NLS-1$
        return doGetDescription(group, key);
    }


    /**
     * Finds the description text of the associated page. Looks for the Text
     * child element whos id is specified as follows:
     * <p>
     * <pageId>.description-id= <id of child description Text element>
     * </p>
     * If not found, use the default description style.
     * 
     * Returns null if no default style found, or any id in path is null.
     * 
     * @param group
     * @return
     */
    public String getPageDescription() {
        if (page.getId() == null)
            return null;
        String key = page.getId() + ".description-id"; //$NON-NLS-1$
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
        String key = "description-style-id"; //$NON-NLS-1$
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
        String key = page.getId() + ".subtitle-id"; //$NON-NLS-1$
        String path = getProperty(key);
        String description = null;
        if (path != null)
            description = findTextFromPath(page, path);
        if (description != null)
            return description;
        return findTextFromStyleId(page, getPageSubTitleStyleId());
    }

    private String getPageSubTitleStyleId() {
        String key = "subtitle-style-id"; //$NON-NLS-1$
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
            if (allText[i].getStyleId() == null)
                // not all elements have style id.
                continue;
            if (allText[i].getStyleId().equals(styleId)) {
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


    public Color getColor(FormToolkit toolkit, AbstractBaseIntroElement element) {
        StringBuffer buff = createPathToElementKey(element);
        if (buff == null)
            return null;
        String key = buff.append(".font.fg").toString(); //$NON-NLS-1$
        return getColor(toolkit, key);
    }

    public boolean isBold(IntroText text) {
        StringBuffer buff = createPathToElementKey(text);
        if (buff != null) {
            String key = buff.append(".font.bold").toString();
            String value = getProperty(key);
            if (value != null)
                return value.toLowerCase().equals("true");
        } else {
            // bold is not specified by ID. Check to see if there is a style-id
            // specified for bold.
            String value = getProperty("bold-style-id");
            if (value != null && text.getStyleId() != null)
                return text.getStyleId().equals(value);
        }
        return false;
    }

    public static Font getDefaultFont() {
        return JFaceResources.getBannerFont();
    }

    /**
     * Retrieves an image for a link in a page. If not found, uses the page's
     * default link image. If still not found, uses the passed default.
     * 
     * @param link
     * @param qualifier
     * @return
     */
    public Image getImage(IntroLink link, String qualifier, String defaultKey) {
        String key = createImageKey(page, link, qualifier);
        String pageKey = createImageKey(page, null, qualifier);
        return getImage(key, pageKey, defaultKey);
    }

    private String createImageKey(AbstractIntroPage page, IntroLink link,
            String qualifier) {
        StringBuffer buff = null;
        if (link != null) {
            buff = createPathToElementKey(link);
            if (buff == null)
                return ""; //$NON-NLS-1$
        } else {
            buff = new StringBuffer();
            buff.append(page.getId());
        }
        buff.append("."); //$NON-NLS-1$
        buff.append(qualifier);
        return buff.toString();
    }

    public Image getImage(IntroImage introImage) {
        String imageLocation = introImage.getSrcAsIs();
        String key = createPathToElementKey(introImage).toString();
        if (ImageUtil.hasImage(key))
            return ImageUtil.getImage(key);
        // key not already registered.
        ImageUtil.registerImage(key, bundle, imageLocation);
        Image image = ImageUtil.getImage(key);
        return image;
    }

    /**
     * Creates a key for the given element. Returns null if any id is null along
     * the path.
     * 
     * @param element
     * @return
     */
    private StringBuffer createPathToElementKey(AbstractIntroIdElement element) {
        if (element.getId() == null)
            return null;
        StringBuffer buffer = new StringBuffer(element.getId());
        AbstractBaseIntroElement parent = (AbstractBaseIntroElement) element
                .getParent();
        while (parent != null
                && !parent.isOfType(AbstractIntroElement.MODEL_ROOT)) {
            if (parent.getId() == null)
                return null;
            buffer.insert(0, parent.getId() + "."); //$NON-NLS-1$
            parent = (AbstractBaseIntroElement) parent.getParent();
        }
        return buffer;
    }

}

