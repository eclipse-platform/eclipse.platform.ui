package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDisconnect;

public class DisconnectActionDelegate extends ControlActionDelegate {
	
	private static final String PREFIX= "disconnect_action.";

	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object element) throws DebugException {
		if (element instanceof IDisconnect)
			 ((IDisconnect) element).disconnect();
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IDisconnect && ((IDisconnect) element).canDisconnect();
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
}