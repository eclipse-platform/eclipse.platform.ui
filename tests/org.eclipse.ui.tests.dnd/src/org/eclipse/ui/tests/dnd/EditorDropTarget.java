package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

/**
 * @since 3.0
 */
public class EditorDropTarget extends WorkbenchWindowDropTarget {

	int editorIdx;

	int side;

	public EditorDropTarget(IWorkbenchWindowProvider provider, int editorIdx, int side) {
		super(provider);
		this.editorIdx = editorIdx;
		this.side = side;
	}

	IEditorPart getPart() {
		return getPage().getEditors()[editorIdx];
	}

	@Override
	public String toString() {
		return DragOperations.nameForConstant(side) + " of editor " + editorIdx;
	}

	@Override
	public Point getLocation() {
		return DragOperations.getLocation(side);
	}

	@Override
	public Shell getShell() {
		return getPart().getSite().getShell();
	}
}