/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.resource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;

import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * {@link Change} that moves a resource.
 *
 * @since 3.4
 */
public class MoveResourceChange extends ResourceChange {

	private final IResource fSource;
	private final IContainer fTarget;
	private final long fStampToRestore;
	
	private ChangeDescriptor fDescriptor;
	
	/**
	 * Creates the change.
	 * 
	 * @param source the resource to move
	 * @param target the container the resource is moved to
	 */
	public MoveResourceChange(IResource source, IContainer target) {
		this(source, target, IResource.NULL_STAMP);
	}

	/**
	 * Creates the change.
	 * 
	 * @param source the resource to move
	 * @param target the container the resource is moved to
	 * @param stampToRestore the stamp to restore
	 */
	protected MoveResourceChange(IResource source, IContainer target, long stampToRestore) {
		fSource= source;
		fTarget= target;
		fStampToRestore= stampToRestore;
		
		// We already present a dialog to the user if he
		// moves read-only resources. Since moving a resource
		// doesn't do a validate edit (it actually doesn't
		// change the content we can't check for READ only
		// here.
		setValidationMethod(VALIDATE_NOT_DIRTY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getDescriptor()
	 */
	public ChangeDescriptor getDescriptor() {
		return fDescriptor;
	}

	/**
	 * Sets the change descriptor to be returned by {@link Change#getDescriptor()}.
	 * 
	 * @param descriptor the change descriptor
	 */
	public void setDescriptor(ChangeDescriptor descriptor) {
		fDescriptor= descriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final Change perform(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			pm.beginTask(getName(), 2);

			IResource newResource= fTarget.findMember(fSource.getName());
			if (newResource != null && newResource.exists()) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), RefactoringCoreMessages.MoveResourceChange_error_destination_already_exists));
			}

			long currentStamp= fSource.getModificationStamp();
			IPath destinationPath= fTarget.getFullPath().append(fSource.getName());
			fSource.move(destinationPath, IResource.KEEP_HISTORY | IResource.SHALLOW, new SubProgressMonitor(pm, 1));
			newResource= ResourcesPlugin.getWorkspace().getRoot().findMember(destinationPath);
			if (fStampToRestore != IResource.NULL_STAMP) {
				newResource.revertModificationStamp(fStampToRestore);
			}
			return new MoveResourceChange(newResource, fSource.getParent(), currentStamp);
		} finally {
			pm.done();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.resource.ResourceChange#getModifiedResource()
	 */
	protected IResource getModifiedResource() {
		return fSource;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return Messages.format(RefactoringCoreMessages.MoveResourceChange_name, new String[] { fSource.getFullPath().toString(), fTarget.getName() });
	}
}