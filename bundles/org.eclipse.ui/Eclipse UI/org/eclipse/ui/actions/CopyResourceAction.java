package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.MessageFormat;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Standard action for copying the currently selected resources elsewhere
 * in the workspace. All resources being copied as a group must be siblings.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CopyResourceAction extends SelectionListenerAction implements ISelectionValidator {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CopyResourceAction"; //$NON-NLS-1$

	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;

	/**
	 * Multi status containing the errors detected when running the operation or
	 * <code>null</code> if no errors detected.
	 */
	private MultiStatus errorStatus;

	private boolean setUpErrors = false;

	private boolean canceled = false;

	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 */
	public CopyResourceAction(Shell shell) {
		this(shell, WorkbenchMessages.getString("CopyResourceAction.title")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.COPY_RESOURCE_ACTION);
	}
	/**
	 * Creates a new action with the given text.
	 *
	 * @param shell the shell for any dialogs
	 * @param text the string used as the text for the action, 
	 *   or <code>null</code> if these is no text
	 */
	CopyResourceAction(Shell shell, String name) {
		super(name);
		setToolTipText(WorkbenchMessages.getString("CopyResourceAction.toolTip")); //$NON-NLS-1$
		setId(CopyResourceAction.ID);
		Assert.isNotNull(shell);
		this.shell = shell;
	}
	/**
	 * Returns whether this action is able to perform on-the-fly auto-renaming of
	 * resources with name collisions.
	 * <p>
	 * The <code>CopyResourceAction</code> implementation of this method returns
	 * <code>true</code>.
	 * </p>
	 *
	 * @return <code>true</code> if auto-rename is supported, and <code>false</code>
	 *   otherwise
	 */
	boolean canPerformAutoRename() {
		return true;
	}
	/**
	 * Check if the user wishes to overwrite the supplied resource
	 * @returns true if there is no collision or delete was successful
	 * @param shell the shell to create the dialog in 
	 * @param destination - the resource to be overwritten
	 */
	private boolean checkOverwrite(final Shell shell, final IResource destination) {

		final boolean[] result = new boolean[1];

		//Run it inside of a runnable to make sure we get to parent off of the shell as we are not
		//in the UI thread.

		Runnable query = new Runnable() {
			public void run() {
				result[0] = MessageDialog.openQuestion(
					shell, 
					WorkbenchMessages.getString("CopyResourceAction.resourceExists"), //$NON-NLS-1$
					WorkbenchMessages.format("CopyResourceAction.overwriteQuestion",  //$NON-NLS-1$
					new Object[] { destination.getFullPath().makeRelative()}));
			}

		};

		shell.getDisplay().syncExec(query);
		return result[0];
	}
	/**
	 * Copy the resources to the given destination.  This method is called recursively to
	 * deal with merging of folders during folder copy.
	 */
	private void copy(IResource[] resources, IPath destination, IProgressMonitor subMonitor) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource source = resources[i];
			IPath destinationPath = destination.append(source.getName());
			IWorkspace workspace = source.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			if ((source.getType() == IResource.FOLDER) && (workspaceRoot.findMember(destinationPath) != null)) {
				// the resource is a folder and it exists in the destination, copy the
				// children of the folder
				IResource[] children = ((IContainer) source).members();
				copy(children, destinationPath, subMonitor);
			} else {
				// if we're merging folders, we could be overwriting an existing file
				IResource existing = workspaceRoot.findMember(destinationPath);
				if (existing != null) {
					delete(existing, subMonitor);
				}
				source.copy(destinationPath, false, new SubProgressMonitor(subMonitor, 0));
				subMonitor.worked(1);
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		}
	}
	/**
	 * Copies the given resource to the given destination.
	 *
	 * @param resource the source resource
	 * @param destination the destination path
	 * @param monitor a progress monitor for progress and cancelation
	 * @exception CoreException if a problem is encountered
	 */
	void copyResource(IResource resource, IPath destination, IProgressMonitor monitor) throws CoreException {
		resource.copy(destination, false, monitor);
	}
	/**
	 * Removes the given resource from the workspace. 
	 *  
	 * @param resourceToDelete resource to remove from the workspace
	 * @param monitor a progress monitor for progress and cancelation
	 */
	void delete(IResource resourceToDelete, IProgressMonitor monitor) throws CoreException {
		boolean force = false; // don't force deletion of out-of-sync resources
		if (resourceToDelete.getType() == IResource.PROJECT) {
			// if it's a project, ask whether content should be deleted too
			IProject project = (IProject) resourceToDelete;
			project.delete(true, force, monitor);
		} else {
			int flags = IResource.KEEP_HISTORY;
			if (force)
				flags = flags | IResource.FORCE;
			// if it's not a project, just delete it
			resourceToDelete.delete(flags, monitor);
		}
	}
	/**
	 * Opens an error dialog to display the given message.
	 * <p>
	 * Note that this method must be called from UI thread.
	 * </p>
	 *
	 * @param message the message
	 */
	void displayError(final String message) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(getShell(), getProblemsTitle(), message);
			}
		});
	}
	/**
	 * Returns the path of the container to initially select in the container selection dialog,
	 * or <code>null</code> if there is no initial selection
	 */
	IContainer getInitialContainer() {
		List resources = getSelectedResources();
		if (resources.size() > 0) {
			IResource resource = (IResource) resources.get(0);
			return resource.getParent();
		}
		return null;
	}
	/**
	 * Returns a new name for a copy of the resource at the given path in the given
	 * workspace. This name could be determined either automatically or by querying
	 * the user. This name will <b>not</b> be verified by the caller, so it must be
	 * valid and unique.
	 * <p>
	 * Note this method is for internal use only.
	 * </p>
	 *
	 * @param originalName the full path of the resource
	 * @param workspace the workspace
	 * @return the new full path for the copy, or <code>null</code> if the resource
	 *   should not be copied
	 */
	public static IPath getNewNameFor(IPath originalName, IWorkspace workspace) {
		int counter = 1;
		String resourceName = originalName.lastSegment();
		IPath leadupSegment = originalName.removeLastSegments(1);

		while (true) {
			String nameSegment;

			if (counter > 1)
				nameSegment = WorkbenchMessages.format("CopyResourceAction.copyNameTwoArgs", new Object[] { new Integer(counter), resourceName }); //$NON-NLS-1$
			else
				nameSegment = WorkbenchMessages.format("CopyResourceAction.copyNameOneArg", new Object[] { resourceName }); //$NON-NLS-1$

			IPath pathToTry = leadupSegment.append(nameSegment);

			if (!workspace.getRoot().exists(pathToTry))
				return pathToTry;

			counter++;
		}
	}
	/**
	 * Returns the message for this action's problems dialog.
	 * <p>
	 * The <code>CopyResourceAction</code> implementation of this method returns a
	 * suitable message (localized counterpart of something like "Problems occurred
	 * copying the selected resources.").
	 * </p>
	 *
	 * @return the problems message
	 */
	String getProblemsMessage() {
		return WorkbenchMessages.getString("CopyResourceAction.problemMessage"); //$NON-NLS-1$
	}
	/**
	 * Returns the title for this action's problems dialog.
	 * <p>
	 * The <code>CopyResourceAction</code> implementation of this method returns a
	 * generic title (localized counterpart of "Copy Problems").
	 * </p>
	 *
	 * @return the problems dialog title
	 */
	String getProblemsTitle() {
		return WorkbenchMessages.getString("CopyResourceAction.copyFailedTitle"); //$NON-NLS-1$
	}
	/**
	 * Return an array of resources from the provided list.
	 * @return org.eclipse.core.resources.IResource[]
	 */
	protected IResource[] getResources(List resourceList) {

		IResource resourceArray[] = new IResource[resourceList.size()];
		resourceList.toArray(resourceArray);
		return resourceArray;
	}
	/**
	 * Return the shell in which to show any dialogs
	 */
	Shell getShell() {
		return shell;
	}
	/**
	 * Returns whether the given resource is accessible, where files and folders
	 * are always considered accessible, and where a project is accessible iff it
	 * is open.
	 *
	 * @param resource the resource
	 * @return <code>true</code> if the resource is accessible, and 
	 *   <code>false</code> if it is not
	 */
	boolean isAccessible(IResource resource) {
		switch (resource.getType()) {
			case IResource.FILE :
				return true;
			case IResource.FOLDER :
				return true;
			case IResource.PROJECT :
				return ((IProject) resource).isOpen();
			default :
				return false;
		}
	}
	/**
	 * Returns whether the given candidate child resource is equal to or a descendent
	 * of the the given candidate parent resource.
	 *
	 * @param child the candidate child resource
	 * @param parent the candidate parent resource
	 * @return <code>true</code> if <code>child</code> is equal to or a descendent
	 *   of <code>parent</code>
	 */
	boolean isDescendentOf(IResource child, IResource parent) {
		if (child.equals(parent))
			return true;

		if (parent.getType() == IResource.FILE)
			return false;

		if (child.getType() == IResource.PROJECT)
			return false;

		return isDescendentOf(child.getParent(), parent);
	}
	/**
	 * Returns whether the given resource is equal to or a descendent of one of the 
	 * the given source resources.
	 *
	 * @param destination the destination container
	 * @param sourceResources the list of resources (element type: <code>IResource</code>)
	 * @return <code>true</code> the given resource is equal to or a descendent of 
	 *   one of the the given source resources, and <code>false</code> otherwise
	 */
	boolean isDestinationDescendentOfSource(IContainer destination, List sourceResources) {
		Iterator sourcesEnum = sourceResources.iterator();
		while (sourcesEnum.hasNext()) {
			IResource currentResource = (IResource) sourcesEnum.next();
			if (!currentResource.equals(destination)) {
				if (isDescendentOf(destination, currentResource))
					return true;
			}
		}

		return false;
	}
	/**
	 * Returns whether any of the given source resources are being recopied to their 
	 * current container.
	 *
	 * @param destination the destination container
	 * @param sourceResources the list of resources (element type: <code>IResource</code>)
	 * @return <code>true</code> if at least one of the given source resource's 
	 *   parent container is the same as the destination 
	 */
	boolean isDestinationSameAsSource(IContainer destination, List sourceResources) {
		for (Iterator e = sourceResources.iterator(); e.hasNext();) {
			IResource source = (IResource) e.next();
			if (source.getParent().equals(destination)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * The <code>CopyResourceAction</code> implementation of this 
	 * <code>ISelectionValidator</code> method checks whether the given path
	 * is a good place to copy the selected resources.
	 */
	public String isValid(Object destination) {
		return validateDestination((IPath) destination, getSelectedResources());
	}
	/**
	 * Copies the given resources to the destination container with the given name.
	 * <p>
	 * Note: the destination container may need to be created prior to copying the
	 * resources.
	 * </p>
	 *
	 * @param resources the resources to copy
	 * @param destination the path of the destination container
	 * @return <code>true</code> if the copy operation completed without errors
	 */
	boolean performCopy(final IResource[] resources, final IPath destination, IProgressMonitor monitor) {

		try {
			monitor.subTask(WorkbenchMessages.getString("CopyResourceAction.copying")); //$NON-NLS-1$
			ContainerGenerator generator = new ContainerGenerator(destination);
			generator.generateContainer(new SubProgressMonitor(monitor, 500));
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 75);
			copy(resources, destination, subMonitor);
		} catch (CoreException e) {
			recordError(e); // log error
			return false;
		} finally {
			monitor.done();
		}
		return true;
	}
	/**
	 * Individually copies the given resources to the specified destination
	 * container checking for name collisions. If a collision is detected, it is
	 * saved with a new name. 
	 * <p>
	 * Note: the destination container may need to be created prior to copying the
	 * resources.
	 * </p>
	 *
	 * @param resources the resources to copy
	 * @param destination the path of the destination container
	 * @return <code>true</code> if the copy operation completed without errors.
	 */
	boolean performCopyWithAutoRename(IResource[] resources, IPath destination, IProgressMonitor monitor) {

		monitor.subTask(WorkbenchMessages.getString("CopyResourceAction.copying")); //$NON-NLS-1$

		IWorkspace workspace = resources[0].getWorkspace();

		try {
			ContainerGenerator generator = new ContainerGenerator(destination);

			//We have 75 units to work with
			generator.generateContainer(new SubProgressMonitor(monitor, 10));
			int copyUnits = 65 / resources.length;

			for (int i = 0; i < resources.length; i++) {
				IResource currentResource = resources[i];
				IPath destinationPath = destination.append(currentResource.getName());

				if (workspace.getRoot().exists(destinationPath))
					destinationPath = getNewNameFor(destinationPath, workspace);

				if (destinationPath != null) {
					try {
						copyResource(currentResource, destinationPath, new SubProgressMonitor(monitor, copyUnits));
					} catch (CoreException e) {
						recordError(e); // log error
					}
				}

				if (monitor.isCanceled())
					throw new OperationCanceledException();
			}
		} catch (CoreException e) {
			recordError(e); // log error
			return false;
		} finally {
			monitor.done();
		}

		return true;
	}
	/**
	 * Asks the user for the destination of this action.
	 *
	 * @return the path on an existing or new resource container, or 
	 *  <code>null</code> if the operation should be abandoned
	 */
	IPath queryDestinationResource() {
		// start traversal at root resource, should probably start at a
		// better location in the tree
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(shell, getInitialContainer(), true, WorkbenchMessages.getString("CopyResourceAction.selectDestination")); //$NON-NLS-1$
		dialog.setValidator(this);
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] result = dialog.getResult();
		if (result != null && result.length == 1) {
			return (IPath) result[0];
		}
		return null;
	}
	/**
	 * Records the core exception to be displayed to the user
	 * once the action is finished.
	 *
	 * @param error a <code>CoreException</code>
	 */
	final void recordError(CoreException error) {
		if (errorStatus == null)
			errorStatus = new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, getProblemsMessage(), error);

		errorStatus.merge(error.getStatus());
	}
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		final IPath destination = queryDestinationResource();
		final List sources = getSelectedResources();
		if (destination == null)
			return;

		String errorMsg = validateDestination(destination, sources);
		if (errorMsg != null) {
			displayError(errorMsg);
			return;
		}

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) {
					// Checks only required if this is an exisiting container path.
	monitor.beginTask(WorkbenchMessages.getString("CopyResourceAction.operationTitle"), 100); //$NON-NLS-1$
				boolean copyWithAutoRename = false;
				IWorkspaceRoot root = WorkbenchPlugin.getPluginWorkspace().getRoot();
				if (root.exists(destination)) {
					IContainer container = (IContainer) root.findMember(destination);
					// If we're copying to the source container then perform
					// auto-renames on all resources to avoid name collisions.
					if (isDestinationSameAsSource(container, sources) && canPerformAutoRename())
						copyWithAutoRename = true;
					else {
						// If no auto-renaming will be happening, check for
						// potential name collisions at the target resource
						if (!validateNoNameCollisions(container, sources, monitor)) {
							if (canceled)
								return;
							displayError(WorkbenchMessages.getString("CopyResourceAction.nameCollision")); //$NON-NLS-1$
							return;
						}
					}
				}

				IResource resourceArray[] = getResources(sources);
				//Don't bother if there is nothing selected
				if (resourceArray.length == 0)
					return;

				errorStatus = null;

				if (copyWithAutoRename)
					performCopyWithAutoRename(resourceArray, destination, monitor);
				else
					performCopy(resourceArray, destination, monitor);
			}
		};

		try {
			new ProgressMonitorDialog(shell).run(true, true, op);
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			// CoreExceptions are collected above, but unexpected runtime exceptions and errors may still occur.
			WorkbenchPlugin.log(MessageFormat.format("Exception in {0}.performCopy(): {1}", new Object[] { getClass().getName(), e.getTargetException()})); //$NON-NLS-1$
			displayError(WorkbenchMessages.format("CopyResourceAction.internalError", new Object[] { e.getTargetException().getMessage()})); //$NON-NLS-1$
		}

		// If errors occurred, open an Error dialog
		if (errorStatus != null) {
		ErrorDialog.openError(shell, getProblemsTitle(), null, // no special message
			errorStatus);
			errorStatus = null;
		}
	}
	/**
	 * The <code>CopyResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables this action only if 
	 * all of the one or more selections are sibling resources which are 
	 * local (depth infinity).
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}
		if (getSelectedNonResources().size() > 0) {
			return false;
		}

		// to enable this command all selected resources must be siblings
		List selectedResources = getSelectedResources();
		if (selectedResources.size() == 0)
			return false;
		IContainer firstParent = ((IResource) selectedResources.get(0)).getParent();
		if (firstParent == null) {
			return false;
		}
		Iterator resourcesEnum = selectedResources.iterator();
		while (resourcesEnum.hasNext()) {
			IResource currentResource = (IResource) resourcesEnum.next();
			if (currentResource.getType() == IResource.PROJECT) {
				return false;
			}
			if (!currentResource.getParent().equals(firstParent)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Checks whether the given path to an existing or new container resource is 
	 * a valid destination for copying the given source resources.
	 *
	 * @param destination the path to an existing or new container
	 * @param sourceResources the list of resources (element type: <code>IResource</code>)
	 * @return an error message, or <code>null</code> if the path is valid
	 */
	String validateDestination(IPath destination, List sourceResources) {
		IWorkspaceRoot root = WorkbenchPlugin.getPluginWorkspace().getRoot();
		if (!root.exists(destination))
			return null;

		IContainer container = (IContainer) root.findMember(destination);

		if (!isAccessible(container)) {
			return WorkbenchMessages.getString("CopyResourceAction.destinationAccessError"); //$NON-NLS-1$
		}

		if (isDestinationDescendentOfSource(container, sourceResources)) {
			return WorkbenchMessages.getString("CopyResourceAction.destinationDescendentError"); //$NON-NLS-1$
		}

		return null;
	}
	/**
	 * Returns whether moving all of the given source resources to the given
	 * destination container could be done without causing name collisions.
	 * 
	 * @param destination the destination container
	 * @param sourceResources the list of resources (element type: <code>IResource</code>)
	 * @return <code>true</code> if there would be no name collisions, and
	 *   <code>false</code> if there would
	 */
	private boolean validateNoNameCollisions(IContainer destination, List sourceResources, IProgressMonitor monitor) {

		List deleteItems = new ArrayList();

		IWorkspaceRoot workspaceRoot = destination.getWorkspace().getRoot();

		Iterator sourcesEnum = sourceResources.iterator();
		while (sourcesEnum.hasNext()) {
			final IResource currentResource = (IResource) sourcesEnum.next();
			final IPath currentPath = destination.getFullPath().append(currentResource.getName());

			IResource newResource = workspaceRoot.findMember(currentPath);
			if (newResource != null) {
				// Check to see if we would be overwriting a parent folder
				if (currentPath.isPrefixOf(currentResource.getFullPath())) {
					//Run it inside of a runnable to make sure we get to parent off of the shell as we are not
					//in the UI thread.
					Runnable notice = new Runnable() {
						public void run() {
							MessageDialog.openError(
								getShell(),
								WorkbenchMessages.getString("CopyResourceAction.overwriteProblemTitle"), //$NON-NLS-1$
								WorkbenchMessages.format("CopyResourceAction.overwriteProblem", new Object[] { currentPath, currentResource.getFullPath()})); //$NON-NLS-1$
						}
					};
					getShell().getDisplay().syncExec(notice);
					canceled = true;
					return false;
				} else {
					if (checkOverwrite(getShell(), newResource)) {
						// do not delete folders, we want to merge in this case,
						// not replace.
						if (newResource.getType() == IResource.FILE) {
							deleteItems.add(newResource);
						}
					} else {
						canceled = true;
						return false;
					}
				}
			}
		} //No overwrite issues
		if (deleteItems.size() == 0)
			return true;

		//Now try deletions

		IResource[] deleteResources = new IResource[deleteItems.size()];
		deleteItems.toArray(deleteResources);
		try {
			monitor.subTask(WorkbenchMessages.getString("CopyResourceAction.deletingCollision")); //$NON-NLS-1$
			destination.getWorkspace().delete(
				deleteResources,
				IResource.KEEP_HISTORY,
				new SubProgressMonitor(monitor, 25));
		} catch (CoreException exception) {
			recordError(exception);
			return false;
		}
		return true;

	}
}