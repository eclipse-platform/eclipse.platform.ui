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
package org.eclipse.team.internal.ui;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.ui.mapping.IResourceMappingContentProvider;
import org.eclipse.team.ui.mapping.ITeamViewerContext;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class ResourceMappingContentProvider implements IResourceMappingContentProvider {

    /**
     * TODO Should root be a resource mapping?
     */
    final class RootObject implements IWorkbenchAdapter, IAdaptable {
        final ResourceMapping[] mappings;

        private RootObject(ResourceMapping[] mappings) {
            super();
            this.mappings = mappings;
        }

        public Object[] getChildren(Object o) {
            return mappings;
        }

        public ImageDescriptor getImageDescriptor(Object object) {
            return null;
        }

        public String getLabel(Object o) {
            return "Other Elements";
        }

        public Object getParent(Object o) {
            return null;
        }

        public Object getAdapter(Class adapter) {
            if (adapter == IWorkbenchAdapter.class)
                return this;
            return null;
        }
    }

    final RootObject root;
	private final ITeamViewerContext context;
	private final String modelId;

    public ResourceMappingContentProvider(ITeamViewerContext context, String modelId) {
        this.context = context;
		this.modelId = modelId;
        root = new RootObject(context.getResourceMappings(modelId));
    }

    public Object getRoot() {
        return root;
    }

    public Object[] getChildren(Object parentElement) {
        if (parentElement == root)
            return root.getChildren(parentElement);
        return new Object[0];
    }

    public Object getParent(Object element) {
        if (element == root)
            return null;
        ResourceMapping[] mappings = (ResourceMapping[])root.getChildren(root);
        for (int i = 0; i < mappings.length; i++) {
            ResourceMapping mapping = mappings[i];
            if (element == mapping)
                return root;
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        if (element == root)
            return true;
        return false;
    }

    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    public void dispose() {
        // Nothing to do
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do
    }
}