/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.views;


import org.eclipse.jface.action.Action;

/**
 * Action used to enable / disable method filter properties
 */
public class CheatSheetExpandRestoreAction extends Action {
	private CheatSheetView theview;

	public CheatSheetExpandRestoreAction(String title, boolean initValue, CheatSheetView theview) {
		super(title);
		this.theview = theview;
		
		setChecked(initValue);
	}
	
	/*
	 * @see Action#actionPerformed
	 */
	public void run() {
		theview.toggleExpandRestore();	
	}
		
}