/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.swt.SWT;

public final class KeySupport {

	private final static ResourceBundle resourceBundle = ResourceBundle.getBundle(KeySupport.class.getName());

	private final static String ALT = "Alt"; //$NON-NLS-1$
	private final static String COMMAND = "Command"; //$NON-NLS-1$
	private final static String CTRL = "Ctrl"; //$NON-NLS-1$
	private final static String MODIFIER_SEPARATOR = "+"; //$NON-NLS-1$
	private final static String SHIFT = "Shift"; //$NON-NLS-1$
	private final static String STROKE_SEPARATOR = " "; //$NON-NLS-1$

	private static Map localizedStringToValueMap = new TreeMap();
	private static Map stringToValueMap = new TreeMap();	
	private static Map valueToLocalizedStringMap = new TreeMap();
	private static Map valueToStringMap = new TreeMap();

	static {		
		localizedStringToValueMap.put(u(l("Backspace")), new Integer(8)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Tab")), new Integer(9)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Return")), new Integer(13)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Enter")), new Integer(13)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Escape")), new Integer(27)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Esc")), new Integer(27)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Delete")), new Integer(127)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Space")), new Integer(' ')); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Arrow_Up")), new Integer(SWT.ARROW_UP)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Arrow_Down")), new Integer(SWT.ARROW_DOWN)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Arrow_Left")), new Integer(SWT.ARROW_LEFT)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Arrow_Right")), new Integer(SWT.ARROW_RIGHT)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Page_Up")), new Integer(SWT.PAGE_UP)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Page_Down")), new Integer(SWT.PAGE_DOWN)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Home")), new Integer(SWT.HOME)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("End")), new Integer(SWT.END)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("Insert")), new Integer(SWT.INSERT)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F1")), new Integer(SWT.F1)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F2")), new Integer(SWT.F2)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F3")), new Integer(SWT.F3)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F4")), new Integer(SWT.F4)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F5")), new Integer(SWT.F5)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F6")), new Integer(SWT.F6)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F7")), new Integer(SWT.F7)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F8")), new Integer(SWT.F8)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F9")), new Integer(SWT.F9)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F10")), new Integer(SWT.F10)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F11")), new Integer(SWT.F11)); //$NON-NLS-1$
		localizedStringToValueMap.put(u(l("F12")), new Integer(SWT.F12)); //$NON-NLS-1$		

		stringToValueMap.put(u("Backspace"), new Integer(8)); //$NON-NLS-1$
		stringToValueMap.put(u("Tab"), new Integer(9)); //$NON-NLS-1$
		stringToValueMap.put(u("Return"), new Integer(13)); //$NON-NLS-1$
		stringToValueMap.put(u("Enter"), new Integer(13)); //$NON-NLS-1$
		stringToValueMap.put(u("Escape"), new Integer(27)); //$NON-NLS-1$
		stringToValueMap.put(u("Esc"), new Integer(27)); //$NON-NLS-1$
		stringToValueMap.put(u("Delete"), new Integer(127)); //$NON-NLS-1$
		stringToValueMap.put(u("Space"), new Integer(' ')); //$NON-NLS-1$
		stringToValueMap.put(u("Arrow_Up"), new Integer(SWT.ARROW_UP)); //$NON-NLS-1$
		stringToValueMap.put(u("Arrow_Down"), new Integer(SWT.ARROW_DOWN)); //$NON-NLS-1$
		stringToValueMap.put(u("Arrow_Left"), new Integer(SWT.ARROW_LEFT)); //$NON-NLS-1$
		stringToValueMap.put(u("Arrow_Right"), new Integer(SWT.ARROW_RIGHT)); //$NON-NLS-1$
		stringToValueMap.put(u("Page_Up"), new Integer(SWT.PAGE_UP)); //$NON-NLS-1$
		stringToValueMap.put(u("Page_Down"), new Integer(SWT.PAGE_DOWN)); //$NON-NLS-1$
		stringToValueMap.put(u("Home"), new Integer(SWT.HOME)); //$NON-NLS-1$
		stringToValueMap.put(u("End"), new Integer(SWT.END)); //$NON-NLS-1$
		stringToValueMap.put(u("Insert"), new Integer(SWT.INSERT)); //$NON-NLS-1$
		stringToValueMap.put(u("F1"), new Integer(SWT.F1)); //$NON-NLS-1$
		stringToValueMap.put(u("F2"), new Integer(SWT.F2)); //$NON-NLS-1$
		stringToValueMap.put(u("F3"), new Integer(SWT.F3)); //$NON-NLS-1$
		stringToValueMap.put(u("F4"), new Integer(SWT.F4)); //$NON-NLS-1$
		stringToValueMap.put(u("F5"), new Integer(SWT.F5)); //$NON-NLS-1$
		stringToValueMap.put(u("F6"), new Integer(SWT.F6)); //$NON-NLS-1$
		stringToValueMap.put(u("F7"), new Integer(SWT.F7)); //$NON-NLS-1$
		stringToValueMap.put(u("F8"), new Integer(SWT.F8)); //$NON-NLS-1$
		stringToValueMap.put(u("F9"), new Integer(SWT.F9)); //$NON-NLS-1$
		stringToValueMap.put(u("F10"), new Integer(SWT.F10)); //$NON-NLS-1$
		stringToValueMap.put(u("F11"), new Integer(SWT.F11)); //$NON-NLS-1$
		stringToValueMap.put(u("F12"), new Integer(SWT.F12)); //$NON-NLS-1$		
		
		valueToLocalizedStringMap.put(new Integer(8), l("Backspace")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(9), l("Tab")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(13), l("Return")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(13), l("Enter")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(27), l("Escape")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(27), l("Esc")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(127), l("Delete")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(' '), l("Space")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.ARROW_UP), l("Arrow_Up")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.ARROW_DOWN), l("Arrow_Down")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.ARROW_LEFT), l("Arrow_Left")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.ARROW_RIGHT), l("Arrow_Right")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.PAGE_UP), l("Page_Up")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.PAGE_DOWN), l("Page_Down")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.HOME), l("Home")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.END), l("End")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.INSERT), l("Insert")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F1), l("F1")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F2), l("F2")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F3), l("F3")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F4), l("F4")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F5), l("F5")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F6), l("F6")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F7), l("F7")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F8), l("F8")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F9), l("F9")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F10), l("F10")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F11), l("F11")); //$NON-NLS-1$
		valueToLocalizedStringMap.put(new Integer(SWT.F12), l("F12")); //$NON-NLS-1$		

		valueToStringMap.put(new Integer(8), "Backspace"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(9), "Tab"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(13), "Return"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(13), "Enter"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(27), "Escape"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(27), "Esc"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(127), "Delete"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(' '), "Space"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.ARROW_UP), "Arrow_Up"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.ARROW_DOWN), "Arrow_Down"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.ARROW_LEFT), "Arrow_Left"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.ARROW_RIGHT), "Arrow_Right"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.PAGE_UP), "Page_Up"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.PAGE_DOWN), "Page_Down"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.HOME), "Home"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.END), "End"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.INSERT), "Insert"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F1), "F1"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F2), "F2"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F3), "F3"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F4), "F4"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F5), "F5"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F6), "F6"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F7), "F7"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F8), "F8"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F9), "F9"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F10), "F10"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F11), "F11"); //$NON-NLS-1$
		valueToStringMap.put(new Integer(SWT.F12), "F12"); //$NON-NLS-1$		
	}

	private static String l(String string) {
		return Util.getString(resourceBundle, string);
	}
	
	private static String u(String string) {
		return string.toUpperCase();
	}

	public static String formatSequence(Sequence sequence, boolean localized)
		throws IllegalArgumentException {
		if (sequence == null)
			throw new IllegalArgumentException();
			
		int i = 0;
		Iterator iterator = sequence.getStrokes().iterator();
		StringBuffer stringBuffer = new StringBuffer();
		
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(STROKE_SEPARATOR);

			stringBuffer.append(formatStroke((Stroke) iterator.next(), localized));
			i++;
		}

		return stringBuffer.toString();
	}

	public static String formatStroke(Stroke stroke, boolean localized)
		throws IllegalArgumentException {
		if (stroke == null)
			throw new IllegalArgumentException();		
			
		StringBuffer stringBuffer = new StringBuffer();
		int value = stroke.getValue();
		
		if ((value & SWT.CTRL) != 0) {
			stringBuffer.append(localized ? l(CTRL) : CTRL);
		}
		
		if ((value & SWT.ALT) != 0) {
			if (stringBuffer.length() > 0)
				stringBuffer.append(MODIFIER_SEPARATOR);
			
			stringBuffer.append(localized ? l(ALT) : ALT);								
		}

		if ((value & SWT.SHIFT) != 0) {
			if (stringBuffer.length() > 0)
				stringBuffer.append(MODIFIER_SEPARATOR);
			
			stringBuffer.append(localized ? l(SHIFT) : SHIFT);								
		}

		if ((value & SWT.COMMAND) != 0) {
			if (stringBuffer.length() > 0)
				stringBuffer.append(MODIFIER_SEPARATOR);
			
			stringBuffer.append(localized ? l(COMMAND) : COMMAND);								
		}		

		if (stringBuffer.length() > 0)
			stringBuffer.append(MODIFIER_SEPARATOR);

		value &= ~(SWT.CTRL | SWT.ALT | SWT.SHIFT | SWT.COMMAND);
		String string = localized ? (String) valueToLocalizedStringMap.get(new Integer(value)) : (String) valueToStringMap.get(new Integer(value));

		if (string != null)
			stringBuffer.append(string);
		else 
			stringBuffer.append(Character.toUpperCase((char) value));

		return stringBuffer.toString();				
	}
	
	public static Sequence parseSequence(String string, boolean localized)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();

		List strokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string);
				
		while (stringTokenizer.hasMoreTokens())
			strokes.add(parseStroke(stringTokenizer.nextToken(), localized));
			
		return Sequence.create(strokes);
	}	
	
	public static Stroke parseStroke(String string, boolean localized)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();
		
		List list = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string, MODIFIER_SEPARATOR, true);
		
		while (stringTokenizer.hasMoreTokens())
			list.add(stringTokenizer.nextToken());

		int size = list.size();
		int value = 0;

		if (size % 2 == 1) {
			String token = (String) list.get(size - 1);			
			Integer integer = localized ? (Integer) localizedStringToValueMap.get(u(token)) : (Integer) stringToValueMap.get(u(token));
		
			if (integer != null)
				value = integer.intValue();
			else if (token.length() == 1)
				value = u(token).charAt(0);

			if (value != 0) {
				for (int i = 0; i < size - 1; i++) {
					token = (String) list.get(i);			
					
					if (i % 2 == 0) {
						if (token.equalsIgnoreCase(localized ? l(CTRL) : CTRL)) {
							if ((value & SWT.CTRL) != 0)
								return Stroke.create(0);
							
							value |= SWT.CTRL;
						} else if (token.equalsIgnoreCase(localized ? l(ALT) : ALT)) {
							if ((value & SWT.ALT) != 0)
								return Stroke.create(0);

							value |= SWT.ALT;
						} else if (token.equalsIgnoreCase(localized ? l(SHIFT) : SHIFT)) {
							if ((value & SWT.SHIFT) != 0)
								return Stroke.create(0);

							value |= SWT.SHIFT;
						} else if (token.equalsIgnoreCase(localized ? l(COMMAND) : COMMAND)) {
							if ((value & SWT.COMMAND) != 0)
								return Stroke.create(0);

							value |= SWT.COMMAND;
						} else
							return Stroke.create(0);
					} else if (!MODIFIER_SEPARATOR.equals(token))
						return Stroke.create(0);
				}				
			}				
		}

		return Stroke.create(value);
	}
	
	private KeySupport() {
		super();
	}
}
