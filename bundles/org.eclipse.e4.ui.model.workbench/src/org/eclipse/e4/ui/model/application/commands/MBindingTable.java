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
package org.eclipse.e4.ui.model.application.commands;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplicationElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Binding Table</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MBindingTable#getBindingContextId <em>Binding Context Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MBindingTable#getBindings <em>Bindings</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MBindingTable extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Binding Context Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Binding Context Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Binding Context Id</em>' attribute.
	 * @see #setBindingContextId(String)
	 * @model required="true"
	 * @generated
	 */
	String getBindingContextId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MBindingTable#getBindingContextId <em>Binding Context Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Binding Context Id</em>' attribute.
	 * @see #getBindingContextId()
	 * @generated
	 */
	void setBindingContextId(String value);

	/**
	 * Returns the value of the '<em><b>Bindings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.commands.MKeyBinding}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bindings</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bindings</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MKeyBinding> getBindings();

} // MBindingTable
