/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets.events;

/**
 * <p>This interface should be implemented if you need to be notified of view events for the cheat sheets view.
 * Sample view events that are fired include the opening and closing events of the view.  A listener that implements this
 * ICheatSheetViewListener must register itself using the cheat sheet extension point.  Here is a sample of a view
 * listener extension point implementation:  <br>
 *&lt;extension point="org.eclipse.ui.cheatsheets.cheatSheetContent"&gt;<br>
 * &lt;cheatSheetListener<br>
*	id="org.xyz.com.Listener"<br>
*			targetCheatSheetId="org.xyz.com.cheatSheetId"<br>
*			class="org.xyz.com.Listener"<br>
*			classPluginId="org.xyz.com.Plugin"&gt;<br>
*		&lt;/cheatSheetListener&gt;<br>
* &lt;extension&gt; <br>
 * </p>
 */
public interface ICheatSheetViewListener extends ICheatSheetListener {

	/**
	 * This method is triggered when a cheat sheet is selected and the cheat sheets view is opened.
	 * @param e the cheat sheet view opened event fired
	  */
	public void cheatSheetOpened(ICheatSheetViewEvent e);
	/**
	 * This method is triggered when the cheat sheet view is closed.
	 * @param e the cheat sheet view closed event fired
	  */
	public void cheatSheetClosed(ICheatSheetViewEvent e);
	/**
	 * This method is called when a cheat sheet is started by the intro item's button having been pushed.
	 * @param e the cheat sheet started event
	 * 	  
	 */
	public void cheatSheetStarted(ICheatSheetViewEvent e);
	/**
	 * This method is triggered when the cheat sheet has been started and the intro item is 
	 * clicked again.  The cheat sheet is restarted when the intro is clicked after the cheat sheet
	 * has already been started.
	 * @param e the cheat sheet restarted event fired
	  */
	public void cheatSheetRestarted(ICheatSheetViewEvent e);
	/**
	 * This method is triggered when the cheat sheet is finished.  The sheet may still be open,
	 * but the last specified item has been either marked as completed or skipped.
	 * @param e the cheat sheet end reached event fired
	  */
	public void cheatSheetEndReached(ICheatSheetViewEvent e);

}
 