/*
 * Created on Jun 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.actions;

import java.io.*;
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
public class TipsAndTricksContentProvider implements IFormContentProvider {
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
		writer.close();
		return swriter.toString();
	}

	private void addFeatureLink(PrintWriter writer, AboutInfo feature) {
		String name = feature.getFeatureLabel();
		String href = feature.getTipsAndTricksHref();
		if (href==null) return;
		writer.print("<li>");
		writer.print("<a href=\"help\" arg=\""+href+"\">"+name+"</a>");
		writer.println("</li>");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.welcome.internal.portal.IFormContentProvider#setContentObserver(org.eclipse.welcome.internal.portal.IFormContentObserver)
	 */
	public void setContentObserver(IFormContentObserver observer) {
		this.observer = observer;
	}
}
