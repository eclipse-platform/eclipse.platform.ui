package org.eclipse.ui.views.navigator;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.actions.CopyResourceAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.part.*;
import org.eclipse.ui.wizards.datatransfer.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements drop behaviour for drag and drop operations
 * that land on the resource navigator.
 */
/* package */ class NavigatorDropAdapter extends PluginDropAdapter implements IOverwriteQuery {
	/**
	 * The time the mouse first started hovering over the current target
	 */
	protected long hoverStart = 0;
	/**
	 * The amount of time to hover over a tree item before expanding it
	 */
	protected static final long hoverThreshold = 1500;

	/**
	 * A flag indicating that the drop has been cancelled by the user.
	 */
	protected boolean isCanceled = false;
	/**
	 * A flag indicating that overwrites should always occur.
	 */
	protected boolean alwaysOverwrite = false;

/**
 * NavigatorDropAction constructor comment.
 */
public NavigatorDropAdapter(StructuredViewer viewer) {
	super(viewer);
}
/**
 * Copies or moves the <code>source</code> file to the given <code>
 * destination</code>.  If overwrite is true, any resource that
 * already exists at the destination will be deleted before the
 * copy/move occurs.
 */
protected IStatus doCopy(final IResource source, final IPath destination, final boolean overwrite) {
	final boolean copy = getCurrentOperation() == DND.DROP_COPY;
	final IStatus[] result = new IStatus[] { ok()};
	try {
		new ProgressMonitorDialog(getShell()).run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					if (overwrite) {
						//delete the destination
						IResource oldResource = source.getWorkspace().getRoot().findMember(destination);
						if (oldResource.exists()) {
							oldResource.delete(true, null);
						}
					}
					if (copy) {
						IPath newName = destination;
						if (source.getWorkspace().getRoot().exists(destination))
							newName = CopyResourceAction.getNewNameFor(destination, source.getWorkspace());
						if (newName != null) {
							source.copy(newName, false, monitor);
						}
					}
					else {
						source.move(destination, false, monitor);
					}
				} catch (CoreException e) {
					result[0] = e.getStatus();
				}
			}
		});
	} catch (InvocationTargetException e) {
		//implementation doesn't throw this
	} catch (InterruptedException e) {
	}
	return result[0];
}
/**
 * Copies the source into the target container.  Returns a status object
 * indicating success or failure.
 */
protected IStatus dragAndDropCopy(IContainer target, IResource source) {
	if (isCanceled) {
		return ok();
	}
	if (getCurrentOperation() != DND.DROP_COPY && (source.equals(target) || source.getParent().equals(target))) {
		return info("Source and destination are the same.");
	}
	if (source.getFullPath().isPrefixOf(target.getFullPath())) {
		return error(source, "Destination is a sub-folder of the source.");
	}
	IPath destination = target.getFullPath().append(source.getName());

	IStatus result = doCopy(source, destination, false);
	if (result.getCode() == IResourceStatus.PATH_OCCUPIED) {
		if (alwaysOverwrite) {
			return doCopy(source, destination, true);
		}
		String query = queryOverwrite(destination.toString());
		if (query == YES) {
		   return doCopy(source, destination, true);
		}
		if (query == CANCEL) {
			isCanceled = true;
			return ok();
		}
		if (query == ALL) {
			alwaysOverwrite = true;
			return doCopy(source, destination, true);
		}
		if (query == NO) {
			return ok();
		}
	}
	return result;
}
/**
 * Performs an import of the given file into the provided
 * container.  Returns a status indicating if the import was successful.
 */
protected IStatus dragAndDropImport(IContainer target, String filePath) {
	File toImport = new File(filePath);
	if (target.getLocation().equals(toImport)) {
		return info("Cannot drop a resource onto itself");
	}
	ImportOperation op = 
		new ImportOperation(target.getFullPath(), new File(toImport.getParent()), FileSystemStructureProvider.INSTANCE, this, Arrays.asList(new File[] {toImport})); 
	op.setCreateContainerStructure(false);
	int result = 0;
	try {
		new ProgressMonitorDialog(getShell()).run(true, true, op);
	} catch (InterruptedException e) {
		return info("Operation Cancelled");
	} catch (InvocationTargetException e) {
		return error("An error occurred during the drop operation: " + e.getTargetException().getMessage(), e.getTargetException()); 
	}
	return op.getStatus();
}
/**
 * The mouse has moved over the drop target.  If the
 * target item has changed, notify the action and check
 * that it is still enabled.
 */
public void dragOver(DropTargetEvent event) {
	try {
		//this method implements the UI behaviour that when the user hovers 
		//over an unexpanded tree item long enough, it will auto-expand.
		Object oldTarget = getCurrentTarget();
		super.dragOver(event);
		if (oldTarget != getCurrentTarget()) {
			hoverStart = System.currentTimeMillis();
		} else {
			//if we've been hovering over this item awhile, expand it.
			if (hoverStart > 0 && (System.currentTimeMillis() - hoverStart) > hoverThreshold) {
				expandSelection((TreeItem) event.item);
				hoverStart = 0;
			}
		}
	} catch (Throwable t) {
		handleException(t, event);
	}
}
/**
 * Returns an error status with the given info.
 */
protected IStatus error(String message) {
	return error(message, null);
}
/**
 * Returns an error status with the given info.
 */
protected IStatus error(String message, Throwable exception) {
	return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, exception);
}
/**
 * Returns an error status, indicating why the given source
 * could not be copied or moved.
 */
protected IStatus error(IResource source, String message) {
	String copyType = getCurrentOperation() == DND.DROP_COPY ? "copy" : "move";
	StringBuffer result = new StringBuffer("Cannot ");
	result.append(copyType);
	result.append(" \"");
	result.append(source.getName());
	result.append("\".  ");
	result.append(message);
	return error(result.toString(), null);
}
/**
 * Expands the selection of the given tree viewer.
 */
protected void expandSelection(TreeItem selection) {
	if (selection == null)
		return;
	if (!selection.getExpanded()) {
		TreeViewer treeViewer = (TreeViewer) getViewer();
		treeViewer.expandToLevel(selection.getData(), 1);
	}
}
/**
 * Returns the actual target of the drop, given the resource
 * under the mouse.  If the mouse target is a file, then the drop actually 
 * occurs in its parent.  If the drop location is before or after the
 * mouse target, the target is also the parent.
 */
protected IContainer getActualTarget(IResource mouseTarget) {
	/* if cursor is before or after mouseTarget, set target to parent */
	if (getCurrentLocation() == LOCATION_BEFORE || getCurrentLocation() == LOCATION_AFTER) {
		return mouseTarget.getParent();
	}
	/* if cursor is on a file, return the parent */
	if (mouseTarget.getType() == IResource.FILE) {
		return mouseTarget.getParent();
	}
	/* otherwise the mouseTarget is the real target */
	return (IContainer)mouseTarget;
}
/**
 * Returns the display
 */
protected Display getDisplay() {
	return getViewer().getControl().getDisplay();
}
/**
 * Returns the shell
 */
protected Shell getShell() {
	return getViewer().getControl().getShell();
}
/**
 * Returns an error status with the given info.
 */
protected IStatus info(String message) {
	return new Status(IStatus.INFO, PlatformUI.PLUGIN_ID, 0, message, null);
}
/**
 * Adds the given status to the list of problems.  Discards
 * OK statuses.  If the status is a multi-status, only its children
 * are added.
 */
protected void mergeStatus(MultiStatus status, IStatus toMerge) {
	if (!toMerge.isOK()) {
		status.merge(toMerge);
	}
}
/**
 * Creates a status object from the given list of problems.
 */
protected IStatus multiStatus(List problems, String message) {
	IStatus[] children = new IStatus[problems.size()];
	problems.toArray(children);
	if (children.length == 1) {
		return children[0];
	} else {
		return new MultiStatus(PlatformUI.PLUGIN_ID, 0, children, message, null);
	}
}
/**
 * Returns an status indicating success.
 */
protected IStatus ok() {
	return new Status(Status.OK, PlatformUI.PLUGIN_ID, 0, "ok", null);
}
/**
 * Opens an error dialog if necessary.  Takes care of
 * complex rules necessary for making the error dialog look nice.
 */
protected void openError(IStatus status) {
	String genericTitle = "Drag and Drop Problem";
	int codes = IStatus.ERROR | IStatus.WARNING;
	
	//simple case: one error, not a multistatus
	if (!status.isMultiStatus()) {
		ErrorDialog.openError(getShell(), genericTitle, null, status, codes);
		return;
	}

	//one error, single child of multistatus
	IStatus[] children = status.getChildren();
	if (children.length == 1) {
		ErrorDialog.openError(getShell(), status.getMessage(), null, children[0], codes);
		return;
	}
	//several problems
	ErrorDialog.openError(getShell(), genericTitle, null, status, codes);
}
/**
 * Invoked when an action occurs. 
 * Argument context is the Window which contains the UI from which this action was fired.
 * This default implementation prints the name of this class and its label.
 * @see IAction#run
 */
public boolean performDrop(Object data) {
	isCanceled = false;
	alwaysOverwrite = false;
	if (getCurrentTarget() == null || data == null) {
		return false;
	}
	TransferData currentTransfer = getCurrentTransfer();
	if (ResourceTransfer.getInstance().isSupportedType(currentTransfer)) {
		return performResourceDrop(data);
	}
	if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
		return performFileDrop(data);
	}
	return super.performDrop(data);
}
/**
 * Performs a drop using the FileTransfer transfer type.
 */
protected boolean performFileDrop(Object data) {
	MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 0, "Problems occurred while importing resources.", null);
	mergeStatus(problems, validateTarget(getCurrentTarget()));

	IContainer targetResource = getActualTarget((IResource)getCurrentTarget());
	String[] names = (String[]) data;
	for (int i = 0; i < names.length; i++) {
		mergeStatus(problems, dragAndDropImport(targetResource, names[i]));
	}
	openError(problems);
	return problems.isOK();
}
/**
 * Performs a drop using the ResourceTransfer transfer type.
 */
protected boolean performResourceDrop(Object data) {
	MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1, "Problems occurred while moving resources.", null);
	mergeStatus(problems, validateTarget(getCurrentTarget()));

	IContainer targetResource = getActualTarget((IResource)getCurrentTarget());
	IResource[] sources = (IResource[]) data;
	for (int i = 0; i < sources.length; i++) {
		mergeStatus(problems, dragAndDropCopy(targetResource, sources[i]));
	}
	openError(problems);

	//always return false because we don't want the source to clean up
	return false;
}
/* (non-Javadoc)
 * Method declared on IOverWriteQuery
 */
public String queryOverwrite(String pathString) {
	final String returnCode[] = {CANCEL};
	final String msg = pathString + " already exists.  Would you like to overwrite it?";
	final String[] options = {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.CANCEL_LABEL};
	getDisplay().syncExec(new Runnable() {
		public void run() {
			MessageDialog dialog = new MessageDialog(getShell(), "Question", null, msg, MessageDialog.QUESTION, options, 0);
			dialog.open();
			int returnVal = dialog.getReturnCode();
			String[] returnCodes = {YES, NO, ALL, CANCEL};
			returnCode[0] = returnVal < 0 ? CANCEL : returnCodes[returnVal];
		}
	});
	return returnCode[0];
}
/**
 * This method is used to notify the action that some aspect of
 * the drop operation has changed.
 */
public boolean validateDrop(Object target, int operation, TransferData transferType) {
	if (super.validateDrop(target, operation, transferType)) {
		return true;
	}
	return validateTarget(target).isOK();
}
/**
 * Ensures that the drop target meets certain criteria
 */
protected IStatus validateTarget(Object target) {
	if (!(target instanceof IResource)) {
		return info("Target must be a resource");
	}
	IResource resource = (IResource)target;
	if (!resource.isAccessible()) {
		return error("Cannot drop a resource into closed project");
	}
	IContainer destination = getActualTarget(resource);
	if (destination.getType() == IResource.ROOT) {
		return error("Resources cannot be siblings of projects");
	}
	return ok();
}
}
