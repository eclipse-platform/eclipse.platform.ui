package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.Logger;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.topics.ITopic;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
/**
 * Handles printing of topics tree
 */
public class TopicTreePrinter
	implements IDocumentCompleteListener, IRunnableWithProgress {
	private IBrowser printBrowser;
	private ITopic rootTopic;
	private ArrayList hrefList;
	private ArrayList labelList;
	private int currentTopic;
	private File tocFile;
	public static boolean busy = false;
	private IProgressMonitor pMonitor;
	/**
	 * @param selection - IStructuredSelection containing
	 * ITopic, which is a root of the tree to be printed
	 */
	public TopicTreePrinter(ITopic rootTopic) {
		this.rootTopic = rootTopic;
		printBrowser = createPrintBrowser();
		if (printBrowser == null) {
			// encountered problems creating print browser
			Logger.logError(WorkbenchResources.getString("WE006"), null);
			return;
		}
	}
	/*
	 * Creates web browser used for printing. 
	 * @return instance of IBrowser or null if error occured
	 */
	private IBrowser createPrintBrowser() {
		try { // get the active view part to be able to get to the Composite
			// and create a Browser for printing
			IWorkbenchPage activePage =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart activeViewPart = activePage.findView(EmbeddedHelpView.ID);
			Composite browserParent = null;
			if (!(activeViewPart instanceof EmbeddedHelpView)) {
				// can not get to EmbeddedHelpView. Do nothing.
				Logger.logError(WorkbenchResources.getString("WE006"), null);
				return null;
			}
			browserParent = ((EmbeddedHelpView) activeViewPart).getViewComposite();
			String factoryClass = "org.eclipse.help.internal.ui.win32.BrowserFactory";
			Class classObject = Class.forName(factoryClass);
			IBrowserFactory factory = (IBrowserFactory) classObject.newInstance();
			// this could throw a HelpDesktopException
			IBrowser webBrowser = factory.createBrowser(browserParent);
			browserParent.layout();
			return webBrowser;
		} catch (Exception e) {
			Logger.logError(WorkbenchResources.getString("WE006"), e);
			return null;
		}
	}
	/**
	 * Start printing process
	 */
	public void print() {
		if (printBrowser == null)
			return;
		busy = true;
		printBrowser.addDocumentCompleteListener(this);
		buildPrintList(rootTopic);
		currentTopic = 0;
		if (okToPrint(hrefList.size() - 1)) {
			ProgressMonitorDialog pMonDialog =
				new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
			try {
				pMonDialog.run(true, true, this);
			} catch (Exception e) {
				Logger.logError(WorkbenchResources.getString("WE020"),e);
			}
		} else {
			endPrinting();
		}
	}
	/*
	 * @see IDocumentCompleteListener#documentComplete()
	 */
	public void documentComplete(String url) {
		if (pMonitor == null || pMonitor.isCanceled()) {
			endPrinting();
			return;
		}
		// check if loaded frame loaded corresponds to our topic
		if (!currentTopicLoaded(url))
			return;
		pMonitor.subTask(
			WorkbenchResources.getString(
				"printing_topic",
				(String) labelList.get(currentTopic)));
		printBrowser.print(false);
		if (tocFile != null){
			tocFile.delete();
			tocFile=null;
		}
		pMonitor.worked(1);
		currentTopic++;
		if (currentTopic < hrefList.size())
			printBrowser.navigate((String) hrefList.get(currentTopic));
		else
			endPrinting();
	}
	/*
	 * Does cleanup
	 */
	private void endPrinting() {
		if (tocFile != null)
			tocFile.delete();
		printBrowser.removeDocumentCompleteListener(this);
		pMonitor = null;
		(printBrowser).dispose();
		busy = false;
	}
	/**
	 * Creates list of hrefs to print
	 */
	private void buildPrintList(ITopic rootTopic) {
		hrefList = new ArrayList();
		labelList = new ArrayList();
		buildTopicList(rootTopic);
		createTOC(rootTopic);
		if (tocFile != null)
			hrefList.add(0, tocFile.getAbsolutePath());
		labelList.add(0, WorkbenchResources.getString("toc"));
	}
	/**
	 * Adds hrefs of this topic and its children to topicHrefList
	 * and corresponding labels to topicLabelList
	 */
	private void buildTopicList(ITopic rootTopic) {
		if ((rootTopic.getHref() != null) && (!"".equals(rootTopic.getHref())))
			hrefList.add(qualifyTopicURL(rootTopic.getHref()));
		labelList.add(rootTopic.getLabel());
		ITopic topics[] = rootTopic.getSubtopics();
		for (int i = 0; i < topics.length; i++) {
			buildTopicList(topics[i]);
		}
	}
	/**
	 * Corrects topic's url for use by the browser
	 */
	private String qualifyTopicURL(String url) {
		if (url.indexOf("http:") == 0) {
			// external url
			return url;
		}
		url = HelpSystem.getLocalHelpServerURL() + url;
		if (url.indexOf("?resultof=") != -1) {
			Locale locale = Locale.getDefault();
			url = url.concat("&lang=") + locale.getDefault().toString();
		} else {
			Locale locale = Locale.getDefault();
			url = url.concat("?lang=") + locale.getDefault().toString();
		}
		return url;
	}
	/**
	 * Creates table of contents in a temporary file tocFile
	 */
	private void createTOC(ITopic rootTopic) {
		try {
			File dir = WorkbenchHelpPlugin.getDefault().getStateLocation().toFile();
			File file = File.createTempFile("toc", ".html", dir);
			PrintWriter writer =
				new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8")),
					false /* no aotoFlush */
			);
			writer.println("<html>");
			writer.println("<head>");
			// officially, a HEAD element needs a TITLE. fake it.
			writer.println(
				"<title>" + WorkbenchResources.getString("Table_Of_Contents") + "</title>");
			// make sure that we have everything in UTF-8 because this is
			// what this string buffer will converted to.
			writer.println("<META http-equiv=\"Content-Type\" ");
			writer.println("content=\"text/html; charset=utf-8\">");
			// set Expires to any old date to avoid caching by IE.
			// HTTP servers sometimes return this info as part of the
			// respone.  
			writer.println("<META HTTP-EQUIV=\"Expires\" ");
			writer.println("CONTENT=\"Mon, 04 Dec 2000 11:11:11 GMT\"> ");
			writer.println("</head>");
			writer.println("<body>");
			writer.println("<h1 ALIGN=CENTER>");
			writer.println(WorkbenchResources.getString("Table_Of_Contents"));
			writer.println("</h1>");
			addTopicToTOC(writer, rootTopic);
			writer.println("</ul>");
			writer.println("</body>");
			writer.println("</html>");
			writer.flush();
			writer.close();
			tocFile = file;
		} catch (IOException ioe) {
		}
	}
	private void addTopicToTOC(PrintWriter writer, ITopic topic) {
		writer.println("<ul>");
		writer.println("<li>");
		writer.println(topic.getLabel());
		ITopic topics[] = topic.getSubtopics();
		for (int i = 0; i < topics.length; i++) {
			addTopicToTOC(writer, topics[i]);
		}
		writer.println("</li>");
		writer.println("</ul>");
	}
	/** 
	 * Confirms with the user if they are O.K to print x separate docs.
	 */
	private boolean okToPrint(int numberOfTopics) {
		String topics = Integer.toString(numberOfTopics);
		if (numberOfTopics == 0) {
			String msg = WorkbenchResources.getString("no_Topics_To_Print");
			ErrorUtil.displayInfoDialog(msg);
			return false;
		} else {
			String question = WorkbenchResources.getString("ok_To_Print", topics);
			return ErrorUtil.displayQuestionDialog(question);
		}
	}
	/*
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)
		throws InvocationTargetException, InterruptedException {
		if (monitor == null)
			return;
		this.pMonitor = monitor;
		monitor.beginTask(
			WorkbenchResources.getString("printing_Topic_Tree"),
			hrefList.size());
		printBrowser.navigate((String) hrefList.get(currentTopic));
		if (monitor.isCanceled())
			return;
		while (busy) {
			try {
				Thread.currentThread().sleep(200);
			} catch (InterruptedException e) {
			}
		}
	}
	/** 
	 * Checks if url corresponds to a topic
	 * that needs to be printed next
	 */
	private boolean currentTopicLoaded(String url) {
		String topicURL = (String) hrefList.get(currentTopic);
		// ignore case, IE changes casing of drive letter
		url = url.toLowerCase();
		topicURL = topicURL.toLowerCase();
		// it may append "/" at the end of path if no file specified
		if (topicURL.equals(url) || (topicURL + "/").equals(url))
			return true;
		return false;
	}
}