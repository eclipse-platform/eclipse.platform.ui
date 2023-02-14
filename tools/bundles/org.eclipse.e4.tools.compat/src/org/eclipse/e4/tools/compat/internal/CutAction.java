/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.compat.internal;

import org.eclipse.e4.tools.services.IClipboardService;
import org.eclipse.jface.action.Action;

public class CutAction extends Action {
	private IClipboardService service;

	public CutAction(IClipboardService service) {
		super(Messages.CutAction);
		this.service = service;
	}

	@Override
	public void run() {
		service.cut();
	}
}
