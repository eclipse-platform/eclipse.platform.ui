/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432372
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.handlers;

import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IViewEObjects;

public class AbstractHandler {

	static final public String VIEWER_KEY = "org.eclipse.e4.tools.active-object-viewer"; //$NON-NLS-1$

	@CanExecute
	public boolean canExecute(@Optional @Named(VIEWER_KEY) IViewEObjects viewer) {
		return (viewer != null);
	}

}
