package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IResource;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.MoveResourceAction;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The ResourceNavigatorMoveAction is a resource move that aso updates the navigator
 * to show the result of the move.
 */
/* package */ class ResourceNavigatorMoveAction extends MoveResourceAction {
	private TreeViewer viewer;
/**
 * Create a ResourceNavigatorMoveAction and use the supplied viewer to update the UI.
 * @param shell Shell
 * @param treeViewer TreeViewer
 */
public ResourceNavigatorMoveAction(Shell shell, TreeViewer treeViewer) {
	super(shell);
	this.viewer = treeViewer;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	super.run();
	IWorkspaceRoot root = WorkbenchPlugin.getPluginWorkspace().getRoot();
	List resources = new ArrayList();
	Iterator iterator = getDestinations().iterator();

	while (iterator.hasNext()) {
		IResource newResource = root.findMember((IPath) iterator.next());
		if (newResource != null)
			resources.add(newResource);
	}

	this.viewer.setSelection(new StructuredSelection(resources), true);

}
}
