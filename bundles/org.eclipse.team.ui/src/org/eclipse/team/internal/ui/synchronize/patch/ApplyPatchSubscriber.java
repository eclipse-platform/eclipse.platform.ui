/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import java.util.*;

import org.eclipse.compare.internal.core.patch.DiffProject;
import org.eclipse.compare.internal.core.patch.FilePatch2;
import org.eclipse.compare.internal.patch.PatchProjectDiffNode;
import org.eclipse.compare.internal.patch.WorkspacePatcher;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.internal.core.mapping.LocalResourceVariant;

public class ApplyPatchSubscriber extends Subscriber {
	
	private class ApplyPatchSyncInfo extends SyncInfo {
		private ApplyPatchSyncInfo(IResource local, IResourceVariant base,
				IResourceVariant remote, IResourceVariantComparator comparator) {
			super(local, base, remote, comparator);
		}

		protected int calculateKind() throws TeamException {
			// TODO: this works only for files, what about excluding individual hunks?
			if (!getPatcher().isEnabled(PatchModelProvider.getPatchObject(getLocal(), patcher)))
				return IN_SYNC;
			// mark diffs with problems as conflicts 
			if (getRemote() != null 
					&& getPatcher().getDiffResult(((PatchedFileVariant)getRemote()).getDiff()).containsProblems())
				return CONFLICTING;
			return super.calculateKind();
		}
	}

	private WorkspacePatcher patcher;
	private IResourceVariantComparator comparator;

	public ApplyPatchSubscriber(WorkspacePatcher patcher) {
		this.patcher = patcher;
		this.comparator = new PatchedFileVariantComparator();
		getPatcher().refresh();
	}

	public String getName() {
		return "Apply Patch Subscriber"; //$NON-NLS-1$
	}

	public IResourceVariantComparator getResourceComparator() {
		return comparator;
	}

	public SyncInfo getSyncInfo(IResource resource) throws TeamException {
		if (!isSupervised(resource)) return null;
		// XXX: doing this here is highly inefficient!
		// getPatcher().refresh();
		// a little bit better but still called gazzilon times
		refresh(new IResource[] { resource }, IResource.DEPTH_ZERO, null);
		try {
			FilePatch2 diff = (FilePatch2) PatchModelProvider.getPatchObject(resource, getPatcher());
			// use null as remote variant for deletions
			IResourceVariant variant = null;
			if (diff.getDiffType(patcher.isReversed()) != FilePatch2.DELETION)
				variant =  new PatchedFileVariant(getPatcher(), diff);
			IResourceVariant base = resource.exists() ?  new LocalResourceVariant(resource) : null;
			SyncInfo info = new ApplyPatchSyncInfo(resource, base, variant, getResourceComparator());
			info.init();
			return info;
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}

	public boolean isSupervised(IResource resource) throws TeamException {
		return resource.getType() == IResource.FILE
				&& PatchModelProvider.getPatchObject(resource, getPatcher()) != null;
	}

	public IResource[] members(IResource resource) throws TeamException {
		try {
			if(resource.getType() == IResource.FILE)
				// file has no IResource members
				return new IResource[0];
			IContainer container = (IContainer) resource;

			// workspace container members
			List existingChildren = new ArrayList(Arrays.asList(container.members()));

			// patch members, subscriber location
			FilePatch2[] diffs = getPatcher().getDiffs();
			for (int i = 0; i < diffs.length; i++) {
				IResource file = PatchModelProvider.getFile(diffs[i], getPatcher());
				if (!container.exists(file.getProjectRelativePath())) {
					existingChildren.add(file);
				}
			}
			return (IResource[]) existingChildren.toArray(new IResource[existingChildren.size()]);
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}

	public void refresh(IResource[] resources, int depth,
			IProgressMonitor monitor) throws TeamException {
		Set /* <FilePatch> */diffs = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			Object object = PatchModelProvider.getPatchObject(resources[i],
					getPatcher());
			if (object instanceof FilePatch2) {
				FilePatch2 filePatch = (FilePatch2) object;
				diffs.add(filePatch);
			}
		}
		getPatcher().refresh((FilePatch2[]) diffs.toArray(new FilePatch2[0]));
	}

	public IResource[] roots() {
		Set roots = new HashSet();
		if (getPatcher().isWorkspacePatch()) {
			IDiffElement[] children = PatchModelProvider.getPatchWorkspace(this).getChildren();
			for (int i = 0; i < children.length; i++) {
				// return array of projects from the patch
				DiffProject diffProject = ((PatchProjectDiffNode)children[i]).getDiffProject();
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(diffProject.getName());
				roots.add(project);
			}
		} else {
			roots.add(getPatcher().getTarget());
		}
		return (IResource[]) roots.toArray(new IResource[0]);
	}

	WorkspacePatcher getPatcher() {
		return patcher;
	}
}
