/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
