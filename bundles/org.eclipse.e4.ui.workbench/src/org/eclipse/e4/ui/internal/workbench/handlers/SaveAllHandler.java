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
package org.eclipse.e4.ui.internal.workbench.handlers;

import org.eclipse.e4.workbench.modeling.EPartService;

public class SaveAllHandler {

	boolean canExecute(EPartService partService) {
		return !partService.getDirtyParts().isEmpty();
	}

	void execute(EPartService partService) {
		partService.saveAll(false);
	}

}
