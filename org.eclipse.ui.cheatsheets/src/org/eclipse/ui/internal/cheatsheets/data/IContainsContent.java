/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.data;

import java.util.ArrayList;

public interface IContainsContent {

	/**
	 * Returns the helpLink.
	 * @return String
	 */
	public String getHref();

	/**
	 * Returns the text.
	 * @return String
	 */
	public String getText();

	/**
	 * Returns the title.
	 * @return String
	 */
	public String getTitle();

	/**
	 * Sets the helpLink.
	 * @param helpLink The helpLink to set
	 */
	public void setHref(String helpLink);

	/**
	 * Sets the text.
	 * @param text The text to set
	 */
	public void setText(String text);

	/**
	 * Sets the title.
	 * @param title The title to set
	 */
	public void setTitle(String title);

	public boolean isDynamic();

	public void setIsDynamic(boolean b);
	
	public void setContent(Content c);
	
	public Content getContent();
	
	public void setItemExtensions(ArrayList itemExtensions);
	
	public ArrayList getItemExtensions();

}
