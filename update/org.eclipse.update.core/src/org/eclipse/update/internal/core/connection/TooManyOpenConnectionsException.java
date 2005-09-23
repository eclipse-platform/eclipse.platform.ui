package org.eclipse.update.internal.core.connection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;


public class TooManyOpenConnectionsException extends CoreException {

	private static final long serialVersionUID = 1L;
	
	public TooManyOpenConnectionsException(IStatus status) {
		super(status);
	}
}
