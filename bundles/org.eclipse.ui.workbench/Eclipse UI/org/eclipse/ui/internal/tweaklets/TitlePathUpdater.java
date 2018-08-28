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

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 *
 * Tweaklet to update the Shell when the active editor is changed
 *
 * @since 3.7
 *
 */
public abstract class TitlePathUpdater {

	public static TweakKey KEY = new Tweaklets.TweakKey(TitlePathUpdater.class);

	static {
		Tweaklets.setDefault(KEY, new DummyTitlePathUpdater());
	}

	public abstract void updateTitlePath(Shell window, String path);

}
