/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import java.util.*;

import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.cheatsheets.*;

/**
 * Cheat sheet manager class.  Manages cheat sheet data,
 * enables access to model for adding/modifying steps and sub steps
 * on the cheat sheet "on the fly".
 */
public class CheatSheetManager implements ICheatSheetManager {

	private String cheatsheetID;
	private Hashtable listenerMap = new Hashtable(20);
	private Hashtable viewListenerMap = new Hashtable(20);
	private Hashtable dataTable = null;
	private CheatSheetView csview;

	//Package protected:  We don't want anyone else creating instances of this class.	
	CheatSheetManager(String id, CheatSheetView csv) {
		csview = csv;
		cheatsheetID = id;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetManager#getCheatSheetID()
	 */
	public String getCheatSheetID() {
		return cheatsheetID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetManager#getItemWithID(java.lang.String)
	 */
	public AbstractItem getItem(String id) {
		try {
			//Check to see if that item with that id is dynamic.
			//If it is not dynamic, return null for it cannot be modified.
			ArrayList contentItems = csview.getListOfContentItems();
			for (int i = 0; i < contentItems.size(); i++) {
				AbstractItem contentItem = (AbstractItem) contentItems.get(i);
				if (contentItem.getID().equals(id)) {
					//return contentItem;
					if (contentItem instanceof IContainsContent) {
						IContainsContent cc = (IContainsContent) contentItem;
						if (cc.isDynamic())
							return contentItem;
					}
					return null;
				}

			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public ContentItemWithSubItems convertToIItemWithSubItems(AbstractItem ai) {
		if (ai instanceof ContentItemWithSubItems)
					return (ContentItemWithSubItems)ai;
		if (!(ai instanceof ActionItem))
			return null;
		String id = ai.getID();
		ArrayList contentItems = csview.getListOfContentItems();
		for (int i = 0; i < contentItems.size(); i++) {
			AbstractItem contentItem = (AbstractItem) contentItems.get(i);
			if (contentItem.getID().equals(id)) {
				ContentItemWithSubItems itemws = convertThisIItem((ContentItem) contentItem);
				//replace item in list with new item.
				contentItems.set(i, itemws);
				//replace coreItem's contentItem with our new one.
				ViewItem[] va = csview.getViewItemArray();
				for(int j=0; j<va.length; j++){
					if(va[j].contentItem == contentItem)
						va[j].contentItem = itemws;	
				}			
				
				return itemws;
			}
		}
		return null;
	}

	private ContentItemWithSubItems convertThisIItem(ContentItem item) {
		if (!(item instanceof ContentItem))
			return null;
		else {
			ContentItem cc = (ContentItem) item;
			ContentItemWithSubItems itemws = new ContentItemWithSubItems();
			itemws.setContent(cc.getContent());
			itemws.setID(cc.getID());
			return itemws;
		}
	}

	private void fillListenerMaps(String cheatsheetID) {
		ArrayList newList = CheatSheetPlugin.getPlugin().getListenerObjects(cheatsheetID);
		if (newList != null) {
			listenerMap.put(cheatsheetID, newList);
			ArrayList itemList = new ArrayList(20);
			ArrayList viewList = new ArrayList(20);
			for (int i = 0; i < newList.size(); i++) {
				CheatSheetListener c = (CheatSheetListener) newList.get(i);
				viewList.add(newList.get(i));
			}
			if (viewList.size() > 0)
				viewListenerMap.put(cheatsheetID, viewList);
		}

	}

	//Package protected:  We don't want anyone else firing events but the view.
	//this method will be called by the c.s. view when events occur.
	void fireEvent(ICheatSheetEvent e) {
		//		System.out.println("Inside Manager Fire Event!");
		String cheatsheetID = e.getCheatSheetID();
		if (cheatsheetID == null)
			return;

		//First check to see if we have the listener classes for this cheatsheet id.
		ArrayList list = (ArrayList) listenerMap.get(cheatsheetID);
		if (list == null)
			fillListenerMaps(cheatsheetID);

		notifyListeners(e, cheatsheetID);
	}

	/**
	 * Notifies all listeners registered of events.
	 */
	private void notifyListeners(ICheatSheetEvent e, String cheatsheetID) {
		//		System.out.println("Inside manager notifyViewListeners!");
		ArrayList listeners = (ArrayList) viewListenerMap.get(cheatsheetID);
		if (listeners == null)
			return;

		for (int i = 0; i < listeners.size(); i++) {
			((CheatSheetListener) listeners.get(i)).cheatSheetEvent(e);
		}
	}

	/**
	 * adds string data to manager data hashtable.
	 * Stores only keyed string data.
	 */
	public void addData(String key, String data) {
		if(key == null || data == null)
			return;
		if (dataTable == null)
			dataTable = new Hashtable(30);
		dataTable.put(key, data);
		return;
	}

	public String getData(String key) {
		if (dataTable == null)
			return null;
		return (String) dataTable.get(key);
	}

	/**
	 * returns the hashtable with all manager data stored.
	 */
	public Map getData() {
		return (Map)dataTable;
	}

	/*package*/ void setData(Hashtable ht) {
		dataTable = ht;
	}

	/**
	 * Removes data from manager data storage hashtable stored with key passed as argument.
	 * returns true if data is removed from manager data storage hashtable.
	 * returns false if no data with the key passed is stored.
	 */
	public boolean removeData(String key) {
		if (dataTable == null)
			return false;
		else if (dataTable.remove(key) != null)
			return true;
		return false;
	}
	
	/*PACKAGE*/ boolean removeAllData(){
		dataTable = new Hashtable(30);
		return true;	
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetManager#setData(java.lang.String, java.lang.String)
	 */
	public void setData(String key, String data) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		// TODO Auto-generated method stub
	}


	/**
	 * Adds a cheat sheet listener to this cheat sheet manager.
     * Has no effect if an identical listener is already registered.
	 * 
	 * @param listener the cheat sheet listener to add
	 * @exception IllegalArgumentException if <code>listener</code>
	 * is <code>null</code>
	 */
	public void addCheatSheetListener(CheatSheetListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		// TODO (lorne) - missing implementation
		throw new RuntimeException("Not implemented yet"); //$NON-NLS-1$
	}


	/**
	 * Removes a cheat sheet listener from this cheat sheet manager.
     * Has no affect if the listener is not registered.
	 * 
	 * @param listener the cheat sheet listener to remove
	 * @exception IllegalArgumentException if <code>listener</code>
	 * is <code>null</code>
	 */
	public void removeCheatSheetListener(CheatSheetListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		// TODO (lorne) - missing implementation
		throw new RuntimeException("Not implemented yet"); //$NON-NLS-1$
	}

}
