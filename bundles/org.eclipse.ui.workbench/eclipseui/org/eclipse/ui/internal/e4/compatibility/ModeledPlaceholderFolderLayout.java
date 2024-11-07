/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewRegistry;

public class ModeledPlaceholderFolderLayout implements IPlaceholderFolderLayout {

	protected MApplication application;
	protected MPartStack folderModel;
	protected ModeledPageLayout layout;
	protected IViewRegistry viewRegistry;

	public ModeledPlaceholderFolderLayout(ModeledPageLayout l, MApplication application, MPartStack stackModel) {
		this.application = application;
		this.viewRegistry = PlatformUI.getWorkbench().getViewRegistry();
		folderModel = stackModel;
		layout = l;
	}

	@Override
	public void addPlaceholder(String viewId) {
		boolean containsWildcards = viewId.indexOf('?') != -1;
		if (containsWildcards) {
			E4Util.unsupported("IPageLayout.addPlacehoder(): wildcard in view id: " + viewId); //$NON-NLS-1$
			return;
		}

		MStackElement viewModel = ModeledPageLayout.createViewModel(application, viewId, false, layout.page,
				layout.partService, layout.createReferences);
		if (viewModel != null) {
			folderModel.getChildren().add(viewModel);
		}
	}

	@Override
	public String getProperty(String id) {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setProperty(String id, String value) {
		// folderModel.setProperty(id, value);
	}
}
