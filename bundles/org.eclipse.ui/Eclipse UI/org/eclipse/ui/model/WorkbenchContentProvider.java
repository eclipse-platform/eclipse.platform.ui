package org.eclipse.ui.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.*;

/**
 * Provides tree contents for objects that have the IWorkbenchAdapter
 * adapter registered.  Note that this class currently implements a
 * couple of old JFace content providers.  This is purely for (temporary) 
 * backwards compatibility with those viewers still living in the past.
 */
public class WorkbenchContentProvider implements ITreeContentProvider, IResourceChangeListener {
	protected Viewer viewer;
/* (non-Javadoc)
 * Method declared on IContentProvider.
 */
public void dispose() {
	if (viewer != null) {
		Object obj = viewer.getInput();
		if (obj instanceof IWorkspace) {
			IWorkspace workspace = (IWorkspace) obj;
			workspace.removeResourceChangeListener(this);
		} else
			if (obj instanceof IContainer) {
				IWorkspace workspace = ((IContainer) obj).getWorkspace();
				workspace.removeResourceChangeListener(this);
			}
	}
}
/**
 * Returns the implementation of IWorkbenchAdapter for the given
 * object.  Returns null if the adapter is not defined or the
 * object is not adaptable.
 */
protected IWorkbenchAdapter getAdapter(Object o) {
	if (!(o instanceof IAdaptable)) {
		return null;
	}
	return (IWorkbenchAdapter)((IAdaptable)o).getAdapter(IWorkbenchAdapter.class);
}
/* (non-Javadoc)
 * Method declared on ITreeContentProvider.
 */
public Object[] getChildren(Object element) {
	IWorkbenchAdapter adapter = getAdapter(element);
	if (adapter != null) {
	    return adapter.getChildren(element);
	}
	return new Object[0];
}
/* (non-Javadoc)
 * Method declared on IStructuredContentProvider.
 */
public Object[] getElements(Object element) {
	return getChildren(element);
}
/* (non-Javadoc)
 * Method declared on ITreeContentProvider.
 */
public Object getParent(Object element) {
	IWorkbenchAdapter adapter = getAdapter(element);
	if (adapter != null) {
	    return adapter.getParent(element);
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on ITreeContentProvider.
 */
public boolean hasChildren(Object element) {
	return getChildren(element).length > 0;
}
/* (non-Javadoc)
 * Method declared on IContentProvider.
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	this.viewer = viewer;
	IWorkspace oldWorkspace = null;
	IWorkspace newWorkspace = null;
	if (oldInput instanceof IWorkspace) {
		oldWorkspace = (IWorkspace) oldInput;
	}
	else if (oldInput instanceof IContainer) {
		oldWorkspace = ((IContainer) oldInput).getWorkspace();
	}
	if (newInput instanceof IWorkspace) {
		newWorkspace = (IWorkspace) newInput;
	} else if (newInput instanceof IContainer) {
		newWorkspace = ((IContainer) newInput).getWorkspace();
	}
	if (oldWorkspace != newWorkspace) {
		if (oldWorkspace != null) {
			oldWorkspace.removeResourceChangeListener(this);
		}
		if (newWorkspace != null) {
			newWorkspace.addResourceChangeListener(this);
		}
	}
}
/**
 * Process a resource delta.  
 */
protected void processDelta(IResourceDelta delta) {
	// This method runs inside a syncExec.  The widget may have been destroyed
	// by the time this is run.  Check for this and do nothing if so.
	Control ctrl = viewer.getControl();
	if (ctrl == null || ctrl.isDisposed())
		return;
		
	// Get the affected resource
	IResource resource = delta.getResource();
	
	// Check the flags for changes the Navigator cares about.
	// See ResourceLabelProvider for the aspects it cares about.
	// Notice we don't care about F_CONTENT or F_MARKERS currently.
	int changeFlags = delta.getFlags();
	if ((changeFlags & (IResourceDelta.OPEN | IResourceDelta.SYNC | IResourceDelta.TYPE)) != 0) {
		((StructuredViewer) viewer).update(resource, null);
	}
	if ((changeFlags & IResourceDelta.CONTENT) != 0 && resource instanceof IProject) {
		//we care about content changes on projects only, because project reference
		//changes show up this way.
		((StructuredViewer)viewer).update(resource, null);
	}

	// Handle changed children .
	IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED);
	for (int i = 0; i < affectedChildren.length; i++) 
		processDelta(affectedChildren[i]);

	// Handle added children. Issue one update for all insertions.
	affectedChildren = delta.getAffectedChildren(IResourceDelta.ADDED);
	if (affectedChildren.length > 0) {
		Object[] affected = new Object[affectedChildren.length];
		for (int i = 0; i < affectedChildren.length; i++)
			affected[i] = affectedChildren[i].getResource();
		if (viewer instanceof AbstractTreeViewer) {
			((AbstractTreeViewer) viewer).add(resource, affected);
		}
		else {
			((StructuredViewer) viewer).refresh(resource);
		}
	}
	
	// Handle removed children. Issue one update for all removals.
	affectedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED);
	if (affectedChildren.length > 0) {
		Object[] affected = new Object[affectedChildren.length];
		for (int i = 0; i < affectedChildren.length; i++)
			affected[i] = affectedChildren[i].getResource();
		if (viewer instanceof AbstractTreeViewer) {
			((AbstractTreeViewer) viewer).remove(affected);
		}
		else {
			((StructuredViewer) viewer).refresh(resource);
		}
	}
}
/**
 * The workbench has changed.  Process the delta and issue updates to the viewer,
 * inside the UI thread.
 *
 * @see IResourceChangeListener#resourceChanged
 */
public void resourceChanged(final IResourceChangeEvent event) {
	// we only care about changes that have already happened.
	if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
		return;
	}
	final IResourceDelta delta = event.getDelta();
	Control ctrl = viewer.getControl();
	if (ctrl != null && !ctrl.isDisposed()) {
		// Do a sync exec, not an async exec, since the resource delta
		// must be traversed in this method.  It is destroyed
		// when this method returns.
		ctrl.getDisplay().syncExec(new Runnable() {
			public void run() {
				processDelta(delta);
			}
		});
	}
}
}
