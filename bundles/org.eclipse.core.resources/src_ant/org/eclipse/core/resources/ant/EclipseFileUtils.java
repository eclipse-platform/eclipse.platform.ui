/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
/**
 * Subclasses FileUtils from Ant to extend its funtionalities.
 */
public class EclipseFileUtils extends FileUtils {

public void copyFile(File sourceFile, File destFile, FilterSetCollection filters, boolean overwrite, boolean preserveLastModified) throws IOException {
	super.copyFile(sourceFile, destFile, filters, overwrite, preserveLastModified);
	CoreFileSystemLibrary.copyAttributes(sourceFile.getAbsolutePath(), destFile.getAbsolutePath(), preserveLastModified);
}
}