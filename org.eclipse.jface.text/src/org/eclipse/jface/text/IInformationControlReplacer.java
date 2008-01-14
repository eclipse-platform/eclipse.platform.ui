/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.graphics.Rectangle;


/**
 * An information control replacer can replace an
 * {@link AbstractInformationControlManager}'s control.
 *
 * @see AbstractInformationControlManager#setInformationControlReplacer(IInformationControlReplacer)
 * @since 3.4
 */
interface IInformationControlReplacer {

	/**
	 * Replace the information control shown by the given {@link AbstractInformationControlManager}.
	 * 
	 * @param replaceable the replaceable manager
	 * @param information the information to show
	 * @param area the hover area
	 * @param takeFocus <code>true</code> iff the replacing information control should take focus
	 */
	public void replaceInformationControl(AbstractInformationControlManager replaceable, Object information, Rectangle area, boolean takeFocus);

	/**
	 * Tells whether the replacer is currently replacing another information control.
	 * 
	 * @return <code>true</code> while code from {@link #replaceInformationControl(AbstractInformationControlManager, Object, Rectangle, boolean)} is run
	 */
	public boolean isReplacing();
	
	/**
	 * @return the current information control, or <code>null</code> if none available
	 */
	public IInformationControl getCurrentInformationControl();
	
	/**
	 * Disposes this information control replacer.
	 * <p>
	 * Can be called more than once. Calling
	 * {@link AbstractInformationControlManager#setInformationControlReplacer(IInformationControlReplacer)}
	 * will dispose its old replacer if set.
	 * </p>
	 * 
	 * @see AbstractInformationControlManager
	 */
	public void dispose();

	/**
	 * The number of pixels to blow up the keep-up zone.
	 * 
	 * @return the margin in pixels
	 */
	public int getKeepUpMargin();

	/**
	 * @param input the delayed input, or <code>null</code> to request cancellation
	 */
	public void setDelayedInput(Object input);
}
