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
package org.eclipse.core.tests.resources.regression;

import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A builder that does not do anything.
 */
public class SimpleBuilder extends IncrementalProjectBuilder {

	protected int triggerForLastBuild;
	protected static SimpleBuilder instance;

	/** contants */
	public static final String BUILDER_ID = "org.eclipse.core.tests.resources.simplebuilder";

	public SimpleBuilder() {
		super();
		instance = this;
	}

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		triggerForLastBuild = kind;
		return null;
	}

	public static SimpleBuilder getInstance() {
		return instance;
	}

	/**
	 * 
	 */
	protected void startupOnInitialize() {
	}

	public boolean wasAutoBuild() {
		return triggerForLastBuild == IncrementalProjectBuilder.AUTO_BUILD;
	}

	public boolean wasFullBuild() {
		return triggerForLastBuild == IncrementalProjectBuilder.FULL_BUILD;
	}

	public boolean wasIncrementalBuild() {
		return triggerForLastBuild == IncrementalProjectBuilder.INCREMENTAL_BUILD;
	}
}
