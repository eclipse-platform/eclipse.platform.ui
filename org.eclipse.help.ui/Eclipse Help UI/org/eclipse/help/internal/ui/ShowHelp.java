package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.action.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.help.internal.Help;

//import org.eclipse.help.internal.contributions.InfoSet;

/**
 * ShowHelp
 */
public class ShowHelp //extends Action {
implements IWorkbenchWindowActionDelegate, IExecutableExtension {

	//  private InfoSet infoset;
	private String infoset;

	public ShowHelp() {

	}
	/*
	public ShowHelp(InfoSet infoset) {
		super(infoset.getLabel());
		setToolTipText("Show " + infoset.getLabel());
		this.infoset = infoset;
	}
	*/
	public void dispose() {
	}
	/**
	 * Initializes the action delegate with the workbench window it will work in.
	 *
	 * @param window the window that provides the context for this delegate
	 */
	public void init(IWorkbenchWindow window) {
	}
	/**
	 * Implementation of method defined on <code>IAction</code>.
	 *
	 * [Issue: Will be made abstract. For now, calls <code>actionPerformed()</code> for backwards compatibility.]
	 */
	public void run() {
		//Help.displayHelp(infoset.getID());
		Help.displayHelp(infoset);
	}
	/**
	 * Implementation of method defined on <code>IAction</code>.
	 *
	 * [Issue: Will be made abstract. For now, calls <code>actionPerformed()</code> for backwards compatibility.]
	 */
	public void run(IAction a) {
		//Help.displayHelp(infoset.getID());
		Help.displayHelp(infoset);
	}
	/**
	 * Selection in the workbench has changed. Plugin provider
	 * can use it to change the availability of the action
	 * or to modify other presentation properties.
	 *
	 * <p>Action delegate cannot be notified about
	 * selection changes before it is loaded. For that reason,
	 * control of action's enable state should also be performed
	 * through simple XML rules defined for the extension
	 * point. These rules allow enable state control before
	 * the delegate has been loaded.</p>
	 *
	 * @param action action proxy that handles presentation
	 * portion of the plugin action
	 * @param selection current selection in the workbench
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	/**
	 * Initializes the action with data from the xml declaration
	 */
	public void setInitializationData(
		IConfigurationElement cfig,
		String propertyName,
		Object data) {
		infoset = (String) data;
	}
}
