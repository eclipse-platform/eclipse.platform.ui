/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.data;

import java.util.ArrayList;

public class Content {
	private String title;
	private String href;
	private String text;
	private boolean dynamic;
	private ArrayList itemExtensions;

	/**
	 * 
	 */
	public Content() {
		super();
	}

	/**
	 * @return
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	/**
	 * @return
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param b
	 */
	public void setDynamic(boolean b) {
		dynamic = b;
	}

	/**
	 * @param string
	 */
	public void setHref(String string) {
		href = string;
	}

	/**
	 * @param string
	 */
	public void setText(String string) {
		text = string;
	}

	/**
	 * @param string
	 */
	public void setTitle(String string) {
		title = string;
	}

	public void setItemExtensions(ArrayList exts){
		this.itemExtensions = exts;	
	}
	
	public ArrayList getItemExtensions(){
		return itemExtensions;
	}

}
