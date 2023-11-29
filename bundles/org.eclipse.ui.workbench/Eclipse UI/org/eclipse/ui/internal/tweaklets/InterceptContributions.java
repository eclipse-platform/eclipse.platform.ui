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

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 * @since 3.4
 */
public abstract class InterceptContributions {
	public static TweakKey KEY = new Tweaklets.TweakKey(InterceptContributions.class);

	static {
		Tweaklets.setDefault(InterceptContributions.KEY, new InterceptContributions() {
			@Override
			public IViewPart tweakView(Object viewContribution) {
				return (IViewPart) viewContribution;
			}

			@Override
			public IEditorPart tweakEditor(Object editorContribution) {
				return (IEditorPart) editorContribution;
			}
		});
	}

	/** Default constructor */
	public InterceptContributions() {
	}

	/**
	 * Tweak the given view contribution.
	 *
	 * @param viewContribution The contributed instance
	 * @return The view part to use
	 */
	public abstract IViewPart tweakView(Object viewContribution);

	/**
	 * Tweak the given editor contribution.
	 *
	 * @param editorContribution The contributed instance
	 * @return The editor part to use
	 */
	public abstract IEditorPart tweakEditor(Object editorContribution);

}