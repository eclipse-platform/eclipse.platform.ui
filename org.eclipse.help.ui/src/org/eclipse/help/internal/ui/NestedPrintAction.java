package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.ui.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.viewers.*;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.contributions.Topic;

/**
 * Nested priting action class.
 * Nested printing is only supported on Windows for now.
 */

public class NestedPrintAction extends org.eclipse.jface.action.Action {

	private IStructuredSelection selection = null;

	// keep state info for tracking printing.
	private static boolean printingInProgress = false;
	public NestedPrintAction(IStructuredSelection topicRoot) {
		super(WorkbenchResources.getString("Print_Topic_Tree"));

		// it is guaranteed here that we have a single selection
		this.selection = topicRoot;

		// if another nested printing operation is in progress, 
		// disable Nested printing. This is because the printed transactions
		// should be synchronized. 
		// OR if the topicRoot selection does not have any children, also
		// disable nested printing. This can be revisited. Possibly provide
		// a simple print action instead of disabling.
		
		Topic rootTopicObject = (Topic)selection.getFirstElement();
		if (rootTopicObject instanceof Topic) {
			if ( (((Topic)rootTopicObject).getChildrenList().isEmpty()) 
				||(printingInProgress) )
				setEnabled(false);
			else 
				setEnabled(true);
		} else
			setEnabled(false);
	}
	/*
	 * Create the Browser dedicated for nested printing. 
	 * This method will return null to signal errors creating the Browser.
	 * It is only called when this action's run method is actually called.
	 */
	private IBrowser createPrintBrowser(Composite browserParent) throws Exception {

		String factoryClass = "org.eclipse.help.internal.ui.win32.BrowserFactory";
		try {
			Class classObject = Class.forName(factoryClass);
			IBrowserFactory factory = (IBrowserFactory) classObject.newInstance();

			//** this could throw a HelpDesktopException
			IBrowser webBrowser = factory.createBrowser(browserParent);

			return webBrowser;
		} catch (Exception e) {
			// delegate to calling method
			throw e;
		}

	}
	public void run() {

		try {
			// get the active view part to be able to get to the Composite
			// and create a Browser for printing
			IWorkbenchPage activePage =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart activeViewPart = activePage.findView(EmbeddedHelpView.ID);
			Composite browserParent = null;
			if (!(activeViewPart instanceof EmbeddedHelpView)) {
				// can not get to EmbeddedHelpView. Do nothing.
				Logger.logError(WorkbenchResources.getString("WE006"), null);
				return;
			} else {
				browserParent = ((EmbeddedHelpView) activeViewPart).getViewComposite();

				// create the print Browser
				IBrowser printBrowser = createPrintBrowser(browserParent);

				if (printBrowser == null) {
					// encountered problems creating print browser
					Logger.logError(WorkbenchResources.getString("WE006"), null);
					return;
				} else {
					if (selection != null) {
						Object rootTopicObject = selection.getFirstElement();

						// make sure we have correct root object first
						if (rootTopicObject instanceof Topic) {
							//** disable further Nested Printing Action
							printingInProgress = true;

							// send the root topic off to nested printing.  
							printBrowser.printFullTopic((Topic) rootTopicObject);

							// enable printing again.
							printingInProgress = false;
						}
					}
				}
			}

		} catch (Exception e) {
			Logger.logError(WorkbenchResources.getString("WE006"), e);
			return;
		}
	}
}
