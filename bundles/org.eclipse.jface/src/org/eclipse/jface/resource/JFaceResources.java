/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Utility methods to access JFace-specific resources.
 * <p>
 * All methods declared on this class are static. This
 * class cannot be instantiated.
 * </p>
 * <p>
 * The following global state is also maintained by this class:
 * <ul>
 *   <li>a font registry</li>
 *	 <li>a color registry</li>
 *   <li>an image registry</li>
 *   <li>a resource bundle</li>
 * </ul>
 * </p>
 */
public class JFaceResources {

	/**
	 * The symbolic font name for the banner font 
	 * (value <code>"org.eclipse.jface.bannerfont"</code>).
	 */
	public static final String BANNER_FONT = "org.eclipse.jface.bannerfont"; //$NON-NLS-1$

	/**
	 * The JFace resource bundle; eagerly initialized.
	 */
	private static final ResourceBundle bundle =
		ResourceBundle.getBundle("org.eclipse.jface.messages"); //$NON-NLS-1$

	/**
	 * The JFace color registry; <code>null</code> until
	 * lazily initialized or explicitly set.
	 */
	private static ColorRegistry colorRegistry;

	/**
	 * The symbolic font name for the standard font 
	 * (value <code>"org.eclipse.jface.defaultfont"</code>).
	 */
	public static final String DEFAULT_FONT = "org.eclipse.jface.defaultfont"; //$NON-NLS-1$

	/**
	 * The symbolic font name for the dialog font 
	 * (value <code>"org.eclipse.jface.dialogfont"</code>).
	 */
	public static final String DIALOG_FONT = "org.eclipse.jface.dialogfont"; //$NON-NLS-1$

	/**
	 * The JFace font registry; <code>null</code> until
	 * lazily initialized or explicitly set.
	 */
	private static FontRegistry fontRegistry = null;

	/**
	 * The symbolic font name for the header font 
	 * (value <code>"org.eclipse.jface.headerfont"</code>).
	 */
	public static final String HEADER_FONT = "org.eclipse.jface.headerfont"; //$NON-NLS-1$

	/**
	 * The JFace image registry; <code>null</code> until
	 * lazily initialized.
	 */
	private static ImageRegistry imageRegistry = null;

	/**
	 * The symbolic font name for the text font 
	 * (value <code>"org.eclipse.jface.textfont"</code>).
	 */
	public static final String TEXT_FONT = "org.eclipse.jface.textfont"; //$NON-NLS-1$

	/**
	 * The symbolic font name for the viewer font 
	 * (value <code>"org.eclipse.jface.viewerfont"</code>).
		* @deprecated This font is not in use
	 */
	public static final String VIEWER_FONT = "org.eclipse.jface.viewerfont"; //$NON-NLS-1$

	/**
	 * The symbolic font name for the window font 
	 * (value <code>"org.eclipse.jface.windowfont"</code>).
	 * @deprecated This font is not in use
	 */
	public static final String WINDOW_FONT = "org.eclipse.jface.windowfont"; //$NON-NLS-1$
	
	/**
	 * Returns the formatted message for the given key in
	 * JFace's resource bundle. 
	 *
	 * @param key the resource name
	 * @param args the message arguments
	 * @return the string
	 */
	public static String format(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}
	
	/**
	 * Returns the JFace's banner font.
	 * Convenience method equivalent to
	 * <pre>
	 * JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT)
	 * </pre>
	 *
	 * @return the font
	 */
	public static Font getBannerFont() {
		return getFontRegistry().get(BANNER_FONT);
	}
	
	/**
	 * Returns the resource bundle for JFace itself.
	 * The resouble bundle is obtained from
	 * <code>ResourceBundle.getBundle("org.eclipse.jface.jface_nls")</code>.
	 * <p>
	 * Note that several static convenience methods are 
	 * also provided on this class for directly accessing
	 * resources in this bundle.
	 * </p>
	 *
	 * @return the resource bundle
	 */
	public static ResourceBundle getBundle() {
		return bundle;
	}

	/**
	 * Returns the color registry for JFace itself.
	 * <p>
	 * @return the <code>ColorRegistry</code>.
	 * @since 3.0
	 */
	public static ColorRegistry getColorRegistry() {
		if (colorRegistry == null)
			colorRegistry = new ColorRegistry();
		return colorRegistry;
	}
	
	/**
	 * Returns the JFace's standard font.
	 * Convenience method equivalent to
	 * <pre>
	 * JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT)
	 * </pre>
	 *
	 * @return the font
	 */
	public static Font getDefaultFont() {
		return getFontRegistry().defaultFont();
	}
	/**
	 * Returns the JFace's dialog font.
	 * Convenience method equivalent to
	 * <pre>
	 * JFaceResources.getFontRegistry().get(JFaceResources.DIALOG_FONT)
	 * </pre>
	 *
	 * @return the font
	 */
	public static Font getDialogFont() {
		return getFontRegistry().get(DIALOG_FONT);
	}
	/**
	 * Returns the font in JFace's font registry with the given
	 * symbolic font name.
	 * Convenience method equivalent to
	 * <pre>
	 * JFaceResources.getFontRegistry().get(symbolicName)
	 * </pre>
	 *
	 * If an error occurs, return the default font.
	 *
	 * @param symbolicName the symbolic font name
	 * @return the font
	 */
	public static Font getFont(String symbolicName) {
		return getFontRegistry().get(symbolicName);
	}
	/**
	 * Returns the font registry for JFace itself.
	 * If the value has not been established by an earlier
	 * call to <code>setFontRegistry</code>, is it
	 * initialized to
	 * <code>new FontRegistry("org.eclipse.jface.resource.jfacefonts")</code>.
	 * <p>
	 * Note that several static convenience methods are 
	 * also provided on this class for directly accessing
	 * JFace's standard fonts.
	 * </p>
	 */
	public static FontRegistry getFontRegistry() {
		if (fontRegistry == null) {
			fontRegistry = new FontRegistry("org.eclipse.jface.resource.jfacefonts"); //$NON-NLS-1$
		}
		return fontRegistry;
	}

	/**
	 * Returns the JFace's header font.
	 * Convenience method equivalent to
	 * <pre>
	 * JFaceResources.getFontRegistry().get(JFaceResources.HEADER_FONT)
	 * </pre>
	 *
	 * @return the font
	 */
	public static Font getHeaderFont() {
		return getFontRegistry().get(HEADER_FONT);
	}

	/**
	 * Returns the image in JFace's image registry with the given key, 
	 * or <code>null</code> if none.
	 * Convenience method equivalent to
	 * <pre>
	 * JFaceResources.getImageRegistry().get(key)
	 * </pre>
	 *
	 * @param key the key
	 * @return the image, or <code>null</code> if none
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	/**
	 * Returns the image registry for JFace itself.
	 * <p>
	 * Note that the static convenience method <code>getImage</code>
	 * is also provided on this class.
	 * </p>
	 */
	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null)
			imageRegistry = new ImageRegistry();
		return imageRegistry;
	}
	/**
	 * Returns the resource object with the given key in
	 * JFace's resource bundle. If there isn't any value under
	 * the given key, the key is returned.
	 *
	 * @param key the resource name
	 * @return the string
	 */
	public static String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	/**
	 * Returns a list of string values corresponding to the
	 * given list of keys. The lookup is done with <code>getString</code>.
	 * The values are in the same order as the keys.
	 *
	 * @param keys a list of keys
	 * @return a list of corresponding string values
	 */
	public static String[] getStrings(String[] keys) {
		Assert.isNotNull(keys);
		int length = keys.length;
		String[] result = new String[length];
		for (int i = 0; i < length; i++)
			result[i] = getString(keys[i]);
		return result;
	}

	/**
	 * Returns the JFace's text font.
	 * Convenience method equivalent to
	 * <pre>
	 * JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT)
	 * </pre>
	 *
	 * @return the font
	 */
	public static Font getTextFont() {
		return getFontRegistry().get(TEXT_FONT);
	}
	/**
	 * Returns the JFace's viewer font.
	 * Convenience method equivalent to
	 * <pre>
	 * JFaceResources.getFontRegistry().get(JFaceResources.VIEWER_FONT)
	 * </pre>
	 *
	 * @return the font
	 * @deprecated This font is not in use
	 */
	public static Font getViewerFont() {
		return getFontRegistry().get(VIEWER_FONT);
	}
	/**
	 * Sets JFace's font registry to the given value.
	 * This method may only be called once; the call must occur
	 * before <code>JFaceResources.getFontRegistry</code>
	 * is invoked (either directly or indirectly). 
	 *
	 * @param registry a font registry
	 */
	public static void setFontRegistry(FontRegistry registry) {
		Assert.isTrue(fontRegistry == null, "Font registry can only be set once."); //$NON-NLS-1$
		fontRegistry = registry;
	}

	/* (non-Javadoc)
	 * Declare a private constructor to block instantiation.
	 */
	private JFaceResources() {
		//no-op
	}
}
