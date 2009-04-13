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
package org.eclipse.e4.ui.model.application;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MTool Bar Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MToolBarContainer#getToolbars <em>Toolbars</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MToolBarContainer#isHorizontal <em>Horizontal</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMToolBarContainer()
 * @model
 * @generated
 */
public interface MToolBarContainer<I extends MToolBar> extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Toolbars</b></em>' containment reference list.
	 * The list contents are of type {@link I}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Toolbars</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Toolbars</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMToolBarContainer_Toolbars()
	 * @model containment="true"
	 * @generated
	 */
	EList<I> getToolbars();

	/**
	 * Returns the value of the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Horizontal</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Horizontal</em>' attribute.
	 * @see #setHorizontal(boolean)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMToolBarContainer_Horizontal()
	 * @model
	 * @generated
	 */
	boolean isHorizontal();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MToolBarContainer#isHorizontal <em>Horizontal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Horizontal</em>' attribute.
	 * @see #isHorizontal()
	 * @generated
	 */
	void setHorizontal(boolean value);

} // MToolBarContainer
