/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.usecase;


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
