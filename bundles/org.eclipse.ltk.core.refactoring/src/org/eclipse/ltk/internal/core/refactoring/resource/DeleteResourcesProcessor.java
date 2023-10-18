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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteArguments;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.eclipse.ltk.core.refactoring.participants.DeleteProcessor;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.resource.Resources;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * A delete processor for {@link IResource resources}. The processor will delete the resources and
 * load delete participants if references should be deleted as well.
 *
 * @since 3.4
 */
public class DeleteResourcesProcessor extends DeleteProcessor {
	private IResource[] fResources;
	private boolean fDeleteContents;

	/**
	 * Create a new delete processor.  Same as DeleteResourcesProcessor(resources, true, false)
	 * @param resources the resources to delete.  They can be either {@link IProject} or {@link IFile} and {@link IFolder}.
	 */
	public DeleteResourcesProcessor(IResource[] resources) {
		this(resources, false);
	}

	/**
	 * Create a new delete processor.
	 * @param resources the resources to delete.  They can be either {@link IProject} or {@link IFile} and {@link IFolder}.
	 * @param deleteContents <code>true</code> if this will delete the project contents.  The content delete is not undoable.
	 */
	public DeleteResourcesProcessor(IResource[] resources, boolean deleteContents) {
		fResources= removeDescendants(resources);
		fDeleteContents= deleteContents;
	}

	/**
	 * Returns the resources to delete.
	 *
	 * @return the resources to delete.
	 */
	public IResource[] getResourcesToDelete() {
		return fResources;
	}

	/**
	 * Delete projects contents.
	 * @return <code>true</code> if this will delete the project contents.  The content delete is not undoable.
	 */
	public boolean isDeleteContents() {
		return fDeleteContents;
	}

	/**
	 * Set to delete the projects content.
	 * @param deleteContents <code>true</code> if this will delete the project contents.  The content delete is not undoable.
	 */
	public void setDeleteContents(boolean deleteContents) {
		fDeleteContents= deleteContents;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		// allow only projects or only non-projects to be selected;
		// note that the selection may contain multiple types of resource
		if (!Resources.containsOnlyProjects(fResources) && !Resources.containsOnlyNonProjects(fResources)) {
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.DeleteResourcesProcessor_delete_error_mixed_types);
		}

		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException, OperationCanceledException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			RefactoringStatus result= new RefactoringStatus();

			for (IResource resource : fResources) {
				if (!isSynchronizedExcludingLinkedResources(resource)) {
					String pathLabel= BasicElementLabels.getPathLabel(resource.getFullPath(), false);

					String locationLabel= null;
					IPath location= resource.getLocation();
					if (location != null) {
						locationLabel= BasicElementLabels.getPathLabel(location, true);
					} else {
						URI uri= resource.getLocationURI();
						if (uri != null) {
							locationLabel= BasicElementLabels.getURLPart(uri.toString());
						}
					}

					String warning;
					if (resource instanceof IFile) {
						if (locationLabel != null) {
							warning= Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_warning_out_of_sync_file_loc, new Object[] { pathLabel, locationLabel });
						} else {
							warning= Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_warning_out_of_sync_file, pathLabel);
						}
					} else {
						if (locationLabel != null) {
							warning= Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_warning_out_of_sync_container_loc, new Object[] { pathLabel, locationLabel });
						} else {
							warning= Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_warning_out_of_sync_container, pathLabel);
						}
					}
					result.addWarning(warning);
				}
			}

			checkDirtyResources(result);

			ResourceChangeChecker checker= context.getChecker(ResourceChangeChecker.class);
			IResourceChangeDescriptionFactory deltaFactory= checker.getDeltaFactory();
			for (IResource fResource : fResources) {
				if (fResource.isPhantom()) {
					result.addFatalError(Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_delete_error_phantom, BasicElementLabels.getPathLabel(fResource.getFullPath(), false)));
				} else if (fDeleteContents && Resources.isReadOnly(fResource)) {
					result.addFatalError(Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_delete_error_read_only, BasicElementLabels.getPathLabel(fResource.getFullPath(), false)));
				} else {
					if (fResource.getType() == IResource.PROJECT) {
						deltaFactory.delete((IProject) fResource, fDeleteContents);
					} else {
						deltaFactory.delete(fResource);
					}
				}
			}
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
		for (IResource resource : fResources) {
			if (resource instanceof IProject && !((IProject) resource).isOpen())
				continue;
			resource.accept((IResourceVisitor) visitedResource -> {
				if (visitedResource instanceof IFile) {
					checkDirtyFile(result, (IFile)visitedResource);
				}
				return true;
			}, IResource.DEPTH_INFINITE, false);
		}
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
		pm.beginTask(RefactoringCoreMessages.DeleteResourcesProcessor_create_task, fResources.length);
		try {
			RefactoringChangeDescriptor descriptor= new RefactoringChangeDescriptor(createDescriptor());
			CompositeChange change= new CompositeChange(RefactoringCoreMessages.DeleteResourcesProcessor_change_name);
			change.markAsSynthetic();
			for (IResource fResource : fResources) {
				pm.worked(1);
				DeleteResourceChange dc= new DeleteResourceChange(fResource.getFullPath(), true, fDeleteContents);
				dc.setDescriptor(descriptor);
				change.add(dc);
			}
			return change;
		} finally {
			pm.done();
		}
	}

	protected DeleteResourcesDescriptor createDescriptor() {
		DeleteResourcesDescriptor descriptor= new DeleteResourcesDescriptor();
		descriptor.setProject(null);
		descriptor.setDescription(getDeleteDescription());
		descriptor.setComment(descriptor.getDescription());
		descriptor.setFlags(RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);

		descriptor.setDeleteContents(fDeleteContents);
		descriptor.setResources(fResources);
		return descriptor;
	}

	private String getDeleteDescription() {
		if (fResources.length == 1) {
			return Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_description_single, BasicElementLabels.getPathLabel(fResources[0].getFullPath(), false));
		}
		return Messages.format(RefactoringCoreMessages.DeleteResourcesProcessor_description_multi, Integer.valueOf(fResources.length));
	}

	@Override
	public Object[] getElements() {
		return fResources;
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.ltk.core.refactoring.deleteResourcesProcessor"; //$NON-NLS-1$
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.DeleteResourcesProcessor_processor_name;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		for (IResource fResource : fResources) {
			if (!canDelete(fResource)) {
				return false;
			}
		}
		return true;
	}

	private boolean canDelete(IResource res) {
		return res.isAccessible() && !res.isPhantom();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
		final ArrayList<DeleteParticipant> result= new ArrayList<>();
		if (!isApplicable()) {
			return new RefactoringParticipant[0];
		}

		final String[] affectedNatures= ResourceProcessors.computeAffectedNatures(fResources);
		final DeleteArguments deleteArguments= new DeleteArguments(fDeleteContents);
		for (IResource fResource : fResources) {
			result.addAll(Arrays.asList(ParticipantManager.loadDeleteParticipants(status, this, fResource, deleteArguments, affectedNatures, sharedParticipants)));
		}

		return result.toArray(new RefactoringParticipant[result.size()]);
	}

	private static IResource[] removeDescendants(IResource[] resources) {
		ArrayList<IResource> result= new ArrayList<>();
		for (IResource resource : resources) {
			addToList(result, resource);
		}
		return result.toArray(new IResource[result.size()]);
	}

	private static void addToList(ArrayList<IResource> result, IResource curr) {
		IPath currPath= curr.getFullPath();
		for (int k= result.size() - 1; k >= 0 ; k--) {
			IResource other= result.get(k);
			IPath otherPath= other.getFullPath();
			if (otherPath.isPrefixOf(currPath)) {
				return; // current entry is a descendant of an entry in the list
			}
			if (currPath.isPrefixOf(otherPath)) {
				result.remove(k); // entry in the list is a descendant of the current entry
			}
		}
		result.add(curr);
	}

	public RefactoringStatus setResources(IResource[] resources, IProgressMonitor pm) throws OperationCanceledException, CoreException {
		this.fResources= resources;
		return checkInitialConditions(pm);
	}


}
