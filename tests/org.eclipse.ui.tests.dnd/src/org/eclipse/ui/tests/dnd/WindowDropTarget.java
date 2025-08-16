package org.eclipse.ui.tests.dnd;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.0
 */
public class WindowDropTarget extends WorkbenchWindowDropTarget {

	private final int side;

	public WindowDropTarget(IWorkbenchWindowProvider provider, int side) {
		super(provider);
		this.side = side;
	}

	@Override
	public String toString() {
		return DragOperations.nameForConstant(side) + " of window";
	}

	@Override
	public Point getLocation() {
		Shell shell = getShell();
		Rectangle clientArea = shell.getClientArea();

		return DragOperations.getPoint(Geometry.toDisplay(shell, clientArea),
				side);
	}
}