package org.eclipse.e4.core.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * TODO Replace with IProgressRunnable from org.eclipse.equinox.concurrent.
 */
public interface IRunnableWithProgress {

	public IStatus run(IProgressMonitor monitor);
}
