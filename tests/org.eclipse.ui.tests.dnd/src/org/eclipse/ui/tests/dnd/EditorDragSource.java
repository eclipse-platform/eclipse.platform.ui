package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IEditorPart;

/**
 * @since 3.0
 */
public class EditorDragSource extends TestDragSource {

	int editorIdx;

	boolean wholeFolder;

	public EditorDragSource(int editorIdx, boolean wholeFolder) {
		super();
		this.editorIdx = editorIdx;
		this.wholeFolder = wholeFolder;
	}

	IEditorPart getPart() {
		return getPage().getEditors()[editorIdx];
	}

	@Override
	public String toString() {
		String title = "editor " + editorIdx;

		if (wholeFolder) {
			return title + " folder";
		}
		return title;
	}

	@Override
	public void drag(TestDropLocation target) {
		DragOperations.drag(getPart(), target, wholeFolder);
	}

}