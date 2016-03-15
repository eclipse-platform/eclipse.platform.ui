/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.wizards.datatransfer;

import java.io.File;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;

/**
 * This interface contains a set of methods that allow to configure an existing
 * or future project, for example to add and configure natures when creating a
 * new project.
 *
 * It is typically used as a:
 * <ul>
 * <li>a filter to check whether the current {@link ProjectConfigurator} can
 * apply</li>
 * <li>a bean to store user configuration while showing wizard page</li>
 * </ul>
 *
 * @since 3.12
 *
 */
public interface ProjectConfigurator {

	/**
	 * From a given {@link File}, detect which directories can/should be imported as projects
	 * in workspace and configured by this configurator
	 * @param root
	 * @param monitor
	 * @return the (recursive) children that this configurator
	 */
	public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor);

	/**
	 * This method MUST BE stateless (ideally static)
	 *
	 * @param container
	 * @param monitor
	 * @return true if the given folder is for sure to be considered as a
	 *         project
	 */
	public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor);

	/**
	 * This method MUST BE stateless (ideally static)
	 *
	 * @param project
	 * @param monitor
	 * @return the set of children folder to ignore in import operation.
	 *         Typically work directories such as bin/ target/ ....
	 */
	public Set<IFolder> getDirectoriesToIgnore(IProject project, IProgressMonitor monitor);

	/**
	 * This method MUST BE be stateless (ideally static)
	 * @param project
	 * @param ignoredPaths	paths that have to be ignore when checking whether configurator applies.
	 * 						Those will typically be nested projects (handled separately), or "work"
	 * 						directories (bin/ target/ ...)
	 * @param monitor
	 * @return true if the current configurator can configure given project
	 */
	public boolean canConfigure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor);

	/**
	 * This method is not used and will be deleted after Mars release
	 * @return an (optional) wizard to configure the project
	 * @deprecated Not used
	 */
	@Deprecated
	public IWizard getConfigurationWizard();

	/**
	 * This method MUST BE be stateless (ideally static)
	 *
	 * @param project
	 * @param excludedDirectories
	 *            paths that have to be ignored when checking whether
	 *            configurator applies. Those will typically be nested projects,
	 *            or "work" directory (bin/ target/ ...)
	 * @param monitor
	 */
	public void configure(IProject project, Set<IPath> excludedDirectories, IProgressMonitor monitor);

}
