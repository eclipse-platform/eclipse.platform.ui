/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.LibraryModel;

public class Library extends LibraryModel implements ILibrary {
  public Library()
  {
	super();
  }  
public String[] getContentFilters() {
	if (!isExported() || isFullyExported())
		return null;
	return getExports();
}
public IPath getPath() {
	return new Path(getName());
}
}
