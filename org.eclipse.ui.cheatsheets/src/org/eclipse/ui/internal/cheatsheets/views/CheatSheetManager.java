/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.views;

import java.util.*;

import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.cheatsheets.events.*;

/**
 * Cheat sheet manager class.  Manages cheat sheet data,
 * enables access to model for adding/modifying steps and sub steps
 * on the cheat sheet "on the fly".
 */
public class CheatSheetManager implements ICheatSheetManager {

	private String cheatsheetID;
	private Hashtable listenerMap = new Hashtable(20);
	private Hashtable itemListenerMap = new Hashtable(20);
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
	public IAbstractItem getItem(String id) {
		try {
			//Check to see if that item with that id is dynamic.
			//If it is not dynamic, return null for it cannot be modified.
			ArrayList contentItems = csview.getListOfContentItems();
			for (int i = 0; i < contentItems.size(); i++) {
				IAbstractItem contentItem = (IAbstractItem) contentItems.get(i);
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

	public IItemWithSubItems convertToIItemWithSubItems(IAbstractItem ai) {
		if (ai instanceof IItemWithSubItems)
					return (IItemWithSubItems)ai;
		if (!(ai instanceof IItem))
			return null;
		String id = ai.getID();
		ArrayList contentItems = csview.getListOfContentItems();
		for (int i = 0; i < contentItems.size(); i++) {
			IAbstractItem contentItem = (IAbstractItem) contentItems.get(i);
			if (contentItem.getID().equals(id)) {
				ContentItemWithSubItems itemws = convertThisIItem((IItem) contentItem);
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

	private ContentItemWithSubItems convertThisIItem(IItem item) {
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
				ICheatSheetListener c = (ICheatSheetListener) newList.get(i);
				if (c instanceof ICheatSheetItemListener)
					itemList.add(c);
				if (c instanceof ICheatSheetViewListener)
					viewList.add(newList.get(i));
			}
			if (itemList.size() > 0)
				itemListenerMap.put(cheatsheetID, itemList);
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

		if (e instanceof CheatSheetViewEvent)
			notifyViewListeners((CheatSheetViewEvent) e, cheatsheetID);
		else if (e instanceof CheatSheetItemEvent)
			notifyItemListeners((CheatSheetItemEvent) e, cheatsheetID);
	}

	/**
	 * Notifies all Item listeners registered of events.
	 */
	private void notifyItemListeners(CheatSheetItemEvent e, String cheatsheetID) {
		ArrayList listeners = (ArrayList) itemListenerMap.get(cheatsheetID);
		if (listeners == null)
			return;

		switch (e.getCheatSheetEventType()) {
			case ICheatSheetItemEvent.ITEM_ACTIVATED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetItemListener) listeners.get(i)).itemActivated(e);
				}
				break;
			case ICheatSheetItemEvent.ITEM_COMPLETED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetItemListener) listeners.get(i)).itemCompleted(e);
				}
				break;
			case ICheatSheetItemEvent.ITEM_DEACTIVATED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetItemListener) listeners.get(i)).itemDeactivated(e);
				}
				break;
			case ICheatSheetItemEvent.ITEM_PERFORMED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetItemListener) listeners.get(i)).itemPerformed(e);
				}
				break;
			case ICheatSheetItemEvent.ITEM_SKIPPED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetItemListener) listeners.get(i)).itemSkipped(e);
				}
				break;
		}
	}

	/**
	 * Notifies all View listeners registered of events.
	 */
	private void notifyViewListeners(CheatSheetViewEvent e, String cheatsheetID) {
		//		System.out.println("Inside manager notifyViewListeners!");
		ArrayList listeners = (ArrayList) viewListenerMap.get(cheatsheetID);
		if (listeners == null)
			return;

		switch (e.getCheatSheetEventType()) {
			case ICheatSheetViewEvent.CHEATSHEET_OPENED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetViewListener) listeners.get(i)).cheatSheetOpened(e);
				}
				break;
			case ICheatSheetViewEvent.CHEATSHEET_CLOSED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetViewListener) listeners.get(i)).cheatSheetClosed(e);
				}
				break;
			case ICheatSheetViewEvent.CHEATSHEET_END_REACHED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetViewListener) listeners.get(i)).cheatSheetEndReached(e);
				}
				break;
			case ICheatSheetViewEvent.CHEATSHEET_RESTARTED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetViewListener) listeners.get(i)).cheatSheetRestarted(e);
				}
				break;
			case ICheatSheetViewEvent.CHEATSHEET_STARTED :
				for (int i = 0; i < listeners.size(); i++) {
					((ICheatSheetViewListener) listeners.get(i)).cheatSheetStarted(e);
				}
				break;
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

}
