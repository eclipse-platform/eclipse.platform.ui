package org.eclipse.jface.preference;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.*;

import java.util.Arrays;
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
	public static final FontData[] FONTDATA_ARRAY_DEFAULT_DEFAULT;
	static {
		Shell shell= new Shell();
		FONTDATA_ARRAY_DEFAULT_DEFAULT = shell.getFont().getFontData();
		shell.dispose();
	}
	
	private static final String ENTRY_SEPARATOR = ";"; //$NON-NLS-1$

	/**
	 * The default-default value for <code>FontData</code> preferences.
	 * This is left in for compatibility purposes. It is recommended that
	 * FONTDATA_ARRAY_DEFAULT_DEFAULT is actually used. 
	 */
	public static final FontData FONTDATA_DEFAULT_DEFAULT;
	static {
		FONTDATA_DEFAULT_DEFAULT= FONTDATA_ARRAY_DEFAULT_DEFAULT[0];
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

	if (value.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT))
		return COLOR_DEFAULT_DEFAULT;

	RGB color = StringConverter.asRGB(value, null);
	if (color == null)
		return COLOR_DEFAULT_DEFAULT;
	return color;
}
/**
 * Helper method to construct a <code>FontData</code> from the given string.
 * String is in the form FontData;FontData; in order that
 * multiple FontDatas can be defined.
 */
private static FontData[] basicGetFontData(String value) {
	if (value.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT))
		return FONTDATA_ARRAY_DEFAULT_DEFAULT;

	//Read in all of them to get the value
	StringTokenizer tokenizer = 
		new StringTokenizer(value,ENTRY_SEPARATOR);
	int numTokens = tokenizer.countTokens();
	FontData[] fontData = new FontData[numTokens];
	
	for(int i = 0; i < numTokens; i ++){
		try{
			fontData[i] = new FontData(tokenizer.nextToken());
		} catch (SWTException error) {
			return FONTDATA_ARRAY_DEFAULT_DEFAULT;
		} catch (IllegalArgumentException error) {
			return FONTDATA_ARRAY_DEFAULT_DEFAULT;
		}
	}
	return fontData;
}
/**
 * Helper method to construct a point from the given string.
 */
private static Point basicGetPoint(String value) {
	Point dp = new Point(POINT_DEFAULT_DEFAULT.x, POINT_DEFAULT_DEFAULT.y);
	if (value.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT))
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

	if (value.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT))
		return dr;
	return StringConverter.asRectangle(value, dr);
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
 * Returns the default value array for the font-valued preference
 * with the given name in the given preference store.
 * Returns the default-default value (<code>FONTDATA_ARRAY_DEFAULT_DEFAULT</code>) 
 * is no default preference with the given name, or if the default 
 * value cannot be treated as font data.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the default value of the preference
 */
public static FontData[] getDefaultFontDataArray(IPreferenceStore store, String name) {
	return basicGetFontData(store.getDefaultString(name));
}

/**
 * Returns a single default value for the font-valued preference
 * with the given name in the given preference store.
 * Returns the default-default value (<code>FONTDATA_DEFAULT_DEFAULT</code>) 
 * is no default preference with the given name, or if the default 
 * value cannot be treated as font data.
 * This method is provided for backwards compatibility. It is
 * recommended that <code>getDefaultFontDataArray</code> is
 * used instead.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the default value of the preference
 */
public static FontData getDefaultFontData(IPreferenceStore store, String name) {
	return getDefaultFontDataArray(store,name)[0];
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
 * Returns the default-default value (<code>FONTDATA_ARRAY_DEFAULT_DEFAULT</code>) 
 * if there is no preference with the given name, or if the current value 
 * cannot be treated as font data.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the font-valued preference
 */
public static FontData[] getFontDataArray(IPreferenceStore store, String name) {
	return basicGetFontData(store.getString(name));
}

/**
 * Returns the current value of the first entry of the
 * font-valued preference with the
 * given name in the given preference store.
 * Returns the default-default value (<code>FONTDATA_ARRAY_DEFAULT_DEFAULT</code>) 
 * if there is no preference with the given name, or if the current value 
 * cannot be treated as font data.
 * This API is provided for backwards compatibility. It is
 * recommended that <code>getFontDataArray</code> is used instead.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @return the font-valued preference
 */
public static FontData getFontData(IPreferenceStore store, String name) {
	return getFontDataArray(store,name)[0];
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
 * in the given preference store. As FontDatas are stored as 
 * arrays this method is only provided for backwards compatibility.
 * Use <code>setDefault(IPreferenceStore, String, FontData[])</code>
 * instead.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new default value of the preference
 */
public static void setDefault(IPreferenceStore store, String name, FontData value) {
	FontData[] fontDatas = new FontData[1];
	fontDatas[0] = value;
	setDefault(store,name, fontDatas);
}

/**
 * Sets the default value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new default value of the preference
 */
public static void setDefault(IPreferenceStore store, String name, FontData[] value) {
	store.setDefault(name, getStoredRepresentation(value));
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
 * in the given preference store. Included for backwards compatibility -
 * use <code>setValue(IPreferenceStore,String,FontData[])</code> instead.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new current value of the preference
 */
public static void setValue(IPreferenceStore store, String name, FontData value) {
	FontData[] data = new FontData[1];
	data[0] = value;
	setValue(store,name,data);
}


/**
 * Sets the current value of the preference with the given name
 * in the given preference store.
 *
 * @param store the preference store
 * @param name the name of the preference
 * @param value the new current value of the preference
 */
public static void setValue(IPreferenceStore store, String name, FontData[] value) {
	FontData[] oldValue = getFontDataArray(store, name);
	// see if the font has changed
	if(!Arrays.equals(oldValue, value)){
		store.putValue(name, getStoredRepresentation(value));
		JFaceResources.getFontRegistry().put(name,value);
		store.firePropertyChangeEvent(name, oldValue, value);
	}
}

/**
 * Return the stored representation of the FontData array.
 * FontDatas are stored in the form FontData;FontData;
 * Only include the non-null entries,
 * @return String - the String that will be stored
 * @param FontData[] - the FontDatas
 */

private static String getStoredRepresentation(FontData[] fontData){
	StringBuffer buffer = new StringBuffer();
	for(int i = 0; i < fontData.length; i++){
		if(fontData[i] != null){
			buffer.append(fontData[i].toString());
			buffer.append(ENTRY_SEPARATOR);
		}
	}
	return buffer.toString();
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
		JFaceColors.clearColor(name);
	}
}
}
