/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.sourcelookup;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;

public class TestSourceDirector extends AbstractSourceLookupDirector {


	@Override
	public Object getSourceElement(Object element) {
		if (element instanceof String) {
			return element.toString() + System.currentTimeMillis();
		} else if (element instanceof IStackFrame) {
			IStackFrame frame = (IStackFrame) element;
			return frame.getModelIdentifier() + System.currentTimeMillis();
		}
		return super.getSourceElement(element);
	}

	@Override
	public void initializeParticipants() {
	}
}
