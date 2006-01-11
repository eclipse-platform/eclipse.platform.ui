/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.io.OutputStream;

import org.eclipse.compare.IStreamMerger;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.mapping.IStorageMerger;
import org.eclipse.team.internal.ui.TeamUIPlugin;

/**
 * A merge context that performs three-way merges using the {@link IStreamMerger}
 * interface.
 * <p>
 * This class may be subclasses by clients
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.2
 */
public class StorageStreamMerger implements IStorageMerger {

	private IStreamMerger merger;
	
	public StorageStreamMerger(IStreamMerger merger) {
		this.merger = merger;
	}
	
	public IStatus merge(OutputStream output, String outputEncoding, IStorage ancestorStorage, IStorage targetStorage, IStorage otherStorage, IProgressMonitor monitor) throws CoreException {
		IStatus status = merger.merge(output, outputEncoding, 
				ancestorStorage.getContents(), getEncoding(ancestorStorage, outputEncoding), 
				targetStorage.getContents(), getEncoding(targetStorage, outputEncoding), 
				otherStorage.getContents(), getEncoding(otherStorage, outputEncoding), 
				monitor);
		if (status.isOK())
			return status;
		if (status.getCode() == IStreamMerger.CONFLICT)
			return new Status(status.getSeverity(), status.getPlugin(), CONFLICT, status.getMessage(), status.getException());
		return status;
	}

	private String getEncoding(IStorage ancestorStorage, String outputEncoding) {
		if (ancestorStorage instanceof IEncodedStorage) {
			IEncodedStorage es = (IEncodedStorage) ancestorStorage;
			try {
				String charSet = es.getCharset();
				if (charSet != null)
					return charSet;
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
		}
		return outputEncoding;
	}

}
