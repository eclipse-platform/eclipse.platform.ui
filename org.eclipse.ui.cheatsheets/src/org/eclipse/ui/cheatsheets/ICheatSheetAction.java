/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets;

/**
 * <p>This interface should be implemented by action classes used by the
 * cheat sheet view, however it is not mandatory.  If action classes implement this
 * interface, parameters that have been set to be passed to this action
 * by the cheat sheet are passed when the action is run.  Furthermore, if this interface
 * is implemented, a handle to the implementation of ICheatSheetManager is passed 
 * while running the action.</p>
 * <p>The action that implements this interface is specified as the class for the class="com.org.xyz.XYZ" portion of the 
 * &lt;item&gt; tag for the cheat sheet content file to be run as the action for a step in the cheat sheet.
 * </p>
 */
public interface ICheatSheetAction {

	/**
	 * This method will be called when the action is run.
	 * Implementors of this method need not do anything with the 
	 * parameters, however, this provides the CheatSheetManager object
	 * access, as well as accomodation for retrieving parameters set in the
	 * cheat sheet content file, or added dynamically during the cheat sheet's
	 * lifetime.
	 * @param params an array of strings
	 * @param csm the ICheatSheetManager used to access the items
	 */
	public void run(String [] params, ICheatSheetManager csm);

}
