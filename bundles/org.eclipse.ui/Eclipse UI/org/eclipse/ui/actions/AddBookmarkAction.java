package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import java.util.Iterator;

/**
 * Standard action for adding a bookmark to the currently selected file
 * resource(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class AddBookmarkAction extends SelectionListenerAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".AddBookmarkAction";
	
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;
/**
 * Creates a new bookmark action.
 *
 * @param shell the shell for any dialogs
 */
public AddBookmarkAction(Shell shell) {
	super("Add Boo&kmark");
	setId(ID);
	Assert.isNotNull(shell);
	this.shell = shell;
	setToolTipText("Add a bookmark");
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.ADD_BOOKMARK_ACTION});
}
/**
 * Creates a marker of the given type on each of the files in the
 * current selection.
 *
 * @param markerType the marker type
 */
void createMarker(String markerType) {
	IStructuredSelection selection = getStructuredSelection();
	for (Iterator enum = selection.iterator(); enum.hasNext();) {
		Object o = enum.next();
		if (o instanceof IFile)
			createMarker((IFile) o, markerType);
	}
}
/**
 * Creates a marker of the given type on the given file resource.
 *
 * @param file the file resource
 * @param markerType the marker type
 */
void createMarker(final IFile file, final String markerType) {
	try {
		file.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = file.createMarker(markerType);
				marker.setAttribute(IMarker.MESSAGE, file.getName());
			}
		}, null);
	} catch (CoreException e) {
		WorkbenchPlugin.log(null, e.getStatus()); // We don't care
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	createMarker(IMarker.BOOKMARK);
}
/**
 * The <code>AddBookmarkAction</code> implementation of this
 * <code>SelectionListenerAction</code> method enables the action only
 * if the selection contains just file resources.
 */
protected boolean updateSelection(IStructuredSelection selection) {
	return super.updateSelection(selection)
		&& selectionIsOfType(IFile.FILE);
}
}
