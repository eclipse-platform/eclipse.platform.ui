package org.eclipse.ui.examples.contributions.rcp;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * The action bar advisor must still be used sometimes to register workbench
 * actions that have not been converted into handlers.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private IWorkbenchAction openWindow;
	private IWorkbenchAction save;
	private IWorkbenchAction saveAll;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// register any actions that need to be there as
		// default handlers for commands.

		openWindow = ActionFactory.OPEN_NEW_WINDOW.create(window);
		register(openWindow);

		save = ActionFactory.SAVE.create(window);
		register(save);

		saveAll = ActionFactory.SAVE_ALL.create(window);
		register(saveAll);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.ActionBarAdvisor#dispose()
	 */
	public void dispose() {
		openWindow.dispose();
		openWindow = null;
		save.dispose();
		save = null;
		saveAll.dispose();
		saveAll = null;
		super.dispose();
	}
}
