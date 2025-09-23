package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

public class ViewDropTarget extends WorkbenchWindowDropTarget {

	String targetPart;

	int side;

	public ViewDropTarget(IWorkbenchWindowProvider provider, String part, int side) {
		super(provider);
		targetPart = part;
		this.side = side;
	}

	IViewPart getPart() {
		return getPage().findView(targetPart);
	}

	@Override
	public String toString() {
		IViewDescriptor desc = PlatformUI.getWorkbench().getViewRegistry()
				.find(targetPart);
		String title = desc.getLabel();

		return DragOperations.nameForConstant(side) + " of " + title;
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