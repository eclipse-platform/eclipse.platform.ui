/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

/**
 * Extends <code>IVerticalRulerInfo</code> with the ability to define a custom hover to be used
 * when hovering over the <code>IVerticalRuler</code> described by this info instance, and to
 * specify the <code>IAnnotationModel</code> used by it.
 * @since 3.0
 */
public interface IVerticalRulerInfoExtension {
	/**
	 * Returns the hover for this vertical ruler (column).
	 * @return the hover for this column
	 */
	IAnnotationHover getHover();
	
	/**
	 * Returns the model currently used by the receiver.
	 * @return the model of the receiver, or <code>null</code> if no model is installed.
	 */
	IAnnotationModel getModel();
}
