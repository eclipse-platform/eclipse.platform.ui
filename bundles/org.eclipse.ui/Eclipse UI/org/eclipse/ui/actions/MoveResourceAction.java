package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Standard action for moving the currently selected resources elsewhere
 * in the workspace. All resources being moved as a group must be siblings.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class MoveResourceAction extends CopyResourceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".MoveResourceAction"; //$NON-NLS-1$

	/**
	* Keep a list of destinations so that any required update can be done after the
	* move.
	*/
	protected List destinations;

	private static final String CHECK_MOVE_TITLE = WorkbenchMessages.getString("MoveResourceAction.title"); //$NON-NLS-1$
	private static final String CHECK_MOVE_MESSAGE = WorkbenchMessages.getString("MoveResourceAction.checkMoveMessage"); //$NON-NLS-1$
	private static final String MOVING_MESSAGE = WorkbenchMessages.getString("MoveResourceAction.progressMessage"); //$NON-NLS-1$

	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 */
	public MoveResourceAction(Shell shell) {
		super(shell, WorkbenchMessages.getString("MoveResourceAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("MoveResourceAction.toolTip")); //$NON-NLS-1$
		setId(MoveResourceAction.ID);
		WorkbenchHelp.setHelp(this, IHelpContextIds.MOVE_RESOURCE_ACTION);
	}
	/**
	 * The <code>MoveResourceAction</code> implementation of this 
	 * <code>CopyResourceAction</code> method returns <code>false</code>.
	 */
	boolean canPerformAutoRename() {
		return false;
	}
	/**
	 * Returns the destination resources for the resources that have been moved so far.
	 *
	 * @return list of destination <code>IResource</code>s
	 */
	protected List getDestinations() {
		return this.destinations;
	}
	/** (non-Javadoc)
	 * Method declared on CopyResourceAction.
	 */
	String getProblemsMessage() {
		return WorkbenchMessages.getString("MoveResourceAction.problemMessage"); //$NON-NLS-1$
	}
	/** (non-Javadoc)
	 * Method declared on CopyResourceAction.
	 */
	String getProblemsTitle() {
		return WorkbenchMessages.getString("MoveResourceAction.dialogTitle"); //$NON-NLS-1$
	}
	/**
	 * Return an array of resources from the provided list.
	 */
	protected IResource[] getResources(List resourceList) {

		ReadOnlyStateChecker checker = new ReadOnlyStateChecker(getShell(), CHECK_MOVE_TITLE, CHECK_MOVE_MESSAGE);

		return checker.checkReadOnlyResources(super.getResources(resourceList));
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
			if (isDescendentOf(destination, currentResource))
				return true;
		}

		return false;
	}
	/**
	 * Move the resources to the given destination.  This method is called recursively to
	 * deal with merging of folders during folder move.
	 */
	private void move(IResource[] resources, IPath destination, IProgressMonitor subMonitor) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource source = resources[i];
			IPath destinationPath = destination.append(source.getName());
			IWorkspace workspace = source.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			boolean isFolder = source.getType() == IResource.FOLDER;
			boolean exists = workspaceRoot.exists(destinationPath);
			if (isFolder && exists) {
				// the resource is a folder and it exists in the destination, copy the
				// children of the folder
				IResource[] children = ((IContainer) source).members();
				move(children, destinationPath, subMonitor);
				// need to explicitly delete the folder since we're not moving it
				delete(source, subMonitor);
			} else {
				// if we're merging folders, we could be overwriting an existing file
				IResource existing = workspaceRoot.findMember(destinationPath);
				if (existing != null) {
					delete(existing, subMonitor);
				}
				source.move(destinationPath, IResource.KEEP_HISTORY, new SubProgressMonitor(subMonitor, 0));
				subMonitor.worked(1);
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		}
	}
	/**
	 * The <code>MoveResourceAction</code> implementation of this 
	 * <code>CopyResourceAction</code> method does a move instead of a copy.
	 */
	boolean performCopy(final IResource[] resources, final IPath destination, IProgressMonitor monitor) {
		try {
			monitor.subTask(MOVING_MESSAGE); //$NON-NLS-1$
			ContainerGenerator generator = new ContainerGenerator(destination);
			generator.generateContainer(new SubProgressMonitor(monitor, 500));
			//We have 75 units to work with
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 75);
			move(resources, destination, subMonitor);
			for (int i = 0; i < resources.length; i++) {
				getDestinations().add(destination.append(resources[i].getName()));
			}
		} catch (CoreException e) {
			recordError(e); // log error
			return false;
		} finally {
			monitor.done();
		}
		return true;
	}
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		//Initialize the destinations
		this.destinations = new ArrayList();
		super.run();
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
		if (root.exists(destination)) {
			IContainer container = (IContainer) root.findMember(destination);
			if (isDestinationSameAsSource(container, sourceResources)) {
				return WorkbenchMessages.getString("MoveResourceAction.sameSourceAndDest"); //$NON-NLS-1$
			}
		}
		return super.validateDestination(destination, sourceResources);
	}
}