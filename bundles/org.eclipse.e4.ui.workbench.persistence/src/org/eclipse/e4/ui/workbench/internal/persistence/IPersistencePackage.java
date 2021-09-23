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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistenceFactory
 * @model kind="package"
 * @generated
 */
public interface IPersistencePackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "persistence"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/ui/persistence"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "org.eclipse.ui.persistence"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	IPersistencePackage eINSTANCE = org.eclipse.e4.ui.workbench.internal.persistence.impl.PersistencePackage.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.workbench.internal.persistence.impl.WorkbenchState <em>Workbench State</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.impl.WorkbenchState
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.impl.PersistencePackage#getWorkbenchState()
	 * @generated
	 */
	int WORKBENCH_STATE = 0;

	/**
	 * The feature id for the '<em><b>Perspective</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_STATE__PERSPECTIVE = 0;

	/**
	 * The feature id for the '<em><b>View Settings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_STATE__VIEW_SETTINGS = 1;

	/**
	 * The feature id for the '<em><b>Editor Area</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_STATE__EDITOR_AREA = 2;

	/**
	 * The feature id for the '<em><b>Trim Bars</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_STATE__TRIM_BARS = 3;

	/**
	 * The number of structural features of the '<em>Workbench State</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_STATE_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Workbench State</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_STATE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.workbench.internal.persistence.impl.PartMemento <em>Part Memento</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.impl.PartMemento
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.impl.PersistencePackage#getPartMemento()
	 * @generated
	 */
	int PART_MEMENTO = 1;

	/**
	 * The feature id for the '<em><b>Part Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_MEMENTO__PART_ID = 0;

	/**
	 * The feature id for the '<em><b>Memento</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_MEMENTO__MEMENTO = 1;

	/**
	 * The number of structural features of the '<em>Part Memento</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_MEMENTO_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Part Memento</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_MEMENTO_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState <em>Workbench State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Workbench State</em>'.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState
	 * @generated
	 */
	EClass getWorkbenchState();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getPerspective <em>Perspective</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Perspective</em>'.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getPerspective()
	 * @see #getWorkbenchState()
	 * @generated
	 */
	EReference getWorkbenchState_Perspective();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getViewSettings <em>View Settings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>View Settings</em>'.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getViewSettings()
	 * @see #getWorkbenchState()
	 * @generated
	 */
	EReference getWorkbenchState_ViewSettings();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getEditorArea <em>Editor Area</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Editor Area</em>'.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getEditorArea()
	 * @see #getWorkbenchState()
	 * @generated
	 */
	EReference getWorkbenchState_EditorArea();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getTrimBars <em>Trim Bars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Trim Bars</em>'.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState#getTrimBars()
	 * @see #getWorkbenchState()
	 * @generated
	 */
	EReference getWorkbenchState_TrimBars();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento <em>Part Memento</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Memento</em>'.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento
	 * @generated
	 */
	EClass getPartMemento();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento#getPartId <em>Part Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Part Id</em>'.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento#getPartId()
	 * @see #getPartMemento()
	 * @generated
	 */
	EAttribute getPartMemento_PartId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento#getMemento <em>Memento</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Memento</em>'.
	 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento#getMemento()
	 * @see #getPartMemento()
	 * @generated
	 */
	EAttribute getPartMemento_Memento();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	IPersistenceFactory getPersistenceFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.workbench.internal.persistence.impl.WorkbenchState <em>Workbench State</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.workbench.internal.persistence.impl.WorkbenchState
		 * @see org.eclipse.e4.ui.workbench.internal.persistence.impl.PersistencePackage#getWorkbenchState()
		 * @generated
		 */
		EClass WORKBENCH_STATE = eINSTANCE.getWorkbenchState();

		/**
		 * The meta object literal for the '<em><b>Perspective</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WORKBENCH_STATE__PERSPECTIVE = eINSTANCE.getWorkbenchState_Perspective();

		/**
		 * The meta object literal for the '<em><b>View Settings</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WORKBENCH_STATE__VIEW_SETTINGS = eINSTANCE.getWorkbenchState_ViewSettings();

		/**
		 * The meta object literal for the '<em><b>Editor Area</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WORKBENCH_STATE__EDITOR_AREA = eINSTANCE.getWorkbenchState_EditorArea();

		/**
		 * The meta object literal for the '<em><b>Trim Bars</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WORKBENCH_STATE__TRIM_BARS = eINSTANCE.getWorkbenchState_TrimBars();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.workbench.internal.persistence.impl.PartMemento <em>Part Memento</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.workbench.internal.persistence.impl.PartMemento
		 * @see org.eclipse.e4.ui.workbench.internal.persistence.impl.PersistencePackage#getPartMemento()
		 * @generated
		 */
		EClass PART_MEMENTO = eINSTANCE.getPartMemento();

		/**
		 * The meta object literal for the '<em><b>Part Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PART_MEMENTO__PART_ID = eINSTANCE.getPartMemento_PartId();

		/**
		 * The meta object literal for the '<em><b>Memento</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PART_MEMENTO__MEMENTO = eINSTANCE.getPartMemento_Memento();

	}

} //IPersistencePackage
