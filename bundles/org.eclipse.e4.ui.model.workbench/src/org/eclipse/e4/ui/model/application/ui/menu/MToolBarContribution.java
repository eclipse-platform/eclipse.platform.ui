/**
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.menu;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Tool Bar Contribution</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * Provisional for 4.3. This represents a potential extension to some toolbar already
 * defined in the UI.
 * </p>
 * @noreference This interface is not intended to be referenced by clients.
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution#getParentId <em>Parent Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution#getPositionInParent <em>Position In Parent</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MToolBarContribution extends MElementContainer<MToolBarElement> {

	/**
	 * Returns the value of the '<em><b>Parent Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The element id of the Toolbar to be contributed to.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parent Id</em>' attribute.
	 * @see #setParentId(String)
	 * @model
	 * @generated
	 */
	String getParentId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution#getParentId <em>Parent Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Id</em>' attribute.
	 * @see #getParentId()
	 * @generated
	 */
	void setParentId(String value);

	/**
	 * Returns the value of the '<em><b>Position In Parent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * Indicates the position in the Toolbar where this contribution should be placed.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Position In Parent</em>' attribute.
	 * @see #setPositionInParent(String)
	 * @model
	 * @generated
	 */
	String getPositionInParent();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution#getPositionInParent <em>Position In Parent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Position In Parent</em>' attribute.
	 * @see #getPositionInParent()
	 * @generated
	 */
	void setPositionInParent(String value);
} // MToolBarContribution
