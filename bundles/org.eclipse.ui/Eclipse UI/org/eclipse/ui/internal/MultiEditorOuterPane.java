package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.ui.IEditorPart;
/**
 * Implements a pane for a MultiEditor.
 */
public class MultiEditorOuterPane extends EditorPane {
	/**
	 * Constructor for MultiEditorOuterPane.
	 */
	public MultiEditorOuterPane(IEditorPart part, WorkbenchPage page, EditorWorkbook workbook) {
		super(part, page, workbook);
	}
	/*
	 * @see EditorPane
	 */
	protected void requestActivation() {
		//Outer editor is never activated.
	}
}
