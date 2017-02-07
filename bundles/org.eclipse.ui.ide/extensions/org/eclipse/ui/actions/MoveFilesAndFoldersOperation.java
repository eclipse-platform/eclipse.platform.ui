/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.undo.AbstractWorkspaceOperation;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Moves files and folders.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MoveFilesAndFoldersOperation extends CopyFilesAndFoldersOperation {

	/**
	 * Creates a new operation initialized with a shell.
	 *
	 * @param shell
	 *            parent shell for error dialogs
	 */
	public MoveFilesAndFoldersOperation(Shell shell) {
		super(shell);
	}

	/**
	 * Returns whether this operation is able to perform on-the-fly
	 * auto-renaming of resources with name collisions.
	 *
	 * @return <code>true</code> if auto-rename is supported, and
	 *         <code>false</code> otherwise
	 */
	@Override
	protected boolean canPerformAutoRename() {
		return false;
	}

	/**
	 * Moves the resources to the given destination. This method is called
	 * recursively to merge folders during folder move.
	 *
	 * @param resources
	 *            the resources to move
	 * @param destination
	 *            destination to which resources will be moved
	 * @param monitor
	 *            a progress monitor for showing progress and for cancelation
	 *
	 * @deprecated As of 3.3, the work is performed in the undoable operation
	 *             created in
	 *             {@link #getUndoableCopyOrMoveOperation(IResource[], IPath)}
	 */
	@Deprecated
	@Override
	protected void copy(IResource[] resources, IPath destination, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, resources.length);
		for (IResource resource : resources) {
			SubMonitor iterationMonitor = subMonitor.split(1).setWorkRemaining(100);
			IPath destinationPath = destination.append(resource.getName());
			IWorkspace workspace = resource.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			IResource existing = workspaceRoot.findMember(destinationPath);
			if (resource.getType() == IResource.FOLDER && existing != null) {
				// the resource is a folder and it exists in the destination,
				// move the children of the folder.
				if (homogenousResources(resource, existing)) {
					IResource[] children = ((IContainer) resource).members();
					copy(children, destinationPath, iterationMonitor.split(50));
					delete(resource, iterationMonitor.split(50));
				} else {
					// delete the destination folder, moving a linked folder
					// over an unlinked one or vice versa. Fixes bug 28772.
					delete(existing, iterationMonitor.split(50));
					resource.move(destinationPath, IResource.SHALLOW | IResource.KEEP_HISTORY,
							iterationMonitor.split(50));
				}
			} else {
				// if we're merging folders, we could be overwriting an existing
				// file
				if (existing != null) {
					if (homogenousResources(resource, existing)) {
						moveExisting(resource, existing, iterationMonitor.split(100));
					} else {
						// Moving a linked resource over unlinked or vice versa.
						// Can't use setContents here. Fixes bug 28772.
						delete(existing, iterationMonitor.split(50));
						resource.move(destinationPath, IResource.SHALLOW | IResource.KEEP_HISTORY,
								iterationMonitor.split(50));
					}
				} else {
					resource.move(destinationPath, IResource.SHALLOW | IResource.KEEP_HISTORY,
							iterationMonitor.split(100));
				}
			}
		}
	}

	/**
	 * Returns the message for querying deep copy/move of a linked resource.
	 *
	 * @param source
	 *            resource the query is made for
	 * @return the deep query message
	 */
	@Override
	protected String getDeepCheckQuestion(IResource source) {
		return NLS
				.bind(
						IDEWorkbenchMessages.CopyFilesAndFoldersOperation_deepMoveQuestion,
						source.getFullPath().makeRelative());
	}

	/**
	 * Returns the task title for this operation's progress dialog.
	 *
	 * @return the task title
	 */
	@Override
	protected String getOperationTitle() {
		return IDEWorkbenchMessages.MoveFilesAndFoldersOperation_operationTitle;
	}

	/**
	 * Returns the message for this operation's problems dialog.
	 *
	 * @return the problems message
	 */
	@Override
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.MoveFilesAndFoldersOperation_problemMessage;
	}

	/**
	 * Returns the title for this operation's problems dialog.
	 *
	 * @return the problems dialog title
	 */
	@Override
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.MoveFilesAndFoldersOperation_moveFailedTitle;
	}

	/**
	 * Returns whether the source file in a destination collision will be
	 * validateEdited together with the collision itself. Returns true.
	 *
	 * @return boolean <code>true</code>, the source file in a destination
	 *         collision should be validateEdited.
	 */
	@Override
	protected boolean getValidateConflictSource() {
		return true;
	}

	/**
	 * Sets the content of the existing file to the source file content. Deletes
	 * the source file.
	 *
	 * @param source
	 *            source file to move
	 * @param existing
	 *            existing file to set the source content in
	 * @param monitor
	 *            a progress monitor for showing progress and for cancelation
	 * @throws CoreException
	 *             setContents failed
	 * @deprecated As of 3.3, this method is not called.
	 */
	@Deprecated
	private void moveExisting(IResource source, IResource existing, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		IFile existingFile = getFile(existing);

		if (existingFile != null) {
			IFile sourceFile = getFile(source);

			if (sourceFile != null) {
				existingFile.setContents(sourceFile.getContents(), IResource.KEEP_HISTORY, subMonitor.split(1));
				delete(sourceFile, subMonitor.split(1));
			}
		}
	}

	@Override
	public String validateDestination(IContainer destination,
			IResource[] sourceResources) {
		IPath destinationLocation = destination.getLocation();

		for (IResource sourceRessource : sourceResources) {
			// is the source being copied onto itself?
			if (sourceRessource.getParent().equals(destination)) {
				return NLS
						.bind(
								IDEWorkbenchMessages.MoveFilesAndFoldersOperation_sameSourceAndDest,
								sourceRessource.getName());
			}
			// test if linked source is copied onto itself. Fixes bug 29913.
			if (destinationLocation != null) {
				IPath sourceLocation = sourceRessource.getLocation();
				IPath destinationResource = destinationLocation
						.append(sourceRessource.getName());
				if (sourceLocation != null
						&& sourceLocation.isPrefixOf(destinationResource)) {
					return NLS
							.bind(
									IDEWorkbenchMessages.MoveFilesAndFoldersOperation_sameSourceAndDest,
									sourceRessource.getName());
				}
			}
		}
		return super.validateDestination(destination, sourceResources);
	}

	@Override
	protected boolean isMove() {
		return true;
	}

	/**
	 * Returns an AbstractWorkspaceOperation suitable for performing the move or
	 * copy operation that will move or copy the given resources to the given
	 * destination path.
	 *
	 * @param resources
	 *            the resources to be moved or copied
	 * @param destinationPath
	 *            the destination path to which the resources should be moved
	 * @return the operation that should be used to perform the move or copy
	 * @since 3.3
	 */
	@Override
	protected AbstractWorkspaceOperation getUndoableCopyOrMoveOperation(
			IResource[] resources, IPath destinationPath) {
		return new MoveResourcesOperation(resources, destinationPath,
				IDEWorkbenchMessages.CopyFilesAndFoldersOperation_moveTitle);

	}
}
