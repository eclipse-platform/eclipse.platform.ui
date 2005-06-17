/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.graphics.Color;

/**
 * An <code>IChangeRulerColumn</code> can display quick diff information.
 *
 * @since 3.0
 */
public interface IChangeRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfoExtension {

	/** The ID under which the quick diff model is registered with a document's annotation model. */
	public static final String QUICK_DIFF_MODEL_ID= "diff"; //$NON-NLS-1$

	/**
	 * Sets the hover of this ruler column.
	 *
	 * @param hover the hover that will produce hover information text for this ruler column
	 */
	public abstract void setHover(IAnnotationHover hover);

	/**
	 * Sets the background color for normal lines. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 *
	 * @param backgroundColor the new color to be used as standard line background
	 */
	public abstract void setBackground(Color backgroundColor);

	/**
	 * Sets the background color for added lines. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 *
	 * @param addedColor the new color to be used for the added lines background
	 */
	public abstract void setAddedColor(Color addedColor);

	/**
	 * Sets the background color for changed lines. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 *
	 * @param changedColor the new color to be used for the changed lines background
	 */
	public abstract void setChangedColor(Color changedColor);

	/**
	 * Sets the color for the deleted lines indicator. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 *
	 * @param deletedColor the new color to be used for the deleted lines indicator.
	 */
	public abstract void setDeletedColor(Color deletedColor);
}
