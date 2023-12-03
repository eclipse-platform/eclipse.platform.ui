/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
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

import java.io.File;

/**
 * An exception to report error when importing a project.
 *
 * @since 3.12
 */
public class CouldNotImportProjectException extends Exception {

	private static final long serialVersionUID = 5207202862271299462L;
	private File location;

	public CouldNotImportProjectException(File location, Exception cause) {
		super("Could not import project located at " + location.getAbsolutePath(), cause); //$NON-NLS-1$
		this.location = location;
	}

	public CouldNotImportProjectException(File location, String message) {
		super(message);
		this.location = location;
	}

	/**
	 *
	 * @return the file which failed at being imported
	 */
	public File getLocation() {
		return this.location;
	}
}
