package org.eclipse.ui.internal.intro.shared;

import java.io.PrintWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;


public class RootContentProvider implements IIntroContentProvider {

	public void init(IIntroContentProviderSite site) {
	}

	public void createContent(String id, PrintWriter out) {
		out.println("<table>");
		out.println("<tr>");
		out.println("<td><a>Overview</a></td>");
		out.println("<td><a>Tutorials</a></td>");
		out.println("<td><a>Samples</a></td>");
		out.println("<td><a>News</a></td>");
		out.println("</tr></table>");
	}

	public void createContent(String id, Composite parent, FormToolkit toolkit) {
		Composite c = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		c.setLayout(layout);
		toolkit.createHyperlink(c, "Overview", SWT.WRAP);
		toolkit.createHyperlink(c, "Tutorials", SWT.WRAP);
		toolkit.createHyperlink(c, "Samples", SWT.WRAP);
		toolkit.createHyperlink(c, "News", SWT.WRAP);
	}

	public void dispose() {
	}
}