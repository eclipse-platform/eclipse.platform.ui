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

    private Properties properties;
    private AbstractIntroPage page;
    private IPluginDescriptor pd;

    /**
     * Constructor used when shared styles need to be loaded. The plugin
     * descriptor is retrieved from the model root.
     * 
     * @param modelRoot
     */
    public FormStyleManager(IntroModelRoot modelRoot) {
        pd = modelRoot.getConfigurationElement().getDeclaringExtension()
                .getDeclaringPluginDescriptor();
        properties = new Properties();
        String sharedStyle = modelRoot.getPresentation().getStyle();
        if (sharedStyle != null)
            load(sharedStyle);
    }

    /**
     * Constructor used when a page styles need to be loaded. The plugin
     * descriptor is retrieved from the page model class. The default
     * properties are assumed to be the presentation shared properties.
     * 
     * @param modelRoot
     */
    public FormStyleManager(AbstractIntroPage page, Properties sharedProperties) {
        this.page = page;
        pd = page.getConfigurationElement().getDeclaringExtension()
                .getDeclaringPluginDescriptor();
        properties = new Properties(sharedProperties);
        String altStyle = page.getAltStyle();
        if (altStyle != null)
            load(altStyle);
    }

    private void load(String style) {
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
        return properties.getProperty(key);
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
        IPluginDescriptor pd = page.getConfigurationElement()
                .getDeclaringExtension().getDeclaringPluginDescriptor();
        String defaultKey = (page instanceof IntroHomePage) ? ImageUtil.ROOT_LINK
                : ImageUtil.LINK;
        return getImage(key, pageKey, defaultKey);
    }

    public Image getImage(String key, String defaultPageKey, String defaultKey) {
        String currentKey = key;
        String value = properties.getProperty(currentKey);
        if (value == null && defaultPageKey != null) {
            currentKey = defaultPageKey;
            value = properties.getProperty(defaultPageKey);
        }
        if (value != null) {
            if (ImageUtil.hasImage(currentKey))
                return ImageUtil.getImage(currentKey);
            // try to register the image
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
        return properties;
    }
}