/*
 * Created on Jun 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.actions;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.ui.internal.*;
import org.eclipse.welcome.internal.WelcomePortal;
import org.eclipse.welcome.internal.portal.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WelcomeContentProvider implements IFormContentProvider {
	private IFormContentObserver observer;

	/* (non-Javadoc)
	 * @see org.eclipse.welcome.internal.portal.IFormContentProvider#getContent()
	 */
	public String getContent() {
		// Ask the user to select a feature
		AboutInfo[] features = ((Workbench) WelcomePortal.getDefault().getWorkbench()).getConfigurationInfo().getFeaturesInfo();
		ArrayList welcomeFeatures = new ArrayList();
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		writer.println("<form>");
		for (int i = 0; i < features.length; i++) {
			AboutInfo feature = features[i];
			if (feature.getWelcomePageURL() != null) {
				addFeatureLink(writer, feature);
			}
		}
		writer.println("</form>");
		/*

		if (perspectiveId != null) {
			try {
				page = (WorkbenchPage) window.getWorkbench().showPerspective(perspectiveId, window);
			} catch (WorkbenchException e) {
				return;
			}
		}

		page.setEditorAreaVisible(true);

		// create input
		WelcomeEditorInput input = new WelcomeEditorInput(feature);

		// see if we already have a welcome editor
		IEditorPart editor = page.findEditor(input);
		if (editor != null) {
			page.activate(editor);
			return;
		}

		try {
			page.openEditor(input, EDITOR_ID);
		} catch (PartInitException e) {
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("QuickStartAction.openEditorException"), e); //$NON-NLS-1$
			ErrorDialog.openError(
				window.getShell(),
				WorkbenchMessages.getString("Workbench.openEditorErrorDialogTitle"),  //$NON-NLS-1$
				WorkbenchMessages.getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
				status);
		}
		*/
		writer.close();
		return swriter.toString();
	}

	private void addFeatureLink(PrintWriter writer, AboutInfo feature) {
		String name = feature.getFeatureLabel();
		URL welcomeURL = feature.getWelcomePageURL();
		String action = "";
		writer.print("<li>");
		writer.print("<a href=\"action\">"+name+"</a>");
		writer.println("</li>");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.welcome.internal.portal.IFormContentProvider#setContentObserver(org.eclipse.welcome.internal.portal.IFormContentObserver)
	 */
	public void setContentObserver(IFormContentObserver observer) {
		this.observer = observer;
	}
}
