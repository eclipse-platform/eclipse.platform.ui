/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets.events;


/**
 * <p>This interface should be implemented if you need to be notified of item events for the cheat sheets.
 * Sample item events that are fired include the performing of an action for an item and the notification when an item
 * becomes the active item that is highlited in the cheat sheets view.  Items are identified by their id's as specified in the
 * cheat sheet content file describing the steps in the cheat sheet.   A listener that implements this ICheatSheetItemListener
 * must register itself using the cheat sheet extension point.  Here is a sample of a view listener extension point implementation:<br>
 *&lt;extension point="org.eclipse.ui.cheatsheets.cheatSheetContent"&gt;<br>
 * &lt;cheatSheetListener<br>
*			id="org.xyz.com.ItemListener"<br>
*			targetCheatSheetId="org.xyz.com.cheatSheetId"<br>
*			class="org.xyz.com.ItemListener"<br>
*			classPluginId="org.xyz.com.Plugin"&gt;<br>
*		&lt;/cheatSheetListener&gt; <br>
* &lt;/extension&gt; <br>
 * </p>
 */

public interface ICheatSheetItemListener extends ICheatSheetListener {

	/**
	 * This method is triggered when the view has an item that becomes the active item in the view.
	 * When an item becomes the active item it is the step that is highlighted in the view.
	 * @param e the item activated event fired
	  */
	public void itemActivated(ICheatSheetItemEvent e);
	/**
	 * This method is triggered when an item has it's action called and the action completes.
	 * If the action for an item spawns a wizard, the item will only be completed if the wizard is finished, not cancelled.
	 * @param e the item performed event fired
	  */
	public void itemPerformed(ICheatSheetItemEvent e);
	/**
	 * This method is triggered after an item in the cheat sheet that has it's skip button pushed
	 * and it is marked as skipped.
	 * @param e the item skipped event fired
	  */
	public void itemSkipped(ICheatSheetItemEvent e);
	/**
	 * This method is triggered when an item is marked as completed in the view either by having it's 
	 * action class called and completed, or by pressing the click when done button.
	 * @param e the item completed event fired
	  */
	public void itemCompleted(ICheatSheetItemEvent e);
	/**
	 * This method is called after an item is no longer the active item.
	 * This method is notified when an item is either completed or was skipped and
	 * the next step in the cheat sheet becomes the active step.
	 * @param e the item deactivated event fired
	  */
	public void itemDeactivated(ICheatSheetItemEvent e);

}
