package org.eclipse.jface.preference;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.*;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * A utility class for dealing with preferences whose values are
 * common SWT objects (color, points, rectangles, and font data).
 * The static methods on this class handle the conversion between
 * the SWT objects and their string representations.
 * <p>
 * Usage:
 * <pre>
 * IPreferenceStore store = ...;
 * PreferenceConverter.setValue(store, "bg", new RGB(127,127,127));
 * ...
 * RBG bgColor = PreferenceConverter.getValue(store, "bg");
 * </pre>
 * </p>
 * <p>
 * This class contains static methods and fields only and cannot 
 * be instantiated.
 * </p>
 */
public class PreferenceConverter {
	
	/**
	 * The default-default value for point preferences
	 * (the origin, <code>(0,0)</code>).
	 */
	public static final Point POINT_DEFAULT_DEFAULT= new Point(0, 0);
	/**
	 * The default-default value for rectangle preferences
	 * (the empty rectangle <code>(0,0,0,0)</code>).
	 */
	public static final Rectangle RECTANGLE_DEFAULT_DEFAULT= 
		new Rectangle(0, 0, 0, 0); 
	/**
	 * The default-default value for color preferences 
	 * (black, <code>RGB(0,0,0)</code>).
	 */
	public static final RGB COLOR_DEFAULT_DEFAULT= new RGB(0, 0, 0);

	/**
	 * The default-default value for <code>FontData</code> preferences.
	 */
	public static final FontData FONTDATA_DEFAULT_DEFAULT;
	/**
	 * The key suffix for font preference.
	 */
	private static String LOCALE_SUFFIX;	
	static {
		Shell shell= new Shell();
		FONTDATA_DEFAULT_DEFAULT= shell.getFont().getFontData()[0];
		shell.dispose();
		LOCALE_SUFFIX = System.getProperty("os.name").trim();//$NON-NLS-1$
		LOCALE_SUFFIX = StringConverter.removeWhiteSpaces(LOCALE_SUFFIX).toLowerCase();
		LOCALE_SUFFIX = "_" + LOCALE_SUFFIX + "_" + Locale.getDefault().toString();
	}
/* (non-Javadoc)
 * private constructor to prevent instantiation.
 */
private PreferenceConverter() {
}
/**
 * Helper method to construct a color from the given string.
 */
private static RGB basicGetColor(String value) {

	if (value == IPreferenceStore.STRING_DEFAULT_DEFAULT)
		return COLOR_DEFAULT_DEFAULT;

	RGB color = StringConverter.asRGB(value, null);
	if (color == null)
		return COLOR_DEFAULT_DEFAULT;
	return color;
}
/**
 * Helper method to construct a <code>FontData</code> from the given string.
 */
private static FontData basicGetFontData(String value) {
	if (value == IPreferenceStore.STRING_DEFAULT_DEFAULT)
		return FONTDATA_DEFAULT_DEFAULT;

	FontData fontData = FONTDATA_DEFAULT_DEFAULT;
	try {
		fontData = new FontData(value);
	} catch (SWTException error) {
	} catch (IllegalArgumentException error) {
	}
	return fontData;
}
/**
 * Helper method to construct a point from the given string.
 */
private static Point basicGetPoint(String value) {
	Point dp = new Point(POINT_DEFAULT_DEFAULT.x, POINT_DEFAULT_DEFAULT.y);
	if (value == IPreferenceStore.STRING_DEFAULT_DEFAULT)
		return dp;
	return StringConverter.asPoint(value, dp);
}
/**
 * Helper method to construct a rectangle from the given string.
 */
private static Rectangle basicGetRectangle(String value) {
	// We can't just return RECTANGLE_DEFAULT_DEFAULT because
	// a rectangle object doesn't have value semantik.
	Rectangle dr= 
		new Rectangle(
			RECTANGLE_DEFAULT_DEFAULT.x, 
			RECTANGLE_DEFAULT_DEFAULT.y, 
			RECTANGLE_DEFAULT_DEFAULT.width, 
			RECTANGLE_DEFAULT_DEFAULT.height); 

	if (value == IPreferenceStore.STRING_DEFAULT_DEFAULT)
		return dr;
	return StringConverter.asRectangle(value, dr);
}

/**
 * Return the symbolicName concatenated with platform name
 * and locale.
 */
public static String localizeFontName(String symbolicName) {
	return symbolicName + LOCALE_SUFFIX;
}
/**
 * Returns the current value of the color-valued preference with the
 * given name in the given preference store.
 * Returns the default-default value (<code>COLOR_DEFAULT_DEFAULT</code>) 
 * if there is no preference with the given name, or if the current value 
 * cannot be treated as a color.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the color-valued preference
 */
public static RGB getColor(IPreferenceStore store, String name) {
	return basicGetColor(store.getString(name));
}
/**
 * Returns the default value for the color-valued preference
 * with the given name in the given preference store.
 * Returns the default-default value (<code>COLOR_DEFAULT_DEFAULT</code>) 
 * is no default preference with the given name, or if the default 
 * value cannot be treated as a color.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the default value of the preference
 */
public static RGB getDefaultColor(IPreferenceStore store, String name) {
	return basicGetColor(store.getDefaultString(name));
}
/**
 * Returns the default value for the font-valued preference
 * with the given name in the given preference store.
 * Returns the default-default value (<code>FONTDATA_DEFAULT_DEFAULT</code>) 
 * is no default preference with the given name, or if the default 
 * value cannot be treated as font data.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the default value of the preference
 */
public static FontData getDefaultFontData(IPreferenceStore store, String name) {
	return basicGetFontData(store.getDefaultString(localizeFontName(name)));
}
/**
 * Returns the default value for the point-valued preference
 * with the given name in the given preference store.
 * Returns the default-default value (<code>POINT_DEFAULT_DEFAULT</code>) 
 * is no default preference with the given name, or if the default 
 * value cannot be treated as a point.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the default value of the preference
 */
public static Point getDefaultPoint(IPreferenceStore store, String name) {
	return basicGetPoint(store.getDefaultString(name));
}
/**
 * Returns the default value for the rectangle-valued preference
 * with the given name in the given preference store.
 * Returns the default-default value (<code>RECTANGLE_DEFAULT_DEFAULT</code>) 
 * is no default preference with the given name, or if the default 
 * value cannot be treated as a rectangle.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the default value of the preference
 */
public static Rectangle getDefaultRectangle(IPreferenceStore store, String name) {
	return basicGetRectangle(store.getDefaultString(name));
}
/**
 * Returns the current value of the font-valued preference with the
 * given name in the given preference store.
 * Returns the default-default value (<code>FONTDATA_DEFAULT_DEFAULT</code>) 
 * if there is no preference with the given name, or if the current value 
 * cannot be treated as font data.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the font-valued preference
 */
public static FontData getFontData(IPreferenceStore store, String name) {
	return basicGetFontData(store.getString(localizeFontName(name)));
}
/**
 * Returns the current value of the point-valued preference with the
 * given name in the given preference store.
 * Returns the default-default value (<code>POINT_DEFAULT_DEFAULT</code>) 
 * if there is no preference with the given name, or if the current value 
 * cannot be treated as a point.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the point-valued preference
 */
public static Point getPoint(IPreferenceStore store, String name) {
	return basicGetPoint(store.getString(name));
}
/**
 * Returns the current value of the rectangle-valued preference with the
 * given name in the given preference store.
 * Returns the default-default value (<code>RECTANGLE_DEFAULT_DEFAULT</code>) 
 * if there is no preference with the given name, or if the current value 
 * cannot be treated as a rectangle.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the rectangle-valued preference
 */
public static Rectangle getRectangle(IPreferenceStore store, String name) {
	return basicGetRectangle(store.getString(name));
}
/**
 * Sets the default value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new defaul value of the preference
 */
public static void setDefault(IPreferenceStore store, String name, FontData value) {
	store.setDefault(localizeFontName(name), value.toString());
}
/**
 * Sets the default value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new defaul value of the preference
 */
public static void setDefault(IPreferenceStore store, String name, Point value) {
	store.setDefault(name, StringConverter.asString(value));
}
/**
 * Sets the default value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new defaul value of the preference
 */
public static void setDefault(IPreferenceStore store, String name, Rectangle value) {
	store.setDefault(name, StringConverter.asString(value));
}
/**
 * Sets the default value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new defaul value of the preference
 */
public static void setDefault(IPreferenceStore store, String name, RGB value) {
	store.setDefault(name, StringConverter.asString(value));
}
/**
 * Sets the current value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new current value of the preference
 */
public static void setValue(IPreferenceStore store, String name, FontData value) {
	FontData oldValue= getFontData(store, name);
	if (oldValue == null || !(oldValue.equals(value))) {
		store.putValue(localizeFontName(name), value.toString());
		store.firePropertyChangeEvent(name, oldValue, value);
	}
}
/**
 * Sets the current value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new current value of the preference
 */
public static void setValue(IPreferenceStore store, String name, Point value) {
	Point oldValue= getPoint(store, name);
	if (oldValue == null || !oldValue.equals(value)) {
		store.putValue(name, StringConverter.asString(value));
		store.firePropertyChangeEvent(name, oldValue, value);
	}
}
/**
 * Sets the current value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new current value of the preference
 */
public static void setValue(IPreferenceStore store, String name, Rectangle value) {
	Rectangle oldValue= getRectangle(store, name);
	if (oldValue == null || !oldValue.equals(value)) {
		store.putValue(name, StringConverter.asString(value));
		store.firePropertyChangeEvent(name, oldValue, value);
	}
}
/**
 * Sets the current value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new current value of the preference
 */
public static void setValue(IPreferenceStore store, String name, RGB value) {
	RGB oldValue= getColor(store, name);
	if (oldValue == null || !oldValue.equals(value)) {
		store.putValue(name, StringConverter.asString(value));
		store.firePropertyChangeEvent(name, oldValue, value);
	}
}
}
