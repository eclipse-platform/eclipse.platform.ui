/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.ui.IFolderLayout;

public class ModeledFolderLayout extends ModeledPlaceholderFolderLayout
		implements IFolderLayout {
	public ModeledFolderLayout(ModeledPageLayout layout, MApplication application,
			MPartStack stackModel) {
		super(layout, application, stackModel);
	}

	@Override
	public void addView(String viewId) {
		MStackElement viewModel = ModeledPageLayout.createViewModel(application, viewId, true,
				layout.page,
				layout.partService,
				layout.createReferences);
		if (viewModel != null) {
			// adding a non-placeholder to this folder, make sure the folder
			// will be rendered
			MUIElement parent = folderModel.getParent();
			while (parent != null && !(parent instanceof MPerspective)) {
				parent.setToBeRendered(true);
				parent = parent.getParent();
			}
			folderModel.setToBeRendered(true);
			boolean isFiltered = layout.isViewFiltered(viewId);
			if (isFiltered)
				layout.addViewActivator(viewModel);
			viewModel.setToBeRendered(!isFiltered);
			folderModel.getChildren().add(viewModel);
		}
	}
}
