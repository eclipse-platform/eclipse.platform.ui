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
package org.eclipse.e4.tools.compat.internal;

import org.eclipse.e4.tools.services.IClipboardService;
import org.eclipse.jface.action.Action;

public class PasteAction extends Action {
	private IClipboardService service;
	public PasteAction(IClipboardService service) {
		super(Messages.PasteAction);
		this.service = service;
	}
	
	@Override
	public void run() {
		service.paste();
	}
}
