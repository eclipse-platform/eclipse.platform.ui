package org.eclipse.jface.resource;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;
import java.util.List;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.*;

/**
 * A font registry maintains a mapping between symbolic font names 
 * and SWT fonts.
 * <p>
 * A font registry owns all of the font objects registered
 * with it, and automatically disposes of them when the SWT Display
 * that creates the fonts is disposed. Because of this, clients do 
 * not need to (indeed, must not attempt to) dispose of font 
 * objects themselves.
 * </p>
 * <p>
 * A special constructor is provided for populating a font registry
 * from a property files using the standard Java resource bundle mechanism.
 * </p>
 * <p>
 * Methods are provided for registering listeners that will be kept
 * apprised of changes to list of registed fonts.
 * </p>
 * <p>
 * Clients may instantiate this class (it was not designed to be subclassed).
 * </p>
 */
public class FontRegistry {

	/**
	 * List of property change listeners
	 * (element type: <code>org.eclipse.jface.util.IPropertyChangeListener</code>).
	 */
	private ListenerList listeners = new ListenerList();
	
	/**
	 * Table of known fonts, keyed by symbolic font name
	 * (key type: <code>String</code>, 
	 *  value type: <code>org.eclipse.swt.graphics.Font</code>.
	 */
	private Map stringToFont = new HashMap(7);
	/**
	 * Table of known font data, keyed by symbolic font name
	 * (key type: <code>String</code>, 
	 *  value type: <code>org.eclipse.swt.graphics.FontData[]</code>).
	 */
	private Map stringToFontData = new HashMap(7);
/**
 * Creates an empty font registry.
 * <p>
 * There must be an SWT Display created in the current 
 * thread before calling this method.
 * </p>
 */
public FontRegistry() {
	Display display = Display.getCurrent();
	Assert.isNotNull(display);
	hookDisplayDispose(display);
}
/**
 * Creates a font registry and initializes its content from
 * a property file.
 * <p>
 * There must be an SWT Display created in the current 
 * thread before calling this method.
 * </p>
 * <p>
 * The OS name (retrieved using <code>System.getProperty("os.name")</code>)
 * is converted to lowercase, purged of whitespace, and appended 
 * as suffix (separated by an underscore <code>'_'</code>) to the given 
 * location string to yield the base name of a resource bundle
 * acceptable to <code>ResourceBundle.getBundle</code>.
 * The standard Java resource bundle mechanism is then used to locate
 * and open the appropriate properties file, taking into account
 * locale specific variations.
 * </p>
 * <p>
 * For example, on the Windows 2000 operating system the location string
 * <code>"com.example.myapp.Fonts"</code> yields the base name 
 * <code>"com.example.myapp.Fonts_windows2000"</code>. For the US English locale,
 * this further elaborates to the resource bundle name
 * <code>"com.example.myapp.Fonts_windows2000_en_us"</code>.
 * </p>
 * <p>
 * If no appropriate OS-specific resource bundle is found, the
 * process is repeated using the location as the base bundle name.
 * </p>
 * <p>
 * The property file contains entries that look like this:
 * <pre>
 *	textfont.0=MS Sans Serif-regular-10
 *	textfont.1=Times New Roman-regular-10
 *	
 *	titlefont.0=MS Sans Serif-regular-12
 *	titlefont.1=Times New Roman-regular-12
 * </pre>
 * Each entry maps a symbolic font names (the font registry keys) with
 * a "<code>.<it>n</it></code> suffix to standard font names
 * on the right. The suffix indicated order of preference: 
 * "<code>.0</code>" indicates the first choice,
 * "<code>.1</code>" indicates the second choice, and so on.
 * </p>
 * The following example shows how to use the font registry:
 * <pre>
 *	FontRegistry registry = new FontRegistry("com.example.myapp.fonts");
 *  Font font = registry.get("textfont");
 *  control.setFont(font);
 *  ...
 * </pre>
 *
 * @param location the name of the resource bundle
 * @exception MissingResourceException if the resource bundle cannot be found
 */
public FontRegistry(String location) throws MissingResourceException {

	Display display = Display.getCurrent();
	Assert.isNotNull(display);
	String osname = System.getProperty("os.name").trim();//$NON-NLS-1$
	osname = StringConverter.removeWhiteSpaces(osname).toLowerCase();
	String OSLocation = location;
	ResourceBundle bundle = null;
	if (osname != null)
		OSLocation = location + "_" + osname;//$NON-NLS-1$

	try {
		bundle = ResourceBundle.getBundle(OSLocation);
		readResourceBundle(bundle, OSLocation);
	} catch (MissingResourceException e) {
		if (location != OSLocation) {
			bundle = ResourceBundle.getBundle(location);
			readResourceBundle(bundle, OSLocation);
		}
		else
			throw e;
	}
	hookDisplayDispose(display);
}
/**
 * Creates an empty font registry.
 *
 * @param display the Display
 */
public FontRegistry(Display display) {
	Assert.isNotNull(display);
	hookDisplayDispose(display);
}
/**
 * Adds a property change listener to this registry.
 *
 * @param listener a property change listener
 */
public void addListener(IPropertyChangeListener listener) {
	listeners.add(listener);
}

/**
 * Find the first valid fontData in the provided list. 
 * If none are valid return the first one regardless.
 * If the list is empty return null.
 * Return null if one cannot be found.
 */
public FontData bestData(FontData[] fonts, Display display) {
	for (int i = 0; i < fonts.length; i++) {
		FontData fd = fonts[i];
		
		if (fd == null)
			break;

		FontData[] fixedFonts = display.getFontList(fd.getName(), false);
		if (isFixedFont(fixedFonts, fd)) {
			return fd;
		}

		FontData[] scalableFonts = display.getFontList(fd.getName(), true);
		if (scalableFonts.length > 0) {
			return fd;
		}
	}
	
	//None of the provided datas are valid. Return the
	//first one as it is at least the first choice.
	if (fonts.length > 0)
			return fonts[0];
	else
		//Nothing specified 
		return null;
}
	
/**
 * Creates a new font with the given font datas or nulk
 * if there is no data.
 */
private Font createFont(String symbolicName, FontData[] fonts) {
	Display display = Display.getCurrent();
	FontData validData = bestData(fonts,display);
	if(validData == null){
		//Nothing specified 
		return null;
	}
	else {
		//Create a font data for updating the registry
		FontData[] newEntry = new FontData[1];
		newEntry[0] = validData;
		//Do not fire the update from creation as it is not a property change
		put(symbolicName,newEntry,false);
		return new Font(display, validData);
	}
}
/**
 * Returns the default font.  Creates it if necessary.
 */
Font defaultFont() {
	Display current = Display.getCurrent();
	if(current == null){
		Shell shell = new Shell();
		Font font = shell.getFont();
		FontData [] data = font.getFontData();
		shell.dispose();
		return font;
	}
	else
		return current.getSystemFont();
}
/**
 * Returns the default font data.  Creates it if necessary.
 */
private FontData[] defaultFontData() {
	return defaultFont().getFontData();
}
/**
 * Fires a PropertyChangeEvent.
 */
private void fireFontMappingChanged(String name) {
	Object[] listeners = this.listeners.getListeners();
	if (listeners.length > 0) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, name, null, null);
		for (int i = 0; i < listeners.length; ++i) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}
}
/**
 * Returns the font data associated with the given symbolic font name.
 * Returns the default font data if there is no special value associated
 * with that name.
 *
 * @param symbolicName symbolic font name
 * @return the font
 */
public FontData[] getFontData(String symbolicName) {

	Assert.isNotNull(symbolicName);	
	Object result = stringToFontData.get(symbolicName);
	if (result == null)
		return defaultFontData();
	
	return (FontData[])result;
}
/**
 * Returns the font associated with the given symbolic font name.
 * Returns the default font if there is no special value associated
 * with that name.
 *
 * @param symbolicName symbolic font name
 * @return the font
 */
public Font get(String symbolicName) {

	Assert.isNotNull(symbolicName);
	Object result = stringToFont.get(symbolicName);
	if (result != null)
		return (Font)result;
	
	result = stringToFontData.get(symbolicName);
	if (result == null)
		return defaultFont();

	// Create the font and update the mapping so it can 
	// be shared.
	Font font = createFont(symbolicName,(FontData[])result);
	stringToFont.put(symbolicName, font);

	// Note, the font may be null if the create() failed. Put a mapping
	// in for this font to prevent repeated attempts to allocate the
	// same font. 
	 
	if (font == null)
		return defaultFont();
	
	return font;
}
/**
 * Shut downs this resource registry and disposes of all registered fonts.
 */
private void handleDisplayDispose() {

	for (Iterator e = stringToFont.values().iterator(); e.hasNext();) {
		Object next = e.next();
		if (next instanceof Font) {
			((Font) next).dispose();
		}
	}
	stringToFont.clear();
	listeners.clear();
}
/**
 * Hook a dispose listener on the SWT display.
 */
private void hookDisplayDispose(Display display) {
	display.disposeExec(new Runnable() {
		public void run() {
			handleDisplayDispose();
		}	
	});
}
/**
 * Checks whether the given font is in the list of fixed fonts.
 */
private boolean isFixedFont(FontData[] fixedFonts, FontData fd) {
	// Can't use FontData.equals() since some values aren't
	// set if a fontdata isn't used.
	int height = fd.getHeight();
	String name = fd.getName();
	for (int i = 0; i < fixedFonts.length; i++) {
		FontData fixed = fixedFonts[i];
		if (fixed.getHeight() == height && fixed.getName().equals(name))
			return true;
	}
	return false;
}
/**
 * Converts a String into a FontData object.
 */
private FontData makeFontData(String value) throws MissingResourceException {
	try {
		return StringConverter.asFontData(value.trim());
	} catch (DataFormatException e) {
		throw new MissingResourceException("Wrong font data format. Value is: \"" + value + "\"", getClass().getName(), value);//$NON-NLS-2$//$NON-NLS-1$
	}
}

/**
 * Adds (or replaces) a font to this font registry under the given
 * symbolic name.
 * <p>
 * A property change event is reported whenever the mapping from
 * a symbolic name to a font changes. The source of the event is
 * this registry; the property name is the symbolic font name.
 * </p>
 *
 * @param symbolicName the symbolic font name
 * @param fontData an Array of FontData
 * @param update - fire a font mapping changed if true. False
 * 	if this method is called from the get method as no setting
 *  has changed.
 */
public void put(String symbolicName, FontData[] fontData) {
	put(symbolicName,fontData,true);
}

/**
 * Adds (or replaces) a font to this font registry under the given
 * symbolic name.
 * <p>
 * A property change event is reported whenever the mapping from
 * a symbolic name to a font changes. The source of the event is
 * this registry; the property name is the symbolic font name.
 * </p>
 *
 * @param symbolicName the symbolic font name
 * @param fontData an Array of FontData
 * @param update - fire a font mapping changed if true. False
 * 	if this method is called from the get method as no setting
 *  has changed.
 */
private void put(String symbolicName, FontData[] fontData, boolean update) {

	Assert.isNotNull(symbolicName);
	Assert.isNotNull(fontData);
		
	FontData[] existing = (FontData []) stringToFontData.get(symbolicName);
	if(Arrays.equals(existing,fontData))
		return;
		
	Font oldFont = (Font)stringToFont.remove(symbolicName);
	stringToFontData.put(symbolicName, fontData);
	if(update)
		fireFontMappingChanged(symbolicName);

	if (oldFont == defaultFont())
		return;

	if (oldFont != null )
		oldFont.dispose();
}
/**
 * Reads the resource bundle.  This puts FontData[] objects
 * in the mapping table.  These will lazily be turned into
 * real Font objects when requested.
 */
private void readResourceBundle(ResourceBundle bundle,String bundleName) throws MissingResourceException {
	Enumeration keys = bundle.getKeys();
	while (keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		int pos = key.lastIndexOf('.');
		if (pos == -1) {
			stringToFontData.put(key, new FontData[] {
	            makeFontData(bundle.getString(key))});
		} else {
			String name = key.substring(0, pos);
			int i = 0;
			try {
				i = Integer.parseInt(key.substring(pos + 1));
			} catch (NumberFormatException e) {
				//Panic the file can not be parsed.
				throw new MissingResourceException("Wrong key format ", bundleName, key);//$NON-NLS-1$
			}
			FontData[] elements = (FontData[]) stringToFontData.get(name);
			if (elements == null) {
				elements = new FontData[8];
				stringToFontData.put(name, elements);
			}
			if (i > elements.length) {
				FontData[] na = new FontData[i + 8];
				System.arraycopy(elements, 0, na, 0, elements.length);
				elements = na;
				stringToFontData.put(name, elements);
			}
			elements[i] = makeFontData(bundle.getString(key));
		}
	}
}
/**
 * Removes the given listener from this registry.
 * Has no affect if the listener is not registered.
 *
 * @param listener a property change listener
 */
public void removeListener(IPropertyChangeListener listener) {
	listeners.remove(listener);
}
}
