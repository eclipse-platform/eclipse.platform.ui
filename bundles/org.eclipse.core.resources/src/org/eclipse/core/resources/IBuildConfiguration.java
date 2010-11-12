/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;


/**
 * Build Configurations provide a mechanism for orthogonal configuration specific
 * builds within a single project.  The resources plugin maintains build deltas per
 * interested builder, per configuration, and allow build configurations to reference
 * each other.
 *<p>
 * All projects have at least one build configuration.  By default this
 * has Id {@link #DEFAULT_CONFIG_ID}.  One configuration in the project is defined 
 * to be 'active'. The active configuration is built by default.  If unset, the
 * active configuration defaults to the first configuration in the project.
 *</p>
 *<p>
 * Build configurations are created with:
 * {@link IWorkspace#newBuildConfiguration(String, String)}, and
 * set on a project using {@link IProjectDescription#setBuildConfigurations(IBuildConfiguration[])}.
 * Build configurations set on Projects must have unique non-null configuration Ids.
 *</p>
 *<p>
 * When a project is built, a specific configuration is built. This configuration
 * is passed to the builders so they can adapt their behavior
 * appropriately. Builders which don't care about configurations may ignore this,
 * and work as before.
 *</p>
 *<p>
 * Build configuration can reference other builds configurations.  These references are 
 * set using {@link IProjectDescription#setBuildConfigReferences(String, IBuildConfiguration[])}.
 * A referenced build configuration may have a <code>null</code> configuration Id which is resolved to the 
 * referenced project's current active build configuration at build time.
 *</p>
 *<p> 
 * Workspace build will ensure that the projects are built in an appropriate order as defined
 * by the reference graph.
 *</p>
 *
 * @see IWorkspace#newBuildConfiguration(String, String)
 * @see IProjectDescription#setActiveBuildConfiguration(String)
 * @see IProjectDescription#setBuildConfigurations(IBuildConfiguration[])
 * @see IProjectDescription#setBuildConfigReferences(String, IBuildConfiguration[])
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.7
 */
public interface IBuildConfiguration {

	/**
	 * The Id of the default build configuration
	 */
	public static final String DEFAULT_CONFIG_ID = ""; //$NON-NLS-1$

	/**
	 * @return the project that the config is for; never null.
	 */
	public IProject getProject();

	/**
	 * Returns the Id of this build configuration.  If this {@link IBuildConfiguration}
	 * is set on a Project, this can never be null.  
	 * <p>
	 * If this IBuildConfiguration is being used as a reference to a build configuration
	 * in another project, this may be null.  Such build configuration references are
	 * resolved to the current active configuration at build time.
	 * </p>
	 * @return the id of the configuration; or null if this is a reference to the active
	 * configuration
	 */
	public String getId();

}
