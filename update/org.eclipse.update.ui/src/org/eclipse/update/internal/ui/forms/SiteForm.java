/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.forms;

import java.net.*;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.ISiteAdapter;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.internal.ui.pages.UpdateFormPage;
import org.eclipse.update.internal.ui.views.DetailsView;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.FormEngine;

public class SiteForm extends UpdateWebForm {
	private static final String KEY_DESC = "SitePage.desc";
	private static final String KEY_LINK = "SitePage.link";
	private static final String KEY_WDESC = "SitePage.wdesc";
	private static final String KEY_WLINK = "SitePage.wlink";
	private Label url;
	private FormEngine desc;
	private SelectableFormLabel link;
	private ISiteAdapter currentAdapter;
	private Image updateSitesImage;
	private static final String KEY_UPDATE_SITES_IMAGE = "updateSites";

	public SiteForm(UpdateFormPage page) {
		super(page);
		updateSitesImage = UpdateUIImages.DESC_SITES_VIEW.createImage();
	}

	public void dispose() {
		updateSitesImage.dispose();
		super.dispose();
	}

	public void initialize(Object modelObject) {
		setHeadingText("");
		super.initialize(modelObject);
		//((Composite)getControl()).layout(true);
	}

	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = layout.rightMargin = 10;
		layout.topMargin = 10;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 20;
		layout.numColumns = 1;
		boolean webSiteFlag = computeWebSiteFlag(currentAdapter);

		FormWidgetFactory factory = getFactory();
		url = factory.createHeadingLabel(parent, null);

		desc = factory.createFormEngine(parent);
		setFocusControl(desc);
		desc.registerTextObject(KEY_UPDATE_SITES_IMAGE, updateSitesImage);
		String text =
			UpdateUI.getString(
				webSiteFlag ? KEY_WDESC : KEY_DESC);
		desc.load(text, true, true);
		TableData td = new TableData();
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		desc.setLayoutData(td);

		IHyperlinkListener listener;
		IActionBars bars = getPage().getView().getViewSite().getActionBars();
		final IStatusLineManager manager = bars.getStatusLineManager();

		listener = new HyperlinkAdapter() {
			public void linkEntered(Control link) {
				URL currentURL = getCurrentURL(true);
				if (currentURL != null)
					manager.setMessage(currentURL.toString());
			}
			public void linkExited(Control link) {
				manager.setMessage(null);
			}
			public void linkActivated(Control link) {
				final URL currentURL = getCurrentURL(false);
				if (currentURL == null)
					return;
				BusyIndicator
					.showWhile(getControl().getDisplay(), new Runnable() {
					public void run() {
						DetailsView.showURL(currentURL.toString());
					}
				});
			}
		};
		link = new SelectableFormLabel(parent, SWT.NULL);
		link.setText(
			UpdateUI.getString(
				webSiteFlag ? KEY_WLINK : KEY_LINK));
		factory.turnIntoHyperlink(link, listener);
		WorkbenchHelp.setHelp(parent, "org.eclipse.update.ui.SiteForm");
	}

	public void expandTo(Object obj) {
		if (obj instanceof ISiteAdapter) {
			inputChanged((ISiteAdapter) obj);
		}
	}

	private void inputChanged(ISiteAdapter adapter) {
		boolean oldWebSiteFlag = computeWebSiteFlag(currentAdapter);
		boolean newWebSiteFlag = computeWebSiteFlag(adapter);

		if (oldWebSiteFlag != newWebSiteFlag) {
			String text =
				UpdateUI.getString(
					newWebSiteFlag ? KEY_WDESC : KEY_DESC);
			desc.load(text, true, true);
			link.setText(
				UpdateUI.getString(
					newWebSiteFlag ? KEY_WLINK : KEY_LINK));
		}
		setHeadingText(adapter.getLabel());
		url.setText(adapter.getURL().toString());
		updateLinkVisibility(adapter);
		url.getParent().layout();
		((Composite) getControl()).layout();
		updateSize();
		getControl().redraw();
		currentAdapter = adapter;
	}

	private URL getCurrentURL(boolean rawURL) {
		if (currentAdapter == null)
			return null;
		boolean webSite = computeWebSiteFlag(currentAdapter);
		if (webSite)
			return currentAdapter.getURL();
		else {
			ISite site = currentAdapter.getSite(null);
			if (site == null)
				return null;
			if (rawURL)
				return getRawURL(site);
			else
				return getURLforSite(site);
		}
	}
	public URL getURLforSite(ISite site) {
		URL link = getRawURL(site);
		return link;
		/*
		String callback = WebInstallHandler.getCallbackString();
		if (callback == null)
			return link;
		try {
			return new URL(link.toExternalForm() + callback);
		} catch (MalformedURLException e) {
			return link;
		}
		*/
	}

	private boolean computeWebSiteFlag(ISiteAdapter adapter) {
		return (
			adapter instanceof SiteBookmark
				&& ((SiteBookmark) adapter).isWebBookmark())
			? true
			: false;
	}

	public void objectChanged(Object object, String property) {
		if (object.equals(currentAdapter)) {
			inputChanged(currentAdapter);
		}
	}

	public URL getRawURL(ISite site) {
		IURLEntry entry = site.getDescription();
		if (entry == null)
			return null;
		return entry.getURL();
	}

	private void updateLinkVisibility(ISiteAdapter adapter) {
		/*
		ISite site = adapter.getSite();
		URL infoURL = getRawURL(site);
		link.setVisible(infoURL!=null);
		*/
	}
}
