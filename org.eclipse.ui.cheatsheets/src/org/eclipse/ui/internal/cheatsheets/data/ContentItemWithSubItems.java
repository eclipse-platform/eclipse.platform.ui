/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.data;

import java.util.*;

import org.eclipse.ui.cheatsheets.*;

public class ContentItemWithSubItems extends AbstractItem implements IContainsContent, IItemWithSubItems {
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItemWithSubItems#addSubItem(org.eclipse.ui.cheatsheets.ISubItem)
	 */
	public boolean addSubItem(ISubItem sub) {
		if(isDuplicateId(sub.getID()))
			return false;
		if(subItems == null)
			subItems = new ArrayList(20);
		subItems.add(sub);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItemWithSubItems#addSubItem(org.eclipse.ui.cheatsheets.ISubItem, int)
	 */
	public boolean addSubItem(ISubItem sub, int index) throws IndexOutOfBoundsException {
		if(isDuplicateId(sub.getID()))
			return false;
		subItems.add(index, sub);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItemWithSubItems#addSubItems(org.eclipse.ui.cheatsheets.ISubItem[])
	 */
	public void addSubItems(ISubItem[] subitems) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItemWithSubItems#getSubItem(int)
	 */
	public ISubItem getSubItem(int index) throws IndexOutOfBoundsException {
		return (ISubItem) subItems.get(index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItemWithSubItems#getSubItems()
	 */
	public ISubItem[] getSubItems() {
		if(subItems == null)
			return null;
		return (ISubItem[]) subItems.toArray(new ISubItem[subItems.size()]);
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
			ISubItem isi = (ISubItem)subItems.get(i);	
			if(isi.getID().equals(id))
				return true;
		}
		return false;
	}

	public boolean isDynamic() {
		return content.isDynamic();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItemWithSubItems#removeSubItem(int)
	 */
	public boolean removeSubItem(int index) throws IndexOutOfBoundsException {
		subItems.remove(index);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItemWithSubItems#removeSubItem(org.eclipse.ui.cheatsheets.ISubItem)
	 */
	public boolean removeSubItem(ISubItem item) {
		int index = subItems.indexOf(item);
		if (index != -1) {
			subItems.remove(index);
			return true;
		} else
			return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItemWithSubItems#removeSubItems()
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
