package org.eclipse.ui.tests.progress;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;

public class CommandHandler extends AbstractHandler implements IHandler {

	
	public boolean executed;
	public Object execute(ExecutionEvent event) {
		executed = true;
		return null;
	}

}
