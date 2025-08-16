package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;

/**
 * @since 3.0
 */
public class EditorAreaDropTarget extends WorkbenchWindowDropTarget {
	int side;

	public EditorAreaDropTarget(IWorkbenchWindowProvider provider, int side) {
		super(provider);
		this.side = side;
	}

	@Override
	public String toString() {
		return DragOperations.nameForConstant(side) + " of editor area";
	}

	@Override
	public Point getLocation() {
		return DragOperations.getPointInEditorArea();
	}

}