package org.eclipse.help.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
import org.eclipse.help.internal.ui.util.*;

/**
 * Help viewer based on the IE5 ActiveX component.
 */
public class HTMLHelpViewer implements ISelectionChangedListener {
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
		//Object in= getInput();
		Contribution url = null;
		//if (in instanceof Contribution)
		//url = (Contribution)in;

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
			throw new HelpWorkbenchException(e.getLocalizedMessage());

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
		if (input instanceof Topic) {
			Topic topicElement = (Topic) input;
			String url = topicElement.getHref();
			if (url == null || url.equals(""))
				return; // no content in this topic
			if (url.indexOf("?resultof=") != -1) {
				Locale locale = Locale.getDefault();
				url = url.concat("&lang=") + locale.getDefault().toString();
			} else {
				Locale locale = Locale.getDefault();
				url = url.concat("?lang=") + locale.getDefault().toString();
			}
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
					return; // no content in this topic
				Locale locale = Locale.getDefault();
				url = url.concat("?lang=") + locale.getDefault().toString();
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
