/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.core.internal.filebuffers;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.Platform;

import org.eclipse.core.filebuffers.ITextFileBufferManager;



public class FileBuffersPlugin {

	public static final String PLUGIN_ID= "org.eclipse.core.filebuffers"; //$NON-NLS-1$

	private static ITextFileBufferManager fTextFileBufferManager;

	/**
	 * Returns the text file buffer manager of this plug-in.
	 *
	 * @return the text file buffer manager of this plug-in
	 */
	public static synchronized ITextFileBufferManager getFileBufferManager() {
		if (fTextFileBufferManager == null) {
			Bundle resourcesBundle= Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
			if (resourcesBundle != null)
				fTextFileBufferManager= new ResourceTextFileBufferManager();
			else
				fTextFileBufferManager= new TextFileBufferManager();
		}
		return fTextFileBufferManager;
	}
}
