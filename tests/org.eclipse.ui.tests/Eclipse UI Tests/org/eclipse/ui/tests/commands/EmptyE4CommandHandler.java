package org.eclipse.ui.tests.commands;

import org.eclipse.e4.core.di.annotations.Execute;

/**
 * @since 3.5
 */
public class EmptyE4CommandHandler {

	@Execute
	public void execute() {
		System.out.println("Executed Command Handler");
	}
}
