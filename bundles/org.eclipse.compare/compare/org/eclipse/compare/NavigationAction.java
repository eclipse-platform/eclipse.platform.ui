/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare;

import java.util.ResourceBundle;
import org.eclipse.jface.action.Action;

import org.eclipse.compare.internal.CompareNavigator;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.Utilities;


public class NavigationAction extends Action {
	
	private boolean fNext;
	private CompareEditorInput fCompareEditorInput;
	
	
	public NavigationAction(boolean next) {
		this(CompareUIPlugin.getResourceBundle(), next);
	}

	public NavigationAction(ResourceBundle bundle, boolean next) {
		Utilities.initAction(this, bundle, next ? "action.Next." : "action.Previous."); //$NON-NLS-2$ //$NON-NLS-1$
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