package org.eclipse.core.internal.events;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.internal.watson.IDeltaFilter;

public class DeltaFilter implements IDeltaFilter {
	protected int mask;
public DeltaFilter(int mask) {
	super();
	this.mask = mask;
}
public boolean includeElement(int flags) {
	/**
	 * There are two conditions for accepting a flag: 1) The flags and the mask
	 * have at least one bit in common, 2) The flags is zero, and the
	 * mask specifies to include changed children.  The only reason
	 * the flags can be zero is if there are changed children, in which
	 * case we want to treat it as a change.
	 */
	if (flags == 0)
		flags = IResourceDelta.CHANGED;
	return (flags & mask) != 0;
}
}
