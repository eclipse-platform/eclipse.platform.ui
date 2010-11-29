/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.ui.IFolderLayout;

public class ModeledFolderLayout extends ModeledPlaceholderFolderLayout
		implements IFolderLayout {
	public ModeledFolderLayout(ModeledPageLayout layout, MApplication application,
			MPartStack stackModel) {
		super(layout, application, stackModel);
	}

	public void addView(String viewId) {
		MStackElement viewModel = ModeledPageLayout.createViewModel(application, viewId, true,
				layout.page,
				layout.partService,
				layout.createReferences);
		if (viewModel != null) {
			folderModel.getChildren().add(viewModel);
		}
	}
}
