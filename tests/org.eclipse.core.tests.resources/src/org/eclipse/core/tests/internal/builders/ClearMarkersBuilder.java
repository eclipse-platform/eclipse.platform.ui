/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.util.Map;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This builder is for a regression test (Bug_147232).  It simply deletes all problem
 * markers on the project being built. If requested, it pauses after deleting the markers
 * to give time for an interim post change event to occur.
 * @see org.eclipse.core.tests.resources.regression.Bug_147232
 */
public class ClearMarkersBuilder extends TestBuilder {
	public static boolean pauseAfterBuild = false;
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.clearmarkersbuilder";

	/*
	 * @see InternalBuilder#build(int, Map, IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		//wait after build if requested
		try {
			if (pauseAfterBuild)
				Thread.sleep(5000);
		} catch (InterruptedException e) {
			//ignore
		}
		return null;
	}
}
