package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;

/**
 * @since 3.5
 * @author Prakash G.R.
 */
public class RadioStateHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		if(HandlerUtil.matchesRadioState(event))
			return null; // do nothing when we are in right state
		
		// else update the state
		String currentState = event.getParameter(RadioState.PARAMETER_ID);
		HandlerUtil.updateRadioState(event.getCommand(), currentState);

		return null;
	}

}
