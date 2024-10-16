/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 * @since 3.3
 */
public abstract class TabBehaviour {

	public static TweakKey KEY = new Tweaklets.TweakKey(TabBehaviour.class);

	static {
		Tweaklets.setDefault(TabBehaviour.KEY, new TabBehaviourMRU());
	}

	public abstract IEditorReference findReusableEditor(WorkbenchPage page);

}
