/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.data;

import java.util.ArrayList;

public class ContentItem extends ActionItem implements IContainsContent {
	private Content content;

	/**
	 * Constructor for ContentItem.
	 */
	public ContentItem() {
		super();
		content = new Content();
	}
	
	public ContentItem(String title, String description, String href){
		super();
		content = new Content();
		content.setText(description);
		content.setTitle(title);
		content.setHref(href);
	}
	
	/**
	 * Returns the helpLink.
	 * @return String
	 */
	public String getHref() {
		return content.getHref();
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

	/**
	 * Sets the helpLink.
	 * @param helpLink The helpLink to set
	 */
	public void setHref(String helpLink) {
		content.setHref(helpLink);
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

	public boolean isDynamic() {
		return content.isDynamic();
	}

	public void setIsDynamic(boolean b) {
		content.setDynamic(b);
	}
	
	public void setContent(Content c){
		this.content = c;	
	}
	
	public Content getContent(){
		return this.content;	
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
