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

/**
 * A <code>NavigationAction</code> is used to navigate through the individual
 * differences of a <code>CompareEditorInput</code>.
 * <p>
 * Clients may instantiate this class; it is not intended to be subclassed.
 * </p>
 * @since 2.0
 */
public class NavigationAction extends Action {
	
	private boolean fNext;
	private CompareEditorInput fCompareEditorInput;
	
	
	/**
	 * Creates a <code>NavigationAction</code>.
	 *
	 * @param next if <code>true</code> action goes to the next difference; otherwise to the previous difference.
	 */
	public NavigationAction(boolean next) {
		this(CompareUIPlugin.getResourceBundle(), next);
	}

	/**
	 * Creates a <code>NavigationAction</code> that initializes its attributes
	 * from the given <code>ResourceBundle</code>.
	 *
	 * @param bundle is used to initialize the action
	 * @param next if <code>true</code> action goes to the next difference; otherwise to the previous difference.
	 */
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
	
	/**
	 * Sets the <code>CompareEditorInput</code> on which this action operates.
	 * 
	 * @param input the <code>CompareEditorInput</code> on which this action operates; if <code>null</code> action does nothing
	 */
	public void setCompareEditorInput(CompareEditorInput input) {
		fCompareEditorInput= input;
	}
}
