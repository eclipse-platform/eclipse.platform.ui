/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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

package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;

/**
 * A bridging interface with the 3.x ICommandService for registering element
 * item update callbacks.
 * <p>
 * See bug 366568.
 * </p>
 */
public interface IUpdateService {

	public Runnable registerElementForUpdate(
			ParameterizedCommand parameterizedCommand, MItem item);

}
