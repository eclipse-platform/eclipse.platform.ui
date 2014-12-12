/*******************************************************************************
 * Copyright (c) 2014-2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nested;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Hide folders when they are actually nested projects (folder is shown as project instead
 */
public class HideFolderWhenProjectIsShownAsNested extends ViewerFilter {

	public static final String EXTENTSION_ID = "org.eclipse.ui.navigator.resources.nested.HideFolderWhenProjectIsShownAsNested"; //$NON-NLS-1$

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		CommonViewer commonViewer = (CommonViewer)viewer;
		if (commonViewer.getNavigatorContentService().getActivationService().isNavigatorExtensionActive(NestedProjectsContentProvider.EXTENSION_ID)) {
			if (element instanceof IFolder) {
				if (NestedProjectManager.isShownAsProject((IFolder)element)) {
					return false;
				}
			}
		}
		return true;
	}

}
