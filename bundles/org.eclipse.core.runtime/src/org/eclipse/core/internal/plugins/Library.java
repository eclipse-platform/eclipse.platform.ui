package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.IModel;

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
