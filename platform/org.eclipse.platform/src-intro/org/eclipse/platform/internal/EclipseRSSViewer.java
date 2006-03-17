/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.platform.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;
import org.osgi.framework.Bundle;

public class EclipseRSSViewer implements IIntroContentProvider {
	private static final String NEWS_URL = "http://www.eclipse.org/home/eclipsenews.rss"; //$NON-NLS-1$
	private static final String HREF_BULLET = "bullet"; //$NON-NLS-1$

	private IIntroContentProviderSite site;

	private boolean disposed;

	private String id;

	private FormToolkit toolkit;

	private Composite parent;

	private Image bulletImage;

	private ArrayList items;

	private FormText formText;

	class NewsItem {
		String label;

		String url;

		public NewsItem(String label, String url) {
			this.label = label;
			this.url = url;
		}
	}

	class NewsFeed implements Runnable {
		public void run() {
			// Fake
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			// important: don't do the work if the
			// part gets disposed in the process
			if (disposed)
				return;
			createNewsItems();
			if (disposed)
				return;
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					if (parent != null) {
						// we must recreate the content
						// for SWT because we will use
						// a gentle incremental reflow.
						// HTML reflow will simply reload the page.
						createContent(id, parent, toolkit);
						reflow(formText);
					}
					site.reflow(EclipseRSSViewer.this, true);
				}
			});
		}
	}

	public void init(IIntroContentProviderSite site) {
		this.site = site;
		Thread newsWorker = new Thread(new NewsFeed());
		newsWorker.start();
	}

	public void createContent(String id, PrintWriter out) {
		if (disposed)
			return;
		this.id = id;
		if (items == null) {
			out.print("<p class=\"status-text\">"); //$NON-NLS-1$
			out.print(Messages.getString("EclipseRSSViewer_loading")); //$NON-NLS-1$
			out.println("</p>"); //$NON-NLS-1$
		} else {
			if (items.size() > 0) {
				out.println("<ul id=\"eclipse-news\">"); //$NON-NLS-1$
				for (int i = 0; i < items.size(); i++) {
					NewsItem item = (NewsItem) items.get(i);
					out.print("<li>"); //$NON-NLS-1$
					out.print("<a class=\"topicList\" href=\""); //$NON-NLS-1$
					out.print(item.url);
					out.print("\">"); //$NON-NLS-1$
					out.print(item.label);
					out.print("</a>"); //$NON-NLS-1$
					out.print("</li>"); //$NON-NLS-1$
				}
			} else {
				out.print("<p class=\"status-text\">"); //$NON-NLS-1$
				out.print(Messages.getString("EclipseRSSViewer_noNews")); //$NON-NLS-1$
				out.println("</p>"); //$NON-NLS-1$
			}
			out.println("</ul>"); //$NON-NLS-1$
		}
	}

	public void createContent(String id, Composite parent, FormToolkit toolkit) {
		if (disposed)
			return;
		if (formText == null) {
			// a one-time pass
			formText = toolkit.createFormText(parent, true);
			formText.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					doNavigate((String) e.getHref());
				}
			});
			bulletImage = createImage(new Path("intro/css/graphics/arrow.gif")); //$NON-NLS-1$
			if (bulletImage!=null)
				formText.setImage(HREF_BULLET, bulletImage);
			this.parent = parent;
			this.toolkit = toolkit;
			this.id = id;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<form>"); //$NON-NLS-1$
		if (items == null) {
			buffer.append("<p>"); //$NON-NLS-1$
			buffer.append(Messages.getString("EclipseRSSViewer_loading")); //$NON-NLS-1$
			buffer.append("</p>"); //$NON-NLS-1$
		} else {
			if (items.size() > 0) {
				for (int i = 0; i < items.size(); i++) {
					NewsItem item = (NewsItem) items.get(i);
					buffer.append("<li style=\"image\" value=\""); //$NON-NLS-1$
					buffer.append(HREF_BULLET);
					buffer.append("\">"); //$NON-NLS-1$
					buffer.append("<a href=\""); //$NON-NLS-1$
					buffer.append(item.url);
					buffer.append("\">"); //$NON-NLS-1$
					buffer.append(item.label);
					buffer.append("</a>"); //$NON-NLS-1$
					buffer.append("</li>"); //$NON-NLS-1$
				}
			} else {
				buffer.append("<p>"); //$NON-NLS-1$
				buffer.append(Messages.getString("EclipseRSSViewer_noNews")); //$NON-NLS-1$
				buffer.append("</p>"); //$NON-NLS-1$
			}
		}
		buffer.append("</form>"); //$NON-NLS-1$
		formText.setText(buffer.toString(), true, false);
	}

	private Image createImage(IPath path) {
		Bundle bundle = Platform.getBundle("org.eclipse.platform"); //$NON-NLS-1$
		URL url = FileLocator.find(bundle, path, null);
		try {
			url = FileLocator.toFileURL(url);
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			return desc.createImage();
		} catch (IOException e) {
			return null;
		}
	}

	private void doNavigate(final String url) {
		BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(),
				new Runnable() {
					public void run() {
						IIntroURL introUrl = IntroURLFactory
								.createIntroURL(url);
						if (introUrl != null) {
							// execute the action embedded in the IntroURL
							introUrl.execute();
							return;
						}
						// delegate to the browser support
						openBrowser(url);
					}
				});
	}

	private void openBrowser(String href) {
		try {
			URL url = new URL(href);
			IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
					.getBrowserSupport();
			support.getExternalBrowser().openURL(url);
		} catch (PartInitException e) {
		} catch (MalformedURLException e) {
		}
	}

	private void createNewsItems() {
		// Fake content here; replace by opening the
		// RSS stream, parsing it and creating the items from it
		items = new ArrayList();
		// It is important to create the array list. After this,
		// if you cannot connnect to the RSS feed or fails for
		// any reason, don't add news items and the 'no news'
		// message will be presented.

		synchronized (items) {
			items.add(new NewsItem("Callisto M5 Build available for download", //$NON-NLS-1$
					"http://www.eclipse.org/projects/callisto.php#Installing")); //$NON-NLS-1$
			items
					.add(new NewsItem("Eclipse Board Election Results", //$NON-NLS-1$
							"http://www.eclipse.org/org/press-release/20060309cb_elections.php")); //$NON-NLS-1$
			items
					.add(new NewsItem(
							"Eclipse Corner Article published: Teach Your Eclipse to Speak the Local Lingo", //$NON-NLS-1$
							"http://www.eclipse.org/articles/Article-Speak-The-Local-Language/article.html")); //$NON-NLS-1$			
			items
					.add(new NewsItem(
							"Voting closes today 4 p.m. EST in the Eclipse Foundation Elections", //$NON-NLS-1$
							"http://www.eclipse.org/org/elections/")); //$NON-NLS-1$			
		}
	}

	/*
	 * This method is copied from Section and seems useful in general. Perhaps
	 * we should move it into content provider site, something like
	 * 'reflow(Control startingControl)'
	 */

	private void reflow(Control initiator) {
		Control c = initiator;
		while (c != null) {
			c.setRedraw(false);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				break;
			}
		}
		c = initiator;
		while (c != null) {
			if (c instanceof Composite)
				((Composite) c).layout(true);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				((ScrolledForm) c).reflow(true);
				break;
			}
		}
		c = initiator;
		while (c != null) {
			c.setRedraw(true);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				break;
			}
		}
	}

	public void dispose() {
		if (bulletImage != null) {
			bulletImage.dispose();
			bulletImage = null;
		}
		disposed = true;
	}
}