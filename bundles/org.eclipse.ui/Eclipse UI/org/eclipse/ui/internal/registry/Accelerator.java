package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Captures the attributes of a mapping between an accelerator key
 * and an action id. Accelerators may be specific to a locale and/or platform,
 * or they may general accelerators applicable to all platforms and locales. If
 * two accelerators exist, one with locale and/or platform of "all", and the other
 * with a more specific locale and/or platform, the more specific accelerator is
 * used.
 */
public class Accelerator {
	private static final String DEFAULT_LOCALE  = "all";
	private static final String DEFAULT_PLATFORM = "all";
	private String id;
	private String key;
	private String locale;
	private String platform;
	private Integer accelerators[][];
	
	private static Map keyCodes = null;
	
	public Accelerator(String id, String key, String locale, String platform) {
		this.id = id;
		this.key = key;
		
		if(locale==null) {
			this.locale = DEFAULT_LOCALE;	
		} else {
			this.locale = locale;	
		}
		
		if(platform==null) {
			this.platform = DEFAULT_PLATFORM;
		} else {
			this.platform = platform;	
		}
	}
	public String getId() {
		return id;
	}
	public String getKey() {
		return key;	
	}
	public String getLocale() {
		return locale;	
	}
	public String getPlatform() {
		return platform;	
	}
	public Integer[][] getAccelerators() {
		if(accelerators == null)
			accelerators = convertAccelerator();
		return accelerators;
	}
	/**
	 * Parses the given accelerator text, and converts it to a
	 * list (possibly of length 1) of accelerator key codes.
	 *
	 * @param acceleratorText the accelerator text
	 * @return the SWT key code list, or an empty list if there is no accelerator
	 */
	private Integer[][] convertAccelerator() {
		List accelerators = new ArrayList(1);
		StringTokenizer orTokenizer = new StringTokenizer(key,"||");
		while (orTokenizer.hasMoreTokens()) {
			List acc = new ArrayList(2);
			StringTokenizer spaceTokenizer = new StringTokenizer(orTokenizer.nextToken());
			while (spaceTokenizer.hasMoreTokens()) {
				int accelerator = 0;
				StringTokenizer keyTok = new StringTokenizer(spaceTokenizer.nextToken(), "+");    //$NON-NLS-1$
				int keyCode = -1;
				
				while (keyTok.hasMoreTokens()) {
					String token = keyTok.nextToken();
					// Every token except the last must be one of the modifiers Ctrl, Shift, or Alt.
					if (keyTok.hasMoreTokens()) {
						int modifier = findModifier(token);
						if (modifier != 0) {
							accelerator |= modifier;
						} else {//Leave if there are none
							return new Integer[0][0];
						}
					} else {
						keyCode = findKeyCode(token);
					}
				}
				if (keyCode != -1) {
					accelerator |= keyCode;
				}
				acc.add(new Integer(accelerator));
			}
			Integer result[] = new Integer[acc.size()];
			acc.toArray(result);
			accelerators.add(result);		
		}
		Integer result[][] = new Integer[accelerators.size()][];
		accelerators.toArray(result);
		return result;
	}
	/**
	 * Maps standard keyboard modifier key names to the corresponding 
	 * SWT modifier bit. The following modifier key names are recognized 
	 * (case is ignored): <code>"CTRL"</code>, <code>"SHIFT"</code>, and
	 * <code>"ALT"</code>.
	 * The given modifier key name is converted to upper case before comparison.
	 *
	 * @param token the modifier key name
	 * @return the SWT modifier bit, or <code>0</code> if no match was found
	 * @see org.eclipse.swt.SWT
	 */
	private static int findModifier(String token) {
		token= token.toUpperCase();
		if (token.equals("CTRL"))//$NON-NLS-1$
			return SWT.CTRL;
		if (token.equals("SHIFT"))//$NON-NLS-1$
			return SWT.SHIFT;
		if (token.equals("ALT"))//$NON-NLS-1$
			return SWT.ALT;
		return 0;
	}
	/**
	 * Maps a standard keyboard key name to an SWT key code.
	 * Key names are converted to upper case before comparison.
	 * If the key name is a single letter, for example "S", its character code is returned.
	 * <p>
	 * The following key names are known (case is ignored):
	 * <ul>
	 * 	<li><code>"BACKSPACE"</code></li>
	 *  <li><code>"TAB"</code></li>
	 *  <li><code>"RETURN"</code></li>
	 *  <li><code>"ENTER"</code></li>
	 *  <li><code>"ESC"</code></li>
	 *  <li><code>"ESCAPE"</code></li>
	 *  <li><code>"DELETE"</code></li>
	 *  <li><code>"SPACE"</code></li>
	 *  <li><code>"ARROW_UP"</code>, <code>"ARROW_DOWN"</code>,
	 *     <code>"ARROW_LEFT"</code>, and <code>"ARROW_RIGHT"</code></li>
	 *  <li><code>"PAGE_UP"</code> and <code>"PAGE_DOWN"</code></li>
	 *  <li><code>"HOME"</code></li>
	 *  <li><code>"END"</code></li>
	 *  <li><code>"INSERT"</code></li>
	 *  <li><code>"F1"</code>, <code>"F2"</code> through <code>"F12"</code></li>
	 * </ul>
	 * </p>
	 *
	 * @param token the key name
	 * @return the SWT key code, <code>-1</code> if no match was found
	 * @see org.eclipse.swt.SWT
	 */
	public static int findKeyCode(String token) {
		if (keyCodes == null)
			initKeyCodes();
		token= token.toUpperCase();
		Integer i= (Integer) keyCodes.get(token);
		if (i != null) 
			return i.intValue();
		if (token.length() == 1)
			return token.charAt(0);
		return -1;
	}
		/** 
	 * Initializes the internal key code table.
	 */
	private static void initKeyCodes() {
		
		keyCodes = new HashMap(40);
	
		keyCodes.put("BACKSPACE", new Integer(8));//$NON-NLS-1$
		keyCodes.put("TAB", new Integer(9));//$NON-NLS-1$
		keyCodes.put("RETURN", new Integer(13));//$NON-NLS-1$
		keyCodes.put("ENTER", new Integer(13));//$NON-NLS-1$
		keyCodes.put("ESCAPE", new Integer(27));//$NON-NLS-1$
		keyCodes.put("ESC", new Integer(27));//$NON-NLS-1$
		keyCodes.put("DEL", new Integer(127));//$NON-NLS-1$
		keyCodes.put("DELETE", new Integer(127));//$NON-NLS-1$
	
		keyCodes.put("SPACE", new Integer(' '));//$NON-NLS-1$
		keyCodes.put("ARROW_UP", new Integer(SWT.ARROW_UP));//$NON-NLS-1$
		keyCodes.put("ARROW_DOWN", new Integer(SWT.ARROW_DOWN));//$NON-NLS-1$
		keyCodes.put("ARROW_LEFT", new Integer(SWT.ARROW_LEFT));//$NON-NLS-1$
		keyCodes.put("ARROW_RIGHT", new Integer(SWT.ARROW_RIGHT));//$NON-NLS-1$
		keyCodes.put("PAGE_UP", new Integer(SWT.PAGE_UP));//$NON-NLS-1$
		keyCodes.put("PAGE_DOWN", new Integer(SWT.PAGE_DOWN));//$NON-NLS-1$
		keyCodes.put("HOME", new Integer(SWT.HOME));//$NON-NLS-1$
		keyCodes.put("END", new Integer(SWT.END));//$NON-NLS-1$
		keyCodes.put("INSERT", new Integer(SWT.INSERT));//$NON-NLS-1$
		keyCodes.put("F1", new Integer(SWT.F1));//$NON-NLS-1$
		keyCodes.put("F2", new Integer(SWT.F2));//$NON-NLS-1$
		keyCodes.put("F3", new Integer(SWT.F3));//$NON-NLS-1$
		keyCodes.put("F4", new Integer(SWT.F4));//$NON-NLS-1$
		keyCodes.put("F5", new Integer(SWT.F5));//$NON-NLS-1$
		keyCodes.put("F6", new Integer(SWT.F6));//$NON-NLS-1$
		keyCodes.put("F7", new Integer(SWT.F7));//$NON-NLS-1$
		keyCodes.put("F8", new Integer(SWT.F8));//$NON-NLS-1$
		keyCodes.put("F9", new Integer(SWT.F9));//$NON-NLS-1$
		keyCodes.put("F10", new Integer(SWT.F10));//$NON-NLS-1$
		keyCodes.put("F11", new Integer(SWT.F11));//$NON-NLS-1$
		keyCodes.put("F12", new Integer(SWT.F12));//$NON-NLS-1$
	}  
}
