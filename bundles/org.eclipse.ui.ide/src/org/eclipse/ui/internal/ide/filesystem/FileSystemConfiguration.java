/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.filesystem;

import org.eclipse.ui.ide.fileSystem.FileSystemContributor;

/**
 * FileSystemContribution is the representation of the configuration element
 * defined by
 *
 * @since 3.2
 */
public class FileSystemConfiguration {

	String label;

	FileSystemContributor contributor;

	private String scheme;


	/**
	 * Create a new FileSystemConfiguration with the defined
	 * contributor and userLabel for the supplied scheme.
	 */
	public FileSystemConfiguration(String name, FileSystemContributor declaredContributor, String fileSystem) {
		label = name;
		contributor = declaredContributor;
		scheme = fileSystem;
	}

	/**
	 * Return the huuman readable label for the receiver.
	 * @return String
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Return the contributor for the receiver.
	 * @return FileSystemContributor
	 */
	public FileSystemContributor getContributor() {
		return contributor;
	}

	/**
	 * Return the filesystem scheme for the receiver.
	 * @return Returns the scheme.
	 */
	public String getScheme() {
		return scheme;
	}

}
