/*******************************************************************************
 * Copyright (c) 2012 Brian de Alwis and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 4.2
 */
public class FullscreenWindowHandler extends AbstractWindowHandler {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		if (!shell.isDisposed()) {
			shell.setFullScreen(!shell.getFullScreen());
		}
	}
}
