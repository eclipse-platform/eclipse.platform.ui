package org.eclipse.help.internal.ui.win32;
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
/**
 * Handles printing of topics tree
 */
public class TopicTreePrinter implements IDocumentCompleteListener {
	private IBrowser printBrowser;
	private ITopic rootTopic;
	private List topicList;
	private int currentTopic;
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
		printBrowser.navigate(qualifyURL((String)topicList.get(currentTopic)));
	}
	/*
	 * @see IDocumentCompleteListener#documentComplete()
	 */
	public void documentComplete() {
		printBrowser.print(false);
		currentTopic++;
		if(currentTopic<topicList.size())
			printBrowser.navigate(qualifyURL((String)topicList.get(currentTopic)));
		else
			endPrinting();
	}
	/*
	 * Does cleanup
	 */
	private void endPrinting() {
		printBrowser.removeDocumentCompleteListener(this);
		((WebBrowser)printBrowser).dispose();
		if(topicList.size()>0){
			File tocFile=new File((String)topicList.get(0));
			tocFile.delete();
		}
		busy=false;
	}
	/**
	 * Creates list of hrefs to print
	 */
	private void buildPrintList(ITopic rootTopic){
		topicList=new ArrayList();
		buildTopicList(topicList, rootTopic);
		String url=createTableOfContents(rootTopic);
		topicList.add(0, url);
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
	private String qualifyURL(String url){
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
	 * @return URL of table of contents document
	 */
	private String createTableOfContents(ITopic rootTopic){
		IPath tocPath=WorkbenchHelpPlugin.getDefault().getStateLocation().append("toc"+System.currentTimeMillis()+".html");
		String tocFilename=tocPath.toOSString();
		new TableOfContentsGenerator(tocFilename, rootTopic).generate();
		return tocFilename;
	}
}