/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.data;

import java.util.*;

import org.eclipse.ui.cheatsheets.*;

/**
 *<p>This interface allows access to read and change information on a step that has sub steps in the cheat sheet. 
 *An IItemWithSubItems represents a step in the cheat sheet that has sub steps.
 *An IItemWithSubItems is received from a call to getItem on ICheatSheetManager if the id passed 
 *to the getItem method matches the id of a step in the cheat sheet that has sub steps.</p>
 *
 *<p>In order to change or manipulate data on an <code>IItemWithSubItems</code> or an <code>IItem</code>
 *it must be declared as being dynamic in the cheat sheet content file.
 *If there is a step in the cheat sheet that does not have sub items but you want to add sub items to it,
 *you must first get a handle to the IItem for that step using getItem on ICheatSheetManager.  After you have
 *the IItem representing the step you want to have sub items, you must call convertToIItemWithSubItems
 *on ICheatSheetManager with this IItem.  This IItem will be replaced by an IItemWithSubItems, and the cheat sheet
 *will display any sub items added to it when that step is activated in the cheat sheet.
 *You will have an IItemWithSubItems returned that can have ISubItems added to it, which will turn the 
 *step in the cheat sheet into a step with sub steps.</p>
 *
 * <p>Note:  You may only use these methods to change the step if it has been marked as
 * "dynamic" in the cheat sheet content file.</p>
 */
public class ContentItemWithSubItems extends AbstractItem implements IContainsContent {
	private Content content;

	private ArrayList subItems;

	/**
	 * 
	 */
	public ContentItemWithSubItems() {
		super();
		content = new Content();
		subItems = new ArrayList(10);
	}

	/**
	 * This method adds a sub item to this item in the cheat sheet.
	 * @param sub  the ISubItem to add.
	 * @return true if it was added, false if it was not added
	 * @throws IllegalArgumentException if the sub item passed is not an ISubItem
	 */
	public boolean addSubItem(SubContentItem sub) {
		if(isDuplicateId(sub.getID()))
			return false;
		if(subItems == null)
			subItems = new ArrayList(20);
		subItems.add(sub);
		return true;
	}

	/**
	 * This method is similar to addSubItem(ISubItem) but you may specify the index you want 
	 * to add it to the list of sub items to be shown in the cheat sheet for this item.
	 * @param sub the ISubItem to add
	 * @param index the index where the ISubItem will be placed or inserted
	 * @return true if it was added, false if not
	 * @throws IndexOutOfBoundsException if the index specified is out of bounds
	 */
	public boolean addSubItem(SubContentItem sub, int index) throws IndexOutOfBoundsException {
		if(isDuplicateId(sub.getID()))
			return false;
		subItems.add(index, sub);
		return true;
	}

	/**
	 * This method allows to set the sub items that will be shown for this item in the cheat sheet.
	 * @param subitems an array of ISubItem's
	 * @throws IllegalArgumentException if the array is not an array of ISubItem's
	 */
	public void addSubItems(SubContentItem[] subitems) {
		if (subitems == null)
			this.subItems = null;
		else
			this.subItems = new ArrayList(Arrays.asList(subitems));
	}

	public Content getContent() {
		return this.content;
	}

	/**
	 * Returns the helpLink.
	 * @return String
	 */
	public String getHref() {
		return content.getHref();
	}

	/**
	 * This method returns the sub item for this item from the specified index in the list of sub items for this item.
	 * @param index of the sub item to retrieve
	 * @return the ISubItem 
	 * @throws IndexOutOfBoundsException if the sub item at the index does not exist
	 */
	public SubContentItem getSubItem(int index) throws IndexOutOfBoundsException {
		return (SubContentItem) subItems.get(index);
	}

	/**
	 * This method returns an array of the sub items specified for this item in the cheat sheet.
	 * @return an array of the ISubItems
	 */
	public SubContentItem[] getSubItems() {
		if(subItems == null)
			return null;
		return (SubContentItem[]) subItems.toArray(new SubContentItem[subItems.size()]);
	}

	/**
	 * Returns the text.
	 * @return String
	 */
	public String getText() {
		return content.getText();
	}

	/**
	 * Returns the title.
	 * @return String
	 */
	public String getTitle() {
		return content.getTitle();
	}
	
	private boolean isDuplicateId(String id){
		if(subItems!=null)
		for(int i=0; i<subItems.size(); i++){
			SubContentItem isi = (SubContentItem)subItems.get(i);	
			if(isi.getID().equals(id))
				return true;
		}
		return false;
	}

	public boolean isDynamic() {
		return content.isDynamic();
	}

	/**
	 * This method removes the ISubItem at the specified index from the list of sub items to show for this item in the cheat sheet.
	 * @param index of the sub item to remove
	 * @return true if removed, false if not
	 * @throws IndexOutOfBoundsException if the index is out of bounds
	 */
	public boolean removeSubItem(int index) throws IndexOutOfBoundsException {
		subItems.remove(index);
		return true;
	}

	/**
	 * This method removes the ISubItem specified from the list of sub items to show for this item in the cheat sheet.
	 * @param item the ISubItem to remove.
	 * @return true if removed, false if it either does not exist or could not be removed
	 */
	public boolean removeSubItem(SubContentItem item) {
		int index = subItems.indexOf(item);
		if (index != -1) {
			subItems.remove(index);
			return true;
		} else
			return false;
	}

	/**
	 * This method removes all of the sub items for this item.
	 */
	public void removeSubItems() {
		subItems = new ArrayList();
	}

	public void setContent(Content c) {
		this.content = c;
	}

	/**
	 * Sets the helpLink.
	 * @param helpLink The helpLink to set
	 */
	public void setHref(String helpLink) {
		content.setHref(helpLink);
	}

	public void setIsDynamic(boolean b) {
		content.setDynamic(b);
	}

	/**
	 * Sets the text.
	 * @param text The text to set
	 */
	public void setText(String text) {
		content.setText(text);
	}

	/**
	 * Sets the title.
	 * @param title The title to set
	 */
	public void setTitle(String title) {
		content.setTitle(title);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.cheatsheets.data.IContainsContent#getItemExtensions()
	 */
	public ArrayList getItemExtensions() {
		return content.getItemExtensions();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.cheatsheets.data.IContainsContent#setItemExtensions(java.util.ArrayList)
	 */
	public void setItemExtensions(ArrayList itemExtensions) {
		content.setItemExtensions(itemExtensions); 
	}

}
