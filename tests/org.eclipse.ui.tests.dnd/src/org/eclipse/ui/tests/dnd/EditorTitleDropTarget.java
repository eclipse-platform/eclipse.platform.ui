package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

public class EditorTitleDropTarget extends WorkbenchWindowDropTarget {

	int editorIdx;

	public EditorTitleDropTarget(IWorkbenchWindowProvider provider, int editorIdx) {
		super(provider);
		this.editorIdx = editorIdx;
	}

	IEditorPart getPart() {
		return getPage().getEditorReferences()[editorIdx].getEditor(true);
	}

	@Override
	public String toString() {
		return "editor " + editorIdx + " title area";
	}

	@Override
	public Shell getShell() {
		return getPart().getSite().getShell();
	}

	@Override
	public Point getLocation() {
		Rectangle bounds = DragOperations.getDisplayBounds();

		return new Point( (bounds.x + bounds.width) - 4, bounds.y + 4);
	}
}