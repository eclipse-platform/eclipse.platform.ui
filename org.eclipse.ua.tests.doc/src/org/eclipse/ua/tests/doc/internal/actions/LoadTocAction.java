/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.doc.internal.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.webapp.servlet.EclipseConnector;
import org.eclipse.help.internal.webapp.servlet.ExtraFilters;
import org.eclipse.help.internal.webapp.servlet.PrioritizedFilter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ua.tests.doc.internal.dialogs.SelectTocDialog;
import org.eclipse.ua.tests.doc.internal.linkchecker.AddScriptFilter;
import org.eclipse.ua.tests.doc.internal.linkchecker.OnLoadFilter;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class LoadTocAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private static ArrayList topicList;
	private static String firstHref;
    public static List errors = new ArrayList();
    public static String lastPage;
    public static String currentPage;
    
    private class MonitorThread extends Thread {
		String lastHref;
		int timesSame = 0;
		boolean isComplete = false;
		public void run() {
			while (!isComplete) {
				if (lastHref == lastPage) {
					timesSame++;
				} else {
					lastHref = lastPage;
					timesSame = 0;
				}
				if (topicList == null) {
					isComplete = true;
				} else if (timesSame >= 10 ) {
					errors.add("Time out on page " + lastPage);
					isComplete = true;
					showErrors();
				} else  {
					try {
						sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
    
    public static void showErrors() {
		if (errors == null) return;
		if (errors.size() == 0) {
			reportStatus("Testing complete, no errors found");
		} else {
			reportStatus("Testing complete, errors found");
		}
		for (Iterator iter = errors.iterator(); iter.hasNext();) {
			String errorMessage = (String)iter.next();
			reportStatus(errorMessage);
		}
		errors = null;	
		topicList = null;
	}

	private static void reportStatus(String errorMessage) {
		//HelpPlugin.logWarning(errorMessage);
		System.out.println(errorMessage);
	}
	
	private class NotFoundCallout implements EclipseConnector.INotFoundCallout {
		public void notFound(String url) {
			if (errors != null) {
			    errors.add("Error opening " + lastPage + "\n   cannot load " + url);	
			}
		}	
	}
	
	private class LinkProvider implements Iterator {
		private List links;
		int lastLink = -1;
		
		public LinkProvider(List links) {
			this.links = links;
		}
		
		public boolean hasNext() {
			if (topicList != null && lastLink < links.size() && links.size() > 0) {
				return true;
			}
			EclipseConnector.setNotFoundCallout(null);
			showErrors();
			return false;
		}
		
		public Object next() {
			if (lastLink >= 0 && lastLink < links.size()) {
				lastPage = (String) links.get(lastLink);
				//System.out.println("Last page is " + lastPage);
			}
			lastLink++;
			if (lastLink < links.size()) {
				currentPage = (String)links.get(lastLink);
				//System.out.println("Current page is " + currentPage);
				return currentPage;	
			} else if (lastLink == links.size()) {
				currentPage =  (String)links.get(lastLink - 1);
				//System.out.println("Current page is " + currentPage);
				return currentPage;	
			}
			return null;
		}
		
		public void remove() {
		}	
		
	}
	
	/**
	 * The constructor.
	 */
	public LoadTocAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		showErrors();
		SelectTocDialog dlg = new SelectTocDialog(window.getShell());
		dlg.open();
		if (dlg.getReturnCode() == SelectTocDialog.CANCEL) {
			return;
		}
		int testKind = dlg.getTestKind();
		PrioritizedFilter[] filters = new PrioritizedFilter[] {
				new PrioritizedFilter(new OnLoadFilter(testKind), 1),
				new PrioritizedFilter(new AddScriptFilter(), 2)};
		ExtraFilters.setFilters(filters);
		Toc[] tocsToCheck = dlg.getTocsToCheck();
		if (testKind == SelectTocDialog.PAGES_EXIST) {
			new CheckTocAction().checkTocFilesExist(tocsToCheck);
			return;
		}
	
		firstHref = null;
		topicList = new ArrayList();
		for (int i = 0; i < tocsToCheck.length; i++) {
		    Toc toc = tocsToCheck[i];
			reportStatus("Test level = " + testKind + " testing " + toc.getTocContribution().getId());
		    ITopic[] topics = toc.getTopics();
		    addTopics(topics);
		}
		lastPage = "No pages read";
		LinkProvider linkProvider = new LinkProvider(topicList);
		OnLoadFilter.setLinkProvider(linkProvider);
		EclipseConnector.setNotFoundCallout(new NotFoundCallout());
		errors = new ArrayList();
		if (linkProvider.hasNext()) {
			firstHref = (String)linkProvider.next();
		    PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(firstHref);
			new MonitorThread().start();
		} else {
			reportStatus("No pages to check");
		}
	}

	private void addTopics(ITopic[] topics) {
		for (int i = 0; i < topics.length; i++) {
			ITopic nextTopic = topics[i];
			addTopic(nextTopic);
		}
	}

	private void addTopic(ITopic nextTopic) {
		String href = nextTopic.getHref();
		if (href != null && !isFiltered(href)) {
		    if (firstHref == null) {
		    	firstHref = href;
		    } 
		    topicList.add(href);
		}
		addTopics(nextTopic.getSubtopics());
	}
	
	private boolean isFiltered(String href) {
		if (!href.startsWith("/")) return true;
		//if (href.startsWith("/org.eclipse.pde.doc.user/reference")) return true;
		//if (href.startsWith("/org.eclipse.platform.doc.isv/reference")) return true;
		//if (href.startsWith("/org.eclipse.platform.doc.isv/samples")) return true;
		//if (href.startsWith("/org.eclipse.jdt.doc.isv/reference")) return true;
		return false;
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}