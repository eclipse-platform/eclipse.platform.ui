package org.eclipse.ui.internal;

import org.eclipse.ui.*;

/**
 * An item in the editor history.
 */
public class EditorHistoryItem {
	public IEditorInput input;
	public IEditorDescriptor desc;
/**
 * Constructs a new item.
 */	
public EditorHistoryItem(IEditorInput input, IEditorDescriptor desc) {
	this.input = input;
	this.desc = desc;
}
}
