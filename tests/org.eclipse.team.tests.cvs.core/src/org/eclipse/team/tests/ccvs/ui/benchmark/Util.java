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
package org.eclipse.team.tests.ccvs.ui.benchmark;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

/**
 * CVS Test related utility methods
 */
public class Util {
	
	/**
	 * Imports a .zip file into a container's root folder.
	 * @param container the container
	 * @param file the path of the .zip file
	 */
	public static void importZip(IContainer container, File file)
		throws IOException, ZipException, InterruptedException, InvocationTargetException {
		ZipFile zipFile = new ZipFile(file);
		ZipFileStructureProvider provider = new ZipFileStructureProvider(zipFile);
		ImportOperation importOperation = new ImportOperation(container.getFullPath(),
			provider.getRoot(), provider, null);
		importOperation.setOverwriteResources(true); // don't ask
		importOperation.run(new NullProgressMonitor());
		Assert.assertTrue(importOperation.getStatus().isOK());
	}
}
