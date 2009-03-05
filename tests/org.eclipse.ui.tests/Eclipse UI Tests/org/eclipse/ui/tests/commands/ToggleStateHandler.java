package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

public class ToggleStateHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		boolean oldValue = HandlerUtil.toggleCommandState(event.getCommand());
		return new Boolean(oldValue);
	}

}
