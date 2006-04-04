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
package org.eclipse.core.tests.internal.builders;

import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Test builder that is associated with the snow nature.
 */
public class SnowBuilder extends TestBuilder {
	private static SnowBuilder singleton;
	private boolean wasDeltaNull = false;

	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.snowbuilder";
	public static final String SNOW_BUILD_EVENT = "SnowBuild";

	/**
	 * Captures the builder instantiated through reflection
	 */
	public SnowBuilder() {
		if (singleton != null) {
			//copy interesting data from old singleton
			//		expectedEvents.addAll(singleton.expectedEvents);
			//		actualEvents.addAll(singleton.actualEvents);
			//		wasDeltaNull = singleton.wasDeltaNull;
		}
		singleton = this;
	}

	/**
	 * Returns the singleton instance
	 */
	public static SnowBuilder getInstance() {
		if (singleton == null) {
			new SnowBuilder();
		}
		return singleton;
	}

	/**
	 * @see InternalBuilder#build(int, Map, IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		wasDeltaNull = getDelta(getProject()) == null;
		return super.build(kind, args, monitor);
	}

	public boolean wasDeltaNull() {
		return wasDeltaNull;
	}
}
