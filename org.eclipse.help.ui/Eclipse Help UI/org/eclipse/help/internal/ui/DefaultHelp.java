package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.*;
import org.eclipse.help.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.Logger;
import org.eclipse.help.internal.contributions.InfoSet;
import org.eclipse.help.internal.ui.util.Util;

/**
 * This class is an implementation of the pluggable help support.
 * In is registered into the support extension point, and all 
 * requests to display help are delegated to this class.
 * The methods on this class interact with the actual
 * UI component handling the display
 */
public class DefaultHelp implements IHelp {
	private static DefaultHelp instance;
	private IWorkbenchPage helpPage = null;
	private ContextHelpDialog f1Dialog = null;

	/**
	 * BaseHelpViewer constructor comment.
	 */
	public DefaultHelp() {
		super();
		instance = this;
	}
	/**
	 * Makes Help Page Active (visible)
	 */
	private void activateHelpPage() {
		// Make Help Page active
		IWorkbench workbench = WorkbenchHelpPlugin.getDefault().getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		if (workbenchWindow.getActivePage() != helpPage)
			if (helpPage != null)
				workbenchWindow.setActivePage(helpPage);
	}
	/**
	 * Displays context-sensitive help for specified contexts
	 * @param contextIds java.lang.String[]. If a context id is a string, context is looked-up.
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayHelp(String[] contextIds, int x, int y) {

		if (f1Dialog != null)
			f1Dialog.close();

		f1Dialog = new ContextHelpDialog(contextIds, x, y);
		f1Dialog.open();
	}
	/**
	 * Displays context-sensitive help for specified contexts
	 * @param contexts org.eclipse.help.IContext[] dyanmically computed by the application.
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayHelp(IContext[] contexts, int x, int y) {

		if (f1Dialog != null)
			f1Dialog.close();

		f1Dialog = new ContextHelpDialog(contexts, x, y);
		f1Dialog.open();
	}
	/**
	 * Display help for the a given topic and related topics.
	 * @param topic topic to be displayed by the help browser
	 * @param relatedTopics topics that will populate related topics view
	 */
	public void displayHelp(IHelpTopic[] relatedTopics, IHelpTopic topic) {
		if (topic == null || topic.getHref() == null)
			return;

		// Do not start help view if documentaton is not available, display error
		if (HelpSystem.getNavigationManager().getCurrentInfoSet() == null) {
			Util.displayErrorDialog(WorkbenchResources.getString("WW001"));
			return;
		}

		EmbeddedHelpView helpView = getHelpViewInCurrentPerpective();

		// if no infoset is available, helpview will be null.
		if (helpView == null)
			return;
		helpView.displayHelp(relatedTopics, topic);

		//activateHelpPage();
	}
	
	/**
	 * Display help.
	 */
	public void displayHelp(String infosetId) {
		displayHelp(infosetId, null);
	}
		
	/**
	 * Display help and selected specified topic.
	 */
	public void displayHelp(String infosetId, String topicURL) {
		// get the new infoset
		InfoSet infoset = HelpSystem.getNavigationManager().getInfoSet(infosetId);
		if (infoset != null)
			// make this infoset current
			HelpSystem.getNavigationManager().setCurrentInfoSet(infosetId);
		else
		{
			// infoset not found, or no infoset specified, so use current.
			// We use current, and not default to avoid refresh. 
			// revisit for inconsistent refresh.
			infoset = HelpSystem.getNavigationManager().getCurrentInfoSet();
			// if infoset not found, log it
			if (infosetId != null && infosetId.trim().length() != 0)
				Logger.logWarning(WorkbenchResources.getString("WE008", infosetId));
		}
		
		// Do not start help view if documentaton is not available, display error
		if (infoset == null) {
			Util.displayErrorDialog(WorkbenchResources.getString("WW001"));
			return;
		}
			
		// First check the current perspective
		EmbeddedHelpView helpView = getHelpViewInCurrentPerpective();
		if (helpView == null) {
			// not found, so open the help in the help perspective
			helpView = getHelpViewInHelpPerspective();
		}
		
		if (helpView == null)
			return;
		else
		{
			activateHelpPage();
			// switch to infoset
			helpView.displayHelp(infoset, topicURL);
		}
	}
	/**
	 * Computes context information for a given context ID.
	 * @param contextID java.lang.String ID of the context
	 * @return IContext
	 */
	public IContext findContext(String contextID) {
		//return HelpSystem.getContextManager().getContext(contextID);
		return new ContextImpl(contextID);
	}
	/**
	 * Obtains HelpView existing in current perspective.  If it does not
	 * exists, obtains HelpView in HelpPerspective.
	 * @return org.eclipse.help.internal.ui.EmbeddedHelpView
	 */
	public EmbeddedHelpView getHelpViewInCurrentPerpective() {
		// returning null in this method is the cleanest way to handle error.
		// what we do is log the error, then die cleanly.

		IWorkbench workbench = WorkbenchHelpPlugin.getDefault().getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		helpPage = null;

		IWorkbenchPage activeP = workbenchWindow.getActivePage();

		if (activeP == null || activeP.findView(EmbeddedHelpView.ID) == null) {
			EmbeddedHelpView aEmbeddedHelpView = getHelpViewInHelpPerspective();
			activateHelpPage();
			return aEmbeddedHelpView;
		}

		try {
			EmbeddedHelpView aEmbeddedHelpView =
				(EmbeddedHelpView) activeP.showView(EmbeddedHelpView.ID);
			// check to see if the view was created successfully, with a valid Infoset
			if (aEmbeddedHelpView.isCreationSuccessful())
				return aEmbeddedHelpView;
			else
				// no need to log anything here because it would already be logged
				// in the UI classes.
				return null;
		} catch (Exception e) {
			// should never be here.
			Logger.logError(WorkbenchResources.getString("WE004"), e);
			return null;
		}

	}
	/**
	 * Obtains HelpView in HelpPerspective.  Opens new HelpPerspective
	 * if necessary.
	 * @return org.eclipse.help.internal.ui.EmbeddedHelpView
	 */
	public EmbeddedHelpView getHelpViewInHelpPerspective() {
		// returning null in this method is the cleanest way to handle error.
		// what we do is log the error, then die cleanly.

		IWorkbench workbench = WorkbenchHelpPlugin.getDefault().getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		helpPage = null;

		// Check if active perspective is a help perspective
		IWorkbenchPage activeP = workbenchWindow.getActivePage();
		if (activeP != null)
			if (activeP.getPerspective().getId().equals(HelpPerspective.ID))
				helpPage = workbenchWindow.getActivePage();

		if (helpPage == null) {
			// Find first Help Page out of not active pages
			IWorkbenchPage[] pages = workbenchWindow.getPages();
			for (int i = 0; i < pages.length; i++) {
				if (pages[i].getPerspective().getId().equals(HelpPerspective.ID)) {
					helpPage = pages[i];
					break;
				}
			}
		}

		if (helpPage == null) {
			if (activeP != null) {
				IAdaptable oldInput = activeP.getInput();
				try {
					helpPage = workbenchWindow.openPage(HelpPerspective.ID, oldInput);
				} catch (WorkbenchException e) {
					// should never be here.
					Logger.logError(WorkbenchResources.getString("WE002"), e);
					return null;
				}
			} else {
				try {
					helpPage =
						workbenchWindow.openPage(
							HelpPerspective.ID,
							ResourcesPlugin.getWorkspace().getRoot());
				} catch (WorkbenchException e) {
					// should never be here.
					Logger.logError(WorkbenchResources.getString("WE002"), e);
					return null;
				}
			}
		}
		if (helpPage == null) {
			// should never be here.
			Logger.logError(WorkbenchResources.getString("WE003"), null);
			return null;
		} else {
			try {
				// workaround for eclipse bug when showing help in an inactive page
				if (helpPage != workbenchWindow.getActivePage())
					workbenchWindow.setActivePage(helpPage);
				EmbeddedHelpView aEmbeddedHelpView =
					(EmbeddedHelpView) helpPage.showView(EmbeddedHelpView.ID);
				// check to see if the view was created successfully, with a valid Infoset
				if (aEmbeddedHelpView.isCreationSuccessful())
					return aEmbeddedHelpView;
				else
					// no need to log anything here because it would already be logged
					// in the UI classes.
					return null;
			} catch (Exception e) {
				// should never be here.
				Logger.logError(WorkbenchResources.getString("WE004"), e);
				return null;
			}
		}
	}
	public static DefaultHelp getInstance() {
		return instance;
	}
}
