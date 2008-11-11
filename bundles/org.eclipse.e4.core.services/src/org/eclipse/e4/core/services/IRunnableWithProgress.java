package org.eclipse.e4.core.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface IRunnableWithProgress {

	public IStatus run(IProgressMonitor monitor);
}
