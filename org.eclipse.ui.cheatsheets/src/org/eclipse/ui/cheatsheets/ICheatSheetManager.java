/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets;

/**
 * <p>While a cheat sheet is running, an implementation of ICheatSheetManager is passed to actions
 * launched by the cheat sheet that implement ICheatSheetAction.  The implementaion of ICheatSheetManager
 * is specific to each cheat sheet available to the user.  It's data is reset when the cheat sheet is
 * restarted and it's data is maintained until the cheat sheet is either re-started or completed.
 * Each cheat sheet that is opened has a unique implementation of ICheatSheetManager.  Data may be stored
 * and retrieved from the implementation of ICheatSheetManager during the lifetime of a cheat sheet.
 * The lifetime of a cheat sheet is from the moment the introductory item is kicked off until the cheat sheet
 * is either completed or re-started.</p>
 */
public interface ICheatSheetManager {

	/**
	 * This method returns the id of the current cheat sheet that is open.
	 * @return the id of the current cheat sheet
	 */
	public String getCheatSheetID();

	/**
	 * This method sets string data to be stored by the ICheatSheetManager using the specified key.
	 * Any data added is persisted accross cheat sheet sessions.
	 * Data is stored until the cheat sheet is completed or restarted.  Any data previously stored
	 * using the key specified will be replaced.
	 * @param key the key to store the data against
	 * @param data string data to be stored
	 */
	public void setData(String key, String data);

	/**
	 * This method returns data that has been stored with the specified key.
	 * @param key the key the data was stored against
	 * @return the string data that was stored against the key.  Null if non existant key
	 */
	public String getData(String key);

	/**
	 * Adds a cheat sheet listener.
	 * 
	 * @param listener the cheat sheet listener to add
	 */
	public void addCheatSheetListener(CheatSheetListener listener);

	/**
	 * Removes a cheat sheet listener.
	 * 
	 * @param listener the cheat sheet listener to remove
	 */
	public void removeCheatSheetListener(CheatSheetListener listener);
}
