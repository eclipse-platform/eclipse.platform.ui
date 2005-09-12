/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;


/**
 * ILabelUpdateProcessor is an interface that visits all of the
 * listeners on an IDecoratorManager to determine if updates
 * are required.
 * @since 3.2
 * <p>
 * <strong>NOTE: </strong> This API is EXPERIMENTAL and subject to
 * change during the 3.2 release cycle.
 * </p>
 *
 */
public interface ILabelUpdateProcessor {

	/**
	 * element has had some changes. Use the validator to see what
	 * sort of updates are required for any objects looked up
	 * using element.
	 * @param element
	 * @param validator
	 */
	void processUpdates(Object element, ILabelUpdateValidator validator);

}
