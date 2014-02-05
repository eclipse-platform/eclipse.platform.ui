/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor;


import java.io.IOException;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class NewApplicationModelHandler {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, MApplication application, EModelService modelService) {
		FileDialog dialog = new FileDialog(shell,SWT.SAVE);
		String file = dialog.open();
		if( file != null ) {
			String name = file.substring(file.lastIndexOf("/") + 1);
			String filePath = "file://" + file;

			try {
				E4XMIResource resource = new E4XMIResource();
				resource.getContents().add((EObject) MApplicationFactory.INSTANCE.createApplication());
				resource.setURI(URI.createFileURI(file));
				resource.save(null);

				MPartStack stack = (MPartStack) modelService.find("modeleditorstack", application);

				MInputPart part = MBasicFactory.INSTANCE.createInputPart();
				part.setLabel(name);
				part.setTooltip(file);
				part.setContributionURI("bundleclass://org.eclipse.e4.tools.emf.ui/org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor");
				part.setIconURI("platform:/plugin/org.eclipse.e4.tools.emf.editor/icons/full/application_view_tile.png");
				part.setInputURI(filePath);

				part.setCloseable(true);
				stack.getChildren().add(part);
				stack.setSelectedElement(part);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
