/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8
 *
 */
public class DummyPrefPageEnhancer extends PreferencePageEnhancer {

	@Override
	public void createContents(Composite parent) {
	}

	@Override
	public void setSelection(Object selection) {

	}

	@Override
	public void performOK() {

	}

	@Override
	public void performCancel() {

	}

	@Override
	public void performDefaults() {

	}

}
