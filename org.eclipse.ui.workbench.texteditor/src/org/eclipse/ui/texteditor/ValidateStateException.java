package org.eclipse.ui.texteditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * A validate state exception can be thrown by
 * <code>AbstractDocumentProvider.doValidateState(Object, Object)</code>.
 * 
 * @since 2.1
 */
public class ValidateStateException extends CoreException {

	/*
	 * @see org.eclipse.core.runtime.CoreException#CoreException(IStatus)
	 */
	 public ValidateStateException(IStatus status) {
		super(status);
	}

}