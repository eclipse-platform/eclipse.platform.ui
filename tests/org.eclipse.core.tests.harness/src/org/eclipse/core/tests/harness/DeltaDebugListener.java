package org.eclipse.core.tests.harness;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.events.ResourceDelta;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;


public class DeltaDebugListener implements IResourceChangeListener {
/**
 * @see IResourceChangeListener#closing(IProject)
 */
public void closing(IProject project) {
}
/**
 * @see IResourceChangeListener#deleting(IProject)
 */

public void deleting(IProject project) {
}
/**
 * @see IResourceChangeListener#resourceChanged
 */
public void resourceChanged(IResourceChangeEvent event) {
	IResourceDelta delta = event.getDelta();
	if (delta == null)
		return;
	try {
		System.out.println();
		visitingProcess(delta);
	} catch (CoreException e) {
		// XXX: dropping exceptions
	}
}
protected boolean visit(IResourceDelta change) throws CoreException {
	System.out.println(((ResourceDelta) change).toDebugString());
	return true;
}
/**
 * Processes the given change by traversing its nodes and calling
 * <code>visit</code> for each.
 *
 * @see #visit
 * @exception CoreException if the operation fails
 */
protected void visitingProcess(IResourceDelta change) throws CoreException {
	if (!visit(change))
		return;
	int kind = IResourceDelta.ADDED | IResourceDelta.REMOVED | IResourceDelta.CHANGED;
	int memberFlags = IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_PHANTOMS;
	IResourceDelta[] children = change.getAffectedChildren(kind, memberFlags);
	for (int i = 0; i < children.length; i++)
		visitingProcess(children[i]);
}
}
