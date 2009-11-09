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
 * A representation of the model object '<em><b>Command Parameter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MCommandParameter#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MCommandParameter#getTypeId <em>Type Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MCommandParameter#isOptional <em>Optional</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommandParameter()
 * @model
 * @generated
 */
public interface MCommandParameter extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommandParameter_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MCommandParameter#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type Id</em>' attribute.
	 * @see #setTypeId(String)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommandParameter_TypeId()
	 * @model
	 * @generated
	 */
	String getTypeId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MCommandParameter#getTypeId <em>Type Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type Id</em>' attribute.
	 * @see #getTypeId()
	 * @generated
	 */
	void setTypeId(String value);

	/**
	 * Returns the value of the '<em><b>Optional</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Optional</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Optional</em>' attribute.
	 * @see #setOptional(boolean)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommandParameter_Optional()
	 * @model default="true"
	 * @generated
	 */
	boolean isOptional();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MCommandParameter#isOptional <em>Optional</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Optional</em>' attribute.
	 * @see #isOptional()
	 * @generated
	 */
	void setOptional(boolean value);

} // MCommandParameter
