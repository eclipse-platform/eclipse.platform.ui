package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * @since 3.5
 * @author Prakash G.R.
 *
 */
public class ExceptionThrowingHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		throw new ExecutionException("");
	}

}
