/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class TargetedView {

	public static final String TARGET_MARKER = "org.eclipse.e4.ui.tests.targetedViewTarget"; //$NON-NLS-1$

	@Inject
	private EPartService partService;

	@Inject
	private PartState state;

	@Inject
	@Named(TARGET_MARKER)
	private MPart part;

	public boolean passed = false;

	@PostConstruct
	void create() {
		partService.showPart(part, state);
		passed = part.getObject() != null;
	}

}
