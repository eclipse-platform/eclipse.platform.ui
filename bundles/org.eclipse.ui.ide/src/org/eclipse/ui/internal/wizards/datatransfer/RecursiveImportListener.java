/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

public interface RecursiveImportListener {

	public void projectCreated(IProject project);

	public void projectConfigured(IProject project, ProjectConfigurator configurator);

	public void errorHappened(IPath location, Exception ex);

}
