/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.IMergeStatus;
import org.eclipse.team.core.mapping.IResourceMappingMerger;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.mapping.DefaultResourceMappingMerger;
import org.eclipse.team.ui.TeamOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An operation for performing model provider based operations.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class ModelProviderOperation extends TeamOperation {

	/**
	 * Create the operation
	 * @param part the workbench part from which the operation was
	 * launched or <code>null</code>
	 */
	public ModelProviderOperation(IWorkbenchPart part) {
		super(part);
	}

	/*
	 * Helper method for extracting the part safely from a configuration
	 */
	private static IWorkbenchPart getPart(ISynchronizePageConfiguration configuration) {
		if (configuration != null) {
			ISynchronizePageSite site = configuration.getSite();
			if (site != null) {
				return site.getPart();
			}
		}
		return null;
	}
	
	/**
	 * Create a model provider operation
	 * @param configuration the configuration of the page from which
	 * the operation was launched
	 */
	protected ModelProviderOperation(ISynchronizePageConfiguration configuration) {
		this(getPart(configuration));
	}
	
	/**
	 * A convenience method that performs a headless merge of the
	 * elements in the given context. The merge is performed by obtaining
	 * the {@link IResourceMappingMerger} for the model providers in the context's
	 * scope.
	 * @param context the merge context
	 * @param monitor a progress monitor
	 * @return <code>true</code> if all elements where merged
	 * TODO should return more useful information
	 * @throws CoreException
	 */
	protected boolean performMerge(final IMergeContext context, IProgressMonitor monitor) throws CoreException {
		final ModelProvider[] providers = context.getScope().getModelProviders();
		final List failedMerges = new ArrayList();
		context.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {	
				monitor.beginTask(null, IProgressMonitor.UNKNOWN);
				for (int i = 0; i < providers.length; i++) {
					ModelProvider provider = providers[i];
					if (!performMerge(provider, context, Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN))) {
						failedMerges.add(provider);
					}
				}
				monitor.done();
			}
		}, null /* scheduling rule */, IResource.NONE, monitor);
		return failedMerges.isEmpty();
	}

	/**
	 * Merge all the mappings that come from the given provider. By default,
	 * an automatic merge is attempted. After this, a manual merge (i.e. with user
	 * intervention) is attempted on any mappings that could not be merged
	 * automatically.
	 * @param provider the model provider IDocumentProviderExtension5 
	 * @param mappings the mappings to be merged
	 * @param monitor a progress monitor
	 * @throws CoreException
	 */
	protected boolean performMerge(ModelProvider provider, IMergeContext mergeContext, IProgressMonitor monitor) throws CoreException {
		IResourceMappingMerger merger = getMerger(provider);
		IStatus status = merger.merge(mergeContext, monitor);
		if (!status.isOK()) {
			if (status.getCode() == IMergeStatus.CONFLICTS) {
				return false;
			} else {
				throw new TeamException(status);
			}
		}
		return true;
	}
	
	/**
	 * Validate the merge by obtaining the {@link IResourceMappingMerger} for the
	 * given provider.
	 * @param provider the model provider
	 * @param context the merge context
	 * @param monitor a progress monitor
	 * @return the status obtained from the merger for the provider
	 */
	protected IStatus validateMerge(ModelProvider provider, IMergeContext context, IProgressMonitor monitor) {
		IResourceMappingMerger merger = getMerger(provider);
		return merger.validateMerge(context, monitor);
	}
	
	/**
	 * Return the auto-merger associated with the given model provider
	 * view the adaptable mechanism.
	 * If the model provider does not have a merger associated with
	 * it, a default merger that performs the merge at the file level
	 * is returned.
	 * @param provider the model provider of the elements to be merged
	 * (must not be <code>null</code>)
	 * @return a merger
	 */
	protected IResourceMappingMerger getMerger(ModelProvider provider) {
		Assert.isNotNull(provider);
		Object o = provider.getAdapter(IResourceMappingMerger.class);
		if (o instanceof IResourceMappingMerger) {
			return (IResourceMappingMerger) o;	
		}
		return new DefaultResourceMappingMerger(provider);
	}

	protected boolean hasIncomingChanges(IDiffTree syncDeltaTree) {
		final CoreException found = new CoreException(Status.OK_STATUS);
		try {
			syncDeltaTree.accept(ResourcesPlugin.getWorkspace().getRoot().getFullPath(), new IDiffVisitor() {
				public boolean visit(IDiffNode delta) throws CoreException {
					if (delta instanceof IThreeWayDiff) {
						IThreeWayDiff twd = (IThreeWayDiff) delta;
						int direction = twd.getDirection();
						if (direction == IThreeWayDiff.INCOMING || direction == IThreeWayDiff.CONFLICTING) {
							throw found;
						}
					} else {
						throw found;
					}
					return false;
				}
			
			}, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			if (e == found)
				return true;
			TeamUIPlugin.log(e);
		}
		return false;
	}

}
