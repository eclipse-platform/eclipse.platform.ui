/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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

package org.eclipse.e4.ui.progress;


import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.progress.internal.JobsViewPreferenceDialog;
import org.eclipse.e4.ui.progress.internal.PreferenceStore;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;


/**
 * Opens the progress view preference dialog.
 *
 * @noreference
 */
public class OpenPreferenceDialogHandler {

	@Execute
	public void openPreferenceDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, PreferenceStore preferences) {
		new JobsViewPreferenceDialog(shell, preferences).open();
	}

}
