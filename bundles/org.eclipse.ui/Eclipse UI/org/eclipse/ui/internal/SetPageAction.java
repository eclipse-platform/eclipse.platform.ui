package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.*;

/**
 * Reset the layout within the active perspective.
 */
public class SetPageAction extends Action {
	private WorkbenchPage page;
	
/**
 *	Create an instance of this class
 */
public SetPageAction(WorkbenchPage page) {
	super("&Switch To Page:");
	setChecked(false);
	this.page = page;
	update();
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SET_PAGE_ACTION});
}
/**
 * Returns the target page.
 */
public WorkbenchPage getPage() {
	return page;
}
/**
 * The user has invoked this action
 */
public void run() {
	IWorkbenchWindow window = page.getWorkbenchWindow();
	window.setActivePage(page);
}
/**
 *	Update the action.
 */
public void update() {
	setToolTipText(page.getLabel());
	IPerspectiveDescriptor persp = page.getPerspective();
	ImageDescriptor image = persp.getImageDescriptor();
	if (image != null) {
		setImageDescriptor(image);
		setHoverImageDescriptor(null);
	} else {
		setImageDescriptor(WorkbenchImages.getImageDescriptor(
			IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE));
		setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(
			IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE_HOVER));
	}
}
}
