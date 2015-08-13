/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.workbench;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

/**
 * Defines a label provider for the title bar in the tabbed properties view.
 *
 * @since 3.2
 */
public class TabbedPropertySheetTitleProvider extends LabelProvider {

	private ILabelProvider labelProvider;

	private IDescriptionProvider descriptionProvider;

	/**
	 * Constructor for CommonNavigatorTitleProvider.
	 */
	public TabbedPropertySheetTitleProvider() {
		super();
		INavigatorContentService contentService = null;

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPart part = window.getActivePage().findView(ProjectExplorer.VIEW_ID);
			if (part != null) {
				contentService = part.getAdapter(INavigatorContentService.class);
				if (contentService != null) {
					labelProvider = contentService.createCommonLabelProvider();
					descriptionProvider = contentService.createCommonDescriptionProvider();
				} else {
					WorkbenchNavigatorPlugin.log(
							"Could not acquire INavigatorContentService from part (\"" + part.getTitle() + "\").", //$NON-NLS-1$ //$NON-NLS-2$
							null);
				}
			} else {
				WorkbenchNavigatorPlugin.log("Could not acquire INavigatorContentService: Project Explorer not found.", //$NON-NLS-1$
						null);
			}
		} else {
			WorkbenchNavigatorPlugin.log("Could not acquire INavigatorContentService: no active window.", null); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object object) {
		return labelProvider != null ? labelProvider.getImage(object) : null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object object) {
		return descriptionProvider != null ? descriptionProvider
				.getDescription(object) : null;
	}
}
