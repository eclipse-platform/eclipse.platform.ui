package org.eclipse.ui.internal;

import org.eclipse.ui.*;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
/**
 * Implements a pane for a MultiEditor.
 */
public class MultiEditorOuterPane extends EditorPane {
	/**
	 * Constructor for MultiEditorOuterPane.
	 */
	public MultiEditorOuterPane(IEditorReference ref, WorkbenchPage page, EditorWorkbook workbook) {
		super(ref, page, workbook);
	}
	/*
	 * @see EditorPane
	 */
	protected void requestActivation() {
		//Outer editor is never activated.
	}
}
