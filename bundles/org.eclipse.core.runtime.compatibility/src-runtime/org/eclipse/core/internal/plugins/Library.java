/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.*;

public class Library implements ILibrary {
	public String path;

	public Library(String path) {
		this.path = path;
	}

	public String[] getContentFilters() {
		return null;
	}

	public IPath getPath() {
		return new Path(path);
	}

	public String getType() {
		return ILibrary.CODE;
	}

	public boolean isExported() {
		return true;
	}

	public boolean isFullyExported() {
		return true;
	}

	public String[] getPackagePrefixes() {
		return null;
	}

}
