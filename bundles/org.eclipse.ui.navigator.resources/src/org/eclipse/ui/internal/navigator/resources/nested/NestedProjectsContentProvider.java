/*******************************************************************************
 * Copyright (c) 2014, 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nested;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorFilterService;

public class NestedProjectsContentProvider implements ITreeContentProvider, IResourceChangeListener {

	public static final String EXTENSION_ID = "org.eclipse.ui.navigator.resources.nested.nestedProjectContentProvider"; //$NON-NLS-1$

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];

	private Command projectPresetionCommand;
	private CommonViewer viewer;

	public NestedProjectsContentProvider() {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		this.projectPresetionCommand = commandService.getCommand(ProjectPresentationHandler.COMMAND_ID);
		try {
			HandlerUtil.updateRadioState(this.projectPresetionCommand, Boolean.TRUE.toString());
		} catch (ExecutionException ex) {
			WorkbenchNavigatorPlugin.log(ex.getMessage(), new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, ex.getMessage(), ex));
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void dispose() {
		try {
			HandlerUtil.updateRadioState(this.projectPresetionCommand, Boolean.FALSE.toString());
		} catch (ExecutionException ex) {
			WorkbenchNavigatorPlugin.log(ex.getMessage(), new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, ex.getMessage(), ex));
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (CommonViewer)viewer;
		ensureFiltersActivated();
	}

	private void ensureFiltersActivated() {
		INavigatorFilterService filterService = this.viewer.getNavigatorContentService().getFilterService();
		Set<String> filters = new HashSet<>();
		for (ICommonFilterDescriptor desc : filterService.getVisibleFilterDescriptors()) {
			if (filterService.isActive(desc.getId())) {
				filters.add(desc.getId());
			}
		}
		if (!filters.contains(HideTopLevelProjectIfNested.EXTENSION_ID)
				|| !filters.contains(HideFolderWhenProjectIsShownAsNested.EXTENTSION_ID)) {
			filters.add(HideTopLevelProjectIfNested.EXTENSION_ID);
			filters.add(HideFolderWhenProjectIsShownAsNested.EXTENTSION_ID);
			filterService.activateFilterIdsAndUpdateViewer(filters.toArray(new String[filters.size()]));
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return EMPTY_OBJECT_ARRAY;
	}

	@Override
	public IProject[] getChildren(Object parentElement) {
		if (! (parentElement instanceof IContainer)) {
			return EMPTY_PROJECT_ARRAY;
		}
		IContainer container = (IContainer)parentElement;
		return NestedProjectManager.getInstance().getDirectChildrenProjects(container);
	}

	@Override
	public IContainer getParent(Object element) {
		if (element instanceof IProject) {
			IProject project = (IProject)element;
			if (NestedProjectManager.getInstance().isShownAsNested(project)) {
				return NestedProjectManager.getInstance().getMostDirectOpenContainer(project);
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContainer) {
			return NestedProjectManager.getInstance().hasDirectChildrenProjects((IContainer) element);
		}
		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta == null || event.getType() != IResourceChangeEvent.POST_CHANGE) {
			return;
		}
		final Set<IContainer> parentsToRefresh = new LinkedHashSet<>();
		if (delta.getKind() == IResourceDelta.CHANGED && delta.getResource() instanceof IWorkspaceRoot) {
			for (IResourceDelta childDelta : event.getDelta().getAffectedChildren()) {
				IResource childResource = childDelta.getResource();
				if (childResource instanceof IProject && (childDelta.getKind() == IResourceDelta.ADDED
						|| childDelta.getKind() == IResourceDelta.REMOVED)) {
					IProject affectedProject = (IProject) childResource;
					IContainer parent = getParent(affectedProject);
					if (parent != null) {
						parentsToRefresh.add(parent);
					} else {
						// workspace root
						parentsToRefresh.clear();
						parentsToRefresh.add(affectedProject.getParent());
						break;
					}
				}
			}
		}
		if (!parentsToRefresh.isEmpty()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				if (viewer.getTree() == null || viewer.getTree().isDisposed()) {
					return;
				}
				for (IContainer parent : parentsToRefresh) {
					viewer.refresh(parent);
				}
			});
		}
	}

}
