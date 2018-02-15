/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.core;


/**
 * Decoration of {@link Tip} that enables HTML content.
 *
 */
public interface IHtmlTip {

	/**
	 * Returns the HTML of the tip to be rendered in the tip UI, together or without
	 * the {@link #getImage()}.
	 *
	 * @return the HMTL of the tip
	 * @see #getImage()
	 */
	public String getHTML();

	/**
	 * Returns the {@link TipImage}.
	 *
	 * @return a TipImage with information about the image or <code>null</code> if the this information cannot be created
	 * @see #getHTML()
	 */
	public TipImage getImage();

}
