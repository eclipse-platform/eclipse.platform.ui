/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.win32;
public interface ICommandStateChangedListener {
	/**
	 */
	public void commandStateChanged(boolean backEnabled, boolean forwardEnabled);
}