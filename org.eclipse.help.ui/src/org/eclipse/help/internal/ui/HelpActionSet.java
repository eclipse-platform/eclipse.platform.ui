package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;
import org.eclipse.jface.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.actions.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.InfoSet;
import org.eclipse.help.internal.HelpSystem;

/**
 * An action set for help.
 */
public class HelpActionSet //implements IActionSet

{
	private IWorkbenchWindow window;
	private IActionBars bars;
	private ShowHelp showHelpAction;
	private ArrayList helpActions;
	/**
	 * ResourceActionSet constructor comment.
	 */
	public HelpActionSet() {
		super();
	}
	/**
	 * Create the actions for this set.
	 */
	protected void createActions() {
		/*
		Iterator infosets = HelpSystem
					.getInstance()
					.getContributionManager()
					.getContributionsOfType(ViewContributor.INFOSET_ELEM);
		
		helpActions = new ArrayList();
		while(infosets.hasNext())
		{
			InfoSet infoset = (InfoSet)infosets.next();
			ShowHelp action = new ShowHelp(infoset);
			helpActions.add(action);
		}
		
		// Add a test context action, Help on Help
		helpActions.add(new ContextHelpAction());
		
		// Add Help menu actions.
		IMenuManager helpMenuManager = bars.getMenuManager().findMenuUsingPath(IWorkbenchActionConstants.M_HELP);
		for (Iterator actions=helpActions.iterator(); actions.hasNext(); )
			helpMenuManager.appendToGroup(IWorkbenchActionConstants.HELP_START, (Action)actions.next());
		bars.updateActionBars();
		*/
	}
	/**
	 * Disposes of this action set.
	 * <p>
	 * Implementation should remove any references to the window and action bars 
	 * created in the <code>init</code>.
	 * </p>
	 * <p>
	 * [Issue: Should this say: "...should remove anything they contributed
	 *  in <code>init</code>? Or is most of the withdrawal done automatically?
	 * ]
	 * </p>
	 */
	public void dispose() {
	}
	/**
	 * Adds actions to the page action bars.
	 */
	public void init(IWorkbenchWindow window, IActionBars bars) {
		// Save state for dispose.
		this.window = window;
		this.bars = bars;

		// Add actions to the window.
		createActions();
	}
}
