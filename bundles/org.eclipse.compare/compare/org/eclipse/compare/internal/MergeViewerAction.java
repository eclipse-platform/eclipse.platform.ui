/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.jface.action.Action;


public abstract class MergeViewerAction extends Action implements IUpdate {
	
	private boolean fMutable;
	private boolean fSelection;
	private boolean fContent;
	
	public MergeViewerAction(boolean mutable, boolean selection, boolean content) {
		fMutable= mutable;
		fSelection= selection;
		fContent= content;
	}

	public boolean isSelectionDependent() {
		return fSelection;
	}
	
	public boolean isContentDependent() {
		return fContent;
	}
	
	public boolean isEditableDependent() {
		return fMutable;
	}
	
	public void update() {
	}
}
