/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432372
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IViewEObjects;

import jakarta.inject.Named;

public class AbstractHandler {

	static final public String VIEWER_KEY = "org.eclipse.e4.tools.active-object-viewer"; //$NON-NLS-1$

	@CanExecute
	public boolean canExecute(@Optional @Named(VIEWER_KEY) IViewEObjects viewer) {
		return (viewer != null);
	}

}
