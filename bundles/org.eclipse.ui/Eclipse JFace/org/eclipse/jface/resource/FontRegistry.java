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
 * Creates a new font with the given font datas.
 */
private Font createFont(FontData[] fonts) {
	Display display = Display.getCurrent();
	for (int i = 0; i < fonts.length; i++) {
		FontData fd = fonts[i];
		if (fd == null)
			break;

		fd.setName(fd.getName().toLowerCase());

		FontData[] fixedFonts = display.getFontList(fd.getName(), false);
		if (isFixedFont(fixedFonts, fd)) {
			return new Font(display, fd);
		}

		FontData[] scalableFonts = display.getFontList(fd.getName(), true);
		if (scalableFonts.length > 0) {
			return new Font(display, fd);
		}
	}
	//unable to find a valid font.
	if (fonts.length > 0) {
		return new Font(display, fonts[0]);
	} else {
		//Failed to find any reasonable font. 
		return null;
	}
}
/**
 * Returns the default font.  Creates it if necessary.
 */
private Font defaultFont() {
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
		return defaultFont().getFontData();
	
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
	Font font = createFont((FontData[])result);
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

	if (stringToFont == null)
		return;
		
	for (Iterator e = stringToFont.values().iterator(); e.hasNext();) {
		Object next = e.next();
		if (next instanceof Font) {
			((Font) next).dispose();
		}
	}
	
	stringToFont = null;
	listeners = null;
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
		if (fixed.getHeight() == height && fixed.getName().toLowerCase().equals(name))
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
	};
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
 */
public void put(String symbolicName, FontData[] fontData) {

	Assert.isNotNull(symbolicName);
	Assert.isNotNull(fontData);
		
	FontData[] existing = (FontData []) stringToFontData.get(symbolicName);
	if(Arrays.equals(existing,fontData))
		return;
		
	Font oldFont = (Font)stringToFont.remove(symbolicName);
	stringToFontData.put(symbolicName, fontData);
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
private void readResourceBundle(ResourceBundle bundle,String bundleName) throws MissingResourceException {}
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
