/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;

/**
 * A Web browser viewer.
 */
public class WebBrowserView extends ViewPart implements
		IBrowserViewerContainer, ISetSelectionTarget {
	public static final String WEB_BROWSER_VIEW_ID = "org.eclipse.ui.browser.view"; //$NON-NLS-1$

	protected BrowserViewer viewer;

	protected ISelectionListener listener;

	@Override
	public void createPartControl(Composite parent) {
		int style = WebBrowserUtil.decodeStyle(getViewSite().getSecondaryId());
		viewer = new BrowserViewer(parent, style);
		viewer.setContainer(this);

		initDragAndDrop();
	}

	@Override
	public void dispose() {
		if (viewer!=null)
			viewer.setContainer(null);
		if (listener != null)
			removeSelectionListener();
	}

	public void setURL(String url) {
		if (viewer != null)
			viewer.setURL(url);
	}

	@Override
	public void setFocus() {
		viewer.setFocus();
	}

	@Override
	public boolean close() {
		try {
			getSite().getPage().hideView(this);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public IActionBars getActionBars() {
		return getViewSite().getActionBars();
	}

	@Override
	public void openInExternalBrowser(String url) {
		try {
			URL theURL = new URL(url);
			IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
					.getBrowserSupport();
			support.getExternalBrowser().openURL(theURL);
		} catch (MalformedURLException e) {
			// TODO handle this
		} catch (PartInitException e) {
			// TODO handle this
		}
	}

	public void addSelectionListener() {
		if (listener != null)
			return;

		listener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				onSelectionChange(selection);
			}
		};
		getSite().getWorkbenchWindow().getSelectionService()
				.addPostSelectionListener(listener);
	}

	private void onSelectionChange(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return;
		IStructuredSelection sel = (IStructuredSelection) selection;
		Object obj = sel.getFirstElement();
		URL url = getURLFrom(obj);
		if (url != null)
			setURL(url.toExternalForm());
	}

	private URL getURLFrom(Object adapt) {
		// test for path
		IPath path = Adapters.getAdapter(adapt, IPath.class, true);
		if (path != null) {
			File file = path.toFile();
			if (file.exists() && isWebFile(file.getName()))
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e) {
					return null;
				}
		}
		return Adapters.getAdapter(adapt, URL.class, true);
	}

	public void removeSelectionListener() {
		if (listener == null)
			return;
		getSite().getWorkbenchWindow().getSelectionService()
				.removePostSelectionListener(listener);
		listener = null;
	}

	/**
	 * Return true if the filename has a "web" extension.
	 *
	 * @param name
	 * @return
	 */
	protected boolean isWebFile(String name) {
		return name.endsWith("html") || name.endsWith("htm") || name.endsWith("gif") || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				name.endsWith("jpg"); //$NON-NLS-1$
	}

	/**
	 * Adds drag and drop support to the view.
	 */
	protected void initDragAndDrop() {
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };

		DropTarget dropTarget = new DropTarget(viewer, DND.DROP_COPY | DND.DROP_DEFAULT);
		dropTarget.setTransfer(transfers);
		dropTarget.addDropListener(new WebBrowserViewDropAdapter(viewer));
	}

	@Override
	public void selectReveal(ISelection selection) {
		onSelectionChange(selection);
	}

	public void setBrowserViewName(String name) {
		setPartName(name);
	}

	public void setBrowserViewTooltip(String tip) {
		setTitleToolTip(tip);
	}
}
