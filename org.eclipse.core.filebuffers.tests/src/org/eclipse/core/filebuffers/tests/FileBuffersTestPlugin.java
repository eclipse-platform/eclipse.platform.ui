/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import org.eclipse.core.runtime.Plugin;

/**
 * The main plug-in class to be used in the desktop.
 *
 * @since 3.0
 */
public class FileBuffersTestPlugin extends Plugin {
	private static FileBuffersTestPlugin fgPlugin;

	public FileBuffersTestPlugin() {
		fgPlugin= this;
	}

	public static FileBuffersTestPlugin getDefault() {
		return fgPlugin;
	}
}
