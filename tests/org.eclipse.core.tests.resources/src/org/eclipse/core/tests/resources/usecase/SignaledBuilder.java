/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.usecase;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SignaledBuilder extends IncrementalProjectBuilder {

	/** contants */
	public static final String BUILDER_ID = "org.eclipse.core.tests.resources.sigbuilder";

	/**
	 * associate IProject->SignaledBuilder
	 */
	private static HashMap instances = new HashMap(10);
	protected boolean wasExecuted;

	public static SignaledBuilder getInstance(IProject project) {
		return (SignaledBuilder) instances.get(project);
	}

	public SignaledBuilder() {
		reset();
	}

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		instances.put(getProject(), this);
		try {
			monitor.beginTask("Building.", 1);
			wasExecuted = true;
			getProject().touch(null);
			return null;
		} finally {
			monitor.done();
		}
	}

	public void reset() {
		wasExecuted = false;
	}

	public boolean wasExecuted() {
		return wasExecuted;
	}
}
