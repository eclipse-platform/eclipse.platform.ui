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
 * Interface for objects interested in getting informed about 
 * annotation model changes. Changes are the addition or removal
 * of annotations managed by the model. Clients may implement
 * this interface.
 *
 * @see IAnnotationModel
 */
public interface IAnnotationModelListener {

	/**
	 * Called if a model change occurred on the given model.
	 *
	 * @param model the changed annotation model
	 */
	void modelChanged(IAnnotationModel model);
}
