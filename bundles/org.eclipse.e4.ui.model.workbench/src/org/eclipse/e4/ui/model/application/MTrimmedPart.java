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
 * $Id: MTrimmedPart.java,v 1.1 2009/04/13 19:47:35 emoffatt Exp $
 */
package org.eclipse.e4.ui.model.application;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MTrimmed Part</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getTopTrim <em>Top Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getLeftTrim <em>Left Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getRightTrim <em>Right Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getBottomTrim <em>Bottom Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getClientArea <em>Client Area</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrimmedPart()
 * @model
 * @generated
 */
public interface MTrimmedPart<P extends MPart<?>> extends MPart<P> {
	/**
	 * Returns the value of the '<em><b>Top Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Top Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Top Trim</em>' containment reference.
	 * @see #setTopTrim(MToolBarContainer)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrimmedPart_TopTrim()
	 * @model containment="true"
	 * @generated
	 */
	MToolBarContainer getTopTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getTopTrim <em>Top Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Top Trim</em>' containment reference.
	 * @see #getTopTrim()
	 * @generated
	 */
	void setTopTrim(MToolBarContainer value);

	/**
	 * Returns the value of the '<em><b>Left Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Left Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Left Trim</em>' containment reference.
	 * @see #setLeftTrim(MToolBarContainer)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrimmedPart_LeftTrim()
	 * @model containment="true"
	 * @generated
	 */
	MToolBarContainer getLeftTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getLeftTrim <em>Left Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Left Trim</em>' containment reference.
	 * @see #getLeftTrim()
	 * @generated
	 */
	void setLeftTrim(MToolBarContainer value);

	/**
	 * Returns the value of the '<em><b>Right Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Right Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Right Trim</em>' containment reference.
	 * @see #setRightTrim(MToolBarContainer)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrimmedPart_RightTrim()
	 * @model containment="true"
	 * @generated
	 */
	MToolBarContainer getRightTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getRightTrim <em>Right Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Right Trim</em>' containment reference.
	 * @see #getRightTrim()
	 * @generated
	 */
	void setRightTrim(MToolBarContainer value);

	/**
	 * Returns the value of the '<em><b>Bottom Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bottom Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bottom Trim</em>' containment reference.
	 * @see #setBottomTrim(MToolBarContainer)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrimmedPart_BottomTrim()
	 * @model containment="true"
	 * @generated
	 */
	MToolBarContainer getBottomTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getBottomTrim <em>Bottom Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bottom Trim</em>' containment reference.
	 * @see #getBottomTrim()
	 * @generated
	 */
	void setBottomTrim(MToolBarContainer value);

	/**
	 * Returns the value of the '<em><b>Client Area</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Client Area</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Client Area</em>' containment reference.
	 * @see #setClientArea(MPart)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMTrimmedPart_ClientArea()
	 * @model containment="true"
	 * @generated
	 */
	MPart<?> getClientArea();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getClientArea <em>Client Area</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Client Area</em>' containment reference.
	 * @see #getClientArea()
	 * @generated
	 */
	void setClientArea(MPart<?> value);

} // MTrimmedPart
