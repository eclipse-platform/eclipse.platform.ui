/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant Corp. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.tests.propertysheet;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.NewPropertySheetHandler;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertyShowInContext;

/**
 * @since 3.5
 *
 */
public class TestNewPropertySheetHandler extends NewPropertySheetHandler {

	public static final String ID = NewPropertySheetHandler.ID + "Test";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return super.execute(event);
	}

	@Override
	public PropertyShowInContext getShowInContext(ExecutionEvent event)
			throws ExecutionException {
		return super.getShowInContext(event);
	}

	@Override
	protected PropertySheet findPropertySheet(ExecutionEvent event,
			PropertyShowInContext context) throws PartInitException,
			ExecutionException {
		return super.findPropertySheet(event, context);
	}
}
