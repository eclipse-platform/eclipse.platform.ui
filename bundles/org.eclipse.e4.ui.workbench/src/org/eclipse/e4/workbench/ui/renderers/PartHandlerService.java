/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers;

import java.util.Iterator;

import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.workbench.ui.IHandlerService;

public class PartHandlerService implements IHandlerService {
	private MPart<?> part;

	public PartHandlerService(MPart<?> p) {
		part = p;
	}

	public MHandler getHandler(MCommand command) {
		return findFirstHandlerFor(part, command);
	}

	private MHandler findFirstHandlerFor(MPart<?> p, MCommand command) {
		if (p == null) {
			return null;
		}
		Iterator<MHandler> i = p.getHandlers().iterator();
		while (i.hasNext()) {
			MHandler h = i.next();
			if (command.equals(h.getCommand())) {
				return h;
			}
		}
		return findFirstHandlerFor(p.getParent(), command);
	}

}
