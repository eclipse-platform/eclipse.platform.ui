/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets;

import java.util.Map;


/**
 *<p> While a cheat sheet is running, an implementation of ICheatSheetManager is passed to actions
 * launched by the cheat sheet that implement ICheatSheetAction.  A handle to an instance of an 
 * ICheatSheetManager is also available to in the events received by implementors of ICheatSheetViewListener
 * and ICheatSheetItemListener.  The implementaion of ICheatSheetManager is specific to each cheat sheet
 * available to the user.  It's data is reset when the cheat sheet is restarted and it's data is maintained until the cheat sheet
 * is either re-started or completed.  Each cheat sheet that is opened has a unique implementation of ICheatSheetManager.
 * Data may be stored and retrieved from the implementation of ICheatSheetManager during the lifetime of a cheat sheet.  
 * The lifetime of  a cheat sheet is from the moment the introductory item is kicked off until the cheat sheet is either completed or
 * re-started.  The implementation  received by cheat sheet actions and listeners may also be used to access and 
 * change information for all of the steps and sub steps in the cheat sheet that are marked as being dynamic in the cheat sheet
 * content file. </p>
 * 
 * <p>The ICheatSheetManager interface provides access to the data representation of the steps
 * in the cheat sheet view.  Each step in the cheat sheet view is represented by an IItem.
 * If a step in the view has sub items, this step is represented by an IItemWithSubItems.
 * Both IItem and IItemWithSubItems have fields that can be changed.
 * IItems and IItemWithSubItems can only be changed if the item in the cheat sheet content file 
 * is marked as a "dynamic" item, then that action can be changed on the fly.  </p>
 * 
 *<p> This ICheatSheetManager interface allows users to get a handle to any of the IItems or IItemWithSubItems
 * in the running cheat sheet.  It also allows the changing of a regular IItem into an IItemWithSubItems
 * if a regular cheat sheet step needs to be changed into a step with sub steps.</p>
 * 
 */
public interface ICheatSheetManager {

	/**
	 * This method returns the id of the current cheat sheet that is open.
	 * @return the id of the current cheat sheet
	 */
	public String getCheatSheetID();
	/**
	 * This method returns an IAbstractItem representing a step in the cheat sheet that corresponds to the specified id.
	 * The returned IAbstractItem could be either an IItem or an IItemWithSubItems and can be checked and cast to one of
	 * those interfaces for data access and manipulation.
	 * @param id the id of the item you want to access
	 * @return IAbstractItem, the item you want to access.  <code>null</code> if there is no item with that id
	 */
	public IAbstractItem getItem(String id);
	/**
	 * This method adds string data to be stored by the ICheatSheetManager using the specified key.
	 * Any data added is persisted accross cheat sheet sessions.
	 * Data is stored until the cheat sheet is completed or restarted.  Any data previously stored
	 * using the key specified will be replaced.
	 * @param key the key to store the data against
	 * @param data string data to be stored
	 */
	public void addData(String key, String data);
	/**
	 * This method returns data that has been stored with the specified key.
	 * @param key the key the  data was stored against
	 * @return the string data that was stored against the key.  Null if non existant key
	  */
	public String getData(String key);
	/**
	 * This method allows you to access a map of all all the data that has been stored in the 
	 * ICheatSheetManager.
	 * @return a map of the data stored
	 */
	public Map getData();
	
	/**
	 * This method attempts to remove the data stored with the given key from the ICheatSheetManager.
	 * @param key the key that you want to remove the data for
	 * @return true if the data with the given key is removed, false if it was not or if the key is not found
	 */
	public boolean removeData(String key);
	
	/**
	 * This method allows you to convert the dynamic IItem that was returned from the call to getItem
	 * to an IItemWithSubItems.  You only need to do this if there was no sub items specified
	 * for this item in the cheat sheet content xml file and you want to change it from a regular cheat sheet step
	 * to a step with sub steps.  The item must be specified as dynamic in the cheat sheet content file.
	 * @param ai the abstract item from a call to getItem
	 * @return the IItem converted to an IItemWithSubItems so that you may add sub items to it. <code>null</code> if the argument cannot be converted
	 */
// TODO: Need a way to convert it way as well!
	public IItemWithSubItems convertToIItemWithSubItems(IAbstractItem ai);
	
}
