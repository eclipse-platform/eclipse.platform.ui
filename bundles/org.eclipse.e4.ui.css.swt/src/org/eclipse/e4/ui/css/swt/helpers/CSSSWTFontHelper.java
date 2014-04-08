/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.normalizeId;

import org.eclipse.e4.ui.css.core.css2.CSS2FontHelper;
import org.eclipse.e4.ui.css.core.css2.CSS2FontPropertiesHelpers;
import org.eclipse.e4.ui.css.core.css2.CSS2PrimitiveValueImpl;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * CSS SWT Font Helper to :
 * <ul>
 * <li>get CSS2FontProperties from Font of SWT Control.</li>
 * <li>get Font of SWT Control from CSS2FontProperties.</li>
 * </ul>
 */
public class CSSSWTFontHelper {
	public static final String FONT_DEFINITION_MARKER = "#";

	private static final String DEFAULT_FONT = "defaultFont";

	/**
	 * Get CSS2FontProperties from Control stored into Data of Control. If
	 * CSS2FontProperties doesn't exist, create it from Font of Control and
	 * store it into Data of Control.
	 *
	 * @param control
	 * @return
	 */
	public static CSS2FontProperties getCSS2FontProperties(Widget widget,
			Font font, CSSElementContext context) {
		// Search into Data of Control if CSS2FontProperties exist.
		CSS2FontProperties fontProperties = CSS2FontPropertiesHelpers
				.getCSS2FontProperties(context);
		if (fontProperties == null) {
			// CSS2FontProperties doesn't exist, create it
			fontProperties = getCSS2FontProperties(font);
			// store into ClientProperty the CSS2FontProperties
			CSS2FontPropertiesHelpers.setCSS2FontProperties(fontProperties,
					context);
		}
		return fontProperties;
	}

	/**
	 * Get CSS2FontProperties from the widget. If CSS2FontProperties doesn't
	 * exist, create it from the widget's font, if it has one, and then store it
	 * in the widget's data if applicable.
	 *
	 * @param widget
	 *            the widget to retrieve CSS2 font properties from
	 * @return the font properties of the specified widget, or <code>null</code>
	 *         if none
	 */
	public static CSS2FontProperties getCSS2FontProperties(Widget widget,
			CSSElementContext context) {
		return getCSS2FontProperties(getFont(widget), context);
	}

	public static CSS2FontProperties getCSS2FontProperties(Font font,
			CSSElementContext context) {
		// Search into Data of Control if CSS2FontProperties exist.
		CSS2FontProperties fontProperties = CSS2FontPropertiesHelpers
				.getCSS2FontProperties(context);
		if (fontProperties == null && font != null) {
			// CSS2FontProperties doesn't exist, create it
			fontProperties = getCSS2FontProperties(font);
			// store into ClientProperty the CSS2FontProperties
			CSS2FontPropertiesHelpers.setCSS2FontProperties(fontProperties,
					context);
		}
		return fontProperties;
	}

	/**
	 * Build CSS2FontProperties from SWT Font.
	 *
	 * @param font
	 * @return
	 */
	public static CSS2FontProperties getCSS2FontProperties(Font font) {
		// Create CSS Font Properties
		CSS2FontProperties fontProperties = new CSS2FontPropertiesImpl();
		if (font != null) {
			FontData fontData = getFirstFontData(font);
			// Update font-family
			String fontFamily = getFontFamily(font);
			fontProperties.setFamily(new CSS2PrimitiveValueImpl(fontFamily));
			// Update font-size
			int fontSize = fontData.getHeight();
			fontProperties.setSize(new CSS2PrimitiveValueImpl(fontSize));
			// Update font-weight
			String fontWeight = getFontWeight(font);
			fontProperties.setWeight((new CSS2PrimitiveValueImpl(fontWeight)));
			// Update font-style
			String fontStyle = getFontStyle(font);
			fontProperties.setStyle((new CSS2PrimitiveValueImpl(fontStyle)));
		}
		return fontProperties;
	}

	/**
	 * Get CSS2FontProperties from Font of JComponent and store
	 * CSS2FontProperties instance into ClientProperty of JComponent.
	 *
	 * @param component
	 * @return
	 */
	public static Font getFont(CSS2FontProperties fontProperties,
			Control control) {
		FontData oldFontData = getFirstFontData(control.getFont());
		return getFont(fontProperties, oldFontData, control.getDisplay());
	}

	public static Font getFont(CSS2FontProperties fontProperties,
			FontData oldFontData, Display display) {
		FontData newFontData = getFontData(fontProperties, oldFontData);
		return new Font(display, newFontData);
	}

	/**
	 * Return FontData from {@link CSS2FontProperties}.
	 *
	 * @param fontProperties
	 * @param control
	 * @return
	 */
	public static FontData getFontData(CSS2FontProperties fontProperties,
			FontData oldFontData) {
		FontData newFontData = new FontData();

		// Family
		CSSPrimitiveValue cssFontFamily = fontProperties.getFamily();
		FontData[] fontDataByDefinition = new FontData[0];
		boolean fontDefinitionAsFamily = hasFontDefinitionAsFamily(fontProperties);

		if (fontDefinitionAsFamily) {
			fontDataByDefinition = findFontDataByDefinition(cssFontFamily);
			if (fontDataByDefinition.length > 0) {
				newFontData.setName(fontDataByDefinition[0].getName());
			}
		} else if (cssFontFamily != null) {
			newFontData.setName(cssFontFamily.getStringValue());
		}

		boolean fontFamilySet = newFontData.getName() != null && newFontData.getName().trim().length() > 0;
		if (!fontFamilySet && oldFontData != null) {
			newFontData.setName(oldFontData.getName());
		}

		// Style
		int style = getSWTStyle(fontProperties, oldFontData);
		if (fontDefinitionAsFamily && fontDataByDefinition.length > 0 && style == SWT.NORMAL) {
			newFontData.setStyle(fontDataByDefinition[0].getStyle());
		} else {
			newFontData.setStyle(style);
		}

		// Height
		CSSPrimitiveValue cssFontSize = fontProperties.getSize();
		boolean fontHeightSet = false;

		if (cssFontSize == null || cssFontSize.getCssText() == null) {
			if (fontDefinitionAsFamily && fontDataByDefinition.length > 0) {
				newFontData.setHeight(fontDataByDefinition[0].getHeight());
				fontHeightSet = true;
			}
		} else if (cssFontSize != null) {
			newFontData.setHeight((int) (cssFontSize).getFloatValue(CSSPrimitiveValue.CSS_PT));
			fontHeightSet = true;
		}
		if (!fontHeightSet && oldFontData != null) {
			newFontData.setHeight(oldFontData.getHeight());
		}

		return newFontData;
	}

	public static boolean hasFontDefinitionAsFamily(CSSValue value) {
		if (value instanceof CSS2FontProperties) {
			CSS2FontProperties props = (CSS2FontProperties) value;
			return props.getFamily() != null
					&& props.getFamily().getStringValue()
					.startsWith(FONT_DEFINITION_MARKER);
		}
		return false;
	}

	private static FontData[] findFontDataByDefinition(CSSPrimitiveValue cssFontFamily) {
		IColorAndFontProvider provider = CSSActivator.getDefault().getColorAndFontProvider();
		FontData[] result = new FontData[0];
		if (provider != null) {
			FontData[] fontData = provider.getFont(normalizeId(cssFontFamily.getStringValue().substring(1)));
			if (fontData != null) {
				result = fontData;
			}
		}
		return result;
	}

	/**
	 * Return SWT style Font from {@link CSS2FontProperties}.
	 *
	 * @param fontProperties
	 * @param control
	 * @return
	 */
	public static int getSWTStyle(CSS2FontProperties fontProperties,
			FontData fontData) {
		if (fontData == null) {
			return SWT.NONE;
		}

		int fontStyle = fontData.getStyle();
		// CSS2 font-style
		CSSPrimitiveValue cssFontStyle = fontProperties.getStyle();
		if (cssFontStyle != null) {
			String style = cssFontStyle.getStringValue();
			if ("italic".equals(style)) {
				fontStyle = fontStyle | SWT.ITALIC;
			} else {
				if (fontStyle == (fontStyle | SWT.ITALIC)) {
					fontStyle = fontStyle ^ SWT.ITALIC;
				}
			}
		}
		// CSS font-weight
		CSSPrimitiveValue cssFontWeight = fontProperties.getWeight();
		if (cssFontWeight != null) {
			String weight = cssFontWeight.getStringValue();
			if ("bold".equals(weight.toLowerCase())) {
				fontStyle = fontStyle | SWT.BOLD;
			} else {
				if (fontStyle == (fontStyle | SWT.BOLD)) {
					fontStyle = fontStyle ^ SWT.BOLD;
				}
			}
		}
		return fontStyle;
	}

	/**
	 * Return CSS Value font-family from the widget's font, if it has a font
	 *
	 * @param widget
	 * @return
	 */
	public static String getFontFamily(Widget widget) {
		return getFontFamily(getFont(widget));
	}

	/**
	 * Return CSS Value font-family from SWT Font
	 *
	 * @param font
	 * @return
	 */
	public static String getFontFamily(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontFamily(fontData);
	}

	public static String getFontFamily(FontData fontData) {
		if (fontData != null) {
			String family = fontData.getName();
			return CSS2FontHelper.getFontFamily(family);
		}
		return null;
	}

	/**
	 * Return CSS Value font-size the widget's font, if it has a font
	 *
	 * @param widget
	 * @return
	 */
	public static String getFontSize(Widget widget) {
		return getFontSize(getFont(widget));
	}

	/**
	 * Return CSS Value font-size from SWT Font
	 *
	 * @param font
	 * @return
	 */
	public static String getFontSize(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontSize(fontData);
	}

	public static String getFontSize(FontData fontData) {
		if (fontData != null) {
			return CSS2FontHelper.getFontSize(fontData.getHeight());
		}
		return null;
	}

	/**
	 * Return CSS Value font-style from the widget's font, if it has a font
	 *
	 * @param widget
	 * @return
	 */
	public static String getFontStyle(Widget widget) {
		return getFontStyle(getFont(widget));
	}

	/**
	 * Return CSS Value font-style from SWT Font
	 *
	 * @param font
	 * @return
	 */
	public static String getFontStyle(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontStyle(fontData);
	}

	public static String getFontStyle(FontData fontData) {
		boolean isItalic = false;
		if (fontData != null) {
			isItalic = isItalic(fontData);
		}
		return CSS2FontHelper.getFontStyle(isItalic);
	}

	public static boolean isItalic(FontData fontData) {
		int fontStyle = fontData.getStyle();
		return ((fontStyle | SWT.ITALIC) == fontStyle);
	}

	/**
	 * Return CSS Value font-weight from the widget's font, if it has a font
	 *
	 * @param widget
	 * @return
	 */
	public static String getFontWeight(Widget widget) {
		return getFontWeight(getFont(widget));
	}

	/**
	 * Return CSS Value font-weight from Control Font
	 *
	 * @param font
	 * @return
	 */
	public static String getFontWeight(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontWeight(fontData);
	}

	public static String getFontWeight(FontData fontData) {
		boolean isBold = false;
		if (fontData != null) {
			isBold = isBold(fontData);
		}
		return CSS2FontHelper.getFontWeight(isBold);
	}

	public static boolean isBold(FontData fontData) {
		int fontStyle = fontData.getStyle();
		return ((fontStyle | SWT.BOLD) == fontStyle);
	}

	/**
	 * Return CSS Value font-family from Control Font
	 *
	 * @param control
	 * @return
	 */
	public static String getFontComposite(Control control) {
		return getFontComposite(control.getFont());
	}

	/**
	 * Return CSS Value font-family from SWT Font
	 *
	 * @param font
	 * @return
	 */
	public static String getFontComposite(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontComposite(fontData);
	}

	public static String getFontComposite(FontData fontData) {
		if (fontData != null) {
			StringBuffer composite = new StringBuffer();
			// font-family
			composite.append(getFontFamily(fontData));
			composite.append(" ");
			// font-size
			composite.append(getFontSize(fontData));
			composite.append(" ");
			// font-weight
			composite.append(getFontWeight(fontData));
			composite.append(" ");
			// font-style
			composite.append(getFontStyle(fontData));
			return composite.toString();
		}
		return null;
	}

	/**
	 * Return first FontData from Control Font.
	 *
	 * @param control
	 * @return
	 */
	public static FontData getFirstFontData(Control control) {
		Font font = control.getFont();
		if (font == null) {
			return null;
		}
		return getFirstFontData(font);
	}

	/**
	 *
	 * Return first FontData from SWT Font.
	 *
	 * @param font
	 * @return
	 */
	public static FontData getFirstFontData(Font font) {
		FontData[] fontDatas = font.getFontData();
		if (fontDatas == null || fontDatas.length < 1) {
			return null;
		}
		return fontDatas[0];
	}

	private static Font getFont(Widget widget) {
		if (widget instanceof CTabItem) {
			return ((CTabItem) widget).getFont();
		} else if (widget instanceof Control) {
			return ((Control) widget).getFont();
		} else {
			return null;
		}
	}

	public static void storeDefaultFont(Control control) {
		storeDefaultFont(control, control.getFont());
	}

	public static void storeDefaultFont(CTabItem item) {
		storeDefaultFont(item, item.getFont());
	}

	private static void storeDefaultFont(Widget widget, Font font) {
		if (widget.getData(DEFAULT_FONT) == null) {
			widget.setData(DEFAULT_FONT, font);
		}
	}

	public static void restoreDefaultFont(Control control) {
		Font defaultFont = (Font) control.getData(DEFAULT_FONT);
		if (defaultFont != null) {
			if (defaultFont.isDisposed()) {
				defaultFont = control.getDisplay().getSystemFont();
			}
			if (!equals(defaultFont, control.getFont())) {
				control.setFont(defaultFont);
			}
		}
	}

	public static void restoreDefaultFont(CTabItem item) {
		Font defaultFont = (Font) item.getData(DEFAULT_FONT);
		if (defaultFont != null) {
			if (defaultFont.isDisposed()) {
				defaultFont = item.getDisplay().getSystemFont();
			}
			if (!equals(defaultFont, item.getFont())) {
				item.setFont(defaultFont);
			}
		}
	}

	/** Helper function to avoid setting fonts unnecessarily */
	public static void setFont(Control control, Font font) {
		if (!equals(control.getFont(), font)) {
			storeDefaultFont(control);
			control.setFont(font);
		}
	}

	/** Helper function to avoid setting fonts unnecessarily */
	public static void setFont(CTabItem item, Font font) {
		if (!equals(item.getFont(), font)) {
			storeDefaultFont(item);
			item.setFont(font);
		}
	}

	/**
	 * On certain platforms, may have two font instances that actually are the
	 * same
	 */
	public static boolean equals(Font f1, Font f2) {
		if (f1 == f2) {
			return true;
		}
		if (f1 == null || f2 == null) {
			return false;
		}
		if (f1.equals(f2)) {
			return true;
		}
		FontData[] fd1 = f1.getFontData();
		FontData[] fd2 = f2.getFontData();
		if (fd1.length != fd2.length) {
			return false;
		}
		for (int i = 0; i < fd1.length; i++) {
			if (!fd1[i].equals(fd2[i])) {
				return false;
			}
		}
		return true;
	}

}
