/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Markus Keller (IBM Corp.) - Cleanup and documentation
 ******************************************************************************/
package org.eclipse.ui.wizards.datatransfer;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface contains a set of methods that allow to configure an existing
 * or future project, for example to add and configure natures when creating a
 * new project.
 *
 * <p>
 * It is typically used as:
 * </p>
 * <ul>
 * <li>a filter to check whether the current {@link ProjectConfigurator} can
 * apply</li>
 * <li>a bean to store user configuration while showing wizard page</li>
 * </ul>
 *
 * <p>
 * Project configurators can be contributed via the
 * {@code org.eclipse.ui.ide.projectConfigurators} extension point. The
 * lifecycle of project configurators is not specified, so implementing classes
 * must be stateless (i.e. their methods cannot keep any state inside or outside
 * this class). The framework may create multiple instances of those classes for
 * a single import session, or it may reuse the same instances for multiple
 * sessions.
 * </p>
 *
 * @since 3.12
 */
public interface ProjectConfigurator {

	/**
	 * From a given {@link File}, detects which directories can/should be
	 * imported as projects into the workspace and configured by this
	 * configurator. This first set of directories is then presented to the user
	 * as import proposals.
	 *
	 * <p>
	 * This method must be stateless.
	 * </p>
	 *
	 * @param root
	 *            the root directory on which to start the discovery
	 * @param monitor
	 *            the progress monitor
	 * @return the children (at any depth) that this configurator suggests to
	 *         import as project
	 */
	public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor);

	/**
	 * Removes from the set of directories those that should not be proposed to
	 * the user for import. Those are typically dirty volatile directories such
	 * as build output directories.
	 *
	 * <p>
	 * This method must be stateless.
	 * </p>
	 *
	 * @param proposals
	 *            the existing import proposals (key is file and value is the
	 *            list of configurators that have identified the key as a
	 *            location they can configure for import). Those can be modified
	 *            and current method is expected to remove some entries from
	 *            this map.
	 */
	default public void removeDirtyDirectories(Map<File, List<ProjectConfigurator>> proposals) {
	}

	/**
	 * Tells whether this configurator thinks that a given {@link IContainer}
	 * should be also imported as a project into the workspace.
	 *
	 * <p>
	 * This method must be stateless (ideally static) and cannot rely on any
	 * class state.
	 * </p>
	 *
	 * @param container
	 *            the container to analyze
	 * @param monitor
	 *            the progress monitor
	 * @return true if the given folder is for sure to be considered as a
	 *         project
	 */
	public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor);

	/**
	 * Returns the folders to exclude from the analysis that happens on an
	 * {@link IProject}.
	 *
	 * <p>
	 * This method must be stateless.
	 * </p>
	 *
	 * @param project
	 *            the project to check for content to ignore
	 * @param monitor
	 *            the progress monitor
	 * @return the set of child folders to ignore in import operation. Typically
	 *         output directories such as bin/ or target/.
	 */
	public Set<IFolder> getFoldersToIgnore(IProject project, IProgressMonitor monitor);

	/**
	 * Checks whether this configurator can contribute to the configuration of
	 * the given project.
	 *
	 * <p>
	 * This method must be stateless.
	 * </p>
	 *
	 * @param project
	 *            the project to check for potential configuration
	 * @param ignoredPaths
	 *            paths that have to be ignored when checking whether this
	 *            configurator applies. Those will typically be nested projects
	 *            (handled separately), or output directories (bin/, target/,
	 *            ...).
	 * @param monitor
	 *            the progress monitor
	 * @return <code>true</code> iff this configurator can configure the given
	 *         project
	 */
	public boolean canConfigure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor);

	/**
	 * Configures a project. This method will only be called if
	 * {@link #canConfigure(IProject, Set, IProgressMonitor)} returned
	 * <code>true</code> for the given project.
	 *
	 * <p>
	 * This method must be stateless.
	 * </p>
	 *
	 * @param project
	 *            the project to configure
	 * @param ignoredPaths
	 *            paths that have to be ignored when configuring the project.
	 *            Those will typically be nested projects, output directories
	 *            (bin/, target/, ...)
	 * @param monitor
	 *            the progress monitor
	 */
	public void configure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor);

}
