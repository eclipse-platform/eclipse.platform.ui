/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

package org.eclipse.ui.tests.menus;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;

public class ContextIdValues implements IParameterValues {

	@Override
	public Map getParameterValues() {
		Map<String, String> values = new HashMap<>();

		IContextService contextService = PlatformUI
				.getWorkbench().getService(IContextService.class);
		Context[] definedContexts = contextService.getDefinedContexts();
		try {
			for (Context definedContext : definedContexts) {
				values.put(definedContext.getName(), definedContext
						.getId());
			}
		} catch (NotDefinedException e) {
			// This shouldn't happen since we asked for defined contexts,
			// but you never know.
			e.printStackTrace();
		}
		return values;
	}
}
