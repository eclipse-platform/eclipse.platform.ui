package org.eclipse.debug.ui;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.operation.IRunnableContext;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ILaunchConfigurationDialog extends IRunnableContext {
	
	/**
	 * Constant used as return value from <code>open()</code> method of a
	 * launch configuration dialog.
	 */
	public static final int SINGLE_CLICK_LAUNCHED = 2;
			
	/**
	 * Adjusts the enable state of the Launch button
	 * to reflect the state of the currently active 
	 * pages in this launch configuration dialog.
	 * <p>
	 * This may be called by a page to force a button state
	 * update.
	 * </p>
	 */
	public void updateButtons();
	
	/**
	 * Updates the message (or error message) shown in the message line to 
	 * reflect the state of the currently active page in this launch
	 * configuration dialog.
	 * <p>
	 * This method may be called by a page to force a message 
	 * update.
	 * </p>
	 */
	public void updateMessage();
	
	/**
	 * Sets the contents of the name field to the given name.
	 * 
	 * @param name new name value
	 */ 
	public void setName(String name);
	
	/**
	 * Returns a unique launch configuration name, using the given name
	 * as a seed.
	 * 
	 * @param name seed from which to generate a new unique name
	 */ 
	public String generateName(String name);
	
	/**
	 * Returns the tabs currently being displayed, or
	 * <code>null</code> if none.
	 * 
	 * @return currently displayed tabs, or <code>null</code>
	 */
	public ILaunchConfigurationTab[] getTabs();
	
	/**
	 * Returns the mode in which this dialog was opened -
	 * run or debug.
	 * 
	 * @return one of <code>RUN_MODE</code> or <code>DEBUG_MODE</code>
	 * @see ILaunchManager
	 */
	public String getMode();		
		
}
