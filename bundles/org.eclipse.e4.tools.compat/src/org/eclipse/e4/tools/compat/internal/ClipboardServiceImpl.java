/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 421453
 ******************************************************************************/
package org.eclipse.e4.tools.compat.internal;

import org.eclipse.e4.tools.services.IClipboardService;

public class ClipboardServiceImpl implements IClipboardService {
	private Handler handler;

	@Override
	public void copy() {
		if( handler != null ) {
			handler.copy();
		}
	}

	@Override
	public void paste() {
		if( handler != null ) {
			handler.paste();
		}
	}

	@Override
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void cut() {
		this.handler.cut();
	}
}