package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.IModel;
import java.io.PrintWriter;

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
