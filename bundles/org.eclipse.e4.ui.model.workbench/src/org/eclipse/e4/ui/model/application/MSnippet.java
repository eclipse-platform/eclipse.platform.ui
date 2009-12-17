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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Snippet</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MSnippet#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MSnippet#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MSnippet#getPositionInParent <em>Position In Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MSnippet#getContents <em>Contents</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getSnippet()
 * @model
 * @generated
 */
public interface MSnippet extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.e4.ui.model.application.SnippetType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.e4.ui.model.application.SnippetType
	 * @see #setType(SnippetType)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getSnippet_Type()
	 * @model
	 * @generated
	 */
	SnippetType getType();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MSnippet#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.e4.ui.model.application.SnippetType
	 * @see #getType()
	 * @generated
	 */
	void setType(SnippetType value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' reference.
	 * @see #setParent(MApplicationElement)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getSnippet_Parent()
	 * @model
	 * @generated
	 */
	MApplicationElement getParent();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MSnippet#getParent <em>Parent</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(MApplicationElement value);

	/**
	 * Returns the value of the '<em><b>Position In Parent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Position In Parent</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Position In Parent</em>' attribute.
	 * @see #setPositionInParent(String)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getSnippet_PositionInParent()
	 * @model
	 * @generated
	 */
	String getPositionInParent();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MSnippet#getPositionInParent <em>Position In Parent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Position In Parent</em>' attribute.
	 * @see #getPositionInParent()
	 * @generated
	 */
	void setPositionInParent(String value);

	/**
	 * Returns the value of the '<em><b>Contents</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Contents</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Contents</em>' reference.
	 * @see #setContents(MApplicationElement)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getSnippet_Contents()
	 * @model required="true"
	 * @generated
	 */
	MApplicationElement getContents();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MSnippet#getContents <em>Contents</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contents</em>' reference.
	 * @see #getContents()
	 * @generated
	 */
	void setContents(MApplicationElement value);

} // MSnippet
