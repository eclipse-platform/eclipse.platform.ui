/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import java.io.*;
import java.io.InputStream;
import java.net.*;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.*;
import org.eclipse.welcome.internal.WelcomePortal;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PortalSection extends FormSection {
	private SectionDescriptor desc;
	private WelcomePortalForm form;
	private HTTPAction httpAction;

	public PortalSection(SectionDescriptor desc, WelcomePortalForm form) {
		this.desc = desc;
		this.form = form;
		setDescriptionPainted(false);
		setCollapsable(true);
		setHeaderText(desc.getName());
		makeHyperlinkActions();
	}

	private void makeHyperlinkActions() {
		httpAction = new HTTPAction() {
			public void linkActivated(IHyperlinkSegment link) {
				String url = link.getArg();
				if (url != null)
					WelcomePortal.showURL(link.getText(), url);
			}
		};
		IActionBars bars =
			form.getPortal().getEditor().getEditorSite().getActionBars();
		httpAction.setStatusLineManager(bars.getStatusLineManager());
	}
	public Composite createClient(
		Composite parent,
		FormWidgetFactory factory) {
		IConfigurationElement config = desc.getConfig();
		Composite container = factory.createComposite(parent);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.marginWidth = layout.marginHeight = 0;
		IConfigurationElement[] children = config.getChildren();
		for (int i = 0; i < children.length; i++) {
			IConfigurationElement child = children[i];
			if (child.getName().equals("form")) {
				createForm(container, child, factory);
			}
		}
		return container;
	}

	private void createForm(
		final Composite parent,
		IConfigurationElement config,
		FormWidgetFactory factory) {
		final FormEngine engine = factory.createFormEngine(parent);
		engine.setHyperlinkSettings(factory.getHyperlinkHandler());
		GridData gd = new GridData(GridData.FILL_BOTH);
		engine.setLayoutData(gd);
		engine.registerTextObject("url", httpAction);
		String type = config.getAttribute("type");
		if (type==null || type.equals("local")) {
			final String value = config.getValue();
			//parent.getDisplay().asyncExec(new Runnable() {
				//public void run() {
					engine.load("<form>" + value + "</form>", true, false);
					//reflow();
				//}
			//});
		} else if (type.equals("remote")) {
			String urlName = config.getAttribute("url");
			try {
				final URL url = new URL(urlName);
				Runnable loader = new Runnable() {
					public void run() {
						InputStream is=null;
						try {
							is = url.openStream();
							engine.load(is, false);
							parent.getDisplay().asyncExec(new Runnable() {
								public void run() {
									reflow();
								}
							});
						} catch (IOException e) {
							WelcomePortal.logException(e);
						} finally {
							try {
								if (is!=null) is.close();
							} catch (IOException e) {
							}
						}
					}
				};
				Thread t = new Thread(loader);
				t.start();
			} catch (MalformedURLException e) {
				WelcomePortal.logException(e);
			}
		}
	}

	protected void reflow() {
		super.reflow();
		if (form instanceof ScrollableSectionForm)
			 ((ScrollableSectionForm) form).updateScrollBars();
	}
}
