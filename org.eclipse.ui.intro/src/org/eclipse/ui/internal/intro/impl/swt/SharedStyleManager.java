/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.swt;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.swt.graphics.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.osgi.framework.*;

public class SharedStyleManager {

    protected Properties properties;
    protected Bundle bundle;

    SharedStyleManager() {
    }

    /**
     * Constructor used when shared styles need to be loaded. The bundle is
     * retrieved from the model root.
     * 
     * @param modelRoot
     */
    public SharedStyleManager(IntroModelRoot modelRoot) {
        bundle = modelRoot.getBundle();
        properties = new Properties();
        String sharedStyle = modelRoot.getPresentation()
                .getImplementationStyle();
        if (sharedStyle != null)
            load(properties, sharedStyle);
    }

    protected void load(Properties properties, String style) {
        if (style == null)
            return;
        try {
            URL styleURL = new URL(style);
            InputStream is = styleURL.openStream();
            properties.load(is);
            is.close();
        } catch (Exception e) {
            Log.error("Could not load SWT style: " + style, e); //$NON-NLS-1$
        }
    }


    /**
     * Get the property from the shared properties.
     * 
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return doGetProperty(properties, key);
    }

    /*
     * Utility method to trim properties retrieval.
     */
    protected String doGetProperty(Properties aProperties, String key) {
        String value = aProperties.getProperty(key);
        if (value != null)
            // trim the properties as trailing balnnks cause problems.
            value = value.trim();
        return value;
    }


    protected RGB getRGB(String key) {
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
     * Finds the bundle from which this key was loaded. This is the bundle from
     * which shared styles where loaded.
     * 
     * @param key
     * @return
     */
    protected Bundle getAssociatedBundle(String key) {
        return bundle;
    }



    /**
     * @return Returns the properties.
     */
    public Properties getProperties() {
        return properties;
    }


    /**
     * 
     * 
     * @param toolkit
     * @param key
     * @return color. May return null.
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



    /**
     * Retrieve an image from this page's properties, given a key.
     * 
     * @param key
     * @param defaultPageKey
     * @param defaultKey
     * @return
     */
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
            Bundle bundle = getAssociatedBundle(currentKey);
            if (bundle == null)
                // it means that we are getting a key defined in this page's
                // styles. (ie: not an inherited style).
                bundle = this.bundle;
            ImageUtil.registerImage(currentKey, bundle, value);
            Image image = ImageUtil.getImage(currentKey);
            if (image != null)
                return image;
        }
        // try default. We know default is already registered,
        if (defaultKey != null)
            return ImageUtil.getImage(defaultKey);
        return null;
    }


    public boolean useCustomHomePagelayout() {
        String key = "home-page-custom-layout"; //$NON-NLS-1$
        String value = getProperty(key);
        if (value == null)
            value = "true"; //$NON-NLS-1$
        return value.equalsIgnoreCase("true"); //$NON-NLS-1$
    }

}

