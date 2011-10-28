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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.tweaklets.PreferencePageEnhancer#createContents
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createContents(Composite parent) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.tweaklets.PreferencePageEnhancer#setSelection
	 * (java.lang.Object)
	 */
	@Override
	public void setSelection(Object selection) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.tweaklets.PreferencePageEnhancer#performOK()
	 */
	@Override
	public void performOK() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.tweaklets.PreferencePageEnhancer#performCancel()
	 */
	@Override
	public void performCancel() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.tweaklets.PreferencePageEnhancer#performDefaults
	 * ()
	 */
	@Override
	public void performDefaults() {

	}

}
