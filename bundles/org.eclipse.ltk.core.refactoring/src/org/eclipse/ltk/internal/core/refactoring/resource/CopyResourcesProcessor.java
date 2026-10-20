/*******************************************************************************
 * Copyright (c) 2026 Felix Schmid
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Felix Schmid - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.CopyParticipant;
import org.eclipse.ltk.core.refactoring.participants.CopyProcessor;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.resource.CopyResourceChange;
import org.eclipse.ltk.core.refactoring.resource.CopyResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.resource.Resources;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * A copy processor for {@link IResource resources}. The processor will copy the resources and load
 * copy participants.
 *
 * @since 3.16
 */
public final class CopyResourcesProcessor extends CopyProcessor {

	private final IResource[] resources;

	private final IPath[] destinationPaths;

	private IContainer destination;

	private final ReorgExecutionLog log;

	public CopyResourcesProcessor(final IResource[] resources, final IPath[] destinationPaths) {
		Assert.isNotNull(resources);
		Assert.isNotNull(destinationPaths);
		Assert.isTrue(resources.length == destinationPaths.length);
		this.resources= resources;
		this.destinationPaths= destinationPaths;
		log = new ReorgExecutionLog();
	}

	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		pm.beginTask("", resources.length); //$NON-NLS-1$
		try {
			RefactoringStatus status= new RefactoringStatus();
			if (destinationPaths.length == 0) {
				return status; // nothing to copy
			}

			// check for unsaved changes
			status.merge(RefactoringStatus.create(Resources.checkInSync(resources)));

			// check if destination containers are consistent for all copy locations
			IPath destPath= destinationPaths[0].removeLastSegments(1);
			IResource dest= ResourcesPlugin.getWorkspace().getRoot().findMember(destPath);
			if (!setAndValidateDestination(dest, status)) {
				return status;
			}

			for (int i= 0; i < resources.length; i++) {
				pm.worked(1);
				if (!destinationPaths[i].removeLastSegments(1).equals(destPath)) {
					status.addFatalError(RefactoringCoreMessages.CopyResourcesProcessor_error_multiple_destinatinos);
					break;
				}

				String destName= destinationPaths[i].lastSegment();
				if (!resources[i].getName().equals(destName)) {
					// copy should use different name then origin
					log.setNewName(resources[i], destName);
				}
			}
			return status;
		} finally {
			pm.done();
		}
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm, final CheckConditionsContext context)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	public boolean setAndValidateDestination(IResource dest, RefactoringStatus status) {
		if (dest == null || !dest.exists()) {
			status.addFatalError(RefactoringCoreMessages.MoveResourceProcessor_error_destination_not_exists);
			return false;
		}
		if (!(dest instanceof IContainer container)) {
			status.addFatalError(RefactoringCoreMessages.MoveResourcesDescriptor_error_destination_not_exists);
			return false;
		}
		destination= container;
		if (destination instanceof IWorkspaceRoot) {
			status.addFatalError(RefactoringCoreMessages.MoveResourceProcessor_error_invalid_destination);
			return false;
		}

		IPath destinationPath= destination.getFullPath();
		for (IResource r : resources) {
			IPath path= r.getFullPath();
			if (path.isPrefixOf(destinationPath) || path.equals(destinationPath)) {
				status.addFatalError(MessageFormat.format(
						RefactoringCoreMessages.CopyResourcesProcessor_destination_inside_moved,
						BasicElementLabels.getPathLabel(path, false)));
				return false;
			}
		}
		return true;
	}

	@Override
	public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		pm.beginTask(RefactoringCoreMessages.CopyResourcesProcessor_create_task, resources.length);
		try {
			final CompositeChange compChange= new CompositeChange(getDescription());
			compChange.markAsSynthetic();
			RefactoringChangeDescriptor descriptor= new RefactoringChangeDescriptor(createDescriptor());

			for (IResource resource : resources) {
				pm.worked(1);
				CopyResourceChange copyChange= new CopyResourceChange(resource, log, destination);
				copyChange.setDescriptor(descriptor);
				compChange.add(copyChange);
			}
			return compChange;
		} finally {
			pm.done();
		}
	}

	@Override
	public RefactoringParticipant[] loadParticipants(final RefactoringStatus status,
			final SharableParticipants sharedParticipants) throws CoreException {
		final List<CopyParticipant> result= new ArrayList<>();
		final String[] affectedNatures= ResourceProcessors.computeAffectedNatures(resources);
		final CopyArguments copyArguments= new CopyArguments(destination, log);

		for (IResource resource : resources) {
			final CopyParticipant[] participants= ParticipantManager.loadCopyParticipants(status, this, resource,
					copyArguments, affectedNatures, sharedParticipants);
			result.addAll(Arrays.asList(participants));
		}
		return result.toArray(new RefactoringParticipant[result.size()]);
	}

	@Override
	public Object[] getElements() {
		return resources;
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.ltk.core.refactoring.copyResourceProcessor"; //$NON-NLS-1$
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.CopyResourcesProcessor_name;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		for (IResource r : resources) {
			if (!canCopy(r)) {
				return false;
			}
		}
		return true;
	}

	private static boolean canCopy(IResource res) {
		return (res instanceof IFile || res instanceof IFolder) && res.exists();
	}

	protected CopyResourcesDescriptor createDescriptor() {
		CopyResourcesDescriptor descriptor= new CopyResourcesDescriptor();
		descriptor.setProject(destination.getProject().getName());
		descriptor.setDescription(getDescription());
		descriptor.setComment(descriptor.getDescription());
		descriptor.setFlags(RefactoringDescriptor.STRUCTURAL_CHANGE |
				RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);

		descriptor.setResources(resources);
		descriptor.setDestinationPaths(destinationPaths);
		return descriptor;
	}

	private String getDescription() {
		if (resources.length == 1) {
			return MessageFormat.format(RefactoringCoreMessages.CopyResourcesProcessor_description_single,
					BasicElementLabels.getResourceName(resources[0]),
					BasicElementLabels.getResourceName(destination));
		} else {
			return MessageFormat.format(RefactoringCoreMessages.CopyResourcesProcessor_description_multiple,
					resources.length,
					BasicElementLabels.getResourceName(destination));
		}
	}
}
