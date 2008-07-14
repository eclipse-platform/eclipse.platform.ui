/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.patch;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.compare.internal.core.Activator;
import org.eclipse.compare.internal.patch.PatchReader;
import org.eclipse.compare.internal.patch.Patcher;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Helper class for parsing patches.
 */
public class PatchParser {

	/**
	 * Return whether the given storage contains a patch.
	 * @param storage the storage
	 * @return whether the given storage contains a patch
	 * @throws CoreException if an error occurs reading the contents from the storage
	 */
	public static boolean isPatch(IStorage storage) throws CoreException {
		return parsePatch(storage).length > 0;
	}
	
	/**
	 * Parse the given patch and return the set of file patches that it contains.
	 * @param storage the storage that contains the patch
	 * @return the set of file patches that the storage contains
	 * @throws CoreException if an error occurs reading the contents from the storage
	 */
	public static IFilePatch[] parsePatch(IStorage storage) throws CoreException {
		BufferedReader reader = Patcher.createReader(storage);
		try {
			PatchReader patchReader= new PatchReader();
			patchReader.parse(reader);
			return patchReader.getAdjustedDiffs();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getMessage(), e));
		} finally {
			try {
				reader.close();
			} catch (IOException e) { //ignored
			}
		}
	}
}
