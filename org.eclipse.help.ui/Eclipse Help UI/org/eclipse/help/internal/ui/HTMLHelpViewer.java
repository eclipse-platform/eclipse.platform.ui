package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import java.net.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.server.PluginURL;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.util.Resources;
import org.eclipse.help.internal.util.TString;

/**
 * Help viewer based on the IE5 ActiveX component.
 */
public class HTMLHelpViewer implements ISelectionChangedListener {
	private final static String defaultSplash = 
		PluginURL.getPrefix()+"/org.eclipse.help/" + Resources.getString("splash_location");
	private IBrowser webBrowser;
	/**
	 * HelpViewer constructor comment.
	 */
	public HTMLHelpViewer(Composite parent) throws HelpWorkbenchException {
		super();
		createControl(parent);
	}
	/**
	 */
	protected Control createControl(Composite parent)
		throws HelpWorkbenchException {

		Contribution url = null;

		String factoryClass = "org.eclipse.help.internal.ui.win32.BrowserFactory";
		try {
			if (!System.getProperty("os.name").startsWith("Win"))
				factoryClass =
					factoryClass = "org.eclipse.help.internal.ui.motif.BrowserFactory";

			Class c = Class.forName(factoryClass);
			IBrowserFactory factory = (IBrowserFactory) c.newInstance();

			// this could throw a HelpDesktopException
			webBrowser = factory.createBrowser(parent);
			/*
			WorkbenchHelp.setHelp(
				webBrowser.getControl(),
				new String[] {
					IHelpUIConstants.BROWSER,
					IHelpUIConstants.EMBEDDED_HELP_VIEW});
			*/

			return webBrowser.getControl();
		} catch (HelpWorkbenchException e) {
			// delegate to calling class
			throw e;

		} catch (Exception e) {
			// worst case scenario. Should never be here!
			throw new HelpWorkbenchException(WorkbenchResources.getString("WE001"));

		}
	}
	public Control getControl() {
		if (webBrowser != null)
			return webBrowser.getControl();
		else
			return null;
	}
	/**
	 * @return org.eclipse.help.internal.ui.IBrowser
	 */
	public IBrowser getWebBrowser() {
		return webBrowser;
	}
	/**
	 * @private
	 */
	protected void navigate(Object input) {
		if (input == null || webBrowser == null)
			return;
		
		// use the client locale to load the correct document
		String locale = Locale.getDefault().toString();
		
		if (input instanceof Topic) {
			Topic topicElement = (Topic) input;
			String url = topicElement.getHref();
			if (url == null || url.equals(""))
				return; // no content in this topic
			// Check for fragments
			int fragmentIndex = url.indexOf('#');
			String fragment = null;
			if (fragmentIndex != -1)
			{
				fragment = url.substring(fragmentIndex);
				url = url.substring(0, fragmentIndex);
			}
			
			if (url.indexOf("?resultof=") != -1) 
				url = url+"&lang=" + locale;
			else 
				url = url + "?lang=" + locale;
			
			if (fragment != null)
				url = url + fragment;
				
			if (url.indexOf("http:") == -1) {
				try {
					url = (new URL(HelpSystem.getLocalHelpServerURL(), url)).toExternalForm();
				} catch (MalformedURLException mue) {
				}
			}
			webBrowser.navigate(url);
		} else
			if (input instanceof InfoSet) {
				InfoSet infoset = (InfoSet) input;
				String url = infoset.getHref();
				if (url == null || url.equals(""))
					url = defaultSplash 
					    + "?title="+URLEncoder.encode(TString.getUnicodeNumbers(infoset.getLabel()))
					    + "&lang=" + locale;
				else
					url = url+ "?lang=" + locale;
					
				if (url.indexOf("http:") == -1) {
					try {
						url = (new URL(HelpSystem.getLocalHelpServerURL(), url)).toExternalForm();
					} catch (MalformedURLException mue) {
					}
				}
				webBrowser.navigate(url);
			}
	}
	/**
	 * Notifies that the selection has changed.
	 *
	 * @param event event object describing the change
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			navigate(((IStructuredSelection) selection).getFirstElement());
		}
	}
}
