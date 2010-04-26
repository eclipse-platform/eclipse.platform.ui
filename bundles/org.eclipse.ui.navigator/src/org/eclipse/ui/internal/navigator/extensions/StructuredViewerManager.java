/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewerInternals;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.Policy;

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

	/*
	 * This map is used to associate elements in the viewer with their
	 * associated NavigatorContentDescriptor. To avoid things getting out of
	 * hand, it associates only the items that are actually present in the tree.
	 * We need this association to make sure that we can always get the source
	 * (NavigatorContentDescriptor) of a given element for the case of providing
	 * the label which must use the same navigator content extension that
	 * provided the element.
	 */
	// Map<element, NavigatorContentDescriptor>
	private Map viewerDataMap;
	
	static class StructuredViewerAccess extends StructuredViewerInternals {
		static class Listener implements StructuredViewerInternals.AssociateListener {
			private final NavigatorContentService contentService;
			private final Map viewerDataMap;
			public Listener(NavigatorContentService contentService, Map viewerDataMap) {
				this.contentService = contentService;
				this.viewerDataMap = viewerDataMap;
			}
			public void associate(Object element, Item item) {
				NavigatorContentDescriptor desc = contentService.getContribution(element);
				contentService.forgetContribution(element);
				synchronized (viewerDataMap) {
					if (viewerDataMap.containsKey(element)) {
						if (Policy.DEBUG_VIEWER_MAP)
							System.out.println("associate: SKIPPED " + element + " item: " + item + " desc: " + desc + " FOUND"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						return;
					}
					viewerDataMap.put(element, desc);
					if (Policy.DEBUG_VIEWER_MAP)
						System.out.println("associate: " + element + " item: " + item + " desc: " + desc); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			public void disassociate(Item item) {
				synchronized (viewerDataMap) {
					if (Policy.DEBUG_VIEWER_MAP)
						System.out.println("disassociate:  item: " + item + " object: " + item.getData()); //$NON-NLS-1$ //$NON-NLS-2$
					viewerDataMap.remove(item.getData());
				}
			}

			public void filteredOut(Object element) {
				contentService.forgetContribution(element);
				synchronized (viewerDataMap) {
					if (Policy.DEBUG_VIEWER_MAP)
						System.out.println("filteredOut: object: " + element); //$NON-NLS-1$
					viewerDataMap.remove(element);
				}
			}
		}
		protected static void hookAssociateListener(StructuredViewer v, Map viewerDataMap, NavigatorContentService contentService) {
			StructuredViewerInternals.setAssociateListener(v, new Listener(contentService, viewerDataMap));
		}
	}
	
	/**
	 * @param element
	 * @return the object
	 */
	public Object getData(Object element) {
		synchronized (viewerDataMap) {
			return viewerDataMap.get(element);
		}
	}

	/**
	 * Used when NCEs associated with the viewer are changed.
	 */
	public void resetViewerData() {
		synchronized (viewerDataMap) {
			if (Policy.DEBUG_VIEWER_MAP)
				System.out.println("viewer map RESET"); //$NON-NLS-1$
			viewerDataMap.clear();
		}
	}

	/**
	 * 
	 * @param aViewer
	 * @param contentService 
	 */
	public StructuredViewerManager(StructuredViewer aViewer, NavigatorContentService contentService) {
		super();
		viewer = aViewer;
		viewerDataMap = new HashMap();
		StructuredViewerAccess.hookAssociateListener(viewer, viewerDataMap, contentService);
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
	public boolean initialize(final IStructuredContentProvider aContentProvider) {
		final boolean[] result = new boolean[1];
		SafeRunner.run(new NavigatorSafeRunnable() {
			public void run() throws Exception {
				if (aContentProvider != null) {
					aContentProvider.inputChanged(viewer, cachedOldInput, cachedNewInput);
				}
				result[0] = true;
			}
		});
		return result[0];
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
				SafeRunner.run(new NavigatorSafeRunnable() {
					public void run() throws Exception {
						localViewer.getControl().setRedraw(false);
						localViewer.refresh();
					}
				});
				localViewer.getControl().setRedraw(true);
			}
		});

	}

}
