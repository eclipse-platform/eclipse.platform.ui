/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.progress.UIJob;

/**
 * <p>
 * Provides a consistent mechanism to interact with StructuredViewers over time. The Common
 * Navigator framework attempts to defer the loading of extensions, which also means defering the
 * loading of Content Providers. To follow the contracts already in place by
 * {@link org.eclipse.jface.viewers.ITreeContentProvider}, the Viewer, Old Input, and New Input
 * parameters for
 * {@link org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)}
 * are cached for content providers that have not been loaded yet.
 * </p>
 * <p>
 * <b>WARNING: </b> The following class is not inherently thread-safe. Appropriate measures should
 * be taken to ensure that {@link #inputChanged(Object, Object)}and
 * {@link #inputChanged(Viewer, Object, Object)}are not called concurrently with
 * {@link #initialize(IStructuredContentProvider)}.
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class StructuredViewerManager {

	private Viewer viewer;
	private Object cachedOldInput;
	private Object cachedNewInput;

	/**
	 * 
	 */
	public StructuredViewerManager(Viewer aViewer) {
		super();
		viewer= aViewer;
	}

	public Viewer getViewer() {
		return viewer;
	}


	public void inputChanged(Object anOldInput, Object aNewInput) {
		cachedOldInput= anOldInput;
		cachedNewInput= aNewInput;
	}

	public void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput) {
		viewer= aViewer;
		cachedOldInput= anOldInput;
		cachedNewInput= aNewInput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.IInitializationManager#initialize(org.eclipse.jface.viewers.IStructuredContentProvider)
	 */
	public boolean initialize(IStructuredContentProvider aContentProvider) {
		boolean result= true;
		try {
			if (aContentProvider != null)
				aContentProvider.inputChanged(viewer, cachedOldInput, cachedNewInput);
		} catch (RuntimeException e) {
			NavigatorPlugin.log(e.toString(), new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, 0, e.toString(), e));
			result= false;
		}
		return result;
	}

	/**
	 * 
	 */
	public void safeRefresh() {
		UIJob refreshJob= new UIJob(CommonNavigatorMessages.StructuredViewerManager_0) {
			public IStatus runInUIThread(org.eclipse.core.runtime.IProgressMonitor monitor) {
				try {
					if (viewer != null)
						viewer.refresh();
				} catch (RuntimeException e) {
					NavigatorPlugin.log(e.toString(), new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, 0, e.toString(), e));
				}
				return Status.OK_STATUS;
			}
		};
		refreshJob.schedule();


	}

}
