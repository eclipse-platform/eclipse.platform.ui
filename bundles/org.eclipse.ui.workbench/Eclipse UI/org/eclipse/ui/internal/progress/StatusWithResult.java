package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author tod
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class StatusWithResult extends Status {

	Object result;

	/**
	 * Create a new instance of the receiver with the same parameters
	 * as an IStatus but with a result. 
	 * @param severity
	 * @param pluginId
	 * @param code
	 * @param message
	 * @param exception
	 */
	public StatusWithResult(
		int severity,
		String pluginId,
		int code,
		String message,
		Throwable exception,
		Object resultObject) {
		super(severity, pluginId, code, message, exception);
		result = resultObject;
	}

	/**
	 * Create a new instance of the receiver with the same parameters
	 * as the passed status but with a result as well.
	 * @param severity
	 * @param pluginId
	 * @param code
	 * @param message
	 * @param exception
	 */
	public StatusWithResult(IStatus status, Object resultObject) {
		this(
			status.getSeverity(),
			status.getPlugin(),
			status.getCode(),
			status.getMessage(),
			status.getException(),
			resultObject);
	}

	/**
	 * Get the result.
	 * @return
	 */
	public Object getResult() {
		return result;
	}

}
