/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.branding.IProductConstants;

/**
 * A class that converts the strings returned by
 * <code>org.eclipse.core.runtime.IProduct.getProperty</code> to the
 * appropriate class. This implementation is tightly bound to the properties
 * provided in IProductConstants. Clients adding their own properties could
 * choose to subclass this.
 * 
 * @see org.eclipse.ui.branding.IProductConstants
 * @since 3.0
 */
public class ProductProperties extends BrandingProperties implements
        IProductConstants {

    private final IProduct product;

    private String appName;

    private String aboutText;

    private ImageDescriptor aboutImageDescriptor;

    private ImageDescriptor[] windowImageDescriptors;

    private URL welcomePageUrl;

    private String productName;

    private String productId;

    private static final String ABOUT_MAPPINGS = "$nl$/about.mappings"; //$NON-NLS-1$

    private static String[] systemPropertiesKeys = new String[0];
    private static String[] mappings = loadMappings();

    private static String[] loadMappings() {
        IProduct product = Platform.getProduct();
        if (product == null) {
			return new String[0];
		}
        URL location = Platform.find(product.getDefiningBundle(), new Path(
                ABOUT_MAPPINGS));
        PropertyResourceBundle bundle = null;
        InputStream is;
        if (location != null) {
            is = null;
            try {
                is = location.openStream();
                bundle = new PropertyResourceBundle(is);
            } catch (IOException e) {
                bundle = null;
            } finally {
                try {
                    if (is != null) {
						is.close();
					}
                } catch (IOException e) {
                    // do nothing if we fail to close
                }
            }
        }

        ArrayList mappingsList = new ArrayList();
        if (bundle != null) {
            boolean found = true;
            int i = 0;
            ArrayList systemPropertiesKeysList = new ArrayList();
            while (found) {
                try {
                	String nextString = bundle.getString(Integer.toString(i));
                	int length = nextString.length();
                	/*
                	 * Check if the mapping value is a system property, specified
                	 * by '$' at the beginning and end of the string.  If so, add 
                	 * the key to the systemPropertiesKeys array and insert array
                	 * indices "{i}" to allow the string to be formatted again with 
                	 * the new system property values.
                	 */
                	if (length > 2 && nextString.indexOf('$') == 0 && nextString.lastIndexOf('$') == length - 1) {
                		int newIndex = systemPropertiesKeysList.size();
                		systemPropertiesKeysList.add(nextString.substring(1, length-1));
                		nextString = "{" + newIndex + "}"; //$NON-NLS-1$ //$NON-NLS-2$
                	}
                    mappingsList.add(nextString);
                } catch (MissingResourceException e) {
                    found = false;
                }
                i++;
            }
            systemPropertiesKeys = (String[]) systemPropertiesKeysList.toArray(new String[systemPropertiesKeysList.size()]) ;
        }
        return (String[]) mappingsList.toArray(new String[mappingsList.size()]);
    }

    /**
     * This instance will return properties from the given product.  The properties are
     * retrieved in a lazy fashion and cached for later retrieval.
     * @param product must not be null
     */
    public ProductProperties(IProduct product) {
        if (product == null) {
			throw new IllegalArgumentException();
		}
        this.product = product;
    }

    /**
     * The application name, used to initialize the SWT Display.  This
     * value is distinct from the string displayed in the application
     * title bar.
     * <p>
     * E.g., On motif, this can be used to set the name used for
     * resource lookup.
     * </p>
     * @see org.eclipse.swt.widgets.Display#setAppName
     */
    public String getAppName() {
        if (appName == null) {
			appName = getAppName(product);
		}
        return appName;
    }

    /**
     * The text to show in an "about" dialog for this product.
     * Products designed to run "headless" typically would not
     * have such text.
     */
    public String getAboutText() {
        if (aboutText == null) {
			aboutText = getAboutText(product);
		}
        return aboutText;
    }

    /**
     * An image which can be shown in an "about" dialog for this
     * product. Products designed to run "headless" typically would not 
     * have such an image.
     * <p>
     * A full-sized product image (no larger than 500x330 pixels) is
     * shown without the "aboutText" blurb.  A half-sized product image
     * (no larger than 250x330 pixels) is shown with the "aboutText"
     * blurb beside it.
     */
    public ImageDescriptor getAboutImage() {
        if (aboutImageDescriptor == null) {
			aboutImageDescriptor = getAboutImage(product);
		}
        return aboutImageDescriptor;
    }

    /**
     * An array of one or more images to be used for this product.  The
     * expectation is that the array will contain the same image rendered
     * at different sizes (16x16 and 32x32).  
     * Products designed to run "headless" typically would not have such images.
     * <p>
     * If this property is given, then it supercedes <code>WINDOW_IMAGE</code>.
     * </p>
     */
    public ImageDescriptor[] getWindowImages() {
        if (windowImageDescriptors == null) {
			windowImageDescriptors = getWindowImages(product);
		}
        return windowImageDescriptors;
    }

    /**
     * Location of the product's welcome page (special XML-based format), either
     * a fully qualified valid URL or a path relative to the product's defining
     * bundle. Products designed to run "headless" typically would not have such
     * a page. Use of this property is discouraged in 3.0, the new
     * org.eclipse.ui.intro extension point should be used instead.
     */
    public URL getWelcomePageUrl() {
        if (welcomePageUrl == null) {
			welcomePageUrl = getWelcomePageUrl(product);
		}
        return welcomePageUrl;
    }

    /**
     * Returns the product name or <code>null</code>.
     * This is shown in the window title and the About action.
     */
    public String getProductName() {
        if (productName == null) {
			productName = getProductName(product);
		}
        return productName;
    }

    /**
     * Returns the id for the product or <code>null</code> if none.
     */
    public String getProductId() {
        if (productId == null) {
			productId = getProductId(product);
		}
        return productId;
    }

    /**
     * The application name, used to initialize the SWT Display.  This
     * value is distinct from the string displayed in the application
     * title bar.
     * <p>
     * E.g., On motif, this can be used to set the name used for
     * resource lookup.
     * </p>
     * <p>
     * The returned value will have {n} values substituted based on the
     * current product's mappings regardless of the given product argument.
     * </p>
     * @see org.eclipse.swt.widgets.Display#setAppName
     */
    public static String getAppName(IProduct product) {
        String property = product.getProperty(APP_NAME);
        if (property == null) {
			return ""; //$NON-NLS-1$
		}
        if (property.indexOf('{') == -1) {
			return property;
		}
        return MessageFormat.format(property, mappings);
    }

    /**
     * The text to show in an "about" dialog for this product.
     * Products designed to run "headless" typically would not
     * have such text.
     * <p>
     * The returned value will have {n} values substituted based on the
     * current product's mappings regardless of the given product argument.
     * </p>
     */
    public static String getAboutText(IProduct product) {
        String property = product.getProperty(ABOUT_TEXT);
        if (property == null) {
			return ""; //$NON-NLS-1$
		}
        if (property.indexOf('{') == -1) {
			return property;
		}
        property = MessageFormat.format(property, mappings);
        
        /*
         * If there is still a "{" character, check if there are
         * System properties in the systemPropertiesKeys array that 
         * need to be loaded. 
         */
        if (property.indexOf('{') == -1) {
        	return property;
        }
        
        if (systemPropertiesKeys.length == 0) 
			return property;
        
        /*
         * Create a String array of the actual values to be mapped
         * to the system properties keys.
         */
        String[] systemPropertiesMappings = new String[systemPropertiesKeys.length];
        for (int i=0; i < systemPropertiesKeys.length; i++){
        	String systemProperty = systemPropertiesKeys[i];
        	// If system property is not set, insert an empty String
        	systemPropertiesMappings[i] = System.getProperty(systemProperty, ""); //$NON-NLS-1$
        }
        /*
         * Format string with the system properties values.
         */
	    return MessageFormat.format(property, systemPropertiesMappings);
    }

    /**
     * An image which can be shown in an "about" dialog for this
     * product. Products designed to run "headless" typically would not 
     * have such an image.
     * <p>
     * A full-sized product image (no larger than 500x330 pixels) is
     * shown without the "aboutText" blurb.  A half-sized product image
     * (no larger than 250x330 pixels) is shown with the "aboutText"
     * blurb beside it.
     */
    public static ImageDescriptor getAboutImage(IProduct product) {
        return getImage(product.getProperty(ABOUT_IMAGE), product
                .getDefiningBundle());
    }

    /**
     * An array of one or more images to be used for this product.  The
     * expectation is that the array will contain the same image rendered
     * at different sizes (16x16 and 32x32).  
     * Products designed to run "headless" typically would not have such images.
     * <p>
     * If this property is given, then it supercedes <code>WINDOW_IMAGE</code>.
     * </p>
     */
    public static ImageDescriptor[] getWindowImages(IProduct product) {
        String property = product.getProperty(WINDOW_IMAGES);

        // for compatibility with pre-3.0 plugins that may still use WINDOW_IMAGE
        if (property == null) {
			property = product.getProperty(WINDOW_IMAGE);
		}

        return getImages(property, product.getDefiningBundle());
    }

    /**
     * Location of the product's welcome page (special XML-based format), either
     * a fully qualified valid URL or a path relative to the product's defining
     * bundle. Products designed to run "headless" typically would not have such
     * a page. Use of this property is discouraged in 3.0, the new
     * org.eclipse.ui.intro extension point should be used instead.
     */
    public static URL getWelcomePageUrl(IProduct product) {
        return getUrl(product.getProperty(WELCOME_PAGE), product
                .getDefiningBundle());
    }

    /**
     * Returns the product name or <code>null</code>.
     * This is shown in the window title and the About action.
     */
    public static String getProductName(IProduct product) {
        return product.getName();
    }

    /**
     * Returns the id for the product.
     */
    public static String getProductId(IProduct product) {
        return product.getId();
    }
}
