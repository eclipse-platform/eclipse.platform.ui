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
package org.eclipse.ui.intro.internal.parts;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.util.*;

public class FormStyleManager {

    private Properties pageProperties;
    private Hashtable altStyleProperties = new Hashtable();
    private AbstractIntroPage page;
    private IPluginDescriptor pd;

    /**
     * Constructor used when shared styles need to be loaded. The plugin
     * descriptor is retrieved from the model root.
     * 
     * @param modelRoot
     */
    public FormStyleManager(IntroModelRoot modelRoot) {
        pd = modelRoot.getPluginDesc();
        pageProperties = new Properties();
        String sharedStyle = modelRoot.getPresentation().getStyle();
        if (sharedStyle != null)
            load(pageProperties, sharedStyle);
    }

    /**
     * Constructor used when a page styles need to be loaded. The plugin
     * descriptor is retrieved from the page model class. The default properties
     * are assumed to be the presentation shared properties.
     * 
     * @param modelRoot
     */
    public FormStyleManager(AbstractIntroPage page, Properties sharedProperties) {
        this.page = page;
        pd = page.getPluginDesc();
        pageProperties = new Properties(sharedProperties);
        String altStyle = page.getAltStyle();
        if (altStyle != null)
            load(pageProperties, altStyle);

        // AltStyles hastable has alt-styles as keys, the plugin descriptors as
        // values.
        Hashtable altStyles = page.getAltStyles();
        Enumeration styles = altStyles.keys();
        while (styles.hasMoreElements()) {
            String style = (String) styles.nextElement();
            Properties inheritedProperties = new Properties();
            IPluginDescriptor pd = (IPluginDescriptor) altStyles.get(style);
            load(inheritedProperties, style);
            altStyleProperties.put(inheritedProperties, pd);
        }
    }

    private void load(Properties properties, String style) {
        if (style == null)
            return;
        try {
            URL styleURL = new URL(style);
            InputStream is = styleURL.openStream();
            properties.load(is);
            is.close();
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }


    public String getProperty(String key) {
        Properties aProperties = findProperty(key);
        return aProperties.getProperty(key);
    }

    /**
     * Finds the plugin descriptor from which as shared styles was loaded.
     * 
     * @param key
     * @return
     */
    private IPluginDescriptor getAltStylePd(String key) {
        Properties aProperties = findProperty(key);
        return (IPluginDescriptor) altStyleProperties.get(aProperties);
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

    private String createImageKey(AbstractIntroPage page, IntroLink link,
            String qualifier) {
        StringBuffer buff = new StringBuffer();
        if (page instanceof IntroHomePage)
            buff.append("rootPage.");
        else {
            buff.append("page.");
            buff.append(page.getId());
            buff.append(".");
        }
        buff.append(qualifier);
        if (link != null) {
            buff.append(".");
            buff.append(link.getId());
        }
        return buff.toString();
    }

    private RGB getRGB(String key) {
        String value = getProperty(key);
        if (value == null)
            return null;
        if (value.charAt(0) == '#') {
            // HEX
            try {
                int r = Integer.parseInt(value.substring(1, 3), 16);
                int g = Integer.parseInt(value.substring(3, 5), 16);
                int b = Integer.parseInt(value.substring(5, 7), 16);
                return new RGB(r, g, b);
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    /**
     * May return null.
     * 
     * @param toolkit
     * @param key
     * @return
     */
    public Color getColor(FormToolkit toolkit, String key) {
        FormColors colors = toolkit.getColors();
        Color color = colors.getColor(key);
        if (color == null) {
            RGB rgb = getRGB(key);
            if (rgb != null)
                color = colors.createColor(key, rgb);
        }
        return color;
    }

    public Image getImage(IntroLink link, String qualifier) {
        String key = createImageKey(page, link, qualifier);
        String pageKey = createImageKey(page, null, qualifier);
        String defaultKey = (page instanceof IntroHomePage) ? ImageUtil.ROOT_LINK
                : ImageUtil.LINK;
        return getImage(key, pageKey, defaultKey);
    }

    public Image getImage(String key, String defaultPageKey, String defaultKey) {
        String currentKey = key;
        String value = getProperty(currentKey);
        if (value == null && defaultPageKey != null) {
            currentKey = defaultPageKey;
            value = getProperty(defaultPageKey);
        }
        if (value != null) {
            if (ImageUtil.hasImage(currentKey))
                return ImageUtil.getImage(currentKey);
            // try to register the image.
            IPluginDescriptor pd = getAltStylePd(currentKey);
            if (pd == null)
                // it means that we are getting a key defined in this page's
                // styles. (ie: not an inherited style).
                pd = this.pd;
            ImageUtil.registerImage(currentKey, pd, value);
            Image image = ImageUtil.getImage(currentKey);
            if (image != null)
                return image;
        }
        // try default
        if (defaultKey != null)
            return ImageUtil.getImage(defaultKey);
        return null;
    }

    /**
     * @return Returns the properties.
     */
    protected Properties getProperties() {
        return pageProperties;
    }
}