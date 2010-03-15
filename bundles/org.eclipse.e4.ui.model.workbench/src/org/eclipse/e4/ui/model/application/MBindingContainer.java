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
 * A representation of the model object '<em><b>Binding Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MBindingContainer#getBindingTables <em>Binding Tables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MBindingContainer#getRootContext <em>Root Context</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getBindingContainer()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MBindingContainer {
	/**
	 * Returns the value of the '<em><b>Binding Tables</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MBindingTable}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Binding Tables</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Binding Tables</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getBindingContainer_BindingTables()
	 * @model containment="true"
	 * @generated
	 */
	EList<MBindingTable> getBindingTables();

	/**
	 * Returns the value of the '<em><b>Root Context</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Root Context</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Root Context</em>' containment reference.
	 * @see #setRootContext(MBindingContext)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getBindingContainer_RootContext()
	 * @model containment="true"
	 * @generated
	 */
	MBindingContext getRootContext();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MBindingContainer#getRootContext <em>Root Context</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Root Context</em>' containment reference.
	 * @see #getRootContext()
	 * @generated
	 */
	void setRootContext(MBindingContext value);

} // MBindingContainer
