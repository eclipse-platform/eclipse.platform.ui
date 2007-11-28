/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 */
public abstract class InterceptContributions {
	public static TweakKey KEY = new Tweaklets.TweakKey(
			InterceptContributions.class);

	static {
		Tweaklets.setDefault(InterceptContributions.KEY,
				new InterceptContributions() {
					public IViewPart tweakView(Object viewContribution) {
						return (IViewPart) viewContribution;
					}
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
	 * @param viewContribution
	 *            The contributed instance
	 * @return The view part to use
	 */
	public abstract IViewPart tweakView(Object viewContribution);

	/**
	 * Tweak the given editor contribution.
	 * 
	 * @param editorContribution
	 *            The contributed instance
	 * @return The editor part to use
	 */
	public abstract IEditorPart tweakEditor(Object editorContribution);

}