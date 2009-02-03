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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MWorkbench</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.workbench.MWorkbench#getWbWindows <em>Wb Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.workbench.MWorkbench#getCurWBW <em>Cur WBW</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getMWorkbench()
 * @model
 * @generated
 */
public interface MWorkbench extends EObject {
	/**
	 * Returns the value of the '<em><b>Wb Windows</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.workbench.MWorkbenchWindow}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Wb Windows</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Wb Windows</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getMWorkbench_WbWindows()
	 * @model containment="true"
	 * @generated
	 */
	EList<MWorkbenchWindow> getWbWindows();

	/**
	 * Returns the value of the '<em><b>Cur WBW</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cur WBW</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cur WBW</em>' reference.
	 * @see #setCurWBW(MWorkbenchWindow)
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getMWorkbench_CurWBW()
	 * @model
	 * @generated
	 */
	MWorkbenchWindow getCurWBW();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.workbench.MWorkbench#getCurWBW <em>Cur WBW</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cur WBW</em>' reference.
	 * @see #getCurWBW()
	 * @generated
	 */
	void setCurWBW(MWorkbenchWindow value);

} // MWorkbench
