/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt.handlers;

import javax.inject.Named;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.internal.copy.ShowViewDialog;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class ShowViewHandler {

	public static final String VIEWS_SHOW_VIEW_PARM_ID = "org.eclipse.ui.views.showView.viewId"; //$NON-NLS-1$

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			MApplication application, EPartService partService,
			IEclipseContext context,
			@Optional @Named(VIEWS_SHOW_VIEW_PARM_ID) String viewId) {
		if (viewId != null) {
			partService.showPart(viewId, PartState.ACTIVATE);
			return;
		}

		final ShowViewDialog dialog = new ShowViewDialog(shell, application,
				context);
		dialog.open();
		if (dialog.getReturnCode() != Window.OK)
			return;

		for (MPartDescriptor descriptor : dialog.getSelection()) {
			partService.showPart(descriptor.getElementId(), PartState.ACTIVATE);
		}
	}
}
