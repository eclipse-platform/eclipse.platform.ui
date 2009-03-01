/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.progress.UIJob;

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

	private Viewer viewer;

	private Object cachedOldInput;

	private Object cachedNewInput;
	
	private UIJob refreshJob = new UIJob(
			CommonNavigatorMessages.StructuredViewerManager_0) {
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if(viewer != null) {
				try {
					if (viewer.getControl().isDisposed()) {
						return Status.OK_STATUS;
					}					
	
					
					Display display = viewer.getControl().getDisplay();
					if (!display.isDisposed() && viewer != null) {
						try {
							viewer.getControl().setRedraw(false);
							viewer.refresh();
						} finally {
							viewer.getControl().setRedraw(true);
						}
						 
					}
				} catch (RuntimeException e) {
					NavigatorPlugin.logError(0, e.toString(), e);
				}
			}
			return Status.OK_STATUS;
		}
	};

	/**
	 * 
	 * @param aViewer
	 */
	public StructuredViewerManager(Viewer aViewer) {
		super();
		viewer = aViewer;
		refreshJob.setSystem(true);
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
		viewer = aViewer;
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
		refreshJob.schedule(10);

	}

}
