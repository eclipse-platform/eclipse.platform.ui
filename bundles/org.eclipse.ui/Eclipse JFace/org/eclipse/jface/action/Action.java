package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionDefinition;
import org.eclipse.ui.internal.registry.ActionDefinitionRegistry;


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
 * @param acceleratorText the accelerator text
 * @result the SWT key code, or 0 if there is no accelerator
 */
private int convertAccelerator(String acceleratorText) {
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
		Object old = new Boolean(this.enabled);
		this.enabled = enabled;
		firePropertyChange(ENABLED, old, new Boolean(enabled));
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
			int newAccelerator = convertAccelerator(acceleratorText);
			//Be sure to not wipe out the accelerator if nothing found
			if(newAccelerator > 0)
				setAccelerator(newAccelerator);
		}
	}
	if (this.accelerator != oldAccel 
			|| (this.text == null && oldText != null)
				|| (this.text != null && oldText == null)
					|| (this.text != null && oldText != null && !oldText.equals(this.text))) {
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
 * @param text the tool tip text, or <code>null</code> if none
 */
public void setToolTipText(String text) {
	if ((toolTipText == null && text != null)
			|| (toolTipText != null && text == null)
				|| (toolTipText != null && text != null && !text.equals(toolTipText))) {
		String oldToolTipText= toolTipText;
		toolTipText= text;
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
