package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.workbench.ui.IExceptionHandler;

public class ExceptionHandler implements IExceptionHandler {

	public void handleException(Exception e) {
		e.printStackTrace();
	}

}
