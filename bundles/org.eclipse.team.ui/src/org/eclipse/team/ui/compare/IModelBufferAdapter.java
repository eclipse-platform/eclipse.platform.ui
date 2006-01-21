/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.compare;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.ICompareInput;

/**
 * Interface for obtaining the modle buffer that is used
 * to buffer any edited content for the model element.
 * Clients can adapt an {@link ITypedElement} or {@link ICompareInput}
 * or other model related objects to this interface in order to obtain the 
 * underlying edit buffer.
 * <p>
 * Clients may implement this interface.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface IModelBufferAdapter {
	
	/**
	 * Return the buffer for the given model element.
	 * @param modelElement
	 * @return
	 */
	IModelBuffer getBuffer(Object modelElement);

}
