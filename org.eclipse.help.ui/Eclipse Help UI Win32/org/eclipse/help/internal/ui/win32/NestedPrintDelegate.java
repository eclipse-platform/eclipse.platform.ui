package org.eclipse.help.internal.ui.win32;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.server.*;
import java.net.URLEncoder;

/**
 * This class handles the Nested Printing action from the Topics Viewer
 */
public class NestedPrintDelegate {

	private WebBrowser webBrowser = null;
	// The automation object and Control for printing
	protected OleAutomation backOleObject;
	protected HelpControlSite backControlSite;

	private PrintMonitorDialog progressDialog = null;

	private IProgressMonitor pm = null;
	private boolean printingComplete = false;
	private IRunnableWithProgress printOperation = null;
	private Topic[] topicList = null;
	private Topic rootTopic = null;
	private int topicsPrinted = -1;

	/**
	 * Operation class for a NestedPrint task issued by a WebBrowser
	 */
	public class NestedPrintOperation implements IRunnableWithProgress {

		public synchronized void run(IProgressMonitor monitor) {
			try {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							pm = progressDialog.getProgressMonitor();
							if (pm == null)
								return;
							// add one to topicList size to compensate for TableOfContents
							pm.beginTask(
								WorkbenchResources.getString("Printing_Topic_Tree"),
								topicList.length + 1);

							// create the special url to kick off creating the TableOfContents.
							// This url is handeled as a special case in the TempURL class. 
							String tableOfContentsURL = createTableOfContentsURL();
							if (tableOfContentsURL != null)
								webBrowser.navigate(backOleObject, tableOfContentsURL);
							if (pm.isCanceled())
								return;

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				while (!printingComplete) {
					try {
						Thread.currentThread().sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				// log and die
			}
		}
	}

	/**
	 * ProgressMonitorDialog sublass for a NestedPrint task.
	 */
	public class PrintMonitorDialog extends ProgressMonitorDialog {
		public PrintMonitorDialog(Shell parent) {
			super(parent);
			// override parent behavior to make dialog modeless.
			setShellStyle(SWT.BORDER | SWT.TITLE | SWT.MODELESS);
			setBlockOnOpen(false);
		}
	}

	/**
	 * Ole Listener class for a NestedPrint task
	 */
	public class PrintOleListener implements OleListener, Runnable {

		public synchronized void run() {
			if (topicList == null)
				return;
			if (pm.isCanceled()) {
				doCleanUp();
				return;
			}
			if (topicsPrinted == -1)
				// this means that we are printing the TableOfContents
				pm.subTask(
					WorkbenchResources.getString("Printing")
						+ WorkbenchResources.getString("Table_Of_Contents"));
			else
				pm.subTask(
					WorkbenchResources.getString("Printing") + topicList[topicsPrinted].getLabel());

			topicsPrinted++;
			webBrowser.print(backControlSite, false);

			// add one to compensate for the TableOfContents
			if (topicsPrinted < topicList.length) {
				if (Logger.DEBUG)
					Logger.logDebugMessage(
						"NestedPrintDelegate",
						"OLE listener: printing from:  " + Thread.currentThread().toString());
				printTopic(topicsPrinted);
				pm.worked(1);
			} else {
				doCleanUp();
				return;
			}
		}

		public synchronized void handleEvent(OleEvent event) {
			try {
				Display.getCurrent().asyncExec(this);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		private void doCleanUp() {
			// do clean up
			topicsPrinted = -1;
			topicList = null;
			if (Logger.DEBUG)
				Logger.logDebugMessage(
					"NestedPrintDelegate",
					"OLE listener: finishing:  " + Thread.currentThread().toString());
			if (progressDialog != null)
				pm.done();
			progressDialog = null;
			webBrowser.dispose();
			printingComplete = true;
		}

	}
	public NestedPrintDelegate(
		WebBrowser aWebBrowser,
		OleAutomation aOleObject,
		HelpControlSite aControlSite) {
		super();

		// The automation object and Control associated with printing
		backControlSite = aControlSite;
		backOleObject = aOleObject;
		webBrowser = aWebBrowser;

		backControlSite.addEventListener(
			WebBrowser.DocumentComplete,
			new PrintOleListener());

	}
	/**
	 * Retruns the fully qualified URL that identifies a Table of Contents URL.
	 * Table of Contents for a given Nested print action is generated
	 * on the fly using the Topic list captured in this class.
	 * No separate class was created for this URL, but instead it 
	 * is handled as a "special" url in the TempURL class.
	 * In this special case, the TempURL class does not read from
	 * a file in the working directory, but actually uses this class 
	 * to dynamically create a table of contents. 
	 */
	private String createTableOfContentsURL() {
		// make sure to append the TempURL prefix first. 
		// Example: http://localhost:80/temp/TableOfContents
		//              /?topicId=org.eclipse.help.examples.ex1.someTopicId
		//              &viewId=org.eclipse.help.examples.ex1.someViewId 
		//              &infosetId=org.eclipse.help.examples.ex1.someInfosetId  
		StringBuffer url = new StringBuffer();
		url.append(HelpSystem.getLocalHelpServerURL());
		url.append("/");
		url.append(TempURL.getPrefix());
		url.append("/");
		url.append(TempURL.TABLE_OF_CONTENTS_PREFIX);
		url.append("/");

		// add query, which is the infosetId, viewId, and TopicID
		// add topicId
		url.append("?topicId=");
		url.append(URLEncoder.encode(rootTopic.getID()));

		// add viewId
		url.append("&viewId=");
		Contribution parent = rootTopic.getParent();
		boolean foundParent = false;
		while (!foundParent) {
			if (parent instanceof InfoView)
				foundParent = true;
			else
				parent = parent.getParent();
		}

		InfoView view = (InfoView) parent;
		url.append(URLEncoder.encode(view.getID()));

		// get to Infoset
		url.append("&infosetId=");
		parent = parent.getParent();
		InfoSet infoSet = (InfoSet) parent;

		// add infosetId
		url.append(URLEncoder.encode(infoSet.getID()));

		return url.toString();
	}
	/** 
	 * creates the fully qualified url name for a given topic
	 * returns null if no topic is associated with the input object.
	 */
	private String createURL(Topic topic) {
		if (topic == null)
			return null;

		String url = topic.getHref();
		if (url == null || url.equals(""))
			return null; // no content in this topic

		if (url.indexOf("?resultof=") != -1) {
			Locale locale = Locale.getDefault();
			url = url.concat("&lang=") + locale.getDefault().toString();
		} else {
			Locale locale = Locale.getDefault();
			url = url.concat("?lang=") + locale.getDefault().toString();
		}
		if (url.indexOf("http:") == -1)
			url = HelpSystem.getLocalHelpServerURL() + url;
		return url;
	}
	/** 
	 * confirms with the user if Nested Printing is ok with given number of Topics.
	 */
	private boolean okToPrint(int numberOfTopics) {
		String topics = Integer.toString(numberOfTopics);
		if (numberOfTopics == 0) {
			String msg = WorkbenchResources.getString("no_Topics_To_Print");
			Util.displayInfoDialog(msg);
			return false;
		} else {
			String question = WorkbenchResources.getString("ok_To_Print", topics);
			return Util.displayQuestionDialog(question);
		}
	}
	public void printFullTopic(Topic rootTopic) {
		if (Logger.DEBUG)
			Logger.logDebugMessage("NestedPrintDelegate", "printTopicTree ");
		try {

			this.rootTopic = rootTopic;
			this.topicList = TableOfContentsGenerator.getTopicList(rootTopic);
			if ((webBrowser == null) || (topicList == null))
				return; // should never be here

			if (okToPrint(topicList.length)) {

				printOperation = new NestedPrintOperation();

				progressDialog = new PrintMonitorDialog(Display.getCurrent().getActiveShell());
				progressDialog.run(true, true, printOperation);

			}

		} catch (Exception e) {
			Logger.logError(WorkbenchResources.getString("WE006"), e);
			return;
		}

	}
	private void printTopic(int i) {
		if (Logger.DEBUG)
			Logger.logDebugMessage("NestedPrintDelegate", "printTopic " + i);
		String topicURL = createURL(topicList[i]);
		if (topicURL != null)
			webBrowser.navigate(backOleObject, topicURL);

	}
}
