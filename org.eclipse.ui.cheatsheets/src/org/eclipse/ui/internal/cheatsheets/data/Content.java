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
