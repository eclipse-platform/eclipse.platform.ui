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
 * A representation of the model object '<em><b>Trim Structure</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimStructure#getTop <em>Top</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimStructure#getBottom <em>Bottom</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimStructure#getLeft <em>Left</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MTrimStructure#getRight <em>Right</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getTrimStructure()
 * @model
 * @generated
 */
public interface MTrimStructure<T extends MUIElement> extends MUIElement, MElementContainer<T> {
	/**
	 * Returns the value of the '<em><b>Top</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Top</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Top</em>' reference.
	 * @see #setTop(MElementContainer)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getTrimStructure_Top()
	 * @model
	 * @generated
	 */
	MElementContainer<MUIElement> getTop();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimStructure#getTop <em>Top</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Top</em>' reference.
	 * @see #getTop()
	 * @generated
	 */
	void setTop(MElementContainer<MUIElement> value);

	/**
	 * Returns the value of the '<em><b>Bottom</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bottom</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bottom</em>' reference.
	 * @see #setBottom(MElementContainer)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getTrimStructure_Bottom()
	 * @model
	 * @generated
	 */
	MElementContainer<MUIElement> getBottom();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimStructure#getBottom <em>Bottom</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bottom</em>' reference.
	 * @see #getBottom()
	 * @generated
	 */
	void setBottom(MElementContainer<MUIElement> value);

	/**
	 * Returns the value of the '<em><b>Left</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Left</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Left</em>' reference.
	 * @see #setLeft(MElementContainer)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getTrimStructure_Left()
	 * @model
	 * @generated
	 */
	MElementContainer<MUIElement> getLeft();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimStructure#getLeft <em>Left</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Left</em>' reference.
	 * @see #getLeft()
	 * @generated
	 */
	void setLeft(MElementContainer<MUIElement> value);

	/**
	 * Returns the value of the '<em><b>Right</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Right</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Right</em>' reference.
	 * @see #setRight(MElementContainer)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getTrimStructure_Right()
	 * @model
	 * @generated
	 */
	MElementContainer<MUIElement> getRight();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MTrimStructure#getRight <em>Right</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Right</em>' reference.
	 * @see #getRight()
	 * @generated
	 */
	void setRight(MElementContainer<MUIElement> value);

} // MTrimStructure
