package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiEditor;

/**
 * Implements a pane of each editor inside a MultiEditor.
 */
public class MultiEditorInnerPane extends EditorPane {
	
	EditorPane parentPane;
	/**
	 * Constructor for MultiEditorInnerPane.
	 */
	public MultiEditorInnerPane(EditorPane pane,IEditorPart part, WorkbenchPage page, EditorWorkbook workbook) {
		super(part, page, workbook);
		parentPane = pane;
	}
	/**
	 * Returns the outer editor.
	 */
	public EditorPane getParentPane() {
		return parentPane;
	}
	/**
	 * Update the gradient on the inner editor title bar
	 */
	private void updateGradient() {
		((MultiEditor)parentPane.getPart()).updateGradient(this.getEditorPart());
	}
	/**
 	 * Indicate focus in part.
     */
	public void showFocus(boolean inFocus) {
		super.showFocus(inFocus);
		updateGradient();
	}
	/* (non-Javadoc)
	 * Method declared on PartPane.
	 */
	/* package */ void shellDeactivated() {
		super.shellDeactivated();
		updateGradient();
	}
	/* (non-Javadoc)
	 * Method declared on PartPane.
	 */
	/* package */ void shellActivated() {
		super.shellActivated();
		updateGradient();
	}

}
