/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.css2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterColorConfig;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;

/**
 * CSS2 Color Helper.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public class CSS2ColorHelper {

	/**
	 * Map with key=color name and value=hexadecimal color.
	 */
	private static Map<String, String> colorNamesMap = new HashMap<String, String>();

	/**
	 * Map with key=hexadecimal color and value=color name.
	 */
	private static Map<String, String> colorHexasMap = new HashMap<String, String>();

	/**
	 * Return w3c {@link RGBColor} from string value. Format String value is
	 * hexadecimal like #FFFFFF or color name like white.
	 *
	 * @param value
	 * @return
	 */
	public static RGBColor getRGBColor(String value) {
		if (value.startsWith("#") && value.length() == 7) {
			// Color is like #FFFFFF
			try {
				int redValue = Integer.decode("0x" + value.substring(1, 3)).intValue();
				int greenValue = Integer.decode("0x" + value.substring(3, 5)).intValue();
				int blueValue = Integer.decode("0x" + value.substring(5)).intValue();
				return new CSS2RGBColorImpl(redValue, greenValue, blueValue);
			} catch (Exception e) {
				return null;
			}
		}
		// Search if it's color name
		value = value.toLowerCase();
		value = colorNamesMap.get(value);
		if (value != null) {
			return getRGBColor(value);
		}
		return null;
	}

	/**
	 * Return color string form w3c <code>rgbColor</code> instance. The format
	 * (Hexa, color name or rgb format) of the color string is managed with
	 * <code>config</code> {@link ICSSValueConverterConfig}.
	 *
	 * @param rgbColor
	 * @param config
	 * @return
	 */
	public static String getColorStringValue(RGBColor rgbColor,
			ICSSValueConverterConfig config) {
		if (config instanceof ICSSValueConverterColorConfig) {
			ICSSValueConverterColorConfig colorConfig = (ICSSValueConverterColorConfig) config;
			switch (colorConfig.getFormat()) {
			case ICSSValueConverterColorConfig.COLOR_HEXA_FORMAT:
				return getHexaColorStringValue(rgbColor);
			case ICSSValueConverterColorConfig.COLOR_RGB_FORMAT:
				return getRGBColorStringValue(rgbColor);
			case ICSSValueConverterColorConfig.COLOR_NAME_FORMAT:
				// Get the Hexa color string value
				String hexaColor = getHexaColorStringValue(rgbColor);
				if (hexaColor != null) {
					// Search into hexa map the color name
					String colorName = getColorNameFromHexaColor(hexaColor);
					if (colorName != null) {
						return colorName;
					}
					// Color name is not found, return the Hexa value
					return hexaColor;

				}
				return getRGBColorStringValue(rgbColor);
			}

		}
		return getHexaColorStringValue(rgbColor);
	}

	/**
	 * Return rgb (ex : rgb(0,0,0)) color string value from w3c
	 * <code>rgbColor</code> instance.
	 *
	 * @param rgbColor
	 * @return
	 */
	public static String getRGBColorStringValue(RGBColor rgbColor) {
		String result = "rgb(";
		int red = (int) rgbColor.getRed().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER);
		result += red;
		result += ",";
		int green = (int) rgbColor.getGreen().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER);
		result += green;
		result += ",";
		int blue = (int) rgbColor.getBlue().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER);
		result += blue;
		result += ")";
		return result;
	}

	/**
	 * Return hexadecimal (ex : #FFFFFF) color string value from w3c
	 * <code>rgbColor</code> instance.
	 *
	 * @param rgbColor
	 * @return
	 */
	public static String getHexaColorStringValue(RGBColor rgbColor) {
		String result = "#";
		int red = (int) rgbColor.getRed().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER);
		if (red < 16) {
			result += "0";
		}
		result += Integer.toHexString(red);
		int green = (int) rgbColor.getGreen().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER);
		if (green < 16) {
			result += "0";
		}
		result += Integer.toHexString(green);
		int blue = (int) rgbColor.getBlue().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER);
		if (blue < 16) {
			result += "0";
		}
		result += Integer.toHexString(blue);
		return result;
	}

	/**
	 * Return the Hexa color (ex : #FFFFFF) from color name (ex : white).
	 *
	 * @param colorName
	 * @return
	 */
	public static String getHexaColorFromColorName(String colorName) {
		return colorNamesMap.get(colorName);
	}

	/**
	 * Return true if <code>value</code> is color name (ex : white) and false
	 * otherwise.
	 *
	 * @param value
	 * @return
	 */
	public static boolean isColorName(String value) {
		return (colorNamesMap.get(value) != null);
	}

	/**
	 * Return the color name (ex : white) from Hexa color (ex : #FFFFFF).
	 *
	 * @param hexaColor
	 * @return
	 */
	public static String getColorNameFromHexaColor(String hexaColor) {
		hexaColor = hexaColor.toUpperCase();
		return colorHexasMap.get(hexaColor);
	}

	static {
		colorNamesMap.put("aliceBlue", "#F0F8FF");
		colorNamesMap.put("aqua", "#00FFFF");
		colorNamesMap.put("aquamarine", "#7FFFD4");
		colorNamesMap.put("azure", "#F0FFFF");
		colorNamesMap.put("beige", "#F5F5DC");
		colorNamesMap.put("bisque", "#FFE4C4");
		colorNamesMap.put("black", "#000000");
		colorNamesMap.put("blanchedalmond", "#FFEBCD");
		colorNamesMap.put("blue", "#0000FF");
		colorNamesMap.put("blueviolet", "#8A2BE2");
		colorNamesMap.put("brown", "#A52A2A");
		colorNamesMap.put("burlywood", "#DEB887");
		colorNamesMap.put("cadetblue", "#5F9EA0");
		colorNamesMap.put("chartreuse", "#7FFF00");
		colorNamesMap.put("chocolate", "#D2691E");
		colorNamesMap.put("coral", "#FF7F50");
		colorNamesMap.put("cornflowerblue", "#6495ED");
		colorNamesMap.put("cornsilk", "#FFF8DC");
		colorNamesMap.put("crimson", "#DC143C");
		colorNamesMap.put("cyan", "#00FFFF");
		colorNamesMap.put("darkblue", "#00008B");
		colorNamesMap.put("darkcyan", "#008B8B");
		colorNamesMap.put("darkgoldenrod", "#B8860B");
		colorNamesMap.put("darkgray", "#A9A9A9");
		colorNamesMap.put("darkgrey", "#A9A9A9");
		colorNamesMap.put("darkgreen", "#006400");
		colorNamesMap.put("darkkhaki", "#BDB76B");
		colorNamesMap.put("darkmagenta", "#8B008B");
		colorNamesMap.put("darkolivegreen", "#556B2F");
		colorNamesMap.put("darkorange", "#FF8C00");
		colorNamesMap.put("darkorchid", "#9932CC");
		colorNamesMap.put("darkred", "#8B0000");
		colorNamesMap.put("darksalmon", "#E9967A");
		colorNamesMap.put("darkseagreen", "#8FBC8F");
		colorNamesMap.put("darkslateblue", "#483D8B");
		colorNamesMap.put("darkslategray", "#2F4F4F");
		colorNamesMap.put("darkslategrey", "#2F4F4F");
		colorNamesMap.put("darkturquoise", "#00CED1");
		colorNamesMap.put("darkviolet", "#9400D3");
		colorNamesMap.put("deeppink", "#FF1493");
		colorNamesMap.put("deepskyblue", "#00BFFF");
		colorNamesMap.put("dimgray", "#696969");
		colorNamesMap.put("dimgrey", "#696969");
		colorNamesMap.put("dodgerblue", "#1E90FF");
		colorNamesMap.put("firebrick", "#B22222");
		colorNamesMap.put("floralwhite", "#FFFAF0");
		colorNamesMap.put("forestgreen", "#228B22");
		colorNamesMap.put("fuchsia", "#FF00FF");
		colorNamesMap.put("gainsboro", "#DCDCDC");
		colorNamesMap.put("ghostwhite", "#F8F8FF");
		colorNamesMap.put("gold", "#FFD700");
		colorNamesMap.put("goldenrod", "#DAA520");
		colorNamesMap.put("gray", "#808080");
		colorNamesMap.put("grey", "#808080");
		colorNamesMap.put("green", "#008000");
		colorNamesMap.put("greenyellow", "#ADFF2F");
		colorNamesMap.put("honeydew", "#F0FFF0");
		colorNamesMap.put("hotpink", "#FF69B4");
		colorNamesMap.put("indianred", "#CD5C5C");
		colorNamesMap.put("indigo", "#4B0082");
		colorNamesMap.put("ivory", "#FFFFF0");
		colorNamesMap.put("khaki", "#F0E68C");
		colorNamesMap.put("lavender", "#E6E6FA");
		colorNamesMap.put("lavenderblush", "#FFF0F5");
		colorNamesMap.put("lawngreen", "#7CFC00");
		colorNamesMap.put("lemonchiffon", "#FFFACD");
		colorNamesMap.put("lightblue", "#ADD8E6");
		colorNamesMap.put("lightcoral", "#F08080");
		colorNamesMap.put("lightcyan", "#E0FFFF");
		colorNamesMap.put("lightgoldenrodyellow", "#FAFAD2");
		colorNamesMap.put("lightgray", "#D3D3D3");
		colorNamesMap.put("lightgrey", "#D3D3D3");
		colorNamesMap.put("lightgreen", "#90EE90");
		colorNamesMap.put("lightpink", "#FFB6C1");
		colorNamesMap.put("lightsalmon", "#FFA07A");
		colorNamesMap.put("lightseagreen", "#20B2AA");
		colorNamesMap.put("lightskyblue", "#87CEFA");
		colorNamesMap.put("lightslategray", "#778899");
		colorNamesMap.put("lightslategrey", "#778899");
		colorNamesMap.put("lightsteelblue", "#B0C4DE");
		colorNamesMap.put("lightyellow", "#FFFFE0");
		colorNamesMap.put("lime", "#00FF00");
		colorNamesMap.put("limegreen", "#32CD32");
		colorNamesMap.put("linen", "#FAF0E6");
		colorNamesMap.put("magenta", "#FF00FF");
		colorNamesMap.put("maroon", "#800000");
		colorNamesMap.put("mediumaquamarine", "#66CDAA");
		colorNamesMap.put("mediumblue", "#0000CD");
		colorNamesMap.put("mediumorchid", "#BA55D3");
		colorNamesMap.put("mediumpurple", "#9370D8");
		colorNamesMap.put("mediumseagreen", "#3CB371");
		colorNamesMap.put("mediumslateblue", "#7B68EE");
		colorNamesMap.put("mediumspringgreen", "#00FA9A");
		colorNamesMap.put("mediumturquoise", "#48D1CC");
		colorNamesMap.put("mediumvioletred", "#C71585");
		colorNamesMap.put("midnightblue", "#191970");
		colorNamesMap.put("mintcream", "#F5FFFA");
		colorNamesMap.put("mistyrose", "#FFE4E1");
		colorNamesMap.put("moccasin", "#FFE4B5");
		colorNamesMap.put("navajowhite", "#FFDEAD");
		colorNamesMap.put("navy", "#000080");
		colorNamesMap.put("oldlace", "#FDF5E6");
		colorNamesMap.put("olive", "#808000");
		colorNamesMap.put("olivedrab", "#6B8E23");
		colorNamesMap.put("orange", "#FFA500");
		colorNamesMap.put("orangered", "#FF4500");
		colorNamesMap.put("orchid", "#DA70D6");
		colorNamesMap.put("palegoldenrod", "#EEE8AA");
		colorNamesMap.put("palegreen", "#98FB98");
		colorNamesMap.put("paleturquoise", "#AFEEEE");
		colorNamesMap.put("palevioletred", "#D87093");
		colorNamesMap.put("papayawhip", "#FFEFD5");
		colorNamesMap.put("peachpuff", "#FFDAB9");
		colorNamesMap.put("peru", "#CD853F");
		colorNamesMap.put("pink", "#FFC0CB");
		colorNamesMap.put("plum", "#DDA0DD");
		colorNamesMap.put("powderblue", "#B0E0E6");
		colorNamesMap.put("purple", "#800080");
		colorNamesMap.put("red", "#FF0000");
		colorNamesMap.put("rosybrown", "#BC8F8F");
		colorNamesMap.put("royalblue", "#4169E1");
		colorNamesMap.put("saddlebrown", "#8B4513");
		colorNamesMap.put("salmon", "#FA8072");
		colorNamesMap.put("sandybrown", "#F4A460");
		colorNamesMap.put("seagreen", "#2E8B57");
		colorNamesMap.put("seashell", "#FFF5EE");
		colorNamesMap.put("sienna", "#A0522D");
		colorNamesMap.put("silver", "#C0C0C0");
		colorNamesMap.put("skyblue", "#87CEEB");
		colorNamesMap.put("slateblue", "#6A5ACD");
		colorNamesMap.put("slategray", "#708090");
		colorNamesMap.put("slategrey", "#708090");
		colorNamesMap.put("snow", "#FFFAFA");
		colorNamesMap.put("springgreen", "#00FF7F");
		colorNamesMap.put("steelblue", "#4682B4");
		colorNamesMap.put("tan", "#D2B48C");
		colorNamesMap.put("teal", "#008080");
		colorNamesMap.put("thistle", "#D8BFD8");
		colorNamesMap.put("tomato", "#FF6347");
		colorNamesMap.put("turquoise", "#40E0D0");
		colorNamesMap.put("violet", "#EE82EE");
		colorNamesMap.put("wheat", "#F5DEB3");
		colorNamesMap.put("white", "#FFFFFF");
		colorNamesMap.put("whitesmoke", "#F5F5F5");
		colorNamesMap.put("yellow", "#FFFF00");
		colorNamesMap.put("yellowgreen", "#9ACD32");

		// Build Map with key=hexadecimal color and value=color name
		for (Entry<String, String> entry : colorNamesMap.entrySet()) {
			colorHexasMap.put(entry.getValue(), entry.getKey());
		}
	}
}
