package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.*;

/**
 * Hides or shows the editor area within the current
 * perspective of the workbench page.
 */
public class ToggleEditorsVisibilityAction extends Action {
	private IWorkbenchWindow workbenchWindow;
/**
 * Creates a new <code>ToggleEditorsVisibilityAction</code>
 */
public ToggleEditorsVisibilityAction(IWorkbenchWindow window) {
	super("Hide Editors");
	setToolTipText("Toggle editor area visibility");
	this.workbenchWindow = window;

	// Once the API on IWorkbenchPage to hide/show
	// the editor area is removed, then switch
	// to using the internal perspective service
	window.addPerspectiveListener(new org.eclipse.ui.IPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			if (page.isEditorAreaVisible())
				setText("Hide Editors");
			else
				setText("Show Editors");
		}			
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			if (changeId == page.CHANGE_RESET || changeId == page.CHANGE_EDITOR_AREA_HIDE || changeId == page.CHANGE_EDITOR_AREA_SHOW) {
				if (page.isEditorAreaVisible())
					setText("Hide Editors");
				else
					setText("Show Editors");
			}
		}			
	});
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {
	boolean visible = workbenchWindow.getActivePage().isEditorAreaVisible();
	if (visible) {
		workbenchWindow.getActivePage().setEditorAreaVisible(false);
		setText("Show Editors");
	}
	else {
		workbenchWindow.getActivePage().setEditorAreaVisible(true);
		setText("Hide Editors");
	}
}
}
