package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

/**
 * 
 * @author Prakash G.R.
 * @since 3.6
 *
 */
public class DefaultHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event){
		// does nothing
		return null;
	}

}
