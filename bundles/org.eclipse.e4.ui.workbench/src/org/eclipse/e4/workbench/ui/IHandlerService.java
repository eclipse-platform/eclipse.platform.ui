package org.eclipse.e4.workbench.ui;

import org.eclipse.e4.ui.model.application.Command;
import org.eclipse.e4.ui.model.application.Handler;

/**
 *
 */
public interface IHandlerService {
	public Handler getHandler(Command command);
}
