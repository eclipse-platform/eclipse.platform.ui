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

import javax.inject.Named;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class OpenModelFileHandler {
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, MApplication application, EModelService modelService) {
		
		FileDialog dialog = new FileDialog(shell);
		String file = dialog.open();
		if( file != null ) {
			String name = file.substring(file.lastIndexOf("/") + 1);
			String filePath = "file://" + file;

			MPartStack stack = (MPartStack) modelService.find("modeleditorstack", application);
			
			MPart part = MApplicationFactory.eINSTANCE.createPart();
			part.setLabel(name);
			part.setTooltip(file);
			part.setURI("platform:/plugin/org.eclipse.e4.tools.emf.editor/org.eclipse.e4.tools.emf.editor.XMIFileEditor");
			part.setIconURI("platform:/plugin/org.eclipse.e4.tools.emf.editor/icons/full/application_view_tile.png");
			part.setPersistedState(filePath);
			
			part.setCloseable(true);
			stack.getChildren().add(part);
			stack.setSelectedElement(part);

			System.err.println(stack);
		}
	}
}
