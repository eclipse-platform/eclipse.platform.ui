/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

public class HistoryEntry {
	public static final int URL = 1;
	public static final int PAGE = 2;
	private int type;
	private String data;
	/**
	 * 
	 */
	public HistoryEntry(int type, String data) {
		this.type = type;
		this.data = data;
	}
	public int getType() {
		return type;
	}
	public String getData() {
		return data;
	}
}
