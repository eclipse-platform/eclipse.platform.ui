package org.eclipse.update.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public class EmptyDirectoryException extends CoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -762930667883265228L;

	public EmptyDirectoryException(IStatus status) {
		super(status);
	}

}
