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
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 * @since 3.8
 *
 */
public abstract class PreferencePageEnhancer {
	public static TweakKey KEY = new Tweaklets.TweakKey(PreferencePageEnhancer.class);

	static {
		Tweaklets.setDefault(PreferencePageEnhancer.KEY, new DummyPrefPageEnhancer());
	}

	public abstract void createContents(Composite parent);

	public abstract void setSelection(Object selection);

	public abstract void performOK();

	public abstract void performCancel();

	public abstract void performDefaults();

}
