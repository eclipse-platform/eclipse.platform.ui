/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ui.internal.texteditor.spelling;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.texteditor.spelling.IPreferenceStatusMonitor;
import org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock;


/**
 * Empty preference block for extensions to the
 * <code>org.eclipse.ui.workbench.texteditor.spellingEngine</code> extension
 * point that do not specify their own.
 *
 * @since 3.1
 */
public class EmptySpellingPreferenceBlock implements ISpellingPreferenceBlock {

	@Override
	public Control createControl(Composite composite) {
		Composite inner= new Composite(composite, SWT.NONE);
		inner.setLayout(new GridLayout(3, false));

		Label label= new Label(inner, SWT.CENTER);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= 30;
		label.setLayoutData(gd);

		label= new Label(inner, SWT.CENTER);
		label.setText(SpellingMessages.EmptySpellingPreferenceBlock_emptyCaption);
		gd= new GridData(GridData.CENTER);
		label.setLayoutData(gd);

		label= new Label(inner, SWT.CENTER);
		gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= 30;
		label.setLayoutData(gd);

		return inner;
	}

	@Override
	public void initialize(IPreferenceStatusMonitor statusMonitor) {
	}

	@Override
	public boolean canPerformOk() {
		return true;
	}

	@Override
	public void performOk() {
	}

	@Override
	public void performDefaults() {
	}

	@Override
	public void performRevert() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void setEnabled(boolean enabled) {
	}
}
