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

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.workbench.modeling.IModelExtension;
import org.eclipse.emf.ecore.EObject;

/**
 * @since 3.5
 *
 */
public class DefaultPerspectiveProcessor implements IModelExtension {

	/* (non-Javadoc)
	 * @see org.eclipse.e4.workbench.modeling.IModelExtension#processElement(org.eclipse.emf.ecore.EObject)
	 */
	public void processElement(EObject parent) {
		System.out.println("process the application: " + parent); //$NON-NLS-1$
	}

}
