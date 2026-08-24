/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - bug 544026
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.Util;
import org.eclipse.pde.api.tools.annotations.NoExtend;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

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
 * apprised of changes to list of registered fonts.
 * </p>
 * <p>
 * Clients may instantiate this class (it was not designed to be subclassed).
 * </p>
 *
 * Since 3.0 this class extends ResourceRegistry.
 */
@NoExtend
public class FontRegistry extends ResourceRegistry {

	private enum FontStyle {
		NORMAL, BOLD, ITALIC
	}

	/**
	 * FontRecord is a private helper class that holds onto a font
	 * and can be used to generate its bold and italic version.
	 */
	private static class FontRecord {

		// volatile: a caller without a display of its own is handed the record of
		// the display owning it, so it reads these fields while that display's
		// thread may still be writing the lazily realized styles
		volatile Font baseFont;

		volatile Font boldFont;

		volatile Font italicFont;

		FontData[] baseData;

		/**
		 * Create a new instance of the receiver based on the
		 * plain font and the data for it.
		 * @param plainFont The base looked up font.
		 * @param data The data used to look it up.
		 */
		FontRecord(Font plainFont, FontData[] data) {
			baseFont = plainFont;
			baseData = data;
		}

		/**
		 * Dispose any of the fonts created for this record.
		 */
		void dispose() {
			baseFont.dispose();
			if (boldFont != null) {
				boldFont.dispose();
			}
			if (italicFont != null) {
				italicFont.dispose();
			}
		}

		/**
		 * Return the base Font.
		 * @return Font
		 */
		private Font getBaseFont() {
			return baseFont;
		}

		/**
		 * Return the font for the given style, creating it lazily if necessary.
		 * @param style the requested style
		 * @return the font
		 */
		Font get(FontStyle style) {
			return switch (style) {
				case NORMAL -> getBaseFont();
				case BOLD -> getBoldFont();
				case ITALIC -> getItalicFont();
			};
		}

		/**
		 * Return the bold Font. Create a bold version
		 * of the base font to get it.
		 * @return Font
		 */
		private Font getBoldFont() {
			if (boldFont != null) {
				return boldFont;
			}

			// only the display owning this record, or a caller without a display of
			// its own, ever reaches this point; either way the font is realized on
			// the display the record belongs to
			Assert.isTrue(Display.getCurrent() == null || Display.getCurrent() == baseFont.getDevice());
			FontData[] boldData = getModifiedFontData(SWT.BOLD);
			boldFont = new Font(baseFont.getDevice(), boldData);
			return boldFont;
		}

		/**
		 * Returns whether the given style has already been realized for this
		 * record.
		 * @param style the style to check
		 * @return whether the given style is already available
		 */
		boolean has(FontStyle style) {
			return switch (style) {
				case NORMAL -> baseFont != null;
				case BOLD -> boldFont != null;
				case ITALIC -> italicFont != null;
			};
		}

		/**
		 * Get a version of the base font data with the specified
		 * style.
		 * @param style the new style
		 * @return the font data with the style {@link FontData#FontData(String, int, int)}
		 * @see SWT#ITALIC
		 * @see SWT#NORMAL
		 * @see SWT#BOLD
		 * @todo Generated comment
		 */
		private FontData[] getModifiedFontData(int style) {
			FontData[] styleData = new FontData[baseData.length];
			for (int i = 0; i < styleData.length; i++) {
				FontData base = baseData[i];
				styleData[i] = new FontData(base.getName(), base.getHeight(),
						base.getStyle() | style);
			}

			return styleData;
		}

		/**
		 * Return the italic Font. Create an italic version of the
		 * base font to get it.
		 * @return Font
		 */
		private Font getItalicFont() {
			if (italicFont != null) {
				return italicFont;
			}

			// only the display owning this record, or a caller without a display of
			// its own, ever reaches this point; either way the font is realized on
			// the display the record belongs to
			Assert.isTrue(Display.getCurrent() == null || Display.getCurrent() == baseFont.getDevice());
			FontData[] italicData = getModifiedFontData(SWT.ITALIC);
			italicFont = new Font(baseFont.getDevice(), italicData);
			return italicFont;
		}

		/**
		 * Return all of the fonts allocated by the receiver, that is the base
		 * font and whichever styled variants have been realized so far.
		 * @return the allocated fonts, never <code>null</code>
		 */
		List<Font> getAllocatedFonts() {
			List<Font> allocatedFonts = new ArrayList<>(3);
			if (baseFont != null) {
				allocatedFonts.add(baseFont);
			}
			if (boldFont != null) {
				allocatedFonts.add(boldFont);
			}
			if (italicFont != null) {
				allocatedFonts.add(italicFont);
			}
			return allocatedFonts;
		}
	}

	private final Map<Display, DisplayFontRecords> displayToFontRecords = new ConcurrentHashMap<>();

	/**
	 * Table of known fonts realized on one particular display, keyed by symbolic
	 * font name (key type: <code>String</code>, value type:
	 * <code>FontRecord</code>). There is one such table per {@link Display} the
	 * registry has been used from, since a {@link Font} is only valid on the
	 * display that created it. Also keeps that display's fonts that were
	 * replaced by {@link FontRegistry#put(String, FontData[])} but may still be
	 * in use elsewhere, so their disposal is deferred until the display itself
	 * is disposed.
	 * <p>
	 * Both collections are concurrent: a display's records are read and written
	 * by its own thread, but {@link FontRegistry#put(String, FontData[])}
	 * invalidates the records of <em>every</em> display from whichever thread it
	 * is called on, and may do so while that display is disposing itself.
	 * </p>
	 */
	private static class DisplayFontRecords {

		private final Map<String, FontRecord> records = new ConcurrentHashMap<>();

		private final Queue<Font> staleFonts = new ConcurrentLinkedQueue<>();

		FontRecord get(String symbolicName) {
			return records.get(symbolicName);
		}

		void put(String symbolicName, FontRecord record) {
			records.put(symbolicName, record);
		}

		/**
		 * Drop the record for the given symbolic name, if any, and defer
		 * disposal of the fonts it had realized until this display is disposed,
		 * since they may still be in use. The display's default font is kept,
		 * as it stays in use under its own symbolic name.
		 */
		void invalidate(String symbolicName) {
			FontRecord replacedRecord = records.remove(symbolicName);
			if (replacedRecord == null) {
				return;
			}
			FontRecord defaultRecord = records.get(JFaceResources.DEFAULT_FONT);
			Font defaultFont = defaultRecord != null ? defaultRecord.get(FontStyle.NORMAL) : null;
			replacedRecord.getAllocatedFonts().stream().filter(font -> font != defaultFont).forEach(staleFonts::add);
		}

		void dispose() {
			records.values().forEach(FontRecord::dispose);
			records.clear();
			// drained rather than iterated and cleared, so that a font enqueued by a
			// concurrent put() is either disposed here or still queued afterwards,
			// but never dropped undisposed
			for (Font staleFont = staleFonts.poll(); staleFont != null; staleFont = staleFonts.poll()) {
				staleFont.dispose();
			}
		}

	}

	/**
	 * Table of known font data, keyed by symbolic font name
	 * (key type: <code>String</code>,
	 *  value type: <code>org.eclipse.swt.graphics.FontData[]</code>).
	 */
	private final Map<String, FontData[]> stringToFontData = new ConcurrentHashMap<>(7);

	/**
	 * Runnable that cleans up the manager on disposal of the display.
	 */
	protected Runnable displayRunnable = this::clearCaches;

	private final boolean cleanOnDisplayDisposal;

	/**
	 * The display this registry was created for. It is assumed to outlive every
	 * other display the registry is used from, so its fonts can serve as a
	 * fallback for callers that have no display of their own.
	 */
	private final Display mainDisplay;

	/**
	 * Creates an empty font registry.
	 * <p>
	 * There must be an SWT Display created in the current
	 * thread before calling this method.
	 * </p>
	 */
	public FontRegistry() {
		this(Display.getCurrent(), true);
	}

	/**
	 * Creates a font registry and initializes its content from a property file.
	 * <p>
	 * There must be an SWT Display created in the current thread before calling
	 * this method.
	 * </p>
	 * <p>
	 * The OS name (retrieved using <code>System.getProperty("os.name")</code>) is
	 * converted to lowercase, purged of whitespace, and appended as suffix
	 * (separated by an underscore <code>'_'</code>) to the given location string to
	 * yield the base name of a resource bundle acceptable to
	 * <code>ResourceBundle.getBundle</code>. The standard Java resource bundle
	 * mechanism is then used to locate and open the appropriate properties file,
	 * taking into account locale specific variations.
	 * </p>
	 * <p>
	 * For example, on the Windows 2000 operating system the location string
	 * <code>"com.example.myapp.Fonts"</code> yields the base name
	 * <code>"com.example.myapp.Fonts_windows2000"</code>. For the US English
	 * locale, this further elaborates to the resource bundle name
	 * <code>"com.example.myapp.Fonts_windows2000_en_us"</code>.
	 * </p>
	 * <p>
	 * If no appropriate OS-specific resource bundle is found, the process is
	 * repeated using the location as the base bundle name.
	 * </p>
	 * <p>
	 * The property file contains entries that look like this:
	 *
	 * <pre>
	 *	textfont.0=MS Sans Serif-regular-10
	 *	textfont.1=Times New Roman-regular-10
	 *
	 *	titlefont.0=MS Sans Serif-regular-12
	 *	titlefont.1=Times New Roman-regular-12
	 * </pre>
	 * <p>
	 * Each entry maps a symbolic font names (the font registry keys) with a
	 * "<code>.<i>n</i></code>" suffix to standard font names on the right. The
	 * suffix indicated order of preference: "<code>.0</code>" indicates the first
	 * choice, "<code>.1</code>" indicates the second choice, and so on.
	 * </p>
	 * The following example shows how to use the font registry:
	 *
	 *
	 * <pre>
	 *  FontRegistry registry = new FontRegistry("com.example.myapp.fonts");
	 *  Font font = registry.get("textfont");
	 *  control.setFont(font);
	 *  ...
	 * </pre>
	 *
	 * @param location the name of the resource bundle
	 * @param loader   the ClassLoader to use to find the resource bundle
	 * @exception MissingResourceException if the resource bundle cannot be found
	 * @since 2.1
	 */
	public FontRegistry(String location, ClassLoader loader)
			throws MissingResourceException {
		mainDisplay = Display.getCurrent();
		Assert.isNotNull(mainDisplay);
		// FIXE: need to respect loader
		//readResourceBundle(location, loader);
		readResourceBundle(location);
		cleanOnDisplayDisposal = true;
		displayToFontRecords.put(mainDisplay, new DisplayFontRecords());
		hookMainDisplayDispose(mainDisplay);
	}

	/**
	 * Load the FontRegistry using the ClassLoader from the PlatformUI
	 * plug-in
	 * <p>
	 * This method should only be called from the UI thread. If you are not on the UI
	 * thread then wrap the call with a
	 * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
	 * guarantee the correct result. Failure to do this may result in an {@link
	 * SWTException} being thrown.
	 * </p>
	 * @param location the location to read the resource bundle from
	 * @throws MissingResourceException Thrown if a resource is missing
	 */
	public FontRegistry(String location) throws MissingResourceException {
		// FIXE:
		//	this(location, WorkbenchPlugin.getDefault().getDescriptor().getPluginClassLoader());
		this(location, null);
	}

	/**
	 * Read the resource bundle at location. Look for a file with the
	 * extension _os_ws first, then _os then just the name.
	 * @param location - String - the location of the file.
	 */

	private void readResourceBundle(String location) {
		String osname = System.getProperty("os.name").trim(); //$NON-NLS-1$
		String wsname = Util.getWS();
		osname = StringConverter.removeWhiteSpaces(osname).toLowerCase();
		wsname = StringConverter.removeWhiteSpaces(wsname).toLowerCase();
		String OSLocation = location;
		String WSLocation = location;
		ResourceBundle bundle = null;
		if (osname != null) {
			OSLocation = location + "_" + osname; //$NON-NLS-1$
			if (wsname != null) {
				WSLocation = OSLocation + "_" + wsname; //$NON-NLS-1$
			}
		}

		try {
			bundle = ResourceBundle.getBundle(WSLocation);
			readResourceBundle(bundle, WSLocation);
		} catch (MissingResourceException wsException) {
			try {
				bundle = ResourceBundle.getBundle(OSLocation);
				readResourceBundle(bundle, WSLocation);
			} catch (MissingResourceException osException) {
				if (location != OSLocation) {
					bundle = ResourceBundle.getBundle(location);
					readResourceBundle(bundle, WSLocation);
				} else {
					throw osException;
				}
			}
		}
	}

	/**
	 * Creates an empty font registry.
	 *
	 * @param display the Display
	 */
	public FontRegistry(Display display) {
		this(display, true);
	}

	/**
	 * Creates an empty font registry.
	 *
	 * @param display
	 *            the <code>Display</code>
	 * @param cleanOnDisplayDisposal
	 *            whether all fonts allocated by this <code>FontRegistry</code>
	 *            should be disposed when the display is disposed. If
	 *            <code>false</code>, this registry never disposes a font by
	 *            itself; the fonts it allocated for any display are retained
	 *            until {@link #clearCaches()} disposes them
	 * @since 3.1
	 */
	public FontRegistry(Display display, boolean cleanOnDisplayDisposal) {
		Assert.isNotNull(display);
		this.mainDisplay = display;
		displayToFontRecords.put(display, new DisplayFontRecords());
		this.cleanOnDisplayDisposal = cleanOnDisplayDisposal;
		if (cleanOnDisplayDisposal) {
			hookMainDisplayDispose(display);
		}
	}

	/**
	 * Find the first valid fontData in the provided list. If none are valid
	 * return the first one regardless. If the list is empty return null. Return
	 * <code>null</code> if one cannot be found.
	 *
	 * @param fonts the font list
	 * @param display the display used
	 * @return the font data of the like describe above
	 *
	 * @deprecated use bestDataArray in order to support Motif multiple entry
	 *             fonts.
	 */
	@Deprecated
	public FontData bestData(FontData[] fonts, Display display) {
		for (FontData fd : fonts) {
			if (fd == null) {
				break;
			}

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
		if (fonts.length > 0) {
			return fonts[0];
		}

		//Nothing specified
		return null;
	}

	/**
	 * Find the first valid fontData in the provided list.
	 * If none are valid return the first one regardless.
	 * If the list is empty return <code>null</code>.
	 *
	 * @param fonts list of fonts
	 * @param display the display
	 * @return font data like described above
	 * @deprecated use filterData in order to preserve
	 * multiple entry fonts on Motif
	 */
	@Deprecated
	public FontData[] bestDataArray(FontData[] fonts, Display display) {

		FontData bestData = bestData(fonts, display);
		if (bestData == null) {
			return null;
		}

		FontData[] datas = new FontData[1];
		datas[0] = bestData;
		return datas;
	}

	/**
	 * Removes from the list all fonts that do not exist in this system.
	 * If none are valid, return the first irregardless.  If the list is
	 * empty return <code>null</code>.
	 *
	 * @param fonts the fonts to check
	 * @param display the display to check against
	 * @return the list of fonts that have been found on this system
	 * @since 3.1
	 */
	public FontData [] filterData(FontData [] fonts, Display display) {
		ArrayList<FontData> good = new ArrayList<>(fonts.length);
		for (FontData fd : fonts) {
			if (fd == null) {
				continue;
			}

			FontData[] fixedFonts = display.getFontList(fd.getName(), false);
			if (isFixedFont(fixedFonts, fd)) {
				good.add(fd);
			}

			FontData[] scalableFonts = display.getFontList(fd.getName(), true);
			if (scalableFonts.length > 0) {
				good.add(fd);
			}
		}


		//None of the provided datas are valid. Return the
		//first one as it is at least the first choice.
		if (good.isEmpty() && fonts.length > 0) {
			good.add(fonts[0]);
		}
		else if (fonts.length == 0) {
			return null;
		}

		return good.toArray(new FontData[good.size()]);
	}


	/**
	 * Creates a new font with the given font datas or <code>null</code>
	 * if there is no data.
	 * @return FontRecord for the new Font or <code>null</code>.
	 */
	private FontRecord createFont(String symbolicName, FontData[] fonts) {
		Display display = getDisplayAndHookForDisposal();
		if (display == null) {
			return null;
		}

		FontData[] validData = filterData(fonts, display);
		if (validData == null || validData.length == 0) {
			//Nothing specified
			return null;
		}

		// Do not fire the update from creation as it is not a property change.
		// Note that this drops any record other displays had realized for this
		// name, should filterData() have narrowed the registered data. That is
		// intended: the registered data is shared by all displays, so a record
		// built from outdated data must not survive on any of them.
		put(symbolicName, validData, false);
		Font newFont = new Font(display, validData);
		FontRecord record = new FontRecord(newFont, validData);
		// the display's records may have been torn down concurrently, e.g. by
		// clearCaches() running on another display's thread, in which case there is
		// nothing left to cache the record in and the next lookup realizes it again
		DisplayFontRecords displayRecords = displayToFontRecords.get(display);
		if (displayRecords != null) {
			displayRecords.put(symbolicName, record);
		}
		return record;
	}

	/**
	 * Calculates the default font and returns the result.
	 * This method creates a font that must be disposed.
	 */
	Font calculateDefaultFont() {
		Display current = Display.getCurrent();
		if (current == null) { // can't do much without Display
			SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
		}
		return new Font(current, current.getSystemFont().getFontData());
	}

	/**
	 * Returns the default font data.  Creates it if necessary.
	 * <p>
	 * This method should only be called from the UI thread. If you are not on the UI
	 * thread then wrap the call with a
	 * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
	 * guarantee the correct result. Failure to do this may result in an {@link
	 * SWTException} being thrown.
	 * </p>
	 * @return Font
	 */
	public Font defaultFont() {
		return defaultFont(FontStyle.NORMAL);
	}

	/**
	 * Return the default font in the given style, creating it if necessary.
	 * @param style the requested style
	 * @return the font
	 */
	private Font defaultFont(FontStyle style) {
		return defaultFontRecord(style).get(style);
	}

	/**
	 * Returns the font descriptor for the font with the given symbolic
	 * font name. Returns the default font if there is no special value
	 * associated with that name
	 *
	 * @param symbolicName symbolic font name
	 * @return the font descriptor (never null)
	 *
	 * @since 3.3
	 */
	public FontDescriptor getDescriptor(String symbolicName) {
		Assert.isNotNull(symbolicName);
		return FontDescriptor.createFrom(getFontData(symbolicName));
	}



	/**
	 * Return the default font record that can provide the passed style, creating it
	 * if necessary. The font record may provide fonts for the current or the main
	 * display. Only the passed style is guaranteed to be available on the returned
	 * record.
	 *
	 * @param style the requested style
	 * @return the font record, never <code>null</code>
	 */
	private FontRecord defaultFontRecord(FontStyle style) {
		FontRecord record = getExistingFontRecord(JFaceResources.DEFAULT_FONT, style);
		if (record == null && Display.getCurrent() == null) {
			// no display to scope the lookup to and none to create a font on: use the
			// main display's record even if the requested style is not realized there
			// yet, so the style gets realized on that display rather than failing
			DisplayFontRecords mainDisplayRecords = displayToFontRecords.get(mainDisplay);
			if (mainDisplayRecords != null) {
				record = mainDisplayRecords.get(JFaceResources.DEFAULT_FONT);
			}
		}
		if (record != null) {
			return record;
		}

		FontData[] fontData = stringToFontData.get(JFaceResources.DEFAULT_FONT);
		if (fontData != null) {
			record = createFont(JFaceResources.DEFAULT_FONT, fontData);
		}

		if (record == null) {
			Font defaultFont = calculateDefaultFont();
			record = createFont(JFaceResources.DEFAULT_FONT, defaultFont.getFontData());
			defaultFont.dispose();
		}
		return record;
	}

	/**
	 * Looks up an already-realized font record for the given symbolic name and
	 * style. A record the current display has already realized the exact style
	 * on is used first, so repeated lookups from that display keep returning the
	 * same font. Otherwise the main display's record is used if it already has
	 * that style, so callers from any display, including a thread with no
	 * Display of its own, reuse it instead of allocating a duplicate. Failing
	 * both, the current display's own record is returned even though it lacks
	 * the requested style, so that the style gets realized on the display owning
	 * the record; <code>null</code> is returned only if the current display has
	 * no record for the name at all, in which case the caller is responsible for
	 * creating one on it.
	 * <p>
	 * Handing out the main display's font to another display is safe because
	 * the main display is assumed to outlive every other display the registry
	 * is used from, so the font cannot be disposed while another display is
	 * still using it.
	 * </p>
	 * <p>
	 * Requiring the exact style to be present is also what keeps the lazy
	 * creation of styled fonts single-threaded: a record is only ever handed to
	 * a foreign display once the style it asks for has been realized, so only
	 * the record's own display ever reaches the creating branch of
	 * {@link FontRecord#get(FontStyle)}. Note that for {@link FontStyle#NORMAL}
	 * every existing record matches, since that font is realized when the record
	 * is created.
	 * </p>
	 */
	private FontRecord getExistingFontRecord(String symbolicName, FontStyle style) {
		FontRecord recordOnCurrentDisplay = null;
		Display currentDisplay = Display.getCurrent();
		if (currentDisplay != null) {
			DisplayFontRecords currentDisplayRecords = displayToFontRecords.get(currentDisplay);
			if (currentDisplayRecords != null) {
				recordOnCurrentDisplay = currentDisplayRecords.get(symbolicName);
				// a style this display already realized itself stays the one it gets, so
				// repeated lookups keep returning the same font instance
				if (recordOnCurrentDisplay != null && recordOnCurrentDisplay.has(style)) {
					return recordOnCurrentDisplay;
				}
			}
		}

		DisplayFontRecords mainDisplayRecords = displayToFontRecords.get(mainDisplay);
		if (mainDisplayRecords != null) {
			FontRecord recordOnMainDisplay = mainDisplayRecords.get(symbolicName);
			// Only return main display record if exact font style already exists
			if (recordOnMainDisplay != null && recordOnMainDisplay.has(style)) {
				return recordOnMainDisplay;
			}
		}

		return recordOnCurrentDisplay;
	}

	/**
	 * Returns the default font data.  Creates it if necessary.
	 */
	private FontData[] defaultFontData() {
		return defaultFontRecord(FontStyle.NORMAL).baseData;
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
		if (result == null) {
			return defaultFontData();
		}

		return (FontData[]) result;
	}

	/**
	 * Returns the font associated with the given symbolic font name.
	 * Returns the default font if there is no special value associated
	 * with that name.
	 * <p>
	 * This method should only be called from the UI thread. If you are not on the UI
	 * thread then wrap the call with a
	 * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
	 * guarantee the correct result. Failure to do this may result in an {@link
	 * SWTException} being thrown.
	 * </p>
	 * @param symbolicName symbolic font name
	 * @return the font
	 */
	public Font get(String symbolicName) {
		return getFont(symbolicName, FontStyle.NORMAL);
	}

	/**
	 * Returns the bold font associated with the given symbolic font name.
	 * Returns the bolded default font if there is no special value associated
	 * with that name.
	 * <p>
	 * This method should only be called from the UI thread. If you are not on the UI
	 * thread then wrap the call with a
	 * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
	 * guarantee the correct result. Failure to do this may result in an {@link
	 * SWTException} being thrown.
	 * </p>
	 * @param symbolicName symbolic font name
	 * @return the font
	 * @since 3.0
	 */
	public Font getBold(String symbolicName) {
		return getFont(symbolicName, FontStyle.BOLD);
	}

	/**
	 * Returns the italic font associated with the given symbolic font name.
	 * Returns the italic default font if there is no special value associated
	 * with that name.
	 * <p>
	 * This method should only be called from the UI thread. If you are not on the UI
	 * thread then wrap the call with a
	 * <code>PlatformUI.getWorkbench().getDisplay().synchExec()</code> in order to
	 * guarantee the correct result. Failure to do this may result in an {@link
	 * SWTException} being thrown.
	 * </p>
	 * @param symbolicName symbolic font name
	 * @return the font
	 * @since 3.0
	 */
	public Font getItalic(String symbolicName) {
		return getFont(symbolicName, FontStyle.ITALIC);
	}

	/**
	 * Return the font for the given key and style.
	 * @param symbolicName The key for the record.
	 * @param style the requested style
	 * @return the font
	 */
	private Font getFont(String symbolicName, FontStyle style) {
		Assert.isNotNull(symbolicName);
		FontRecord existingRecord = getExistingFontRecord(symbolicName, style);
		if (existingRecord != null) {
			return existingRecord.get(style);
		}

		FontData[] existingFontData = stringToFontData.get(symbolicName);

		FontRecord fontRecord;

		if (existingFontData == null) {
			fontRecord = defaultFontRecord(style);
		} else {
			fontRecord = createFont(symbolicName, existingFontData);
		}

		if (fontRecord == null) {
			fontRecord = defaultFontRecord(style);
			if (Display.getCurrent() == null) { // log error but don't throw an exception to preserve existing functionality
				String msg = "Unable to create font \"" + symbolicName + "\" in a non-UI thread. Using default font instead."; //$NON-NLS-1$ //$NON-NLS-2$
				Policy.logException(new SWTException(msg));
			}
		}

		return fontRecord.get(style);
	}

	@Override
	public Set<String> getKeySet() {
		return Collections.unmodifiableSet(stringToFontData.keySet());
	}

	@Override
	public boolean hasValueFor(String fontKey) {
		return stringToFontData.containsKey(fontKey);
	}

	@Override
	protected void clearCaches() {
		// disposes every display's fonts, not only the current display's: the
		// contract is to dispose all allocated resources, and this also runs on
		// disposal of the main display, which outlives all other displays
		for (Display display : displayToFontRecords.keySet()) {
			unhookDisplayAndDisposeFonts(display);
		}
	}

	/**
	 * Hook a dispose listener on the SWT display this registry was created for.
	 * Since that display outlives all others, its disposal tears down the whole
	 * registry, not just its own fonts.
	 */
	private void hookMainDisplayDispose(Display display) {
		display.disposeExec(displayRunnable);
	}

	private Display getDisplayAndHookForDisposal() {
		Display display = Display.getCurrent();
		if (display == null) {
			return null;
		}
		displayToFontRecords.computeIfAbsent(display, newDisplay -> {
			if (cleanOnDisplayDisposal) {
				newDisplay.disposeExec(() -> unhookDisplayAndDisposeFonts(newDisplay));
			}
			return new DisplayFontRecords();
		});
		return display;
	}

	private void unhookDisplayAndDisposeFonts(Display display) {
		DisplayFontRecords records = displayToFontRecords.remove(display);
		if (records != null) {
			records.dispose();
		}
	}

	/**
	 * Checks whether the given font is in the list of fixed fonts.
	 */
	private boolean isFixedFont(FontData[] fixedFonts, FontData fd) {
		// Can't use FontData.equals() since some values aren't
		// set if a fontdata isn't used.
		int height = fd.getHeight();
		String name = fd.getName();
		for (FontData fixed : fixedFonts) {
			if (fixed.getHeight() == height && fixed.getName().equals(name)) {
				return true;
			}
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
			throw new MissingResourceException(
					"Wrong font data format. Value is: \"" + value + "\"", getClass().getName(), value); //$NON-NLS-2$//$NON-NLS-1$
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
	 */
	public void put(String symbolicName, FontData[] fontData) {
		put(symbolicName, fontData, true);
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
	 * @param update - fire a property change if true. False
	 * 	if this method is called from the get method as no setting
	 *  has changed.
	 */
	private void put(String symbolicName, FontData[] fontData, boolean update) {

		Assert.isNotNull(symbolicName);
		Assert.isNotNull(fontData);

		// single atomic read-modify-write; replacing an equal mapping with the
		// given, content-equal one is a no-op for every reader
		FontData[] existing = stringToFontData.put(symbolicName, fontData);
		if (Arrays.equals(existing, fontData)) {
			return;
		}

		// the font data is shared by all displays, so the font has to be
		// invalidated on all of them. Stale fonts are queued per display, so
		// each display disposes its own replaced fonts when it is itself
		// disposed, instead of every display's replaced fonts only being
		// disposed together with one particular display
		for (DisplayFontRecords records : displayToFontRecords.values()) {
			records.invalidate(symbolicName);
		}
		if (update) {
			fireMappingChanged(symbolicName, existing, fontData);
		}
	}

	/**
	 * Reads the resource bundle.  This puts FontData[] objects
	 * in the mapping table.  These will lazily be turned into
	 * real Font objects when requested.
	 */
	private void readResourceBundle(ResourceBundle bundle, String bundleName)
			throws MissingResourceException {
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			int pos = key.lastIndexOf('.');
			if (pos == -1) {
				stringToFontData.put(key, new FontData[] { makeFontData(bundle
						.getString(key)) });
			} else {
				String name = key.substring(0, pos);
				int i = 0;
				try {
					i = Integer.parseInt(key.substring(pos + 1));
				} catch (NumberFormatException e) {
					//Panic the file can not be parsed.
					throw new MissingResourceException(
							"Wrong key format ", bundleName, key); //$NON-NLS-1$
				}
				FontData[] elements = stringToFontData.get(name);
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
	 * Returns the font descriptor for the JFace default font.
	 *
	 * @return the font descriptor for the JFace default font
	 * @since 3.3
	 */
	public FontDescriptor defaultFontDescriptor() {
		return FontDescriptor.createFrom(defaultFontData());
	}
}
