/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.filesystem.zip;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.*;


/**
 * 
 */
public class ZipFileSystem extends FileSystem {
	public IFileStore getStore(URI uri) {
		if (EFS.SCHEME_ZIP.equals(uri.getScheme())) {
			IPath path = new Path(uri.getPath());
			try {
				return new ZipFileStore(EFS.getStore(new URI(uri.getQuery())), path);
			} catch (URISyntaxException e) {
				//ignore and fall through below
			} catch (CoreException e) {
				//ignore and fall through below
			}
		}
		return EFS.getNullFileSystem().getStore(uri);
	}
}
