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

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplicationElement#getElementId <em>Element Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplicationElement#getTags <em>Tags</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplicationElement#getContributorURI <em>Contributor URI</em>}</li>
 * </ul>
 * </p>
 *
 * @model abstract="true"
 * @generated
 */
public interface MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Element Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Element Id</em>' attribute.
	 * @see #setElementId(String)
	 * @model
	 * @generated
	 */
	String getElementId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getElementId <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Element Id</em>' attribute.
	 * @see #getElementId()
	 * @generated
	 */
	void setElementId(String value);

	/**
	 * Returns the value of the '<em><b>Tags</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tags</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tags</em>' attribute list.
	 * @model
	 * @generated
	 */
	List<String> getTags();

	/**
	 * Returns the value of the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Contributor URI</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Contributor URI</em>' attribute.
	 * @see #setContributorURI(String)
	 * @model
	 * @generated
	 */
	String getContributorURI();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getContributorURI <em>Contributor URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contributor URI</em>' attribute.
	 * @see #getContributorURI()
	 * @generated
	 */
	void setContributorURI(String value);

} // MApplicationElement
