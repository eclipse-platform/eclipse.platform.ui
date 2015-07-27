/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brian de Alwis - rewritten for e4
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 *
 * @author Prakash G.R. (grprakash@gmail.com)
 * @since 3.6
 *
 */
public class CloseDialogHandler {

	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		// perform only if close is enabled
		if ((shell.getStyle() & SWT.CLOSE) != 0) {
			shell.close();
		}
		return null;
	}

}
