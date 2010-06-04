/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import org.eclipse.emf.ecore.EObject;

/**
 * A Model extension can programmatically update the model.
 */
public interface IModelExtension {
	/**
	 * Process the model. This is useful for additions, but not for removals as the order of
	 * processing is not specified.
	 * 
	 * @param parent
	 *            the parent object specified by the extension.
	 */
	public void processElement(EObject parent);
}
