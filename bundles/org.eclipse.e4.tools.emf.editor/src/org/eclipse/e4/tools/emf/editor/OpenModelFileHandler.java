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

import java.util.Collection;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class OpenModelFileHandler {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, MApplication application, EModelService modelService, EPartService partService) {
		System.err.println("Execute!");
		FileDialog dialog = new FileDialog(shell);
		String file = dialog.open();
		if( file != null ) {
			String name = file.substring(file.lastIndexOf("/") + 1);
			String filePath = "file://" + file;
			Collection<MInputPart> parts = partService.getInputParts(filePath);
			if( parts.size() == 0 ) {
				MPartStack stack = (MPartStack) modelService.find("org.eclipse.e4.tools.emf.editor.mainwindow.editorstack", application);

				try {
					MInputPart part = MBasicFactory.INSTANCE.createInputPart();
					part.setLabel(name);
					part.setTooltip(file);
					part.setContributionURI("bundleclass://org.eclipse.e4.tools.emf.ui/org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor");
					part.setIconURI("platform:/plugin/org.eclipse.e4.tools.emf.editor/icons/full/application_view_tile.png");
					part.setInputURI(filePath);

					part.setCloseable(true);
					stack.getChildren().add(part);
					stack.setSelectedElement(part);
					System.err.println("Done");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				partService.showPart(parts.iterator().next(), PartState.ACTIVATE);
			}
		}
	}
}
