package org.eclipse.e4.core.commands;

import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.core.commands.ParameterizedCommand;

/**
 */
public interface EHandlerService {
	public IEclipseContext getContext();

	public void activateHandler(String commandId, Object handler);

	public void deactivateHandler(String commandId, Object handler);

	public Object executeHandler(ParameterizedCommand command);

	public boolean canExecute(ParameterizedCommand command);
}
