package org.eclipse.ui.tests.services;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class CheckHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// It's OK do do nothing
		return null;
	}

}
