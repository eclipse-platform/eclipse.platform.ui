/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

/**
 * Manages the running of a cheat sheet.
 * <p>
 * Each cheat sheet that is opened in the UI is assigned its own cheat sheet
 * manager, which stays with it until the cheat sheet is completed (or
 * restarted). The cheat sheet manager is passed as a parameter to cheat
 * sheet-aware actions which implement {@link ICheatSheetAction}. The manager
 * carries arbitrary key-value data (strings) for the lifetime of a cheat sheet,
 * and can be accessed via {@link #getData(String)}and
 * {@link #setData(String, String)}. If the workbench is shut down while the
 * cheat sheet is in progress, this data will generally be saved and later
 * restored when the workbench is restarted and cheat sheet is resumed. The
 * manager also supports listeners, which are kept informed of life cycle events
 * over the course of the cheat sheet's life time.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.0
 */
public interface ICheatSheetManager {

	/**
	 * Returns the id of the cheat sheet managed by this manager.
	 * 
	 * @return the cheat sheet id
	 */
	public String getCheatSheetID();

	/**
	 * Returns the data value associated with the given key.
	 * 
	 * @param key the key
	 * @return the string data associated with the key, or
	 * <code>null</code> none
	 */
	public String getData(String key);

	/**
	 * Sets the data value associated with the given key.
	 * <p>
	 * Data associated with a cheat sheet manager is remembered
	 * for the life of the manager. All data is discarded when 
	 * the cheat sheet is completed (or restarted).
	 * </p>
	 * 
	 * @param key the key
	 * @param data the string data associated with the key,
	 * or <code>null</code> to remove
	 */
	public void setData(String key, String data);

	/**
	 * Adds a cheat sheet listener to this cheat sheet manager.
     * Has no effect if an identical listener is already registered.
	 * 
	 * @param listener the cheat sheet listener to add
	 */
	public void addCheatSheetListener(CheatSheetListener listener);

	/**
	 * Removes a cheat sheet listener from this cheat sheet manager.
     * Has no affect if the listener is not registered.
	 * 
	 * @param listener the cheat sheet listener to remove
	 */
	public void removeCheatSheetListener(CheatSheetListener listener);
}
