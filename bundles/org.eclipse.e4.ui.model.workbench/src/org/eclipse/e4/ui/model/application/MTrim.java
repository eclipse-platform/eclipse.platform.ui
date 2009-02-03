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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MTrim</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrim#getTopTrim <em>Top Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrim#getLeftTrim <em>Left Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrim#getRightTrim <em>Right Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrim#getBottomTrim <em>Bottom Trim</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrim()
 * @model
 * @generated
 */
public interface MTrim extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Top Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Top Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Top Trim</em>' containment reference.
	 * @see #setTopTrim(MPart)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrim_TopTrim()
	 * @model containment="true"
	 * @generated
	 */
	MPart<?> getTopTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrim#getTopTrim <em>Top Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Top Trim</em>' containment reference.
	 * @see #getTopTrim()
	 * @generated
	 */
	void setTopTrim(MPart<?> value);

	/**
	 * Returns the value of the '<em><b>Left Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Left Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Left Trim</em>' containment reference.
	 * @see #setLeftTrim(MPart)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrim_LeftTrim()
	 * @model containment="true"
	 * @generated
	 */
	MPart<?> getLeftTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrim#getLeftTrim <em>Left Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Left Trim</em>' containment reference.
	 * @see #getLeftTrim()
	 * @generated
	 */
	void setLeftTrim(MPart<?> value);

	/**
	 * Returns the value of the '<em><b>Right Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Right Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Right Trim</em>' containment reference.
	 * @see #setRightTrim(MPart)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrim_RightTrim()
	 * @model containment="true"
	 * @generated
	 */
	MPart<?> getRightTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrim#getRightTrim <em>Right Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Right Trim</em>' containment reference.
	 * @see #getRightTrim()
	 * @generated
	 */
	void setRightTrim(MPart<?> value);

	/**
	 * Returns the value of the '<em><b>Bottom Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bottom Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bottom Trim</em>' containment reference.
	 * @see #setBottomTrim(MPart)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrim_BottomTrim()
	 * @model containment="true"
	 * @generated
	 */
	MPart<?> getBottomTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrim#getBottomTrim <em>Bottom Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bottom Trim</em>' containment reference.
	 * @see #getBottomTrim()
	 * @generated
	 */
	void setBottomTrim(MPart<?> value);

} // MTrim
