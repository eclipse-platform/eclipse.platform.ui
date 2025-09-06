package org.eclipse.ui.tests.dnd;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

public class DragOperations {

	public static void drag(IWorkbenchPart part, TestDropLocation target, boolean wholeFolder) {
	    Display.getDefault().syncExec(() -> {
	        Point startLocation = Display.getDefault().getCursorLocation();
	        Point endLocation;

	        if (wholeFolder) {
	            Rectangle bounds = part.getSite().getShell().getBounds();
	            endLocation = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
	        } else {
	            endLocation = target.getLocation();
	        }

	        Event press = new Event();
	        press.type = SWT.MouseDown;
	        press.x = startLocation.x;
	        press.y = startLocation.y;
	        Display.getDefault().post(press);

	        Event move = new Event();
	        move.type = SWT.MouseMove;
	        move.x = endLocation.x;
	        move.y = endLocation.y;
	        Display.getDefault().post(move);

	        Event release = new Event();
	        release.type = SWT.MouseUp;
	        release.x = endLocation.x;
	        release.y = endLocation.y;
	        Display.getDefault().post(release);
	    });
	}

	/**
	 * Returns the name of the given editor
	 */
	public static String getName(IEditorPart editor) {
		IWorkbenchPage page = editor.getSite().getPage();
		IWorkbenchPartReference ref = page.getReference(editor);
		return ref.getPartName();
	}

	public static Rectangle getDisplayBounds() {
		return new Rectangle(0, 0, 0, 0);
	}

	public static Point getLocation(int side) {
		return DragOperations.getPoint(getDisplayBounds(), side);
	}

	public static Point getPointInEditorArea() {
		return new Point(0, 0);
	}

	public static Point getPoint(Rectangle bounds, int side) {
		Point centerPoint = Geometry.centerPoint(bounds);

		switch (side) {
		case SWT.TOP:
			return new Point(centerPoint.x, bounds.y + 1);
		case SWT.BOTTOM:
			return new Point(centerPoint.x, bounds.y + bounds.height - 1);
		case SWT.LEFT:
			return new Point(bounds.x + 1, centerPoint.y);
		case SWT.RIGHT:
			return new Point(bounds.x + bounds.width - 1, centerPoint.y);
		}

		return centerPoint;
	}

	public static String nameForConstant(int swtSideConstant) {
		switch (swtSideConstant) {
		case SWT.TOP:
			return "top";
		case SWT.BOTTOM:
			return "bottom";
		case SWT.LEFT:
			return "left";
		case SWT.RIGHT:
			return "right";
		}

		return "center";
	}

	public static String getName(IViewPart targetPart) {
		return targetPart.getTitle();
	}
}