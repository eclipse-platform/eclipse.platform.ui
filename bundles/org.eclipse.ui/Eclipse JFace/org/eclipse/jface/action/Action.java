package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;


/**
 * The standard abstract implementation of an action.
 * <p>
 * Subclasses must implement the <code>IAction.run</code> method to carry out
 * the action's semantics.
 * </p>
 */
public abstract class Action implements IAction {

	/**
	 * Table of key codes (key type: <code>String</code>,
	 * value type: <code>Integer</code>); <code>null</code>
	 * if not yet initialized.
	 * @see #findKeyCode
	 */
	private static Map keyCodes = null;
	/**
	 * Table of key codes (key type: <code>String</code>,
	 * value type: <code>Integer</code>); <code>null</code>
	 * if not yet initialized. The key is the localalized name
	 * of the key as it appears in menus.
	 * @see #findLocalizedKeyCode
	 */
	private static Map localizedKeyCodes = null;
	
	/**
	 * The localized uppercase versions of the modifer 
	 * keys.
	 */
	private static String LOCALIZED_CTRL;
	private static String LOCALIZED_SHIFT;
	private static String LOCALIZED_ALT;
	
	
	/**
	 * Table of string representations of keys
	 * (key type: <code>Integer</code>,
	 * value type: <code>String</code>); <code>null</code>>
	 * if not yet initialized.
	 * @see #findKeyString
	 */
	private static Map keyStrings = null;

	/**
	 * List of registered listeners (element type: <code>IPropertyChangeListener</code>).
	 */
	private ListenerList listeners = new ListenerList(3);
	
	/**
	 * This action's text, or <code>null</code> if none.
	 */
	private String text;
	
	/**
	 * This action's description, or <code>null</code> if none.
	 */
	private String description;
	
	/**
	 * This action's id, or <code>null</code> if none.
	 */
	private String id;
	
	/**
	 * This action's action definition id, or <code>null</code> if none.
	 */
	private String actionDefinitionId;
	
	/**
	 * This action's tool tip text, or <code>null</code> if none.
	 */
	private String toolTipText;

	/**
	 * An action's help listener, or <code>null</code> if none.
	 */
	private HelpListener helpListener;
	
	/**
	 * This action's image, or <code>null</code> if none.
	 */
	private ImageDescriptor image;

	/**
	 * This action's hover image, or <code>null</code> if none.
	 */
	private ImageDescriptor hoverImage;

	/**
	 * This action's disabled image, or <code>null</code> if none.
	 */
	private ImageDescriptor disabledImage;
	
	/**
	 * Holds the action's menu creator (an IMenuCreator) or checked state (a Boolean),
	 * or <code>null</code> if neither have been set.
	 * The value of this field affects the value of <code>getStyle()</code>.
	 */
	private Object value = null;
	
	/**
	 * This action's accelerator; <code>0</code> means none.
	 */
	private int accelerator = 0;
	
	/**
	 * Indicates this action is enabled.
	 */
	private boolean enabled = true;
/**
 * Creates a new action with no text and no image.
 * <p>
 * Configure the action later using the set methods.
 * </p>
 */
protected Action() {
}
/**
 * Creates a new action with the given text and no image.
 * Calls the zero-arg constructor, then <code>setText</code>.
 *
 * @param text the string used as the text for the action, 
 *   or <code>null</code> if these is no text
 * @see #setText
 */
protected Action(String text) {
	this();
	setText(text);
}
/**
 * Creates a new action with the given text and image.
 * Calls the zero-arg constructor, then <code>setText</code> and <code>setImageDescriptor</code>.
 *
 * @param text the action's text, or <code>null</code> if there is no text
 * @param image the action's image, or <code>null</code> if there is no image
 * @see #setText
 * @see #setImageDescriptor
 */
protected Action(String text, ImageDescriptor image) {
	this(text);
	setImageDescriptor(image);
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void addPropertyChangeListener(IPropertyChangeListener listener) { 
	listeners.add(listener);
}

/**
 * Parses the given accelerator text, and converts it to an accelerator key code.
 * 
 * Support for localized modifiers is for backwards compatibility
 * with 1.0. Use setAccelerator(int) to set accelerators programatically
 * or the <code>accelerator</code> tag in action definitions in 
 * plugin.xml.
 *
 * @param acceleratorText the accelerator text localized to the current locale
 * @return the SWT key code, or 0 if there is no accelerator
 */
private static int convertLocalizedAccelerator(String acceleratorText) {
	int accelerator = 0;
	StringTokenizer stok = new StringTokenizer(acceleratorText, "+");    //$NON-NLS-1$

	int keyCode = -1;

	boolean hasMoreTokens = stok.hasMoreTokens();
	while (hasMoreTokens) {
		String token = stok.nextToken();
		hasMoreTokens = stok.hasMoreTokens();
		// Every token except the last must be one of the modifiers
		// Ctrl, Shift, or Alt.
		if (hasMoreTokens) {
			int modifier = findLocalizedModifier(token);
			if (modifier != 0) {
				accelerator |= modifier;
			} else {//Leave if there are none
				return 0;
			}
		} else {
			keyCode = findLocalizedKeyCode(token);
		}
	}
	if (keyCode != -1) {
		accelerator |= keyCode;
	}
	return accelerator;
}

/**
 * Parses the given accelerator text, and converts it to an accelerator key code.
 *
 * @param acceleratorText the accelerator text
 * @return the SWT key code, or 0 if there is no accelerator
 */
public static int convertAccelerator(String acceleratorText) {
	int accelerator = 0;
	StringTokenizer stok = new StringTokenizer(acceleratorText, "+");    //$NON-NLS-1$

	int keyCode = -1;

	boolean hasMoreTokens = stok.hasMoreTokens();
	while (hasMoreTokens) {
		String token = stok.nextToken();
		hasMoreTokens = stok.hasMoreTokens();
		// Every token except the last must be one of the modifiers
		// Ctrl, Shift, or Alt.
		if (hasMoreTokens) {
			int modifier = findModifier(token);
			if (modifier != 0) {
				accelerator |= modifier;
			} else {//Leave if there are none
				return 0;
			}
		} else {
			keyCode = findKeyCode(token);
		}
	}
	if (keyCode != -1) {
		accelerator |= keyCode;
	}
	return accelerator;
}
/**
 * Converts an accelerator key code to a string representation.
 * 
 * @param keyCode the key code to be translated
 * @return a string representation of the key code
 */
public static String convertAccelerator(int keyCode) {
	String modifier = getModifierString(keyCode);
	String fullKey;
	if(modifier.equals("")) { //$NON-NLS-1$
		fullKey = findKeyString(keyCode);
	} else {
		fullKey = modifier + "+" + findKeyString(keyCode);	 //$NON-NLS-1$
	}
	return fullKey;
}
/*
 * Returns the string representation of the modifiers (Ctrl, Alt, Shift)
 * of the key event.
 */
private static String getModifierString(int keyCode) {
	String modString = ""; //$NON-NLS-1$
	if((keyCode & SWT.CTRL) != 0) {
		modString = findModifierString(keyCode & SWT.CTRL);
	}
	if((keyCode & SWT.ALT) != 0) {
		if(modString.equals("")) { //$NON-NLS-1$
			modString = findModifierString(keyCode & SWT.ALT);					
		} else {
			modString = modString+"+"+findModifierString(keyCode & SWT.ALT); //$NON-NLS-1$
		}
	}
	if((keyCode & SWT.SHIFT) != 0) {
		if(modString.equals("")) { //$NON-NLS-1$
			modString = findModifierString(keyCode & SWT.SHIFT);					
		} else {
			modString = modString+"+"+findModifierString(keyCode & SWT.SHIFT); //$NON-NLS-1$
		}
	}
	return modString;	
}
/**
 * Extracts the accelerator text from the given text.
 * Returns <code>null</code> if there is no accelerator text,
 * and the empty string if there is no text after the accelerator delimeter (tab or '@').
 *
 * @param text the text for the action
 * @return the accelerator text, or <code>null</code>
 */
private static String extractAcceleratorText(String text) {
	int index = text.lastIndexOf('\t');
	if (index == -1)
		index = text.lastIndexOf('@');
	if (index >= 0)
		return text.substring(index + 1);
	return null;
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
 * Find the supplied code for a localized key. As
 * #findKeyCode but localized to the current locale.
 * 
 * Support for localized modifiers is for backwards compatibility
 * with 1.0. Use setAccelerator(int) to set accelerators programatically
 * or the <code>accelerator</code> tag in action definitions in 
 * plugin.xml.
 *
 * @param token the localized key name
 * @return the SWT key code, <code>-1</code> if no match was found
 * @see #findKeyCode
 */
private static int findLocalizedKeyCode(String token) {
	if (localizedKeyCodes == null)
		initLocalizedKeyCodes();
	token= token.toUpperCase();
	Integer i= (Integer) localizedKeyCodes.get(token);
	if (i != null) 
		return i.intValue();
	if (token.length() == 1)
		return token.charAt(0);
	return -1;
}
/**
 * Maps an SWT key code to a standard keyboard key name. The key code is
 * stripped of modifiers (SWT.CTRL, SWT.ALT, and SWT.SHIFT). If the key code is
 * not an SWT code (for example if it a key code for the key 'S'), a string
 * containing a character representation of the key code is returned.
 * 
 * @param keyCode the key code to be translated
 * @return the string representation of the key code
 * @see org.eclipse.swt.SWT
 * @since 2.0
 */
public static String findKeyString(int keyCode) {
	if (keyStrings == null)
		initKeyStrings();
	int i = keyCode & ~(SWT.CTRL|SWT.ALT|SWT.SHIFT);
	Integer integer = new Integer(i);
	String result = (String)keyStrings.get(integer);
	if(result != null)
		return result;
	result = new String(new char[] {(char) i});
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
public static int findModifier(String token) {
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
 * Maps the localized modifier names to a code in the same
 * manner as #findModifier.
 * 
 * Support for localized modifiers is for backwards compatibility
 * with 1.0. Use setAccelerator(int) to set accelerators programatically
 * or the <code>accelerator</code> tag in action definitions in 
 * plugin.xml.
 * 
 * @see findModifier
 */
private static int findLocalizedModifier(String token) {
	if(LOCALIZED_CTRL == null)
		initLocalizedModifiers();
		
	token= token.toUpperCase();
	if (token.equals(LOCALIZED_CTRL))//$NON-NLS-1$
		return SWT.CTRL;
	if (token.equals(LOCALIZED_SHIFT))//$NON-NLS-1$
		return SWT.SHIFT;
	if (token.equals(LOCALIZED_ALT))//$NON-NLS-1$
		return SWT.ALT;
	return 0;
}

/**
 * Initialize the list of localized modifiers
 */
private static void initLocalizedModifiers(){
	LOCALIZED_CTRL = JFaceResources.getString("Ctrl").toUpperCase();//$NON-NLS-1$
	LOCALIZED_SHIFT = JFaceResources.getString("Shift").toUpperCase();//$NON-NLS-1$
	LOCALIZED_ALT = JFaceResources.getString("Alt").toUpperCase();//$NON-NLS-1$
}

/**
 * Returns a string representation of an SWT modifier bit (SWT.CTRL,
 * SWT.ALT, and SWT.SHIFT). Returns <code>null</code> if the key code 
 * is not an SWT modifier bit.
 * 
 * @param keyCode the SWT modifier bit to be translated
 * @return the string representation of the SWT modifier bit, or <code>null</code> if the key code was not an SWT modifier bit
 * @see org.eclopse.swt.SWT
 * @since 2.0
 */
public static String findModifierString(int keyCode) {
	if(keyCode == SWT.CTRL)
		return JFaceResources.getString("Ctrl"); //$NON-NLS-1$
	if(keyCode == SWT.ALT)
		return JFaceResources.getString("Alt"); //$NON-NLS-1$
	if(keyCode == SWT.SHIFT)
		return JFaceResources.getString("Shift"); //$NON-NLS-1$
	return null;	
}
/**
 * Notifies any property change listeners that a property has changed.
 * Only listeners registered at the time this method is called are notified.
 * This method avoids creating an event object if there are no listeners registered,
 * but calls <code>firePropertyChange(PropertyChangeEvent)</code> if there are.
 *
 * @param propertyName the name of the property that has changed
 * @param oldValue the old value of the property, or <code>null</code> if none
 * @param newValue the new value of the property, or <code>null</code> if none
 *
 * @see IPropertyChangeListener#propertyChange
 */
protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	if (!listeners.isEmpty()) {
		firePropertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
	}
}
/**
 * Notifies any property change listeners that a property has changed.
 * Only listeners registered at the time this method is called are notified.
 *
 * @param event the property change event
 *
 * @see IPropertyChangeListener#propertyChange
 */
protected void firePropertyChange(PropertyChangeEvent event) {
	Object[] listeners = this.listeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((IPropertyChangeListener) listeners[i]).propertyChange(event);
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public int getAccelerator() {
	return accelerator;
}

/*(non-Javadoc)
 * Method declared on IAction.
 * 
 */
public String getActionDefinitionId() {
	return actionDefinitionId;	
}

/* (non-Javadoc)
 * Method declared on IAction.
 */
public String getDescription() {
	if (description != null)
		return description;
	return getToolTipText();
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public ImageDescriptor getDisabledImageDescriptor() {
	return disabledImage;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public HelpListener getHelpListener() {
	return helpListener;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public ImageDescriptor getHoverImageDescriptor() {
	return hoverImage;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public String getId() {
	return id;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public ImageDescriptor getImageDescriptor() {
	return image;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public IMenuCreator getMenuCreator() {
	Object o = value;
	if (o instanceof IMenuCreator)
		return (IMenuCreator) o;
	return null;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public int getStyle() {
	// Infer the style form the fValue field.
	if (value != null) {
		if (value instanceof Boolean)
			return AS_CHECK_BOX;
		if (value instanceof IMenuCreator)
			return AS_DROP_DOWN_MENU;
	}
	return AS_PUSH_BUTTON;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public String getText() {
	return text;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public String getToolTipText() {
	return toolTipText;
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

/** 
 * Initializes the localized internal key code table.
 */
private static void initLocalizedKeyCodes() {
	localizedKeyCodes = new HashMap(40);

	localizedKeyCodes.put(JFaceResources.getString("Backspace"),new Integer(8));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Tab"),new Integer(9));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Return"), new Integer(13));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Enter"), new Integer(13));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Escape"), new Integer(27));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Esc"), new Integer(27));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Delete"), new Integer(127));//$NON-NLS-1$

	localizedKeyCodes.put(JFaceResources.getString("Space"), new Integer(' '));//$NON-NLS-1$
	
	localizedKeyCodes.put(JFaceResources.getString("Arrow_Up"), new Integer(SWT.ARROW_UP));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Arrow_Down"), new Integer(SWT.ARROW_DOWN));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Arrow_Left"), new Integer(SWT.ARROW_LEFT));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Arrow_Right"), new Integer(SWT.ARROW_RIGHT));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Page_Up"), new Integer(SWT.PAGE_UP));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Page_Down"), new Integer(SWT.PAGE_DOWN));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Home"), new Integer(SWT.HOME));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("End"), new Integer(SWT.END));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("Insert"), new Integer(SWT.INSERT));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F1"), new Integer(SWT.F1));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F2"), new Integer(SWT.F2));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F3"), new Integer(SWT.F3));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F4"), new Integer(SWT.F4));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F5"), new Integer(SWT.F5));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F6"), new Integer(SWT.F6));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F7"), new Integer(SWT.F7));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F8"), new Integer(SWT.F8));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F9"), new Integer(SWT.F9));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F10"), new Integer(SWT.F10));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F11"), new Integer(SWT.F11));//$NON-NLS-1$
	localizedKeyCodes.put(JFaceResources.getString("F12"), new Integer(SWT.F12));//$NON-NLS-1$
}

/**
 * Initializes the internal key string table.
 */
private static void initKeyStrings() {
	keyStrings = new HashMap(40);
	
	keyStrings.put(new Integer(8), JFaceResources.getString("Backspace"));//$NON-NLS-1$
	keyStrings.put(new Integer(9), JFaceResources.getString("Tab"));//$NON-NLS-1$
	keyStrings.put(new Integer(13), JFaceResources.getString("Return"));//$NON-NLS-1$
	keyStrings.put(new Integer(13), JFaceResources.getString("Enter"));//$NON-NLS-1$
	keyStrings.put(new Integer(27), JFaceResources.getString("Escape"));//$NON-NLS-1$
	keyStrings.put(new Integer(27), JFaceResources.getString("Esc"));//$NON-NLS-1$
	keyStrings.put(new Integer(127), JFaceResources.getString("Delete"));//$NON-NLS-1$

	keyStrings.put(new Integer(' '), JFaceResources.getString("Space"));//$NON-NLS-1$
	
	keyStrings.put(new Integer(SWT.ARROW_UP), JFaceResources.getString("Arrow_Up"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.ARROW_DOWN), JFaceResources.getString("Arrow_Down"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.ARROW_LEFT), JFaceResources.getString("Arrow_Left"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.ARROW_RIGHT), JFaceResources.getString("Arrow_Right"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.PAGE_UP), JFaceResources.getString("Page_Up"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.PAGE_DOWN), JFaceResources.getString("Page_Down"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.HOME), JFaceResources.getString("Home"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.END), JFaceResources.getString("End"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.INSERT), JFaceResources.getString("Insert"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F1), JFaceResources.getString("F1"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F2), JFaceResources.getString("F2"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F3), JFaceResources.getString("F3"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F4), JFaceResources.getString("F4"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F5), JFaceResources.getString("F5"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F6), JFaceResources.getString("F6"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F7), JFaceResources.getString("F7"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F8), JFaceResources.getString("F8"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F9), JFaceResources.getString("F9"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F10), JFaceResources.getString("F10"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F11), JFaceResources.getString("F11"));//$NON-NLS-1$
	keyStrings.put(new Integer(SWT.F12), JFaceResources.getString("F12"));//$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public boolean isChecked() {
	Object o = value;
	if (o instanceof Boolean)
		return ((Boolean) o).booleanValue();
	return false;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public boolean isEnabled() {
	return enabled;
}
/**
 * Convenience method for removing any optional accelerator text from the given string.
 * The accelerator text appears at the end of the text, and is separated
 * from the main part by a single tab character <code>'\t'</code>.
 *
 * @param text the text
 * @return the text sans accelerator
 */
public static String removeAcceleratorText(String text) {
	int index = text.lastIndexOf('\t');
	if (index == -1)
		index = text.lastIndexOf('@');
	if (index >= 0)
		return text.substring(0, index);
	return text;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void removePropertyChangeListener(IPropertyChangeListener listener) {
	listeners.remove(listener);
}

/**
 * The default implementation of this <code>IAction</code> method
 * does nothing.  Subclasses should override this method
 * if they do not need information from the triggering event,
 * or override <code>run(Event)</code> if they do.
 */
public void run() {
}

/**
 * The default implementation of this <code>IAction</code> method
 * ignores the event argument, and simply calls <code>run()</code>.
 * Subclasses should override this method if they need information 
 * from the triggering event, or override <code>run()</code> if not.
 * 
 * NOTE: This is experimental API, which may change in the future.
 *
 * @since 2.0
 */
public void runWithEvent(Event event) {
	run();
}

/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setActionDefinitionId(String id) {
	actionDefinitionId = id;	
}

/*
 * This method is called by constructors of actions with valid action definitions in the registry.
 * We do not use a constructor Action(String id) to do this work because that would conflict with
 * the pre-existing constructor Action(String text).
 */

/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setChecked(boolean checked) {
	// Use prefab Booleans so that != check in Action.setValue() works.
	Object newValue = checked ? Boolean.TRUE : Boolean.FALSE;
	if (newValue != value) {
		Object oldValue = value;
		value = newValue;
		firePropertyChange(CHECKED, oldValue, value);
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setDescription(String text) {
	
	if ((description == null && text != null)
			|| (description != null && text == null)
				|| (description != null && text != null && !text.equals(description))) {
		String oldDescription = description;
		description = text;
		firePropertyChange(DESCRIPTION, oldDescription, description);
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setDisabledImageDescriptor(ImageDescriptor newImage) {
	if (disabledImage != newImage) {
		ImageDescriptor oldImage = disabledImage;
		disabledImage = newImage;
		firePropertyChange(IMAGE, oldImage, newImage);
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setEnabled(boolean enabled) {
	if (enabled != this.enabled) {
		Boolean oldVal = this.enabled ? Boolean.TRUE : Boolean.FALSE;
		Boolean newVal = enabled ? Boolean.TRUE : Boolean.FALSE;
		this.enabled = enabled;
		firePropertyChange(ENABLED, oldVal, newVal);
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setHelpListener(HelpListener listener) {
	helpListener = listener;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setHoverImageDescriptor(ImageDescriptor newImage) {
	if (hoverImage != newImage) {
		ImageDescriptor oldImage = hoverImage;
		hoverImage = newImage;
		firePropertyChange(IMAGE, oldImage, newImage);
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setId(String id) {
	this.id = id;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void setImageDescriptor(ImageDescriptor newImage) {
	if (image != newImage) {
		ImageDescriptor oldImage = image;
		image = newImage;
		firePropertyChange(IMAGE, oldImage, newImage);
	}
}
/**
 * Sets the menu creator for this action.
 * <p>
 * Note that if this method is called, it overrides the check status.
 * </p>
 *
 * @param creator the menu creator, or <code>null</code> if none
 */
public void setMenuCreator(IMenuCreator creator) {
	value = creator;
}
/**
 * Sets the text for this action.
 * <p>
 * Fires a property change event for the <code>TEXT</code> property
 * if the text actually changes as a consequence.
 * </p>
 *
 * @param text the text, or <code>null</code> if none
 */
public void setText(String text) {
	String oldText = this.text;
	int oldAccel = this.accelerator;
	this.text = text;
	if(text != null) {
		String acceleratorText = extractAcceleratorText(text);
		if (acceleratorText != null) {
			int newAccelerator = convertLocalizedAccelerator(acceleratorText);
			//Be sure to not wipe out the accelerator if nothing found
			if (newAccelerator > 0) {
				setAccelerator(newAccelerator);
			}
		}
	}
	if (!(this.accelerator == oldAccel &&
		(oldText == null ? this.text == null : oldText.equals(this.text)))) {
		firePropertyChange(TEXT, oldText, this.text);
	}
}
/**
 * Sets the tool tip text for this action.
 * <p>
 * Fires a property change event for the <code>TOOL_TIP_TEXT</code> property
 * if the tool tip text actually changes as a consequence.
 * </p>
 *
 * @param toolTipText the tool tip text, or <code>null</code> if none
 */
public void setToolTipText(String toolTipText) {
	String oldToolTipText= this.toolTipText;
	if (!(oldToolTipText == null ? toolTipText == null : oldToolTipText.equals(toolTipText))) {
		this.toolTipText = toolTipText;
		firePropertyChange(TOOL_TIP_TEXT, oldToolTipText, toolTipText);
	}
}
/*
 * @see IAction#setAccelerator(int)
 */
public void setAccelerator(int keycode) {
	this.accelerator = keycode;
}
}
