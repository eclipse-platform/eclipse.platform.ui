/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Part</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getMenus <em>Menus</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getToolbar <em>Toolbar</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getPart()
 * @model
 * @generated
 */
public interface MPart extends MContribution, MContext, MPSCElement, MUIItem {
	/**
	 * Returns the value of the '<em><b>Menus</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MMenu}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Menus</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Menus</em>' reference list.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getPart_Menus()
	 * @model
	 * @generated
	 */
	EList<MMenu> getMenus();

	/**
	 * Returns the value of the '<em><b>Toolbar</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Toolbar</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Toolbar</em>' reference.
	 * @see #setToolbar(MToolBar)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getPart_Toolbar()
	 * @model
	 * @generated
	 */
	MToolBar getToolbar();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#getToolbar <em>Toolbar</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Toolbar</em>' reference.
	 * @see #getToolbar()
	 * @generated
	 */
	void setToolbar(MToolBar value);

} // MPart
