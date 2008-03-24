/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.registry;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Provides the parameter values for the show perspective command.
 * 
 * @since 3.1
 */
public final class PerspectiveParameterValues implements IParameterValues {

	public final Map getParameterValues() {
		final Map values = new HashMap();

		final IPerspectiveDescriptor[] perspectives = WorkbenchPlugin
				.getDefault().getPerspectiveRegistry().getPerspectives();
		for (int i = 0; i < perspectives.length; i++) {
			final IPerspectiveDescriptor perspective = perspectives[i];
			values.put(perspective.getLabel(), perspective.getId());
		}

		return values;
	}
}
