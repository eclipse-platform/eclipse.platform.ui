/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.MoveProcessor;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;
import org.eclipse.ltk.core.refactoring.resource.MoveResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.resource.Resources;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * A move processor for {@link IResource resources}. The processor will move the resources and
 * load move participants if references should be move as well.
 *
 * @since 3.4
 */
public class MoveResourcesProcessor extends MoveProcessor {

	private final IResource[] fResourcesToMove;
	private IContainer fDestination;
	private boolean fUpdateReferences;
	private MoveArguments fMoveArguments; // set after checkFinalConditions

	/**
	 * Creates a new move resource processor.
	 *
	 * @param resourcesToMove the resources to move
	 */
	public MoveResourcesProcessor(IResource[] resourcesToMove) {
		if (resourcesToMove == null) {
			throw new IllegalArgumentException("resources must not be null"); //$NON-NLS-1$
		}

		fResourcesToMove= resourcesToMove;
		fDestination= null;
		fUpdateReferences= true;
	}

	/**
	 * Returns the resources to move.
	 *
	 * @return the resources to move.
	 */
	public IResource[] getResourcesToMove() {
		return fResourcesToMove;
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
	 * Specifies if the refactoring processor also updates references. The default behavior is to update references.
	 *
	 * @param updateReferences <code>true</code> if the refactoring processor should also updates references
	 */
	public void setUpdateReferences(boolean updateReferences) {
		fUpdateReferences= updateReferences;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();
		result.merge(RefactoringStatus.create(Resources.checkInSync(fResourcesToMove)));
		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			RefactoringStatus status= validateDestination(fDestination);
			if (status.hasFatalError()) {
				return status;
			}
			fMoveArguments= new MoveArguments(fDestination, isUpdateReferences());

			ResourceChangeChecker checker= context.getChecker(ResourceChangeChecker.class);
			IResourceChangeDescriptionFactory deltaFactory= checker.getDeltaFactory();

			for (IResource resource : fResourcesToMove) {
				IResource newResource= fDestination.findMember(resource.getName());
				if (newResource != null) {
					status.addWarning(Messages.format(RefactoringCoreMessages.MoveResourcesProcessor_warning_destination_already_exists, BasicElementLabels.getPathLabel(newResource.getFullPath(), false)));
					deltaFactory.delete(newResource);
				}
				ResourceModifications.buildMoveDelta(deltaFactory, resource, fMoveArguments);
			}
			return status;
		} finally {
			pm.done();
		}
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
		for (IResource r : fResourcesToMove) {
			IPath path= r.getFullPath();
			if (path.isPrefixOf(destinationPath) || path.equals(destinationPath)) {
				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.MoveResourceProcessor_destination_inside_moved, BasicElementLabels.getPathLabel(path, false)));
			}
			if (path.removeLastSegments(1).equals(destinationPath)) {
				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.MoveResourceProcessor_destination_same_as_moved, BasicElementLabels.getPathLabel(path, false)));
			}
		}
		return new RefactoringStatus();
	}

	private String getMoveDescription() {
		if (fResourcesToMove.length == 1) {
			return Messages.format(RefactoringCoreMessages.MoveResourceProcessor_description_single, new String[] { BasicElementLabels.getResourceName(fResourcesToMove[0]), BasicElementLabels.getResourceName(fDestination) });
		} else {
			return Messages.format(RefactoringCoreMessages.MoveResourceProcessor_description_multiple, new Object[] { Integer.valueOf(fResourcesToMove.length), BasicElementLabels.getResourceName(fDestination) });
		}
	}

	protected MoveResourcesDescriptor createDescriptor() {
		MoveResourcesDescriptor descriptor= new MoveResourcesDescriptor();
		descriptor.setProject(fDestination.getProject().getName());
		descriptor.setDescription(getMoveDescription());
		if (fResourcesToMove.length <= 1) {
			descriptor.setComment(descriptor.getDescription());
		} else {
			StringBuilder buf= new StringBuilder();
			for (int i= 0; i < fResourcesToMove.length; i++) {
				if (i > 0)
					buf.append(", "); //$NON-NLS-1$
				buf.append(fResourcesToMove[i].getName());
			}
			descriptor.setComment(Messages.format(RefactoringCoreMessages.MoveResourceProcessor_comment, new String[] { buf.toString(), BasicElementLabels.getResourceName(fDestination) }));
		}
		descriptor.setFlags(RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
		descriptor.setDestination(fDestination);
		descriptor.setUpdateReferences(isUpdateReferences());
		descriptor.setResourcesToMove(fResourcesToMove);
		return descriptor;
	}


	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", fResourcesToMove.length); //$NON-NLS-1$
		try {
			CompositeChange compositeChange= new CompositeChange(getMoveDescription());
			compositeChange.markAsSynthetic();

			RefactoringChangeDescriptor descriptor= new RefactoringChangeDescriptor(createDescriptor());
			for (IResource r : fResourcesToMove) {
				MoveResourceChange moveChange= new MoveResourceChange(r, fDestination);
				moveChange.setDescriptor(descriptor);
				compositeChange.add(moveChange);
			}
			return compositeChange;
		} finally {
			pm.done();
		}
	}

	@Override
	public Object[] getElements() {
		return fResourcesToMove;
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.ltk.core.refactoring.moveResourcesProcessor"; //$NON-NLS-1$
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.MoveResourceProcessor_processor_name;
	}

	@Override
	public boolean isApplicable() {
		for (IResource r : fResourcesToMove) {
			if (!canMove(r)) {
				return false;
			}
		}
		return true;
	}

	private static boolean canMove(IResource res) {
		return (res instanceof IFile || res instanceof IFolder) && res.exists() && !res.isPhantom();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants shared) throws CoreException {
		String[] affectedNatures= ResourceProcessors.computeAffectedNatures(fResourcesToMove);

		List<MoveParticipant> result= new ArrayList<>();
		for (IResource r : fResourcesToMove) {
			MoveParticipant[] participants= ParticipantManager.loadMoveParticipants(status, this, r, fMoveArguments, null, affectedNatures, shared);
			result.addAll(Arrays.asList(participants));
		}
		return result.toArray(new RefactoringParticipant[result.size()]);
	}

}