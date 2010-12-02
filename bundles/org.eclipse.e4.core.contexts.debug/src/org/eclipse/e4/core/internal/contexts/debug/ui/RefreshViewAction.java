/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.EventUtils;
import org.osgi.service.event.EventAdmin;

public class RefreshViewAction {

	@Inject
	public EventAdmin eventAdmin;

	@Inject
	public RefreshViewAction() {
		// placeholder
	}

	@Execute
	public void refresh(IEclipseContext context) {
		EventUtils.send(eventAdmin, ContextsView.REFRESH_EVENT, null);
	}

}
