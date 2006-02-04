/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.core.mapping.IResourceMappingScopeManager;
import org.eclipse.team.core.mapping.provider.IResourceMappingScopeParticipant;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.*;

public class ResourceModelScopeParticipant implements
		IResourceMappingScopeParticipant, IResourceChangeListener, IPropertyChangeListener {

	private final IResourceMappingScopeManager manager;
	private final ModelProvider provider;

	public ResourceModelScopeParticipant(ModelProvider provider, IResourceMappingScopeManager manager) {
		this.provider = provider;
		this.manager = manager;
		if (hasWorkspaceMapping()) {
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		}
		if (hasWorkingSetMappings()) {
			PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(this);
		}
	}

	private boolean hasWorkingSetMappings() {
		ResourceMapping[] mappings = manager.getScope().getMappings(provider.getDescriptor().getId());
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			Object modelObject = mapping.getModelObject();
			if (modelObject instanceof IWorkingSet) {
				return true;
			}
		}
		return false;
	}

	private boolean hasWorkspaceMapping() {
		ResourceMapping[] mappings = manager.getScope().getMappings(provider.getDescriptor().getId());
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			Object modelObject = mapping.getModelObject();
			if (modelObject instanceof IResource) {
				IResource resource = (IResource) modelObject;
				if (resource.getType() == IResource.ROOT) {
					return true;
				}
			}
		}
		return false;
	}

	public ResourceMapping[] handleContextChange(
			IResourceMappingScopeManager manager, IResource[] resources,
			IProject[] projects) {
		Set result = new HashSet();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			collectMappings(project, result);
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
	}

	private void collectMappings(IProject project, Set result) {
		ResourceMapping[] mappings = manager.getScope().getMappings(provider.getDescriptor().getId());
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			Object modelObject = mapping.getModelObject();
			if (modelObject instanceof IWorkingSet) {
				IWorkingSet set = (IWorkingSet)modelObject;
				IAdaptable[] elements = set.getElements();
				for (int j = 0; j < elements.length; j++) {
					IAdaptable adaptable = elements[j];
					ResourceMapping m = (ResourceMapping)Utils.getAdapter(adaptable, ResourceMapping.class);
					IProject[] p = m.getProjects();
					for (int k = 0; k < p.length; k++) {
						IProject mp = p[k];
						if (mp.equals(project)) {
							result.add(m);
						}
					}
				}
			} else if (modelObject instanceof IResource) {
				IResource resource = (IResource) modelObject;
				if (resource.getType() == IResource.ROOT) {
					result.add(Utils.getResourceMapping(resource));
				}
			}
		}
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		// Only interested in project additions and removals
		Set result = new HashSet();
		IResourceDelta[] children = event.getDelta().getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
			IResourceDelta delta = children[i];
			IResource resource = delta.getResource();
			if (resource.getType() == IResource.PROJECT 
					&& ((delta.getKind() & (IResourceDelta.ADDED | IResourceDelta.REMOVED)) != 0
						|| (delta.getFlags() & IResourceDelta.OPEN) != 0)) {
				if (isInContext(resource))
					collectMappings((IProject)resource, result);
			}
		}
		if (!result.isEmpty())
			fireChange((ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]));
			
			
	}

	private boolean isInContext(IResource resource) {
		IProject[] projects = manager.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (project.equals(resource.getProject())) {
				return true;
			}
		}
		return false;
	}

	private void fireChange(ResourceMapping[] mappings) {
		manager.refresh(mappings);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty() == IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE) {
			IWorkingSet newSet = (IWorkingSet) event.getNewValue();
			ResourceMapping[] mappings = manager.getScope().getMappings(provider.getDescriptor().getId());
			for (int i = 0; i < mappings.length; i++) {
				ResourceMapping mapping = mappings[i];
				if (newSet == mapping.getModelObject()) {
					fireChange(new ResourceMapping[] { mapping });
				}
			}
		} else if(event.getProperty() == IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE) {
			// TODO: Need to update the participant description somehow
			//firePropertyChangedEvent(new PropertyChangeEvent(this, NAME, null, event.getNewValue()));
		}
	}

}
