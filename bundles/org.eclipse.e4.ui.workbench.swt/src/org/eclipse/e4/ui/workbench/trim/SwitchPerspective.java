/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.trim;

import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.workbench.modeling.EModelService;

/**
 * @author emoffatt
 * 
 */
public class SwitchPerspective {
	@Inject
	EModelService modelService;

	public void execute(MApplication appModel, MItem theItem) {
		Object persp = modelService.find(theItem.getElementId(), appModel);
		if (persp instanceof MPerspective) {
			MPerspective thePersp = (MPerspective) persp;
			thePersp.getParent().setSelectedElement(thePersp);
		}
	}
}
