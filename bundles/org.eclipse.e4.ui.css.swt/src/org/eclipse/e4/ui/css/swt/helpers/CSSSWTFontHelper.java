/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.normalizeId;

import java.util.OptionalInt;

import org.eclipse.e4.ui.css.core.css2.CSS2FontHelper;
import org.eclipse.e4.ui.css.core.css2.CSS2FontPropertiesHelpers;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssDimension;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumber;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumeric;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssUnit;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.internal.css.swt.ColorAndFontUtil;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
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

	/** Step between two sizes for the 'larger' and 'smaller' keywords. */
	private static final double RELATIVE_FONT_SIZE_STEP = 1.2;

	/** Lower bound so that repeated 'smaller' cannot shrink a font away. */
	private static final int MIN_FONT_HEIGHT = 1;

	/** A CSS pixel is 1/96 inch, an SWT font height is 1/72 inch. */
	private static final double PX_TO_PT = 72d / 96d;

	/** Context key for the font an element had before it was ever styled. */
	private static final String BASE_FONT_DATA = "org.eclipse.e4.ui.css.swt.baseFontData"; //$NON-NLS-1$

	/**
	 * Get CSS2FontProperties from Control stored into Data of Control. If
	 * CSS2FontProperties doesn't exist, create it from Font of Control and
	 * store it into Data of Control.
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
			setBaseFontData(context, getFirstFontData(font));
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
			setBaseFontData(context, getFirstFontData(font));
		}
		return fontProperties;
	}

	/**
	 * Build CSS2FontProperties from SWT Font.
	 */
	public static CSS2FontProperties getCSS2FontProperties(Font font) {
		// Create CSS Font Properties
		CSS2FontProperties fontProperties = new CSS2FontPropertiesImpl();
		if (font != null) {
			FontData fontData = getFirstFontData(font);
			// Update font-family
			String fontFamily = getFontFamily(font);
			fontProperties.setFamily(new CssText(CssText.Kind.IDENT, fontFamily));
			// Update font-size; mirrors the widget font, not a CSS declaration
			int fontSize = fontData.getHeight();
			fontProperties.setSize(new CssNumber(fontSize, true));
			fontProperties.setSizeFromCSS(false);
			// Update font-weight
			String fontWeight = getFontWeight(font);
			fontProperties.setWeight(new CssText(CssText.Kind.IDENT, fontWeight));
			// Update font-style
			String fontStyle = getFontStyle(font);
			fontProperties.setStyle(new CssText(CssText.Kind.IDENT, fontStyle));
		}
		return fontProperties;
	}

	/**
	 * Get CSS2FontProperties from Font of JComponent and store
	 * CSS2FontProperties instance into ClientProperty of JComponent.
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
	 */
	public static FontData getFontData(CSS2FontProperties fontProperties, FontData oldFontData) {
		FontData newFontData = new FontData();

		// Family
		CssPrimitive cssFontFamily = fontProperties.getFamily();
		FontData[] fontDataByDefinition = new FontData[0];
		boolean fontDefinitionAsFamily = hasFontDefinitionAsFamily(fontProperties);

		if (fontDefinitionAsFamily) {
			fontDataByDefinition = findFontDataByDefinition((CssText) cssFontFamily);
			if (fontDataByDefinition.length > 0) {
				newFontData.setName(fontDataByDefinition[0].getName());
			}
		} else if (cssFontFamily instanceof CssText family) {
			newFontData.setName(family.value());
		}

		boolean fontFamilySet = newFontData.getName() != null && newFontData.getName().trim().length() > 0;
		if (!fontFamilySet && oldFontData != null) {
			newFontData.setName(oldFontData.getName());
		}

		// Style (bold and italic)
		int style = getSWTStyle(fontProperties, oldFontData);
		if (fontDefinitionAsFamily && fontDataByDefinition.length > 0) {
			// Style cannot be overridden with 'normal', because we don't know
			// if the style in the fontProperties is actually from a CSS
			// specification or from getCSS2FontProperties(). Therefore we
			// cannot decide if the font definition overwrites the default or is
			// overridden in CSS. As best effort we keep all set styles.
			style |= fontDataByDefinition[0].getStyle();
		}
		newFontData.setStyle(style);

		// Height
		OptionalInt cssFontHeight = fontProperties.isSizeFromCSS()
				? getFontHeight(fontProperties.getSize(), oldFontData)
				: OptionalInt.empty();
		boolean fontHeightSet = false;

		if (cssFontHeight.isPresent()) {
			newFontData.setHeight(cssFontHeight.getAsInt());
			fontHeightSet = true;
		} else if (fontDefinitionAsFamily && fontDataByDefinition.length > 0) {
			newFontData.setHeight(fontDataByDefinition[0].getHeight());
			fontHeightSet = true;
		}
		if (!fontHeightSet && oldFontData != null) {
			newFontData.setHeight(oldFontData.getHeight());
		}

		return newFontData;
	}

	/**
	 * Resolves a CSS font-size to an SWT font height in points, empty if the value
	 * cannot be resolved. Relative sizes (em, %, larger, smaller) scale the font
	 * the widget had before styling.
	 */
	private static OptionalInt getFontHeight(CssPrimitive cssFontSize, FontData oldFontData) {
		if (cssFontSize instanceof CssNumeric numeric) {
			return switch (numeric.unit()) {
			case EM -> scaleFontHeight(numeric.value(), oldFontData);
			case PERCENT -> scaleFontHeight(numeric.value() / 100, oldFontData);
			case PX -> OptionalInt.of(toFontHeight(numeric.value() * PX_TO_PT));
			// SWT font heights are points, so any other unit is taken as-is
			default -> OptionalInt.of(toFontHeight(numeric.value()));
			};
		}
		if (cssFontSize instanceof CssText text) {
			if ("larger".equalsIgnoreCase(text.value())) {
				return scaleFontHeight(RELATIVE_FONT_SIZE_STEP, oldFontData);
			}
			if ("smaller".equalsIgnoreCase(text.value())) {
				return scaleFontHeight(1 / RELATIVE_FONT_SIZE_STEP, oldFontData);
			}
		}
		return OptionalInt.empty();
	}

	private static OptionalInt scaleFontHeight(double factor, FontData oldFontData) {
		if (oldFontData == null) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(toFontHeight(oldFontData.getHeight() * factor));
	}

	private static int toFontHeight(double size) {
		return Math.max(MIN_FONT_HEIGHT, (int) Math.round(size));
	}

	/**
	 * Remembers the font an element had before any style was applied to it, the
	 * base that relative font sizes are resolved against.
	 */
	public static void setBaseFontData(CSSElementContext context, FontData fontData) {
		if (context != null && fontData != null) {
			context.setData(BASE_FONT_DATA, fontData);
		}
	}

	/**
	 * The font an element had before any style was applied to it, or
	 * <code>null</code> if it was never recorded.
	 */
	public static FontData getBaseFontData(CSSElementContext context) {
		return context != null && context.getData(BASE_FONT_DATA) instanceof FontData fontData ? fontData : null;
	}

	/**
	 * Returns font properties whose size is an absolute point size, resolving a
	 * relative size against <code>baseFontData</code>. Relative sizes have to be
	 * resolved before the font is converted, because the converted fonts are
	 * cached by their CSS values alone and 'larger' means a different font for
	 * every element it is applied to.
	 */
	public static CSS2FontProperties resolveRelativeSize(CSS2FontProperties fontProperties, FontData baseFontData) {
		if (!fontProperties.isSizeFromCSS() || !isRelativeSize(fontProperties.getSize())) {
			return fontProperties;
		}
		OptionalInt height = getFontHeight(fontProperties.getSize(), baseFontData);
		if (height.isEmpty()) {
			return fontProperties;
		}
		CSS2FontProperties resolved = new CSS2FontPropertiesImpl();
		resolved.setFamily(fontProperties.getFamily());
		resolved.setSize(new CssDimension(height.getAsInt(), CssUnit.PT));
		resolved.setSizeFromCSS(true);
		resolved.setSizeAdjust(fontProperties.getSizeAdjust());
		resolved.setWeight(fontProperties.getWeight());
		resolved.setStyle(fontProperties.getStyle());
		resolved.setVariant(fontProperties.getVariant());
		resolved.setStretch(fontProperties.getStretch());
		return resolved;
	}

	private static boolean isRelativeSize(CssPrimitive size) {
		if (size instanceof CssNumeric numeric) {
			return numeric.unit() == CssUnit.EM || numeric.unit() == CssUnit.PERCENT;
		}
		return size instanceof CssText text
				&& ("larger".equalsIgnoreCase(text.value()) || "smaller".equalsIgnoreCase(text.value()));
	}

	public static boolean hasFontDefinitionAsFamily(CSSValue value) {
		if (value instanceof CSS2FontProperties props) {
			return props.getFamily() instanceof CssText family
					&& family.value().startsWith(FONT_DEFINITION_MARKER);
		}
		return false;
	}

	private static FontData[] findFontDataByDefinition(CssText cssFontFamily) {
		IColorAndFontProvider provider = ColorAndFontUtil.getColorAndFontProvider();
		FontData[] result = new FontData[0];
		if (provider != null) {
			FontData[] fontData = provider.getFont(normalizeId(cssFontFamily.value().substring(1)));
			if (fontData != null) {
				result = fontData;
			}
		}
		return result;
	}

	/**
	 * Return SWT style Font from {@link CSS2FontProperties}.
	 */
	public static int getSWTStyle(CSS2FontProperties fontProperties,
			FontData fontData) {

		int fontStyle = SWT.NONE;
		if (fontData != null) {
			fontStyle = fontData.getStyle();
		}

		// CSS2 font-style
		CssPrimitive cssFontStyle = fontProperties.getStyle();
		if (cssFontStyle instanceof CssText styleText) {
			String style = styleText.value();
			if ("italic".equals(style)) {
				fontStyle = fontStyle | SWT.ITALIC;
			} else if (fontStyle == (fontStyle | SWT.ITALIC)) {
				fontStyle = fontStyle ^ SWT.ITALIC;
			}
		}
		// CSS font-weight
		CssPrimitive cssFontWeight = fontProperties.getWeight();
		if (cssFontWeight instanceof CssText weightText) {
			String weight = weightText.value();
			if ("bold".equals(weight.toLowerCase())) {
				fontStyle = fontStyle | SWT.BOLD;
			} else if (fontStyle == (fontStyle | SWT.BOLD)) {
				fontStyle = fontStyle ^ SWT.BOLD;
			}
		}
		return fontStyle;
	}

	/**
	 * Return CSS Value font-family from the widget's font, if it has a font
	 */
	public static String getFontFamily(Widget widget) {
		return getFontFamily(getFont(widget));
	}

	/**
	 * Return CSS Value font-family from SWT Font
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
	 */
	public static String getFontSize(Widget widget) {
		return getFontSize(getFont(widget));
	}

	/**
	 * Return CSS Value font-size from SWT Font
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
	 */
	public static String getFontStyle(Widget widget) {
		return getFontStyle(getFont(widget));
	}

	/**
	 * Return CSS Value font-style from SWT Font
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
	 */
	public static String getFontWeight(Widget widget) {
		return getFontWeight(getFont(widget));
	}

	/**
	 * Return CSS Value font-weight from Control Font
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
	 */
	public static String getFontComposite(Control control) {
		return getFontComposite(control.getFont());
	}

	/**
	 * Return CSS Value font-family from SWT Font
	 */
	public static String getFontComposite(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontComposite(fontData);
	}

	public static String getFontComposite(FontData fontData) {
		if (fontData != null) {
			StringBuilder composite = new StringBuilder();
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
	 */
	public static FontData getFirstFontData(Control control) {
		Font font = control.getFont();
		if (font == null || font.isDisposed()) {
			return null;
		}
		return getFirstFontData(font);
	}

	/**
	 *
	 * Return first FontData from SWT Font.
	 */
	public static FontData getFirstFontData(Font font) {
		FontData[] fontDatas = !font.isDisposed() ? font.getFontData() : null;
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
