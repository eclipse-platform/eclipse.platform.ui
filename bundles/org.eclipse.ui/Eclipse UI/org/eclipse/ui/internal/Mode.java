package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

/**
 * A mode represents a particular state of use in which the user has already
 * pressed a sequence (possibly of length one) of accelerator keys which matches
 * the beginning of a valid sequence of accelerator keys which will cause an
 * action to occur.
 * <p>
 * When a mode is active, the next accelerator key to be pressed is processed
 * in the context of the active mode. If it matches the next key in any valid
 * sequence (a valid sequence is one which will cause an action to occur), a new
 * mode (representing the new full sequence of accelerator keys which have been
 * pressed) will become the active mode. If the next key presses is invalid, the
 * active mode becomes inactive and the user is returned to a modeless state.
 * </p>
 */
public class Mode {
	private List accelerators;
	
	/*
	 * A mapping of valid accelerator keys for this node to the actions they
	 * will produce or the list of mode options they will make available. For
	 * example, if pressing the accelerator key SWT.F1 in the current mode will
	 * cause IAction X to occur, there will be a mapping from SWT.F1 to X. If
	 * pressing SWT.F1 will open a mode with the ModeOptions A,B,C available,
	 * then there will be a mapping from SWT.F1 to a List of A,B,C.
	 * 
	 *  keys: aceclerator keys (Integers)
	 * values: IActions or Lists of ModeOptions
	 */
	private HashMap keyBindings;
	
	public Mode(List accelerators) {
		this.accelerators = accelerators;	
		this.keyBindings = new HashMap();
	}
	
	public Mode(Integer accelerator) {
		this.accelerators = new ArrayList();
		accelerators.add(accelerator);
	}
	
	/**
	 * Returns a list of the accelerators pressed so far, in sequential order.
	 */
	public List getAccelerators() {
		return accelerators;	
	}
	
	/**
	 * Returns the key bindings table for this mode.
	 */
	public HashMap getKeyBindings() {
		return keyBindings;	
	}
	
	/**
	 * The list of ModeOptions which is received represents the options
	 * available from this mode.An option is added to the mode's key
	 * bindings table in the form of a mapping from the first accelerator
	 * key of it's list to either an IAction which that key causes to occur
	 * or a ModeOption of the mode which the key will cause to become active.
	 */
	public void addOptions(List options) {
		for(int i=0; i<options.size(); i++) {
			ModeOption option = (ModeOption)(options.get(i));
			Integer next = option.nextAccelerator();
			Object nextStep = option.nextStep();
			Object o = keyBindings.put(next, nextStep);
		}
	}
}
