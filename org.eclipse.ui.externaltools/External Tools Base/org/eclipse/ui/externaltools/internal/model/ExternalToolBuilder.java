/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.model;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolMigration;
import org.osgi.framework.Bundle;

/**
 * This project builder implementation will run an external tool during the
 * build process. 
 */
public final class ExternalToolBuilder extends IncrementalProjectBuilder {
	private final class IgnoreTeamPrivateChanges implements IResourceDeltaVisitor {
		private boolean[] fTrueChange;
		private IgnoreTeamPrivateChanges(boolean[] trueChange) {
			super();
			fTrueChange= trueChange;
		}
		public boolean visit(IResourceDelta visitDelta) throws CoreException {
			IResource resource= visitDelta.getResource();
			if (resource instanceof IFile) {
				fTrueChange[0]= true;
				return false;
			}
			return true;
		}
	}

	public static final String ID = "org.eclipse.ui.externaltools.ExternalToolBuilder"; //$NON-NLS-1$;

	private static final String BUILD_TYPE_SEPARATOR = ","; //$NON-NLS-1$
	private static final int[] DEFAULT_BUILD_TYPES= new int[] {
									IncrementalProjectBuilder.INCREMENTAL_BUILD,
									IncrementalProjectBuilder.FULL_BUILD};

	private static String buildType = IExternalToolConstants.BUILD_TYPE_NONE;
	
	private static IProject buildProject= null;
	
	private List projectsWithinScope;
	
	private boolean buildKindCompatible(int kind, ILaunchConfiguration config) throws CoreException {
		int[] buildKinds = buildTypesToArray(config.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, "")); //$NON-NLS-1$
		for (int j = 0; j < buildKinds.length; j++) {
			if (kind == buildKinds[j]) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {		
		if (ExternalToolsPlugin.getDefault().getBundle().getState() != Bundle.ACTIVE) {
			return null;
		}
		
		projectsWithinScope= new ArrayList();
		ILaunchConfiguration config = BuilderUtils.configFromBuildCommandArgs(getProject(), args, new String[1]);
		if (config != null && buildKindCompatible(kind, config) && configEnabled(config)) {
			doBuildBasedOnScope(kind, config, monitor);
		}
		return getProjectsWithinScope();
	}
	
	/**
	 * Returns whether the given builder config is enabled or not.
	 * 
	 * @param config the config to examine
	 * @return whether the config is enabled
	 */
	private boolean configEnabled(ILaunchConfiguration config) {
		try {
			return ExternalToolsUtil.isBuilderEnabled(config);
		} catch (CoreException e) {
			ExternalToolsPlugin.getDefault().log(e);
		}
		return true;
	}

	private IProject[] getProjectsWithinScope() {
		if (projectsWithinScope == null || projectsWithinScope.isEmpty()) {
			projectsWithinScope = null;
			return null;
		}
		return (IProject[])projectsWithinScope.toArray(new IProject[projectsWithinScope.size()]);
	}

	private void doBuildBasedOnScope(int kind, ILaunchConfiguration config, IProgressMonitor monitor) throws CoreException {
		boolean buildForChange = true;
		if (kind != FULL_BUILD) { //scope not applied for full builds
			IResource[] resources = ExternalToolsUtil.getResourcesForBuildScope(config);
			if (resources != null && resources.length > 0) {
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					projectsWithinScope.add(resource.getProject());
				}
				buildForChange = buildScopeIndicatesBuild(resources);
			}
		}

		if (buildForChange) {
			launchBuild(kind, config, monitor);
		}
	}
	
	private void launchBuild(int kind, ILaunchConfiguration config, IProgressMonitor monitor) throws CoreException {
		monitor.subTask(MessageFormat.format(ExternalToolsModelMessages.getString("ExternalToolBuilder.Running_{0}..._1"), new String[] { config.getName()})); //$NON-NLS-1$
		buildStarted(kind);
		// The default value for "launch in background" is true in debug core. If
		// the user doesn't go through the UI, the new attribute won't be set. This means
		// that existing Ant builders will try to run in the background (and likely conflict with
		// each other) without migration.
		config= ExternalToolMigration.migrateRunInBackground(config);
		config.launch(ILaunchManager.RUN_MODE, monitor);
		buildEnded();
	}

	/**
	 * Returns the build type being performed if the
	 * external tool is being run as a project builder.
	 * 
	 * @return one of the <code>IExternalToolConstants.BUILD_TYPE_*</code> constants.
	 */
	public static String getBuildType() {
		return buildType;
	}
	
	/**
	 * Returns the project that is being built and has triggered the current external
	 * tool builder. <code>null</code> is returned if no build is currently occurring.
	 * 
	 * @return project being built or <code>null</code>.
	 */
	public static IProject getBuildProject() {
		return buildProject;
	}
	
	/**
	 * Stores the currently active build kind and build project when a build begins
	 * @param buildKind
	 */
	private void buildStarted(int buildKind) {
		switch (buildKind) {
			case IncrementalProjectBuilder.INCREMENTAL_BUILD :
				buildType = IExternalToolConstants.BUILD_TYPE_INCREMENTAL;
				break;
			case IncrementalProjectBuilder.FULL_BUILD :
				buildType = IExternalToolConstants.BUILD_TYPE_FULL;
				break;
			case IncrementalProjectBuilder.AUTO_BUILD :
				buildType = IExternalToolConstants.BUILD_TYPE_AUTO;
				break;
			default :
				buildType = IExternalToolConstants.BUILD_TYPE_NONE;
				break;
		}
		buildProject= getProject();
	}
	
	/**
	 * Clears the current build kind and build project when a build finishes.
	 */
	private void buildEnded() {
		buildType= IExternalToolConstants.BUILD_TYPE_NONE;
		buildProject= null;
	}
	
	private boolean buildScopeIndicatesBuild(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			IResourceDelta delta = getDelta(resources[i].getProject());
			if (delta == null) {
				//project just added to the workspace..no previous build tree
				return true;
			} 
			IPath path= resources[i].getProjectRelativePath();
			IResourceDelta change= delta.findMember(path);
			if (change != null) {
				final boolean[] trueChange= new boolean[1];
				trueChange[0]= false;
				try {
					change.accept(new IgnoreTeamPrivateChanges(trueChange));
				} catch (CoreException e) {
					ExternalToolsPlugin.getDefault().log("Internal error resolving changed resources during build", e); //$NON-NLS-1$
				}
				
				return trueChange[0]; //filtered out team private changes
			}
		}
		return false;
	}

	/**
	 * Converts the build types string into an array of
	 * build kinds.
	 *
	 * @param buildTypes the string of built types to convert
	 * @return the array of build kinds.
	 */
	public static int[] buildTypesToArray(String buildTypes) {
		if (buildTypes == null || buildTypes.length() == 0) {
			return DEFAULT_BUILD_TYPES;
		}
		
		int count = 0;
		boolean incremental = false;
		boolean full = false;
		boolean auto = false;

		StringTokenizer tokenizer = new StringTokenizer(buildTypes, BUILD_TYPE_SEPARATOR);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (IExternalToolConstants.BUILD_TYPE_INCREMENTAL.equals(token)) {
				if (!incremental) {
					incremental = true;
					count++;
				}
			} else if (IExternalToolConstants.BUILD_TYPE_FULL.equals(token)) {
				if (!full) {
					full = true;
					count++;
				}
			} else if (IExternalToolConstants.BUILD_TYPE_AUTO.equals(token)) {
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