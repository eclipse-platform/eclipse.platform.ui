package org.eclipse.core.tests.resources.usecase;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SignaledBuilder extends IncrementalProjectBuilder {
	protected boolean wasExecuted;

	/** associate instances and projects */
	private static Vector instances = new Vector(5);
	
	/** contants */
	public static final String BUILDER_ID = "org.eclipse.core.tests.resources.sigbuilder";
public SignaledBuilder() {
	super();
	reset();
	instances.add(this);
}
protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
	try {
		monitor.beginTask("Building.", 1);
		wasExecuted = true;
		getProject().touch(null);
		return null;
	} finally {
		monitor.done();
	}
}
public static SignaledBuilder getInstance(IProject project) {
	for (Enumeration enum = instances.elements(); enum.hasMoreElements();) {
		SignaledBuilder builder = (SignaledBuilder) enum.nextElement();
		if (builder.getProject().equals(project))
			return builder;
	}
	return null;
}
public void reset() {
	wasExecuted = false;
}
public boolean wasExecuted() {
	return wasExecuted;
}
}
