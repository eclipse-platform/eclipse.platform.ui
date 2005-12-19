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
package org.eclipse.team.internal.core.mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IModelProviderDescriptor;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * Validator that queries the model providers for
 * side effects by adapting ModelProvider to 
 * IResourceOperationValidator.
 * @since 3.2
 */
public class WorkspaceOperationValidator extends ResourceOperationValidator {

	private IResourceOperationValidator[] getValidators(IResourceDiffTree tree) {
		IResource[] resources = tree.getAffectedResources();
		ModelProvider[] providers = getInterestedProviders(resources);
		List result = new ArrayList();
		for (int i = 0; i < providers.length; i++) {
			ModelProvider provider = providers[i];
			IResourceOperationValidator validator = getValidator(provider);
			if (validator != null)
				result.add(validator);
		}
		return (IResourceOperationValidator[]) result.toArray(new IResourceOperationValidator[result.size()]);
	}
	
	/**
	 * TODO: This method should be API somewhere
	 */
	private ModelProvider[] getInterestedProviders(IResource[] resources) {
		IModelProviderDescriptor[] descriptors = ModelProvider.getModelProviderDescriptors();
		List result = new ArrayList();
		for (int i = 0; i < descriptors.length; i++) {
			IModelProviderDescriptor descriptor = descriptors[i];
			try {
				//TODO Need to fix the use of the nature id by adding a nature property tester
				IResource[] matchingResources = descriptor.getMatchingResources(resources, resources[0].getProject().getDescription().getNatureIds());
				if (matchingResources.length > 0) {
					result.add(descriptor.getModelProvider());
				}
			} catch (CoreException e) {
				TeamPlugin.log(e);
			}
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
	}
	
	private IResourceOperationValidator getValidator(ModelProvider provider) {
		Object o = provider.getAdapter(IResourceOperationValidator.class);
		if (o instanceof IResourceOperationValidator) {
			IResourceOperationValidator rov = (IResourceOperationValidator) o;
			return rov;
		}
		return null;
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
			return (IStatus)notOK.get(0);
		}
		return new MultiStatus(TeamPlugin.ID, 0, (IStatus[]) notOK.toArray(new IStatus[notOK.size()]), "Multiple potential side effects have been identified.", null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceOperationValidator#validateChange(org.eclipse.team.core.mapping.IResourceDiffTree)
	 */
	public IStatus validateChange(IResourceDiffTree tree) {
		IResourceOperationValidator[] validators = getValidators(tree);
		IStatus[] result = new IStatus[validators.length];
		for (int i = 0; i < validators.length; i++) {
			IResourceOperationValidator validator = validators[i];
			result[i] = validator.validateChange(tree);
		}
		return combineResults(result);
	}

}
