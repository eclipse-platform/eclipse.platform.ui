/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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

	private HashMap<String, String> values = new HashMap<String, String>();
	
	public SplitValues() {
		values.put(WorkbenchMessages.SplitValues_Horizontal, "true"); //$NON-NLS-1$
		values.put(WorkbenchMessages.SplitValues_Vertical, "false"); //$NON-NLS-1$
	}

	@Override
	public Map getParameterValues() {
		return values;
	}

}
