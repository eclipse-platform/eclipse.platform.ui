package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.context.*;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.util.Logger;
import org.eclipse.ui.*;
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
	private int idCounter = 0;
	/**
	 * BaseHelpViewer constructor.
	 */
	public DefaultHelp() {
		super();
		instance = this;
	}
	/**
	 * Makes Help Perspective Active (visible)
	 */
	private void activateHelpPerspective() {
		// Make Help Page active
		IWorkbench workbench = WorkbenchHelpPlugin.getDefault().getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		if (workbenchWindow.getActivePage() != helpPage)
			if (helpPage != null)
				workbenchWindow.setActivePage(helpPage);
	}
	/**
	 * Displays context-sensitive help for specified context
	 * @param contextIds context identifier
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayHelp(String contextId, int x, int y) {
		IContext context = HelpSystem.getContextManager().getContext(contextId);
		displayHelp(context, x, y);
	}
	/**
	 * Displays context-sensitive help for specified contexts
	 * @param contextIds java.lang.String[]. 
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayHelp(String[] contextIds, int x, int y) {
		displayHelp(contextIds[0], x, y);
	}
	/**
	 * Displays context-sensitive help for specified context
	 * @param contexts the context to display
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayHelp(IContext context, int x, int y) {
		if (f1Dialog != null)
			f1Dialog.close();
		if (context == null)
			return;
		f1Dialog = new ContextHelpDialog(context, x, y);
		f1Dialog.open();
		// if any errors or parsing errors have occurred, display them in a pop-up
		ErrorUtil.displayStatus();
	}
	/**
	 * Displays context-sensitive help for specified contexts
	 * @param contexts org.eclipse.help.IContext[] dyanmically computed by the application.
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayHelp(IContext[] contexts, int x, int y) {
		displayHelp(contexts[0], x, y);
	}
	/**
	 * Display help for the a given topic and related topics.
	 * @param topic topic to be displayed by the help browser
	 * @param relatedTopics topics that will populate related topics view
	 */
	public void displayHelp(IContext context, IHelpResource topic) {
		if (context == null || topic == null || topic.getHref() == null)
			return;
		String contextID = getContextID(context);
		IAppServer appServer = WorkbenchHelpPlugin.getDefault().getAppServer();
		if (appServer == null)
			return; // may want to display an error message
		String url =
			"http://"
				+ appServer.getHost()
				+ ":"
				+ appServer.getPort()
				+ "/help?tab=links&contextId="
				+ contextID
				+ "&topic=http://"
				+ appServer.getHost()
				+ ":"
				+ appServer.getPort()
				+ "/help/content/help:"
				+ topic.getHref();
		WorkbenchHelpPlugin.getDefault().getHelpBrowser().displayURL(url);
	}
	/**
	 * Display help.
	 */
	public void displayHelp(String tocFileHref) {
		displayHelp(tocFileHref, null);
	}
	/**
	 * Display help and selected specified topic.
	 */
	public void displayHelp(String tocFileHref, String topicHref) {
		// Do not start help view if documentaton is not available, display error
		if (getTocs().length == 0) {
			// There is no documentation
			ErrorUtil.displayErrorDialog(WorkbenchResources.getString("WW001"));
			//Documentation is not installed.
			return;
		}
		IToc toc = HelpSystem.getTocManager().getToc(tocFileHref);
		if (toc == null) {
			// if toc href specified, but not found, log it
			if (tocFileHref != null && tocFileHref.trim().length() != 0)
				Logger.logWarning(WorkbenchResources.getString("WE008", tocFileHref));
			//Help Toc %1 are not installed.
		}
		EmbeddedHelpView helpView = getHelpView();
		if (helpView == null)
			return; // could not open any help view
		activateHelpPerspective();
		// switch to specified toc and topic
		helpView.displayHelp(toc, topicHref);
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
	 * Returns the list of all integrated tables of contents available.
	 * @return an array of TOC's
	 */
	public IToc[] getTocs() {
		return HelpSystem.getTocManager().getTocs();
	}
	/**
	 * Obtains HelpView by first checking current perspective.
	 * If no help view is open, a help view is obtained
	 * (created if necessary) from help perspective.
	 * @return org.eclipse.help.internal.ui.EmbeddedHelpView
	 */
	private EmbeddedHelpView getHelpView() {
		// First check the current perspective
		EmbeddedHelpView helpView = getHelpViewInCurrentPerpective();
		if (helpView == null) {
			// not found, so open the help in the help perspective
			helpView = getHelpViewInHelpPerspective();
		}
		return helpView;
	}
	/**
	 * Obtains HelpView existing in current perspective
	 * @return EmbeddedHelpView,
	 * or null if the view does not exist in current perspective.
	 */
	private EmbeddedHelpView getHelpViewInCurrentPerpective() {
		// returning null in this method is the cleanest way to handle error.
		// what we do is log the error, then die cleanly.
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return null;
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		helpPage = null;
		IWorkbenchPage activeP = workbenchWindow.getActivePage();
		if (activeP == null || activeP.findView(EmbeddedHelpView.ID) == null) {
			return null;
		}
		try {
			EmbeddedHelpView aEmbeddedHelpView =
				(EmbeddedHelpView) activeP.showView(EmbeddedHelpView.ID);
			// check to see if the view was created successfully
			if (aEmbeddedHelpView.isCreationSuccessful())
				return aEmbeddedHelpView;
			else // no need to log anything here because it would already be logged
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
	private EmbeddedHelpView getHelpViewInHelpPerspective() {
		// returning null in this method is the cleanest way to handle error.
		// what we do is log the error, then die cleanly.
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return null;
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		helpPage = null;
		// Check if active perspective is a help perspective
		IWorkbenchPage activeP = workbenchWindow.getActivePage();
		if (activeP != null
			&& activeP.getPerspective().getId().equals(HelpPerspective.ID))
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
			// Open new Help Page
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
		}
		try {
			// workaround for eclipse bug when showing help in an inactive page
			if (helpPage != workbenchWindow.getActivePage())
				workbenchWindow.setActivePage(helpPage);
			EmbeddedHelpView aEmbeddedHelpView =
				(EmbeddedHelpView) helpPage.showView(EmbeddedHelpView.ID);
			// check to see if the view was created successfully
			if (aEmbeddedHelpView.isCreationSuccessful())
				return aEmbeddedHelpView;
			else // no need to log anything here because it would already be logged
				// in the UI classes.
				return null;
		} catch (Exception e) {
			// should never be here.
			Logger.logError(WorkbenchResources.getString("WE004"), e);
			return null;
		}
	}
	public static DefaultHelp getInstance() {
		return instance;
	}
	private String getContextID(IContext context) {
		if (context instanceof ContextContribution)
			return ((ContextContribution) context).getID();
		if (context instanceof ContextImpl)
			return ((ContextImpl) context).getID();
		// TODO add code not to generate new ID for the same context
		String id = "org.eclipse.help.ID" + idCounter++;
		HelpSystem.getContextManager().addContext(id, context);
		return id;
	}
}