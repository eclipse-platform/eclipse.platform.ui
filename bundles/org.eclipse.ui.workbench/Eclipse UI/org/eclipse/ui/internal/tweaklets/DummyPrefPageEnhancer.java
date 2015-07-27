/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
