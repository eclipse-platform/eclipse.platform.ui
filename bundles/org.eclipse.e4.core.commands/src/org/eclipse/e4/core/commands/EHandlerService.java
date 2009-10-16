package org.eclipse.e4.core.commands;

import org.eclipse.core.commands.ParameterizedCommand;

/**
 */
public interface EHandlerService {
	public void activateHandler(String commandId, Object handler);

	public void deactivateHandler(String commandId, Object handler);

	public Object executeHandler(ParameterizedCommand command);

	public boolean canExecute(ParameterizedCommand command);
}
