package org.eclipse.ui.internal.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.model.IWorkbenchAdapter;
import java.util.*;

public class WorkbenchStatusList extends WorkbenchAdapter implements IAdaptable {
	private ArrayList statii = new ArrayList(10);
public void add(IStatus status) {
	statii.add(new WorkbenchStatus(status));
}
public void clear() {
	statii.clear();
}
/**
 * Returns an object which is an instance of the given class
 * associated with this object. Returns <code>null</code> if
 * no such object can be found.
 */
public Object getAdapter(Class adapter) {
	if (adapter == IWorkbenchAdapter.class) return this;
	return null;
}
/**
 * Returns the children of this element.
 */
public Object[] getChildren(Object o) {
	return statii.toArray();
}
public void remove(WorkbenchStatus status) {
	statii.remove(status);
}
}
