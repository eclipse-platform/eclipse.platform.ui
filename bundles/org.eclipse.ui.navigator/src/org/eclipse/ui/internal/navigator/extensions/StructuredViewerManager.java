/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewerInternals;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;

/**
 * <p>
 * Provides a consistent mechanism to interact with StructuredViewers over time.
 * The Common Navigator framework attempts to defer the loading of extensions,
 * which also means deferring the loading of Content Providers. To follow the
 * contracts already in place by
 * {@link org.eclipse.jface.viewers.ITreeContentProvider}, the Viewer, Old
 * Input, and New Input parameters for
 * {@link org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)}
 * are cached for content providers that have not been loaded yet.
 * </p>
 * <p>
 * <b>WARNING: </b> The following class is not inherently thread-safe.
 * Appropriate measures should be taken to ensure that
 * {@link #inputChanged(Object, Object)}and
 * {@link #inputChanged(Viewer, Object, Object)}are not called concurrently
 * with {@link #initialize(IStructuredContentProvider)}.
 * 
 * 
 * 
 * @since 3.2
 */
public class StructuredViewerManager {

	private StructuredViewer viewer;

	private Object cachedOldInput;

	private Object cachedNewInput;

	static class StructuredViewerAccess extends StructuredViewerInternals {
		static class Listener implements StructuredViewerInternals.AssociateListener {
			private final NavigatorContentService contentService;
			public Listener(NavigatorContentService contentService) {
				this.contentService = contentService;
			}
			public void associate(Object element, Item item) {
				NavigatorContentDescriptor desc = contentService.getContribution(element);
				if (desc != null)
					item.setData(NavigatorContentService.WIDGET_KEY, desc);
				//System.out.println("associate: " + element + " item: " + item + " desc: " + desc); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			public void disassociate(Item item) {
				item.setData(NavigatorContentService.WIDGET_KEY, null);
				//System.out.println("disassociate:  item: " + item); //$NON-NLS-1$
			}
		}
		protected static void hookAssociateListener(StructuredViewer v, NavigatorContentService contentService) {
			StructuredViewerInternals.setAssociateListener(v, new Listener(contentService));
		}
		protected static Widget[] getItems(StructuredViewer v, Object element) {
			return StructuredViewerInternals.getItems(v, element);
		}
	}
	
	/**
	 * @param element
	 * @return the items
	 */
	public Widget[] getItems(Object element) {
		return StructuredViewerAccess.getItems(viewer, element);
	}

	/**
	 * 
	 * @param aViewer
	 * @param contentService 
	 */
	public StructuredViewerManager(StructuredViewer aViewer, NavigatorContentService contentService) {
		super();
		viewer = aViewer;
		StructuredViewerAccess.hookAssociateListener(viewer, contentService);
	}

	/**
	 * 
	 * @return The real viewer.
	 */
	public Viewer getViewer() {
		return viewer;
	}

	/**
	 * 
	 * @param anOldInput
	 * @param aNewInput
	 */
	public void inputChanged(Object anOldInput, Object aNewInput) {
		cachedOldInput = anOldInput;
		cachedNewInput = aNewInput;
	}

	/**
	 * 
	 * @param aViewer
	 * @param anOldInput
	 * @param aNewInput
	 */
	public void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput) {
		viewer = (StructuredViewer) aViewer;
		cachedOldInput = anOldInput;
		cachedNewInput = aNewInput;
	}

	/**
	 * 
	 * @param aContentProvider
	 * @return True if all is well.
	 */
	public boolean initialize(IStructuredContentProvider aContentProvider) {
		boolean result = true;
		try {
			if (aContentProvider != null) {
				aContentProvider.inputChanged(viewer, cachedOldInput,
						cachedNewInput);
			}
		} catch (RuntimeException e) {
			NavigatorPlugin.logError(0, e.toString(), e);
			result = false;
		}
		return result;
	}

	/**
	 * 
	 */
	public void safeRefresh() {

		final Viewer localViewer = viewer;

		if (localViewer == null || localViewer.getControl().isDisposed())
			return;
		Display display = localViewer.getControl().getDisplay();
		if (display.isDisposed())
			return;
		display.syncExec(new Runnable() {
			public void run() {
				if (localViewer.getControl().isDisposed())
					return;
				try {
					localViewer.getControl().setRedraw(false);
					localViewer.refresh();
				} catch (RuntimeException e) {
					NavigatorPlugin.logError(0, e.toString(), e);
				} finally {
					localViewer.getControl().setRedraw(true);
				}

			}
		});

	}

}
