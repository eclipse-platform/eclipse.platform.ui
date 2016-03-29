/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     Fair Isaac Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.m12;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider2;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.eclipse.ui.tests.navigator.m12.model.ResourceWrapper;

public abstract class ResourceWrapperContentProvider implements
		IPipelinedTreeContentProvider2 {
	protected static final Object[] NO_CHILDREN = new Object[0];

	protected static final String INTERCEPT_ADD = "ADD";
	protected static final String INTERCEPT_REMOVE = "REMOVE";
	protected static final String INTERCEPT_REFRESH = "REFRESH";
	protected static final String INTERCEPT_UPDATE = "UPDATE";

	private static Map _counters;

	public ResourceWrapperContentProvider() {
		super();
		_counters = new HashMap();
	}

	@Override
	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		// Nothing to do, we replaced IProjects with ModelProjects in
		// getPipelinedElements
		// and from there children are provided by getChildren.
	}

	@Override
	public void getPipelinedElements(Object input, Set currentElements) {

	}

	@Override
	public Object getPipelinedParent(Object object, Object suggestedParent) {
		return (object instanceof ResourceWrapper) ? ((ResourceWrapper) object)
				.getParent() : suggestedParent;
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		// ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
		// IResourceChangeEvent.PRE_BUILD |
		// IResourceChangeEvent.POST_BUILD |
		// IResourceChangeEvent.POST_CHANGE |
		// IResourceChangeEvent.PRE_DELETE |
		// IResourceChangeEvent.PRE_CLOSE);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return NO_CHILDREN;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ResourceWrapper) {
			return ((ResourceWrapper) element).getParent();
		} else {
			return null;
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		try {
			return ((ResourceWrapper) element).hasChildren();
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return NO_CHILDREN;
	}

	@Override
	public void dispose() {
		// not implemented
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// _viewer = viewer;
	}

	@Override
	public void restoreState(IMemento aMemento) {
		// not implemented
	}

	@Override
	public void saveState(IMemento aMemento) {
		// not implemented
	}

	@Override
	public PipelinedShapeModification interceptAdd(
			PipelinedShapeModification modification) {
		_convertToModelObjects(modification);
		_incrementCounter(INTERCEPT_ADD);
		return modification;
	}

	@Override
	public PipelinedShapeModification interceptRemove(
			PipelinedShapeModification modification) {
		_incrementCounter(INTERCEPT_REMOVE);
		_convertToModelObjects(modification.getChildren());
		return modification;
	}

	@Override
	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization) {
		_incrementCounter(INTERCEPT_REFRESH);
		return _convertToModelObjects(refreshSynchronization
				.getRefreshTargets());
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate update) {
		_incrementCounter(INTERCEPT_UPDATE);
		return _convertToModelObjects(update.getRefreshTargets());
	}

	private void _incrementCounter(String counterId) {
		Map counters = (Map) _counters.get(getClass().getName());
		if (counters == null) {
			counters = new HashMap();
			_counters.put(getClass().getName(), counters);
		}
		Integer current = (Integer) counters.get(counterId);
		if (current == null) {
			counters.put(counterId, Integer.valueOf(1));
		} else {
			counters.put(counterId, Integer.valueOf(current.intValue() + 1));
		}
	}

	protected static void resetCounters(String className) {
		_counters.remove(className);
	}

	protected static int getCounter(String className, String counterId) {
		Map counters = (Map) _counters.get(className);
		if (counters == null) {
			return 0;
		}
		Integer value = (Integer) counters.get(counterId);
		return (value == null) ? 0 : value.intValue();
	}

	protected boolean _convertToModelObjects(
			PipelinedShapeModification modification) {
		Object parent = modification.getParent();
		Object node = _convertToModelObject(parent);
		if (node != null && node != parent) {
			modification.setParent(node);
		}

		return _convertToModelObjects(modification.getChildren());
	}

	protected boolean _convertToModelObjects(Set children) {
		Set convertedChildren = new LinkedHashSet();
		for (Iterator childrenItr = children.iterator(); childrenItr.hasNext();) {
			Object child = childrenItr.next();
			Object node = _convertToModelObject(child);
			if (node != null && node != child) {
				childrenItr.remove();
				convertedChildren.add(node);
			}
		}
		children.addAll(convertedChildren);
		return (!convertedChildren.isEmpty());
	}

	protected abstract Object _convertToModelObject(Object object);
}