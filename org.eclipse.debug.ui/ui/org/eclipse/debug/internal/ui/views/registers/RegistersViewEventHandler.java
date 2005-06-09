/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.registers;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewEventHandler;
import org.eclipse.debug.ui.AbstractDebugView;

public class RegistersViewEventHandler extends VariablesViewEventHandler {

	public RegistersViewEventHandler(AbstractDebugView view) {
		super(view);
	}

	protected boolean isFiltered(DebugEvent event) {
		if (event.getKind() == DebugEvent.CHANGE) {
			Object source = event.getSource();
			switch (event.getDetail()) {
				case DebugEvent.CONTENT:
					if (source instanceof IRegisterGroup) {
						return false;
					}
					break;
				case DebugEvent.STATE:
					if (source instanceof IRegisterGroup) {
						return false;
					}
					break;
				default: // UNSPECIFIED
					break;
			}
		}
		return super.isFiltered(event);
	}	
}
