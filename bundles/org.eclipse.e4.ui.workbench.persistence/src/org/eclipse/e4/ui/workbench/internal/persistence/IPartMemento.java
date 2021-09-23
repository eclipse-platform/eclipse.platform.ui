/**
 * Copyright (c) 2021 EclipseSource GmbH and others.
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:   
 *     EclipseSource GmbH - initial API and implementation
 */
package org.eclipse.e4.ui.workbench.internal.persistence;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Part Memento</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Container to easily store and restore part settings. To do so the id of the part as well as the stringified memento is stored.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento#getPartId <em>Part Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento#getMemento <em>Memento</em>}</li>
 * </ul>
 *
 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage#getPartMemento()
 * @model
 * @generated
 */
public interface IPartMemento extends EObject {
	/**
	 * Returns the value of the '<em><b>Part Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The id of the part the memento is stored for.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Part Id</em>' attribute.
	 * @see #setPartId(String)
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage#getPartMemento_PartId()
	 * @model
	 * @generated
	 */
	String getPartId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento#getPartId <em>Part Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Part Id</em>' attribute.
	 * @see #getPartId()
	 * @generated
	 */
	void setPartId(String value);

	/**
	 * Returns the value of the '<em><b>Memento</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The stringified memento. This way it can be directly set into the persisted state and thus used by the workbench to pass into the ViewPart#init method.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Memento</em>' attribute.
	 * @see #setMemento(String)
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage#getPartMemento_Memento()
	 * @model
	 * @generated
	 */
	String getMemento();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento#getMemento <em>Memento</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Memento</em>' attribute.
	 * @see #getMemento()
	 * @generated
	 */
	void setMemento(String value);

} // IPartMemento
