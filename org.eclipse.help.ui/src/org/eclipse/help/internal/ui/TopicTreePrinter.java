package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.internal.ui.win32.WebBrowser;
import org.eclipse.ui.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.viewers.*;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.topics.*;
import org.eclipse.jface.action.Action;
import org.eclipse.help.internal.ui.*;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import java.util.*;
import org.eclipse.help.internal.*;
import org.eclipse.core.runtime.*;
import java.io.*;
import org.eclipse.help.internal.ui.win32.*;
/**
 * Handles printing of topics tree
 */
public class TopicTreePrinter implements IDocumentCompleteListener {
	private IBrowser printBrowser;
	private ITopic rootTopic;
	private List topicList;
	private int currentTopic;
	private File tocFile;
	public static boolean busy=false;
	/**
	 * @param selection - IStructuredSelection containing
	 * ITopic, which is a root of the tree to be printed
	 */
	public TopicTreePrinter(ITopic rootTopic) {
		this.rootTopic=rootTopic;
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
	private IBrowser createPrintBrowser(){
		try{// get the active view part to be able to get to the Composite
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
		}catch(Exception e){
			Logger.logError(WorkbenchResources.getString("WE006"), e);
			return null;
		}
	}
	/**
	 * Start printing process
	 */
	public void print() {
		if(printBrowser==null)
			return;
		busy=true;
		printBrowser.addDocumentCompleteListener(this);
		buildPrintList(rootTopic);
		currentTopic=0;
		if(tocFile!=null){
			if(okToPrint(topicList.size()-1)){
				printBrowser.navigate((String)topicList.get(currentTopic));
			}else{
				endPrinting();
			}
		}else{
			if(okToPrint(topicList.size())){
				printBrowser.navigate(qualifyTopicURL((String)topicList.get(currentTopic)));
			}else{
				endPrinting();
			}
		}
	}
	/*
	 * @see IDocumentCompleteListener#documentComplete()
	 */
	public void documentComplete() {
		printBrowser.print(false);
		currentTopic++;
		if(currentTopic<topicList.size())
			printBrowser.navigate(qualifyTopicURL((String)topicList.get(currentTopic)));
		else
			endPrinting();
	}
	/*
	 * Does cleanup
	 */
	private void endPrinting() {
		printBrowser.removeDocumentCompleteListener(this);
		((WebBrowser)printBrowser).dispose();
		if(tocFile!=null)
			tocFile.delete();
		busy=false;
	}
	/**
	 * Creates list of hrefs to print
	 */
	private void buildPrintList(ITopic rootTopic){
		topicList=new ArrayList();
		buildTopicList(topicList, rootTopic);
		createTOC(rootTopic);
		if(tocFile!=null)
			topicList.add(0, tocFile.getAbsolutePath());
	}
	/**
	 * Adds hrefs of this topic and its children to topicList
	 */
	private List buildTopicList(List topicList, ITopic rootTopic){
		if((rootTopic.getHref()!=null) && (!"".equals(rootTopic.getHref())))
			topicList.add(rootTopic.getHref());
		for(Iterator it=rootTopic.getChildTopics().iterator(); it.hasNext();){
			buildTopicList(topicList, (ITopic)it.next());
		}
		return topicList;
	}
	/**
	 * Corrects topic's url for use by the browser
	 */
	private String qualifyTopicURL(String url){
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
	 * Creates table of contents in a temporary file tocFile
	 */
	private void createTOC(ITopic rootTopic){
		try {
			File dir=WorkbenchHelpPlugin.getDefault().getStateLocation().toFile();
			File file=File.createTempFile("toc", ".html", dir);
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
			tocFile=file;
		} catch (IOException ioe) {
		}

	}
	private void addTopicToTOC(PrintWriter writer, ITopic topic) {
		writer.println("<ul>");
		writer.println("<li>");
		writer.println(topic.getLabel());
		for (Iterator it = topic.getChildTopics().iterator(); it.hasNext();) {
			addTopicToTOC(writer, (ITopic) it.next());
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
}