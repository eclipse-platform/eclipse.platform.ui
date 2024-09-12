/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.CopyProcessor;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.resource.CopyProjectChange;
import org.eclipse.ltk.core.refactoring.resource.CopyProjectDescriptor;
import org.eclipse.ltk.core.refactoring.resource.Resources;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * A copy processor for {@link IProject projects}. The processor will copy the project and load copy
 * participants.
 *
 * @since 3.15
 */
public class CopyProjectProcessor extends CopyProcessor {
	private IProject fProject;

	private String fNewName;

	private IPath fNewLocation;

	/**
	 * Create a new copy project processor.
	 *
	 * @param project the {@link IProject} to copy.
	 * @param newLocation the new Location for the project.
	 * @param newName name of the new project.
	 */
	public CopyProjectProcessor(IProject project, String newName, IPath newLocation) {
		if (project == null || !project.exists()) {
			throw new IllegalArgumentException("project must not be null and must exist"); //$NON-NLS-1$
		}

		fProject= project;
		fNewName= newName;
		fNewLocation= newLocation;
	}

	/**
	 * Returns the project to copy
	 *
	 * @return the project to copy
	 */
	public IProject getProjectToCopy() {
		return fProject;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		IStatus status= Resources.checkInSync(fProject);
		if (!status.isOK()) {
			boolean autoRefresh= Platform.getPreferencesService().getBoolean(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, false, null);
			if (autoRefresh) {
				fProject.refreshLocal(IResource.DEPTH_INFINITE, pm);
				status= Resources.checkInSync(fProject);
			}
		}
		return RefactoringStatus.create(status);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException, OperationCanceledException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			RefactoringStatus result= new RefactoringStatus();

			if (!isSynchronizedExcludingLinkedResources(fProject)) {
				String pathLabel= BasicElementLabels.getPathLabel(fProject.getFullPath(), false);

				String locationLabel= null;
				IPath location= fProject.getLocation();
				if (location != null) {
					locationLabel= BasicElementLabels.getPathLabel(location, true);
				} else {
					URI uri= fProject.getLocationURI();
					if (uri != null) {
						locationLabel= BasicElementLabels.getURLPart(uri.toString());
					}
				}

				String warning;
				if (locationLabel != null) {
					warning= Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_warning_out_of_sync_container_loc, new Object[] { pathLabel, locationLabel });
				} else {
					warning= Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_warning_out_of_sync_container, pathLabel);
				}
				result.addWarning(warning);
			}

			checkDirtyResources(result);

			if (ResourcesPlugin.getWorkspace().getRoot().getProject(fNewName).exists()) {
				result.addError(Messages.format(RefactoringCoreMessages.CopyProjectProcessor_error_project_exists, fNewName));
			}

			ResourceChangeChecker checker= context.getChecker(ResourceChangeChecker.class);
			IResourceChangeDescriptionFactory deltaFactory= checker.getDeltaFactory();
			deltaFactory.copy(fProject, fNewLocation.append(fNewName));

			return result;
		} finally {
			pm.done();
		}
	}

	/**
	 * Checks whether this resource and its descendents are considered to be in sync with the local
	 * file system. The linked resources and their descendents are excluded from the check.
	 *
	 * @param resource the resource to check
	 * @return <code>true</code> if this resource and its descendents except linked resources are
	 *         synchronized, and <code>false</code> in all other cases
	 * @throws CoreException if visiting the resource descendents fails for any reason
	 * @see IResource#isSynchronized(int)
	 */
	public boolean isSynchronizedExcludingLinkedResources(IResource resource) throws CoreException {
		boolean[] result= { true };
		resource.accept((IResourceVisitor) visitedResource -> {
			if (!result[0] || visitedResource.isLinked())
				return false;
			if (!visitedResource.isSynchronized(IResource.DEPTH_ZERO)) {
				result[0]= false;
				return false;
			}
			return true;
		}, IResource.DEPTH_INFINITE, IContainer.DO_NOT_CHECK_EXISTENCE);
		return result[0];
	}

	private void checkDirtyResources(final RefactoringStatus result) throws CoreException {
		if (!fProject.isOpen()) {
			return;
		}
		fProject.accept((IResourceVisitor) visitedResource -> {
			if (visitedResource instanceof IFile) {
				checkDirtyFile(result, (IFile) visitedResource);
			}
			return true;
		}, IResource.DEPTH_INFINITE, false);
	}

	private void checkDirtyFile(RefactoringStatus result, IFile file) {
		if (!file.exists())
			return;
		ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		if (buffer != null && buffer.isDirty()) {
			String message= RefactoringCoreMessages.DeleteResourcesProcessor_warning_unsaved_file;
			if (buffer.isStateValidated() && buffer.isSynchronized()) {
				result.addWarning(Messages.format(message, BasicElementLabels.getPathLabel(file.getFullPath(), false)));
			} else {
				result.addFatalError(Messages.format(message, BasicElementLabels.getPathLabel(file.getFullPath(), false)));
			}
		}
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		pm.beginTask(RefactoringCoreMessages.DeleteResourcesProcessor_create_task, 1);
		try {
			CopyProjectChange change= new CopyProjectChange(fProject, fNewLocation, fNewName);
			change.setDescriptor(new RefactoringChangeDescriptor(createDescriptor()));
			return change;
		} finally {
			pm.done();
		}
	}

	protected CopyProjectDescriptor createDescriptor() {
		CopyProjectDescriptor descriptor= new CopyProjectDescriptor();
		descriptor.setProject(null);
		descriptor.setDescription(getDescription());
		descriptor.setComment(descriptor.getDescription());
		descriptor.setFlags(RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);

		descriptor.setProjectToCopy(fProject);
		descriptor.setNewName(fNewName);
		descriptor.setNewLocation(fNewLocation);
		return descriptor;
	}

	private String getDescription() {
		return Messages.format(RefactoringCoreMessages.CopyProjectProcessor_description, BasicElementLabels.getPathLabel(fProject.getFullPath(), false));
	}

	@Override
	public Object[] getElements() {
		return new Object[] { fProject };
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.ltk.core.refactoring.copyProjectProcessor"; //$NON-NLS-1$
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.CopyProjectProcessor_name;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		if (fProject == null)
			return false;
		if (!fProject.exists())
			return false;
		if (!fProject.isAccessible())
			return false;
		return true;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
		final String[] affectedNatures= ResourceProcessors.computeAffectedNatures(fProject);
		final CopyArguments copyArguments= new CopyArguments(fNewLocation.append(fNewName), new ReorgExecutionLog());

		return ParticipantManager.loadCopyParticipants(status, this, fProject, copyArguments, affectedNatures, sharedParticipants);
	}
}