/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Francis Upton IV (Oakland Software) - adapted for CNF tests
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.cdt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

/**
 * A content provider populating a Common Navigator view with CDT model content.
 */
public class CNavigatorContentProvider implements
		IPipelinedTreeContentProvider, ICommonContentProvider {

	/**
	 * Flag set in {@link #restoreState(IMemento) restoreState}, indicating
	 * whether link-with-editor should be enabled delayed as a (old) workaround
	 * for <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186344">bug
	 * 186344</a>
	 */
	private boolean fLinkingEnabledDelayed;

	protected Map _resourceToModel = new HashMap();

	protected CRoot _root;
	protected Object _realInput;

	@Override
	public void init(ICommonContentExtensionSite commonContentExtensionSite) {
		IMemento memento = commonContentExtensionSite.getMemento();
		restoreState(memento);

	}

	@Override
	public void restoreState(IMemento memento) {
	}

	@Override
	public void saveState(IMemento memento) {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		_realInput = newInput;
		if (newInput instanceof IWorkspaceRoot) {
			_root = new CRoot(this, (IResource) newInput);
		}

		workaroundForBug186344();
	}

	/**
	 * Old workaround for <a
	 * href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186344">bug
	 * 186344</a>. Kept for backword compatibility.
	 */
	private void workaroundForBug186344() {
		if (fLinkingEnabledDelayed) {
			// enable linking delayed
			fLinkingEnabledDelayed = false;
			final IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window != null) {
				final IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IViewPart viewPart = page.findView(ProjectExplorer.VIEW_ID);
					if (viewPart instanceof CommonNavigator) {
						final CommonNavigator cn = ((CommonNavigator) viewPart);
						viewPart.getSite().getShell().getDisplay().asyncExec(
								new Runnable() {
									@Override
									public void run() {
										cn.setLinkingEnabled(true);
									}
								});
					}
				}
			}
		}
	}

	@Override
	public Object getParent(Object element) {
		Object parent;
		if (element instanceof CElement)
			parent = ((CElement) element).getParent();
		else
			parent = ((IResource) element).getParent();
		if (parent instanceof CRoot) {
			return ResourcesPlugin.getWorkspace().getRoot();
		} else if (parent instanceof CProject)
			return ((CProject) parent).getResource();
		return parent;
	}

	@Override
	public Object[] getElements(Object parent) {
		if (parent instanceof IWorkspaceRoot) {
			IProject[] projects = ((IWorkspaceRoot) parent).getProjects();
			for (int i = 0; i < projects.length; i++) {
				new CProject(this, projects[i], _root);
			}
			return projects;
		}
		CElement cElement = (CElement) _resourceToModel.get(parent);
		if (cElement == null)
			return new Object[]{};
		return cElement.getChildren().toArray();
	}

	@Override
	public Object[] getChildren(Object element) {
		return getElements(element);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IProject) {
			IProject project = (IProject) element;
			return project.isAccessible();
		}
		return getChildren(element).length > 0;
	}

	@Override
	public void getPipelinedChildren(Object parent, Set currentChildren) {
		customizeCElements(getChildren(parent), currentChildren);
	}

	@Override
	public void getPipelinedElements(Object input, Set currentElements) {
		// only replace plain resource elements with custom elements
		// and avoid duplicating elements already customized
		// by upstream content providers
		customizeCElements(getElements(input), currentElements);
	}

	private void customizeCElements(Object[] cChildren, Set proposedChildren) {
		List elementList = Arrays.asList(cChildren);
		Iterator pcIt = proposedChildren.iterator();
		while (pcIt.hasNext()) {
			Object element = pcIt.next();
			IResource resource = null;
			if (element instanceof IResource) {
				resource = (IResource) element;
			} else if (element instanceof IAdaptable) {
				resource = (IResource) ((IAdaptable) element)
						.getAdapter(IResource.class);
			}
			if (resource != null) {
				int i = elementList.indexOf(resource);
				if (i >= 0) {
					cChildren[i] = null;
				}
			}
		}
		for (int i = 0; i < cChildren.length; i++) {
			if (cChildren[i] instanceof CElement) {
				IResource resource = ((CElement) cChildren[i]).getResource();
				if (resource != null) {
					proposedChildren.remove(resource);
				}
				proposedChildren.add(cChildren[i]);
			} else if (cChildren[i] != null) {
				proposedChildren.add(cChildren[i]);
			}
		}
	}

	@Override
	public Object getPipelinedParent(Object object, Object suggestedParent) {
		return getParent(object);
	}

	@Override
	public PipelinedShapeModification interceptAdd(
			PipelinedShapeModification addModification) {
		Object parent = addModification.getParent();
		if (parent instanceof CProject) {
			if (_realInput instanceof IWorkspaceRoot) {
				addModification.setParent(((CProject) parent).getResource());
			}
		} else if (parent instanceof IProject || parent instanceof IFolder) {
			// ignore adds to C projects (we are issuing a refresh)
			IProject project = ((IResource) parent).getProject();
			if (hasCNature(project)) {
				addModification.getChildren().clear();
				return addModification;
			}
		} else if (parent instanceof IWorkspaceRoot) {
			// ignore adds of C projects (we are issuing a refresh)
			for (Iterator iterator = addModification.getChildren().iterator(); iterator
					.hasNext();) {
				Object child = iterator.next();
				if (child instanceof IProject) {
					if (hasCNature((IProject) child)) {
						iterator.remove();
					}
				}
			}
		}
		convertToCElements(addModification);
		return addModification;
	}

	@Override
	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization) {
		final Set refreshTargets = refreshSynchronization.getRefreshTargets();
		return convertToCElements(refreshTargets);
	}

	@Override
	public PipelinedShapeModification interceptRemove(
			PipelinedShapeModification removeModification) {
		final Set children = removeModification.getChildren();
		convertToCElements(children);
		return removeModification;
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate updateSynchronization) {
		final Set refreshTargets = updateSynchronization.getRefreshTargets();
		return convertToCElements(refreshTargets);
	}

	/**
	 * Converts the shape modification to use CElements.
	 *
	 * @param modification
	 *            the shape modification to convert
	 * @return <code>true</code> if the shape modification set was modified
	 */
	private boolean convertToCElements(PipelinedShapeModification modification) {
		Object parent = modification.getParent();
		// don't convert projects
		if (parent instanceof IContainer) {
			IContainer container = (IContainer) parent;
			IProject project = container.getProject();
			if (project != null && hasCNature(project)) {
				CElement element = new CContainer(this, container,
						(CElement) _resourceToModel.get(container.getParent()));
				if (element != null) {
					// don't convert the root
					if (!(element instanceof CElement)
							&& !(element instanceof CProject)) {
						modification.setParent(element);
					}
					final Set children = modification.getChildren();
					return convertToCElements(children);
				}
			}
		}
		return false;
	}

	/**
	 * Converts the given set to CElements.
	 *
	 * @param currentChildren
	 *            The set of current children that would be contributed or
	 *            refreshed in the viewer.
	 * @return <code>true</code> if the input set was modified
	 */
	private boolean convertToCElements(Set currentChildren) {
		LinkedHashSet convertedChildren = new LinkedHashSet();
		CElement newChild;
		for (Iterator iter = currentChildren.iterator(); iter.hasNext();) {
			Object child = iter.next();
			// do not convert IProject
			if (child instanceof IFile) {
				IResource resource = (IResource) child;
				if (resource.isAccessible()
						&& hasCNature(resource.getProject())) {
					if ((newChild = new CElement(this, resource,
							(CElement) _resourceToModel.get(resource
									.getParent()))) != null) {
						iter.remove();
						convertedChildren.add(newChild);
					}
				}
			}
			if (child instanceof IFolder) {
				IResource resource = (IResource) child;
				if (resource.isAccessible()
						&& hasCNature(resource.getProject())) {
					if ((newChild = new CContainer(this, resource,
							(CElement) _resourceToModel.get(resource
									.getParent()))) != null) {
						iter.remove();
						convertedChildren.add(newChild);
					}
				}
			}
		}
		if (!convertedChildren.isEmpty()) {
			currentChildren.addAll(convertedChildren);
			return true;
		}
		return false;
	}

	private boolean hasCNature(IProject project) {
		return project.getName().startsWith("C");
	}

	@Override
	public void dispose() {
	}

}
