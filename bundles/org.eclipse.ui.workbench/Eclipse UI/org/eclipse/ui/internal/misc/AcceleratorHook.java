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
package org.eclipse.ui.internal.misc;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 * A strategy for hooking accelerators on a control.
 * <p>
 * In SWT an accelerator which is defined in a shell menu is global.  If 
 * enabled, it will always be executed regardless of the current focus control or its 
 * abilities.  This creates a problem where the focus control has an equivalent
 * key sequence.  For example, if <code>Ctrl+C</code> is defined as the accelerator
 * for <code>Copy</code> at the shell level it will override the same key sequence
 * in a text widget within the shell, thus breaking the text widget.
 * </p><p>
 * To avoid this problem an <code>AcceleratorHook</code> may be used to define
 * the accelerators locally on each control rather than globally on the shell.
 * The accelerators defined in an <code>AcceleratorHook</code> are only operational
 * when the control has focus.
 * </p><p>
 * To use this class, instantiate an instance for a particular control and register
 * each action which has an accelerator.  If the accelerator is pressed the action will
 * be invoked.
 * </p><p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class AcceleratorHook implements Listener {
	private ArrayList actionList;
	
	private class ActionItem {
		public ActionItem(int accel, IAction act) {
			accelerator = accel;
			action = act;
		}
		public int accelerator;
		public IAction action;
	}

	private class FakeAction extends Action {
		public FakeAction(String name) {
			super(name);
		}
		public void run() {
		}
	}
/**
 * AcceleratorHook constructor comment.
 */
public AcceleratorHook(Control ctrl) {
	actionList = new ArrayList(5);
	ctrl.addListener(SWT.KeyDown, this);
	ctrl.addListener(SWT.KeyUp, this);
}
/**
 * Adds an action to the control.  If the action accelerator is pressed
 * the action will be run.
 * <p>
 * The accelerator for the action is derived from the action.   
 * </p>
 *
 * @param action an action with unique accelerator
 */
public void add(IAction action) {
	if (action.getAccelerator() == 0)
		return;
	actionList.add(new ActionItem(action.getAccelerator(), action));
}
/**
 * Adds an action to the control with a particular accelerator.  
 * If the accelerator is pressed the action will be run.
 * <p>
 * The accelerator for the action is derived from <code>strAccel</code>
 * string. The content of this string must conform to the standard JFace
 * conventions for accelerator declaration.  For more information see
 * the <code>org.eclipse.jface.action.Action</code> class.
 * </p>
 *
 * @param action an action
 * @param strAccel the action accelerator
 * @see org.eclipse.jface.action.Action
 */
public void add(IAction action, String strAccel) {
	Action fakeAction = new FakeAction("Fake\t"+strAccel);//$NON-NLS-1$
	if (fakeAction.getAccelerator() == 0)
		return;
	actionList.add(new ActionItem(fakeAction.getAccelerator(), action));
}
/**
 * Returns the first item which represents the action.
 *
 * @returns the first item to match, or <code>null</code>.
 */
private ActionItem findItem(IAction action) {
	Iterator iter = actionList.iterator();
	while (iter.hasNext()) {
		ActionItem item = (ActionItem)iter.next();
		if (item.action == action)
			return item;
	}
	return null;
}
/**
 * Returns the first item with an accelerator which maches
 * the key event.
 *
 * @returns the first item to match, or <code>null</code>.
 */
private ActionItem findItem(Event e) {
	// Map event to accel.
	int accel = getAccel(e);
	if (accel == 0)
		return null;
	
	// Map accelerator to item.
	Iterator iter = actionList.iterator();
	while (iter.hasNext()) {
		ActionItem item = (ActionItem)iter.next();
		// System.out.println("Accel = " + Integer.toString(item.accelerator, 16));
		if (item.accelerator == accel)
			return item;
	}
	return null;
}
/**
 * Convert a key event to an accelerator.
 *
 * @param e the key event
 * @return the int accelerator value
 */
private int getAccel(Event e) {
	// Debug.
	/*
	System.out.println("KeyEvent");
	System.out.println("\tChar = " + Integer.toString((int)e.character, 16));
	System.out.println("\tKeyCode = " + Integer.toString(e.keyCode, 16));
	System.out.println("\tState Mask = " + Integer.toString(e.stateMask, 16));
	*/
	
	// Real work.
	int key = (int)Character.toUpperCase(e.character);
	int mods = 0;
	if ((e.stateMask & SWT.ALT) > 0)
		mods |= SWT.ALT;
	if ((e.stateMask & SWT.SHIFT) > 0)
		mods |= SWT.SHIFT;
	if ((e.stateMask & SWT.CTRL) > 0) {
		mods |= SWT.CTRL;
		key = key + 'A' - 1; // Convert unicode to char.
	}
	int accel = key | mods | e.keyCode;

	// Debug
	/*
	System.out.println("Accel = " + Integer.toString(accel, 16));
	*/
	return accel;
}

/**
 * Notifies that a key has been pressed on the system keyboard.
 * <p>
 * This method is a callback from the target control for this hook.
 * Other clients are not expected to call this method.
 * </p>
 *
 * @param e an event containing information about the key press
 */
public void handleEvent(Event event) {
	ActionItem item = findItem(event);
	if (item != null)
		item.action.runWithEvent(event);
}

/**
 * Removes an action from the hook.  Does nothing if the action is
 * not found.
 *
 * @param action an action
 */
public void remove(IAction action) {
	ActionItem item = findItem(action);
	if (item != null)
		actionList.remove(item);
}
}
