/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import java.util.List;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class SwitchPerspectiveHandler {

	@Execute
	public void execute(EModelService modelService, MWindow window) {
		MPerspectiveStack ps = (MPerspectiveStack) modelService.find("DefaultPerspectiveStack", window); //$NON-NLS-1$
		List<MPerspective> kids = ps.getChildren();
		if (kids.size() < 2)
			return;
		
		if (ps.getSelectedElement() == kids.get(0)) {
			ps.setSelectedElement(kids.get(1));
		} else {
			ps.setSelectedElement(kids.get(0));	
		}
	}

}
