/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.mapping;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.internal.resources.mapping.ChangeDescription;
import org.eclipse.core.internal.resources.mapping.ResourceChangeDescriptionFactory;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * The resource change validator is used to validate that changes made to
 * resources will not adversely affect the models stored in those resources.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Resources team.
 * </p>
 * @since 3.2
 */
public final class ResourceChangeValidator {

	private static ResourceChangeValidator instance;

	/**
	 * Return the singleton change validator.
	 * @return the singleton change validator
	 */
	public static ResourceChangeValidator getValidator() {
		if (instance == null)
			instance = new ResourceChangeValidator();
		return instance;
	}
	
	/**
	 * Singleton accessor method should be used instead.
	 * @see #getValidator()
	 */
	private ResourceChangeValidator() {
		super();
	}

	private IStatus combineResults(IStatus[] result) {
		List notOK = new ArrayList();
		for (int i = 0; i < result.length; i++) {
			IStatus status = result[i];
			if (!status.isOK()) {
				notOK.add(status);
			}
		}
		if (notOK.isEmpty()) {
			return Status.OK_STATUS;
		}
		if (notOK.size() == 1) {
			return (IStatus) notOK.get(0);
		}
		return new MultiStatus(ResourcesPlugin.PI_RESOURCES, 0, (IStatus[]) notOK.toArray(new IStatus[notOK.size()]), Messages.ResourceChangeValidator_0, null);
	}

	/**
	 * Return an empty change description factory that can be used to build a
	 * proposed resource delta.
	 * @return an empty change description factory that can be used to build a
	 * proposed resource delta
	 */
	public IResourceChangeDescriptionFactory createDeltaFactory() {
		return new ResourceChangeDescriptionFactory();
	}

	private ModelProvider[] getProviders(IResource[] resources) {
		IModelProviderDescriptor[] descriptors = ModelProvider.getModelProviderDescriptors();
		List result = new ArrayList();
		for (int i = 0; i < descriptors.length; i++) {
			IModelProviderDescriptor descriptor = descriptors[i];
			try {
				IResource[] matchingResources = descriptor.getMatchingResources(resources);
				if (matchingResources.length > 0) {
					result.add(descriptor.getModelProvider());
				}
			} catch (CoreException e) {
				ResourcesPlugin.getPlugin().getLog().log(new Status(e.getStatus().getSeverity(), ResourcesPlugin.PI_RESOURCES, 0, NLS.bind("Could not instantiate provider {0}", descriptor.getId()), e)); //$NON-NLS-1$
			}
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
	}

	/*
	 * Get the roots of any changes.
	 */
	private IResource[] getRootResources(IResourceDelta root) {
		final ChangeDescription changeDescription = new ChangeDescription();
		try {
			root.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					return changeDescription.recordChange(delta);
				}
			});
		} catch (CoreException e) {
			// Shouldn't happen since the ProposedResourceDelta accept doesn't thow an
			// exception and our visitor doesn't either
			ResourcesPlugin.getPlugin().getLog().log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 0, "Internal error", e)); //$NON-NLS-1$
		}
		return changeDescription.getRootResources();
	}

	/**
	 * Validate the proposed changes contained in the given delta
	 * by consulting all model providers to determine if the changes
	 * have any adverse side effects.
	 * <p>
	 * This method returns either a {@link ModelStatus}, or a {@link MultiStatus}
	 * whose children are {@link ModelStatus}.  In either case, the severity
	 * of the status indicates the severity of the possible side-effects of 
	 * the operation.  Any severity other than <code>OK</code> should be
	 * shown to the user. The message should be a human readable message that
	 * will allow the user to make a decision on whether to continue with the 
	 * operation. The model provider id should indicate which model is flagging the
	 * the possible side effects.
	 * </p>
	 * 
	 * @param delta a delta tree containing the proposed changes
	 * @return a status indicating any potential side effects
	 * on models stored in the affected resources.
	 */
	public IStatus validateChange(IResourceDelta delta) {
		IResource[] resources = getRootResources(delta);
		ModelProvider[] providers = getProviders(resources);
		if (providers.length == 0)
			return Status.OK_STATUS;
		IStatus[] result = new IStatus[providers.length];
		for (int i = 0; i < providers.length; i++) {
			ModelProvider provider = providers[i];
			result[i] = provider.validateChange(delta);
		}
		return combineResults(result);
	}
}
