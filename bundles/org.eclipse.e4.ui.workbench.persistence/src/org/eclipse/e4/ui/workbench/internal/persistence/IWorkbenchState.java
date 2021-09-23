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

import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;

import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Workbench State</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A container for the relevant parts of a workbench state.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getPerspective <em>Perspective</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getViewSettings <em>View Settings</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getEditorArea <em>Editor Area</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getTrimBars <em>Trim Bars</em>}</li>
 * </ul>
 *
 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage#getWorkbenchState()
 * @model
 * @generated
 */
public interface IWorkbenchState extends EObject {
	/**
	 * Returns the value of the '<em><b>Perspective</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The active perspective of the workbench.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Perspective</em>' containment reference.
	 * @see #setPerspective(MPerspective)
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage#getWorkbenchState_Perspective()
	 * @model containment="true"
	 * @generated
	 */
	MPerspective getPerspective();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getPerspective <em>Perspective</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Perspective</em>' containment reference.
	 * @see #getPerspective()
	 * @generated
	 */
	void setPerspective(MPerspective value);

	/**
	 * Returns the value of the '<em><b>View Settings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Store view settings with part id and its stringified memento.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>View Settings</em>' containment reference list.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage#getWorkbenchState_ViewSettings()
	 * @model containment="true"
	 * @generated
	 */
	EList<IPartMemento> getViewSettings();

	/**
	 * Returns the value of the '<em><b>Editor Area</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The editor area of the workbench.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Editor Area</em>' containment reference.
	 * @see #setEditorArea(MArea)
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage#getWorkbenchState_EditorArea()
	 * @model containment="true"
	 * @generated
	 */
	MArea getEditorArea();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getEditorArea <em>Editor Area</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Editor Area</em>' containment reference.
	 * @see #getEditorArea()
	 * @generated
	 */
	void setEditorArea(MArea value);

	/**
	 * Returns the value of the '<em><b>Trim Bars</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MTrimBar}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * All trimbars of the workbench.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Trim Bars</em>' containment reference list.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage#getWorkbenchState_TrimBars()
	 * @model containment="true"
	 * @generated
	 */
	EList<MTrimBar> getTrimBars();

} // IWorkbenchState
