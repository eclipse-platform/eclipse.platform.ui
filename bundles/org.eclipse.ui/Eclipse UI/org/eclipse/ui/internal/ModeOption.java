package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.jface.action.IAction;

/**
 * A mode option represents an option available to the user when the user is in
 * a mode. A mode option keeps track of an action and a sequence of accelerator
 * keys. If the sequence of accelerator keys is pressed in order, starting from
 * the active mode, the action will occur. The sequence may be of length 1.
 * <p>
 * In any mode, several mode options may be available.
 */
public class ModeOption {
	private IAction action;
	private List accelerators;
	
	public ModeOption(IAction action, List accelerators) {
		this.action = action;
		this.accelerators = accelerators;
	}
	
	/**
	 * Returns the action of this mode option.
	 */
	public IAction getAction() {
		return action;	
	}
	
	/**
	 * Returns the ordered sequence of keys which should be pressed to cause
	 * this ModeOption's action to occur.
	 */
	public List getAccelerators() {
		return accelerators;
	}
	
	/**
	 * Returns the next accelerator in the sequence.
	 */
	public Integer nextAccelerator() {
		return (Integer)(accelerators.get(0));	
	}
	
	/**
	 * If there is only one accelerator key in the sequence, nextStep() returns
	 * the action that pressing that key will cause to occur. If there are more
	 * than one accelerator key in the sequence, nextStep() returns the ModeOption
	 * that becomes available if the first accelerator in the sequence is pressed.
	 */
	public Object nextStep() {
		if(accelerators.size()==1) {
			return action;
		} else {
			List subList = accelerators.subList(1,accelerators.size());
			return new ModeOption(action, subList);	
		}
	}
}
