package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	super(WorkbenchMessages.getString("SwitchPage.text")); //$NON-NLS-1$
	setChecked(false);
	this.page = page;
	update();
	WorkbenchHelp.setHelp(this, IHelpContextIds.SET_PAGE_ACTION);
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
