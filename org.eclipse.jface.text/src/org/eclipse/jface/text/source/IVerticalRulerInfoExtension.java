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

/**
 * Extension interface for
 * {@link org.eclipse.jface.text.source.IVerticalRulerInfo}.
 * <p>
 * Introduces the ability to define a custom hover to be used when hovering over
 * the vertical ruler described by this info instance, and to specify the
 * annotation model used by it.
 * <p>
 * It also allows client to register as listeners on the represented vertical
 * ruler and sends out notifications similar to selection events such as that a
 * particular annotation presented in the vertical ruler has been selected.
 *
 * @see org.eclipse.jface.text.source.IVerticalRuler
 * @see org.eclipse.jface.text.source.IAnnotationModel
 * @since 3.0
 */
public interface IVerticalRulerInfoExtension {
	/**
	 * Returns the hover for this vertical ruler (column).
	 *
	 * @return the hover for this column
	 */
	IAnnotationHover getHover();

	/**
	 * Returns the model currently used by the receiver.
	 *
	 * @return the model of the receiver, or <code>null</code> if no model is
	 *         installed.
	 */
	IAnnotationModel getModel();

	/**
	 * Registers a vertical ruler listener to be informed if an annotation gets
	 * selected on the vertical ruler.
	 *
	 * @param listener the listener to be informed
	 */
	void addVerticalRulerListener(IVerticalRulerListener listener);

	/**
	 * Removes a previously registered listener. If <code>listener</code> is not registered
	 * with the receiver, calling this method has no effect.
	 *
	 * @param listener the listener to be removed
	 */
	void removeVerticalRulerListener(IVerticalRulerListener listener);
}
