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

    protected Properties pageProperties;
    protected AbstractIntroPage page;
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
        pageProperties = new Properties();
        String sharedStyle = modelRoot.getPresentation()
                .getImplementationStyle();
        if (sharedStyle != null)
            load(pageProperties, sharedStyle);
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
        return pageProperties.getProperty(key);
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
    protected Properties getProperties() {
        return pageProperties;
    }



    /**
     * 
     * 
     * @param toolkit
     * @param key
     * @return color. May return null.
     */
    public Color getColor(FormToolkit toolkit, String qualifier) {
        FormColors colors = toolkit.getColors();
        String key = createColorKey(page, qualifier);
        Color color = colors.getColor(key);
        if (color == null) {
            RGB rgb = getRGB(key);
            if (rgb != null)
                color = colors.createColor(key, rgb);
        }
        return color;
    }

    private String createColorKey(AbstractIntroPage page, String qualifier) {
        if (page != null)
            return StringUtil.concat(page.getId(), ".", qualifier).toString(); //$NON-NLS-1$
        return qualifier;
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
            buff = createPathKey(link);
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

    /**
     * Creates a key for the given element. Returns null if any id is null along
     * teh path.
     * 
     * @param element
     * @return
     */
    protected StringBuffer createPathKey(AbstractIntroIdElement element) {
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
        // try default
        if (defaultKey != null)
            return ImageUtil.getImage(defaultKey);
        return null;
    }



}

