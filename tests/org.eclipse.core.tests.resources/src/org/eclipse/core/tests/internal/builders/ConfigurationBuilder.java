/*******************************************************************************
 * Copyright (c) 2010, 2012 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.util.*;
import junit.framework.Assert;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A builder used that stores statistics, such as which target was built, per project build config.
 */
public class ConfigurationBuilder extends TestBuilder {
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.configbuilder";

	/** Stores IBuildConfiguration -> ConfigurationBuilder */
	private static HashMap<IBuildConfiguration, ConfigurationBuilder> builders = new HashMap<IBuildConfiguration, ConfigurationBuilder>();
	/** Order in which builders have been run */
	static List<IBuildConfiguration> buildOrder = new ArrayList<IBuildConfiguration>();

	// Per builder instance stats
	int triggerForLastBuild;
	IResourceDelta deltaForLastBuild;
	int buildCount;

	public ConfigurationBuilder() {
	}

	public static ConfigurationBuilder getBuilder(IBuildConfiguration config) {
		return builders.get(config);
	}

	public static void clearBuildOrder() {
		buildOrder = new ArrayList<IBuildConfiguration>();
	}

	public static void clearStats() {
		for (Iterator<ConfigurationBuilder> it = builders.values().iterator(); it.hasNext();) {
			ConfigurationBuilder builder = it.next();
			builder.buildCount = 0;
			builder.triggerForLastBuild = 0;
			builder.deltaForLastBuild = null;
		}
	}

	protected void startupOnInitialize() {
		super.startupOnInitialize();
		builders.put(getBuildConfig(), this);
		buildCount = 0;
	}

	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		buildCount++;
		triggerForLastBuild = kind;
		deltaForLastBuild = getDelta(getProject());
		buildOrder.add(getBuildConfig());
		return super.build(kind, args, monitor);
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		IResourceDelta delta = getDelta(getProject());
		Assert.assertNull(delta);
		buildCount++;
		triggerForLastBuild = IncrementalProjectBuilder.CLEAN_BUILD;
	}
}
