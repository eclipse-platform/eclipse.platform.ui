package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.help.*;
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
	super(WorkbenchMessages.getString("ToggleEditor.hideEditors")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ToggleEditor.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.TOGGLE_EDITORS_VISIBILITY_ACTION);
	setEnabled(false);
	this.workbenchWindow = window;

	// Once the API on IWorkbenchPage to hide/show
	// the editor area is removed, then switch
	// to using the internal perspective service
	window.addPerspectiveListener(new org.eclipse.ui.IPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			if (page.isEditorAreaVisible())
				setText(WorkbenchMessages.getString("ToggleEditor.hideEditors")); //$NON-NLS-1$
			else
				setText(WorkbenchMessages.getString("ToggleEditor.showEditors")); //$NON-NLS-1$
		}			
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			if (changeId == page.CHANGE_RESET || changeId == page.CHANGE_EDITOR_AREA_HIDE || changeId == page.CHANGE_EDITOR_AREA_SHOW) {
				if (page.isEditorAreaVisible())
					setText(WorkbenchMessages.getString("ToggleEditor.hideEditors")); //$NON-NLS-1$
				else
					setText(WorkbenchMessages.getString("ToggleEditor.showEditors")); //$NON-NLS-1$
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
		setText(WorkbenchMessages.getString("ToggleEditor.showEditors")); //$NON-NLS-1$
	}
	else {
		workbenchWindow.getActivePage().setEditorAreaVisible(true);
		setText(WorkbenchMessages.getString("ToggleEditor.hideEditors")); //$NON-NLS-1$
	}
}
}
