/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;
import org.eclipse.jface.action.Action;
import org.eclipse.compare.CompareEditorInput;


public class NavigationAction extends Action {
	
	private boolean fNext;
	private CompareEditorInput fCompareEditorInput;

	public NavigationAction(ResourceBundle bundle, boolean next) {
		Utilities.initAction(this, bundle, next ? "action.Next." : "action.Previous.");
		fNext= next;
	}

	public void run() {
		if (fCompareEditorInput != null) {
			Object adapter= fCompareEditorInput.getAdapter(CompareNavigator.class);
			if (adapter instanceof CompareNavigator)
				((CompareNavigator)adapter).selectChange(fNext);
		}
	}
	
	public void setCompareEditorInput(CompareEditorInput input) {
		fCompareEditorInput= input;
	}
}