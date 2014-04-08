/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator;

import java.io.FileOutputStream;
import java.util.jar.Manifest;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Converts the given install location into a bundle.
 *
 * @author Steven Spungin
 *
 */
public class BundleConverter {

	/**
	 *
	 * @param installLocation
	 * @return The bundleId of the bundle, or null if not converted
	 * @throws Exception
	 */
	public static String convertProjectToBundle(String installLocation, IWorkspace workspace) throws Exception {
		IPath project = Path.fromOSString(installLocation);
		if (project.toFile().isDirectory() == false) {
			throw new Exception(Messages.BundleConverter_installLocationNotADirectory);
		}
		IPath metaDir = project.append("META-INF"); //$NON-NLS-1$
		if (metaDir.toFile().isDirectory() == false) {
			if (metaDir.toFile().mkdir() == false) {
				throw new Exception(Messages.BundleConverter_couldNotCreateMetaInfDir);
			}
		}
		IPath manifestPath = metaDir.append("MANIFEST.MF"); //$NON-NLS-1$
		if (manifestPath.toFile().isFile()) {
			throw new Exception(Messages.BundleConverter_projectIsAlreadyABundle);
		}
		Manifest manifest = new Manifest();
		// TODO prompt for names
		IPath path = Path.fromOSString(installLocation);
		String bundleId = path.lastSegment();
		String bundleName = path.lastSegment();
		manifest.getMainAttributes().putValue("Manifest-Version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
		manifest.getMainAttributes().putValue("Bundle-SymbolicName", bundleId); //$NON-NLS-1$
		manifest.getMainAttributes().putValue("Bundle-Name", bundleName); //$NON-NLS-1$
		manifest.getMainAttributes().putValue("Bundle-Version", "0.0.1"); //$NON-NLS-1$ //$NON-NLS-2$
		manifest.write(new FileOutputStream(manifestPath.toFile()));
		IProject targetProject = workspace.getRoot().getProject(bundleId);
		targetProject.refreshLocal(2, null);
		return bundleId;
	}

}
