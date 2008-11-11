/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *
 * $Id$
 */
package org.eclipse.e4.ui.model.workbench;

import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.e4.ui.model.application.Window;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Window</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.workbench.WorkbenchWindow#getSharedParts <em>Shared Parts</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getWorkbenchWindow()
 * @model
 * @generated
 */
public interface WorkbenchWindow extends Window<Perspective<?>> {
	/**
	 * Returns the value of the '<em><b>Shared Parts</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.Part}&lt;?>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shared Parts</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shared Parts</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getWorkbenchWindow_SharedParts()
	 * @model containment="true"
	 * @generated
	 */
	EList<Part<?>> getSharedParts();

} // WorkbenchWindow
