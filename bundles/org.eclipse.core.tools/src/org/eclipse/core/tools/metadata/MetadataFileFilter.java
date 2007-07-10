/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.metadata;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * Filters accepted files (the ones who are registered in the DumperFactory).
 * 
 * @see MetadataTreeContentProvider#MetadataTreeContentProvider(String[]) 
 * @see java.io.FileFilter
 */
class MetadataFileFilter implements FileFilter {
	private String[] fileNames;

	MetadataFileFilter(String[] fileNames) {
		this.fileNames = fileNames;
		Arrays.sort(this.fileNames);
	}

	/**
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File file) {
		return file.isFile() && Arrays.binarySearch(fileNames, file.getName()) >= 0;
	}
}
