/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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
 *     Mickael Istria (Red Hat Inc.) - [266030] Allow "others" working set
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.workingsets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

/**
 * Provides children and parents for IWorkingSets.
 *
 * @since 3.2.1
 *
 */
public class WorkingSetsContentProvider implements ICommonContentProvider {

	/**
	 * The extension id for the WorkingSet extension.
	 */
	public static final String EXTENSION_ID = "org.eclipse.ui.navigator.resources.workingSets"; //$NON-NLS-1$

	/**
	 * A key used by the Extension State Model to keep track of whether top level Working Sets or
	 * Projects should be shown in the viewer.
	 */
	public static final String SHOW_TOP_LEVEL_WORKING_SETS = EXTENSION_ID + ".showTopLevelWorkingSets"; //$NON-NLS-1$


	private static final Object[] NO_CHILDREN = new Object[0];

	/**
	 * An object representing the "Others" working set, showing unassigned
	 * content
	 */
	public static final Object OTHERS_WORKING_SET = new Object();

	private WorkingSetHelper helper;
	private IAggregateWorkingSet workingSetRoot;
	private IExtensionStateModel extensionStateModel;
	private IWorkingSetManager workingSetManager;
	private CommonNavigator projectExplorer;
	private CommonViewer viewer;

	private IPropertyChangeListener rootModeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if(SHOW_TOP_LEVEL_WORKING_SETS.equals(event.getProperty())) {
				updateRootMode();
			}
		}
	};

	private IPropertyChangeListener workingSetManagerListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (helper != null) {
				helper.refreshWorkingSetTreeState();
			}
		}
	};

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		NavigatorContentService cs = (NavigatorContentService) aConfig.getService();
		viewer = (CommonViewer) cs.getViewer();
		projectExplorer = viewer.getCommonNavigator();

		extensionStateModel = aConfig.getExtensionStateModel();
		extensionStateModel.addPropertyChangeListener(rootModeListener);
		updateRootMode();

		workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		workingSetManager.addPropertyChangeListener(workingSetManagerListener);

	}

	@Override
	public void restoreState(IMemento aMemento) {

	}

	@Override
	public void saveState(IMemento aMemento) {

	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IWorkingSet) {
			IWorkingSet workingSet = (IWorkingSet) parentElement;
			if (workingSet.isAggregateWorkingSet() && projectExplorer != null) {
				switch (projectExplorer.getRootMode()) {
					case ProjectExplorer.WORKING_SETS :
						IWorkingSet[] activeWorkingSets = ((IAggregateWorkingSet) workingSet).getComponents();
						if (helper.getUnassignedProjects().isEmpty()) {
							return activeWorkingSets;
						}
						Object[] res;
						res = new Object[activeWorkingSets.length + 1];
						System.arraycopy(activeWorkingSets, 0, res, 0, activeWorkingSets.length);
						res[activeWorkingSets.length] = OTHERS_WORKING_SET;
						return res;
					case ProjectExplorer.PROJECTS:
						return getWorkingSetElements(workingSet);
				}
			}
			return getWorkingSetElements(workingSet);
		} else if (parentElement == OTHERS_WORKING_SET) {
			Set<IProject> res = helper.getUnassignedProjects();
			return res.toArray(new Object[res.size()]);
		}
		return NO_CHILDREN;
	}

	private IAdaptable[] getWorkingSetElements(IWorkingSet workingSet) {
		IAdaptable[] children = workingSet.getElements();
		for (int i = 0; i < children.length; i++) {
			IResource resource = Adapters.adapt(children[i], IResource.class);
			if (resource instanceof IProject)
				children[i] = resource;
		}
		return children;
	}

	@Override
	public Object getParent(Object element) {
		if (helper != null && projectExplorer.getRootMode() == ProjectExplorer.WORKING_SETS)
			return helper.getParent(element);
		return null;
	}

	@Override
	public boolean hasChildren(Object parentElement) {
		// since getChildren is a low-cost operation, we can use it
		// to compute hasChildren. That should prevent the expand arrow
		// to be shown when there's no content.
		return getChildren(parentElement).length > 0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		helper = null;
		extensionStateModel.removePropertyChangeListener(rootModeListener);
		workingSetManager.removePropertyChangeListener(workingSetManagerListener);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IWorkingSet) {
			IWorkingSet rootSet = (IWorkingSet) newInput;
			helper = new WorkingSetHelper(rootSet);
		}
	}

	private void updateRootMode() {
		if (projectExplorer == null) {
			return;
		}
		if( extensionStateModel.getBooleanProperty(SHOW_TOP_LEVEL_WORKING_SETS) )
			projectExplorer.setRootMode(ProjectExplorer.WORKING_SETS);
		else
			projectExplorer.setRootMode(ProjectExplorer.PROJECTS);
	}

	protected class WorkingSetHelper {

		private final IWorkingSet workingSet;
		private Map<IAdaptable, IAdaptable> parents;
		private Set<IProject> unassignedProjects;

		/**
		 * Create a Helper class for the given working set
		 *
		 * @param set
		 *            The set to use to build the item to parent map.
		 */
		public WorkingSetHelper(IWorkingSet set) {
			workingSet = set;
			refreshWorkingSetTreeState();
		}

		void refreshWorkingSetTreeState() {
			parents = new WeakHashMap<IAdaptable, IAdaptable>();
			if (workingSet.isAggregateWorkingSet()) {
				IAggregateWorkingSet aggregateSet = (IAggregateWorkingSet) workingSet;
				if (workingSetRoot == null)
					workingSetRoot = aggregateSet;

				IWorkingSet[] components = aggregateSet.getComponents();

				for (IWorkingSet component : components) {
					IAdaptable[] elements = getWorkingSetElements(component);
					for (IAdaptable element : elements) {
						parents.put(element, component);
					}
					parents.put(component, aggregateSet);

				}
			} else {
				IAdaptable[] elements = getWorkingSetElements(workingSet);
				for (IAdaptable element : elements) {
					parents.put(element, workingSet);
				}
			}

			unassignedProjects = new HashSet<>(Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()));
			for (IAdaptable node : parents.keySet()) {
				unassignedProjects.remove(node.getAdapter(IProject.class));
			}
		}

		/**
		 * @return projects that aren't part of a selected working set
		 */
		public Set<IProject> getUnassignedProjects() {
			return unassignedProjects;
		}

		/**
		 *
		 * @param element
		 *            An element from the viewer
		 * @return The parent associated with the element, if any.
		 */
		public Object getParent(Object element) {
			if (element instanceof IWorkingSet && element != workingSetRoot)
				return workingSetRoot;
			Object res = parents.get(element);
			if (res != null) {
				return res;
			}
			if (unassignedProjects.contains(element)) {
				return OTHERS_WORKING_SET;
			}
			return null;
		}
	}



}
