package org.eclipse.ui.tests.dnd;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.junit.Assert;

/**
 * @since 3.0
 */
public class DragOperations {

	/**
	 * Drags the given view OR editor to the given location (i.e. it only cares that we're given
	 * a 'Part' and doesn't care whether it's a 'View' or an 'Editor'.
	 * <p>
	 * This method should eventually replace the original one once the Workbench has been updated
	 * to handle Views and Editors without distincton.
	 */
	@SuppressWarnings("unused")
	public static void drag(IWorkbenchPart part, TestDropLocation target, boolean wholeFolder) {
//        DragUtil.forceDropLocation(target);

//        PartSite site = (PartSite) part.getSite();
//        PartPane pane = site.getPane();
//        PartStack parent = ((PartStack) (pane.getContainer()));
//
//        parent.paneDragStart(wholeFolder ? null : pane, Display.getDefault().getCursorLocation(), false);

		Assert.fail("DND needs some updating");
//        DragUtil.forceDropLocation(null);
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