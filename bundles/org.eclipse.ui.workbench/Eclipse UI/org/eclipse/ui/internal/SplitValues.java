/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.IParameterValues;

/**
 * Display the values that can be used in the keybindings page and quick access.
 *
 * @since 3.106
 */
public class SplitValues implements IParameterValues {

	private HashMap<String, String> values = new HashMap<>();

	public SplitValues() {
		values.put(WorkbenchMessages.SplitValues_Horizontal, "true"); //$NON-NLS-1$
		values.put(WorkbenchMessages.SplitValues_Vertical, "false"); //$NON-NLS-1$
	}

	@Override
	public Map getParameterValues() {
		return values;
	}

}
