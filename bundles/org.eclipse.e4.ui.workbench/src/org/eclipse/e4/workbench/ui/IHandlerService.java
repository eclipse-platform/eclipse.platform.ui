package org.eclipse.e4.workbench.ui;

import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MHandler;

/**
 *
 */
public interface IHandlerService {
	public MHandler getHandler(MCommand command);
}
