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
 * A representation of the model object '<em><b>Model Component</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MModelComponent#getPositionInParent <em>Position In Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MModelComponent#getParentID <em>Parent ID</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MModelComponent#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MModelComponent#getCommands <em>Commands</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MModelComponent#getProcessor <em>Processor</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MModelComponent#getBindings <em>Bindings</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getModelComponent()
 * @model
 * @generated
 */
public interface MModelComponent extends MPartDescriptorContainer, MApplicationElement, MHandlerContainer, MBindingContainer {
	/**
	 * Returns the value of the '<em><b>Position In Parent</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Position In Parent</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Position In Parent</em>' attribute.
	 * @see #setPositionInParent(String)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getModelComponent_PositionInParent()
	 * @model default=""
	 * @generated
	 */
	String getPositionInParent();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MModelComponent#getPositionInParent <em>Position In Parent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Position In Parent</em>' attribute.
	 * @see #getPositionInParent()
	 * @generated
	 */
	void setPositionInParent(String value);

	/**
	 * Returns the value of the '<em><b>Parent ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent ID</em>' attribute.
	 * @see #setParentID(String)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getModelComponent_ParentID()
	 * @model required="true"
	 * @generated
	 */
	String getParentID();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MModelComponent#getParentID <em>Parent ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent ID</em>' attribute.
	 * @see #getParentID()
	 * @generated
	 */
	void setParentID(String value);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MUIElement}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getModelComponent_Children()
	 * @model containment="true"
	 * @generated
	 */
	EList<MUIElement> getChildren();

	/**
	 * Returns the value of the '<em><b>Commands</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MCommand}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Commands</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Commands</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getModelComponent_Commands()
	 * @model containment="true"
	 * @generated
	 */
	EList<MCommand> getCommands();

	/**
	 * Returns the value of the '<em><b>Processor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Processor</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Processor</em>' attribute.
	 * @see #setProcessor(String)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getModelComponent_Processor()
	 * @model
	 * @generated
	 */
	String getProcessor();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MModelComponent#getProcessor <em>Processor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Processor</em>' attribute.
	 * @see #getProcessor()
	 * @generated
	 */
	void setProcessor(String value);

	/**
	 * Returns the value of the '<em><b>Bindings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MKeyBinding}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bindings</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bindings</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getModelComponent_Bindings()
	 * @model containment="true"
	 * @generated
	 */
	EList<MKeyBinding> getBindings();

} // MModelComponent
