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
package org.eclipse.ui.externaltools.internal.model;


import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;

/**
 * This project builder implementation will run an external tool during the
 * build process. 
 * <p>
 * Note that there is only ever one instance of ExternalToolBuilder per project,
 * and the external tool to run is specified in the builder's arguments.
 * </p>
 */
public final class ExternalToolBuilder extends IncrementalProjectBuilder {
	public static final String ID = "org.eclipse.ui.externaltools.ExternalToolBuilder"; //$NON-NLS-1$;

	private static final String BUILD_TYPE_SEPARATOR = ","; //$NON-NLS-1$
	private static final int[] DEFAULT_BUILD_TYPES= new int[] {
									IncrementalProjectBuilder.INCREMENTAL_BUILD,
									IncrementalProjectBuilder.FULL_BUILD};

	/**
	 * Creates an uninitialized external tool builder.
	 */
	public ExternalToolBuilder() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on IncrementalProjectBuilder.
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration config= ExternalToolsUtil.configFromBuildCommandArgs(args);
		if (config == null) {
			return null;
		}
		boolean runTool = false;
		int[] buildKinds = buildTypesToArray((String)config.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, "")); //$NON-NLS-1$
		for (int i = 0; i < buildKinds.length; i++) {
			if (kind == buildKinds[i]) {
				runTool = true;
				break;
			}
		}
		if (!runTool)
			return null;
		VariableContextManager.getDefault().buildStarted(getProject(), kind);
		config.launch(ILaunchManager.RUN_MODE, null);
		VariableContextManager.getDefault().buildEnded();
		forgetLastBuiltState();
				
		return null;
	}
	
	/**
	 * Converts the build types string into an array of
	 * build kinds.
	 *
	 * @param buildTypes the string of built types to convert
	 * @return the array of build kinds.
	 */
	public static int[] buildTypesToArray(String buildTypes) {
		int count = 0;
		boolean incremental = false;
		boolean full = false;
		boolean auto = false;
		
		if (buildTypes == null || buildTypes.length() == 0) {
			return DEFAULT_BUILD_TYPES;
		}

		StringTokenizer tokenizer = new StringTokenizer(buildTypes, BUILD_TYPE_SEPARATOR);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (IExternalToolConstants.BUILD_TYPE_INCREMENTAL.equals(token)) {
				if (!incremental) {
					incremental = true;
					count++;
				}
			}
			else if (IExternalToolConstants.BUILD_TYPE_FULL.equals(token)) {
				if (!full) {
					full = true;
					count++;
				}
			}
			else if (IExternalToolConstants.BUILD_TYPE_AUTO.equals(token)) {
				if (!auto) {
					auto = true;
					count++;
				}
			}
		}

		int[] results = new int[count];
		count = 0;
		if (incremental) {
			results[count] = IncrementalProjectBuilder.INCREMENTAL_BUILD;
			count++;
		}
		if (full) {
			results[count] = IncrementalProjectBuilder.FULL_BUILD;
			count++;
		}
		if (auto) {
			results[count] = IncrementalProjectBuilder.AUTO_BUILD;
			count++;
		}

		return results;
	}
}
