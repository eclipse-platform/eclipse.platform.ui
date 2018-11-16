/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - created based on RenameResourceProcessor
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.resource.MoveRenameResourceChange;
import org.eclipse.ltk.core.refactoring.resource.MoveRenameResourceDescriptor;
import org.eclipse.ltk.core.refactoring.resource.Resources;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * A move/rename processor for {@link IResource}. The processor will move and rename the resource and
 * load rename participants if references should be renamed as well.
 *
 * @since 3.10
 */
public class MoveRenameResourceProcessor extends RenameProcessor {

	private IResource fResource;
	private String fNewResourceName;
	private IContainer fDestination;
	private boolean fUpdateReferences;
	private RenameArguments fRenameArguments; // set after checkFinalConditions

	/**
	 * Creates a new rename resource processor.
	 *
	 * @param resource the resource to rename.
	 */
	public MoveRenameResourceProcessor(IResource resource) {
		if (resource == null || !resource.exists()) {
			throw new IllegalArgumentException("resource must not be null and must exist"); //$NON-NLS-1$
		}

		fResource= resource;
		fRenameArguments= null;
		fDestination= null;
		fUpdateReferences= true;
		setNewResourceName(resource.getName()); // Initialize new name
	}

	/**
	 * Returns the resource this processor was created on
	 *
	 * @return the resource to rename
	 */
	public IResource getResource() {
		return fResource;
	}

	/**
	 * Returns the new resource name
	 *
	 * @return the new resource name
	 */
	public String getNewResourceName() {
		return fNewResourceName;
	}

	/**
	 * Sets the new resource name
	 *
	 * @param newName the new resource name
	 */
	public void setNewResourceName(String newName) {
		Assert.isNotNull(newName);
		fNewResourceName= newName;
	}

	/**
	 * Sets the move destination
	 *
	 * @param destination the move destination
	 */
	public void setDestination(IContainer destination) {
		Assert.isNotNull(destination);
		fDestination= destination;
	}


	/**
	 * Returns <code>true</code> if the refactoring processor also updates references
	 *
	 * @return <code>true</code> if the refactoring processor also updates references
	 */
	public boolean isUpdateReferences() {
		return fUpdateReferences;
	}

	/**
	 * Specifies if the refactoring processor also updates references. The default behaviour is to update references.
	 *
	 * @param updateReferences <code>true</code> if the refactoring processor should also updates references
	 */
	public void setUpdateReferences(boolean updateReferences) {
		fUpdateReferences= updateReferences;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		IStatus status= Resources.checkInSync(fResource);
		if (!status.isOK()) {
			boolean autoRefresh= Platform.getPreferencesService().getBoolean(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, false, null);
			if (autoRefresh) {
				fResource.refreshLocal(IResource.DEPTH_INFINITE, pm);
				status= Resources.checkInSync(fResource);
			}
		}
		return RefactoringStatus.create(status);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			fRenameArguments= new RenameArguments(getNewResourceName(), isUpdateReferences());

			ResourceChangeChecker checker= context.getChecker(ResourceChangeChecker.class);
			IResourceChangeDescriptionFactory deltaFactory= checker.getDeltaFactory();

			ResourceModifications.buildMoveDelta(deltaFactory, fResource, fRenameArguments);

			return new RefactoringStatus();
		} finally {
			pm.done();
		}
	}

	/**
	 * Validates if the a name is valid. This method does not change the name settings on the refactoring. It is intended to be used
	 * in a wizard to validate user input.
	 *
	 * @param newName the name to validate
	 * @return returns the resulting status of the validation
	 */
	public RefactoringStatus validateNewElementName(String newName) {
		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$
		IContainer c= fResource.getParent();
		if (c == null)
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameResourceProcessor_error_no_parent);

		if (!c.getFullPath().isValidSegment(newName))
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameResourceProcessor_error_invalid_name);

		if (fDestination.findMember(newName) != null)
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameResourceProcessor_error_resource_already_exists);

		RefactoringStatus result= RefactoringStatus.create(c.getWorkspace().validateName(newName, fResource.getType()));
		if (!result.hasFatalError())
			result.merge(RefactoringStatus.create(c.getWorkspace().validatePath(createNewPath(newName), fResource.getType())));
		return result;
	}

	/**
	 * Validates if the a destination is valid. This method does not change the destination settings on the refactoring. It is intended to be used
	 * in a wizard to validate user input.
	 *
	 * @param destination the destination to validate
	 * @return returns the resulting status of the validation
	 */
	public RefactoringStatus validateDestination(IContainer destination) {
		Assert.isNotNull(destination, "container is null"); //$NON-NLS-1$
		if (destination instanceof IWorkspaceRoot)
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.MoveResourceProcessor_error_invalid_destination);

		if (!destination.exists()) {
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.MoveResourceProcessor_error_destination_not_exists);
		}

		IPath destinationPath= destination.getFullPath();
		IPath path= fResource.getFullPath();
		if (path.isPrefixOf(destinationPath) || path.equals(destinationPath)) {
			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.MoveResourceProcessor_destination_inside_moved, BasicElementLabels.getPathLabel(path, false)));
		}
		return new RefactoringStatus();
	}


	protected MoveRenameResourceDescriptor createDescriptor() {
		IResource resource= getResource();

		MoveRenameResourceDescriptor descriptor= new MoveRenameResourceDescriptor();
		descriptor.setProject(resource instanceof IProject ? null : resource.getProject().getName());
		descriptor.setDescription(Messages.format(RefactoringCoreMessages.RenameResourceProcessor_description, BasicElementLabels.getResourceName(resource)));
		descriptor.setComment(Messages.format(RefactoringCoreMessages.RenameResourceProcessor_comment, new String[] { BasicElementLabels.getPathLabel(resource.getFullPath(), false), BasicElementLabels.getResourceName(getNewResourceName()) }));
		descriptor.setFlags(RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
		descriptor.setResourcePath(resource.getFullPath());
		descriptor.setDestination(fDestination);
		descriptor.setNewName(getNewResourceName());
		descriptor.setUpdateReferences(isUpdateReferences());
		return descriptor;
	}


	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			MoveRenameResourceChange change= new MoveRenameResourceChange(fResource, fDestination, getNewResourceName());
			change.setDescriptor(new RefactoringChangeDescriptor(createDescriptor()));
			return change;
		} finally {
			pm.done();
		}
	}

	private String createNewPath(String newName) {
		return fResource.getFullPath().removeLastSegments(1).append(newName).toString();
	}

	@Override
	public Object[] getElements() {
		return new Object[] { fResource};
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.ltk.core.refactoring.moveRenameResourceProcessor"; //$NON-NLS-1$
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.MoveRenameResourceProcessor_processor_name;
	}

	@Override
	public boolean isApplicable() {
		if (fResource == null)
			return false;
		if (!fResource.exists())
			return false;
		if (!fResource.isAccessible())
			return false;
		return true;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants shared) throws CoreException {
		String[] affectedNatures= ResourceProcessors.computeAffectedNatures(fResource);
		return ParticipantManager.loadRenameParticipants(status, this, fResource, fRenameArguments, null, affectedNatures, shared);
	}

}