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

import org.eclipse.compare.internal.core.patch.*;
import org.eclipse.compare.internal.patch.WorkspacePatcher;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberMergeContext;

class ApplyPatchSubscriberMergeContext extends SubscriberMergeContext {

	protected ApplyPatchSubscriberMergeContext(Subscriber subscriber,
			ISynchronizationScopeManager manager) {
		super(subscriber, manager);
	}

	public static ApplyPatchSubscriberMergeContext createContext(
			Subscriber subscriber, ISynchronizationScopeManager manager) {
		ApplyPatchSubscriberMergeContext mergeContext = new ApplyPatchSubscriberMergeContext(
				subscriber, manager);
		// Initialize using the ApplyPatchSubscriber to populate the diff tree.
		mergeContext.initialize();
		return mergeContext;
	}

	protected void makeInSync(IDiff diff, IProgressMonitor monitor)
			throws CoreException {
		markAsMerged(diff, true, monitor);
	}

	public void markAsMerged(IDiff node, boolean inSyncHint,
			IProgressMonitor monitor) throws CoreException {
		IResource resource = getDiffTree().getResource(node);
		WorkspacePatcher patcher = ((ApplyPatchSubscriber) getSubscriber())
				.getPatcher();
		Object object = PatchModelProvider.getPatchObject(resource, patcher);
		if (object instanceof FilePatch2) {
			FilePatch2 filePatch = (FilePatch2) object;
			FileDiffResult fileDiffResult = patcher.getDiffResult(filePatch);
			HunkResult[] hunkResults = fileDiffResult.getHunkResults();
			for (int i = 0; i < hunkResults.length; i++) {
				// disable hunks that were merged
				if (hunkResults[i].isOK())
					patcher.setEnabled(hunkResults[i].getHunk(), false);
			}
		} else {
			patcher.setEnabled(object, false);
		}
	}

	public void reject(IDiff diff, IProgressMonitor monitor)
			throws CoreException {
		// do nothing
	}

	public ISchedulingRule getMergeRule(IDiff diff) {
		IResource resource = getDiffTree().getResource(diff);
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace()
				.getRuleFactory();
		if (!resource.exists()) {
			// for additions return rule for all parents that need to be created
			IContainer parent = resource.getParent();
			while (!parent.exists()) {
				resource = parent;
				parent = parent.getParent();
			}
			return ruleFactory.createRule(resource);
		} else {
			return super.getMergeRule(diff);
		}
	}
}
