/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.registry;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

/**
 * Provides the parameter values for the show perspective command.
 *
 * @since 3.1
 */
public final class PerspectiveParameterValues implements IParameterValues {

	@Override
	public Map getParameterValues() {
		final Map values = new HashMap();

		final IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry()
				.getPerspectives();
		for (final IPerspectiveDescriptor perspective : perspectives) {
			values.put(perspective.getLabel(), perspective.getId());
		}

		return values;
	}
}
