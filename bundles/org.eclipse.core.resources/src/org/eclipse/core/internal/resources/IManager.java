package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IManager {
public void shutdown(IProgressMonitor monitor) throws CoreException;
public void startup(IProgressMonitor monitor) throws CoreException;
}
