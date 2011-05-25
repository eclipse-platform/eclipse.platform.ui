/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class SaveAllHandler {

	@CanExecute
	boolean canExecute(@Optional MWindow window) {
		if (window != null) {
			IEclipseContext context = window.getContext();
			if (context != null) {
				EPartService partService = context.get(EPartService.class);
				if (partService != null) {
					return !partService.getDirtyParts().isEmpty();
				}
			}
		}
		return false;
	}

	@Execute
	void execute(EPartService partService) {
		partService.saveAll(false);
	}

}
