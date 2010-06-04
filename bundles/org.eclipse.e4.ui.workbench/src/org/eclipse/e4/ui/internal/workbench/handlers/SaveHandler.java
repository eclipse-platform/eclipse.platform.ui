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
package org.eclipse.e4.ui.internal.workbench.handlers;

import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class SaveHandler {

	@CanExecute
	boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MDirtyable dirtyable) {
		return dirtyable == null ? false : dirtyable.isDirty();
	}

	@Execute
	void execute(EPartService partService, @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		partService.savePart(part, false);
	}

}
