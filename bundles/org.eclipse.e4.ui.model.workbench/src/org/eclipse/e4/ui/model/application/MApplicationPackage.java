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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.application.MApplicationFactory
 * @model kind="package"
 * @generated
 */
public interface MApplicationPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "application"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/ui/2008/UIModel"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "application"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MApplicationPackage eINSTANCE = org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV____________Abstract_____________V <em>VAbstract V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV____________Abstract_____________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV____________Abstract_____________V()
	 * @generated
	 */
	int VABSTRACT_V = 0;

	/**
	 * The number of structural features of the '<em>VAbstract V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VABSTRACT_V_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl <em>Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getApplicationElement()
	 * @generated
	 */
	int APPLICATION_ELEMENT = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION_ELEMENT__ID = 0;

	/**
	 * The number of structural features of the '<em>Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION_ELEMENT_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ContributionImpl <em>Contribution</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ContributionImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getContribution()
	 * @generated
	 */
	int CONTRIBUTION = 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__URI = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__OBJECT = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__PERSISTED_STATE = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Contribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.CommandImpl <em>Command</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.CommandImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getCommand()
	 * @generated
	 */
	int COMMAND = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Command URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__COMMAND_URI = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Impl</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__IMPL = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Args</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__ARGS = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__COMMAND_NAME = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Command</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.InputImpl <em>Input</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.InputImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getInput()
	 * @generated
	 */
	int INPUT = 4;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT__INPUT_URI = 0;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT__DIRTY = 1;

	/**
	 * The number of structural features of the '<em>Input</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ParameterImpl <em>Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ParameterImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getParameter()
	 * @generated
	 */
	int PARAMETER = 5;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tag</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__TAG = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__VALUE = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.UIItemImpl <em>UI Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.UIItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getUIItem()
	 * @generated
	 */
	int UI_ITEM = 6;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ITEM__NAME = 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ITEM__ICON_URI = 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ITEM__TOOLTIP = 2;

	/**
	 * The number of structural features of the '<em>UI Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ITEM_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.UIElementImpl <em>UI Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.UIElementImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getUIElement()
	 * @generated
	 */
	int UI_ELEMENT = 7;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__WIDGET = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__FACTORY = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__VISIBLE = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__PARENT = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>UI Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ContextImpl <em>Context</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ContextImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getContext()
	 * @generated
	 */
	int CONTEXT = 8;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTEXT__CONTEXT = 0;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTEXT__VARIABLES = 1;

	/**
	 * The number of structural features of the '<em>Context</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTEXT_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV_________Testing__________V <em>VTesting V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV_________Testing__________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV_________Testing__________V()
	 * @generated
	 */
	int VTESTING_V = 9;

	/**
	 * The number of structural features of the '<em>VTesting V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VTESTING_V_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl <em>Test Harness</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.TestHarnessImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getTestHarness()
	 * @generated
	 */
	int TEST_HARNESS = 10;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Command URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__COMMAND_URI = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Impl</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__IMPL = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Args</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ARGS = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__COMMAND_NAME = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CONTEXT = APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VARIABLES = APPLICATION_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__URI = APPLICATION_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__OBJECT = APPLICATION_ELEMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PERSISTED_STATE = APPLICATION_ELEMENT_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__WIDGET = APPLICATION_ELEMENT_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__FACTORY = APPLICATION_ELEMENT_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VISIBLE = APPLICATION_ELEMENT_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PARENT = APPLICATION_ELEMENT_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CHILDREN = APPLICATION_ELEMENT_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ACTIVE_CHILD = APPLICATION_ELEMENT_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Tag</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TAG = APPLICATION_ELEMENT_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VALUE = APPLICATION_ELEMENT_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__INPUT_URI = APPLICATION_ELEMENT_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__DIRTY = APPLICATION_ELEMENT_FEATURE_COUNT + 18;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__NAME = APPLICATION_ELEMENT_FEATURE_COUNT + 19;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ICON_URI = APPLICATION_ELEMENT_FEATURE_COUNT + 20;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TOOLTIP = APPLICATION_ELEMENT_FEATURE_COUNT + 21;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ENABLED = APPLICATION_ELEMENT_FEATURE_COUNT + 22;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__SELECTED = APPLICATION_ELEMENT_FEATURE_COUNT + 23;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__SEPARATOR = APPLICATION_ELEMENT_FEATURE_COUNT + 24;

	/**
	 * The number of structural features of the '<em>Test Harness</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 25;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V <em>VAbstract Containers V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV_________AbstractContainers__________V()
	 * @generated
	 */
	int VABSTRACT_CONTAINERS_V = 11;

	/**
	 * The number of structural features of the '<em>VAbstract Containers V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VABSTRACT_CONTAINERS_V_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ElementContainerImpl <em>Element Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ElementContainerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getElementContainer()
	 * @generated
	 */
	int ELEMENT_CONTAINER = 12;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__CHILDREN = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__ACTIVE_CHILD = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Element Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.GenericTileImpl <em>Generic Tile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.GenericTileImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getGenericTile()
	 * @generated
	 */
	int GENERIC_TILE = 13;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__FACTORY = ELEMENT_CONTAINER__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__ACTIVE_CHILD = ELEMENT_CONTAINER__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Weights</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__WEIGHTS = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__HORIZONTAL = ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Generic Tile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.TrimStructureImpl <em>Trim Structure</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.TrimStructureImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getTrimStructure()
	 * @generated
	 */
	int TRIM_STRUCTURE = 14;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__CHILDREN = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__ACTIVE_CHILD = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Top</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__TOP = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Bottom</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__BOTTOM = UI_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Left</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__LEFT = UI_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Right</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE__RIGHT = UI_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Trim Structure</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_STRUCTURE_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV______________RCP_______________V <em>VRCP V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV______________RCP_______________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________RCP_______________V()
	 * @generated
	 */
	int VRCP_V = 15;

	/**
	 * The number of structural features of the '<em>VRCP V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VRCP_V_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl <em>Application</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getApplication()
	 * @generated
	 */
	int APPLICATION = 16;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__CONTEXT = CONTEXT__CONTEXT;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__VARIABLES = CONTEXT__VARIABLES;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__ID = CONTEXT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__WIDGET = CONTEXT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__FACTORY = CONTEXT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__VISIBLE = CONTEXT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__PARENT = CONTEXT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__CHILDREN = CONTEXT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__ACTIVE_CHILD = CONTEXT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Commands</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__COMMANDS = CONTEXT_FEATURE_COUNT + 7;

	/**
	 * The number of structural features of the '<em>Application</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION_FEATURE_COUNT = CONTEXT_FEATURE_COUNT + 8;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ItemImpl <em>Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getItem()
	 * @generated
	 */
	int ITEM = 17;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__NAME = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__ICON_URI = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__TOOLTIP = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__URI = UI_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__OBJECT = UI_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__PERSISTED_STATE = UI_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__ENABLED = UI_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__SELECTED = UI_ELEMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__SEPARATOR = UI_ELEMENT_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 9;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.HandledItemImpl <em>Handled Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.HandledItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandledItem()
	 * @generated
	 */
	int HANDLED_ITEM = 18;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__ID = ITEM__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__WIDGET = ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__FACTORY = ITEM__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__VISIBLE = ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__PARENT = ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__NAME = ITEM__NAME;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__ICON_URI = ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__TOOLTIP = ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__URI = ITEM__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__OBJECT = ITEM__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__PERSISTED_STATE = ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__ENABLED = ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__SELECTED = ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__SEPARATOR = ITEM__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__COMMAND = ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__MENU = ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__WB_COMMAND = ITEM_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__PARAMETERS = ITEM_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Handled Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM_FEATURE_COUNT = ITEM_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.MenuItemImpl <em>Menu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.MenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMenuItem()
	 * @generated
	 */
	int MENU_ITEM = 19;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__ID = ITEM__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__WIDGET = ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__FACTORY = ITEM__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__VISIBLE = ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__PARENT = ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__NAME = ITEM__NAME;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__ICON_URI = ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__TOOLTIP = ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__URI = ITEM__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__OBJECT = ITEM__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__PERSISTED_STATE = ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__ENABLED = ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__SELECTED = ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__SEPARATOR = ITEM__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__CHILDREN = ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__ACTIVE_CHILD = ITEM_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM_FEATURE_COUNT = ITEM_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.MenuImpl <em>Menu</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.MenuImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMenu()
	 * @generated
	 */
	int MENU = 20;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__CHILDREN = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__ACTIVE_CHILD = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ToolItemImpl <em>Tool Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ToolItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getToolItem()
	 * @generated
	 */
	int TOOL_ITEM = 21;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__ID = ITEM__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__WIDGET = ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__FACTORY = ITEM__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__VISIBLE = ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__PARENT = ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__NAME = ITEM__NAME;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__ICON_URI = ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__TOOLTIP = ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__URI = ITEM__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__OBJECT = ITEM__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__PERSISTED_STATE = ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__ENABLED = ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__SELECTED = ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__SEPARATOR = ITEM__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__CHILDREN = ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__ACTIVE_CHILD = ITEM_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM_FEATURE_COUNT = ITEM_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ToolBarImpl <em>Tool Bar</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ToolBarImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getToolBar()
	 * @generated
	 */
	int TOOL_BAR = 22;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__CHILDREN = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__ACTIVE_CHILD = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Tool Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MPSCElement <em>PSC Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MPSCElement
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPSCElement()
	 * @generated
	 */
	int PSC_ELEMENT = 23;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The number of structural features of the '<em>PSC Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PartImpl <em>Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PartImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPart()
	 * @generated
	 */
	int PART = 24;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__ID = CONTRIBUTION__ID;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__URI = CONTRIBUTION__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__OBJECT = CONTRIBUTION__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__PERSISTED_STATE = CONTRIBUTION__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__CONTEXT = CONTRIBUTION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__VARIABLES = CONTRIBUTION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__WIDGET = CONTRIBUTION_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__FACTORY = CONTRIBUTION_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__VISIBLE = CONTRIBUTION_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__PARENT = CONTRIBUTION_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__NAME = CONTRIBUTION_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__ICON_URI = CONTRIBUTION_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__TOOLTIP = CONTRIBUTION_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__MENUS = CONTRIBUTION_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__TOOLBAR = CONTRIBUTION_FEATURE_COUNT + 10;

	/**
	 * The number of structural features of the '<em>Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_FEATURE_COUNT = CONTRIBUTION_FEATURE_COUNT + 11;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PartStackImpl <em>Part Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PartStackImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartStack()
	 * @generated
	 */
	int PART_STACK = 25;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__FACTORY = ELEMENT_CONTAINER__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__ACTIVE_CHILD = ELEMENT_CONTAINER__ACTIVE_CHILD;

	/**
	 * The number of structural features of the '<em>Part Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PartSashContainerImpl <em>Part Sash Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PartSashContainerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartSashContainer()
	 * @generated
	 */
	int PART_SASH_CONTAINER = 26;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__ID = GENERIC_TILE__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__WIDGET = GENERIC_TILE__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__FACTORY = GENERIC_TILE__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__VISIBLE = GENERIC_TILE__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__PARENT = GENERIC_TILE__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__CHILDREN = GENERIC_TILE__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__ACTIVE_CHILD = GENERIC_TILE__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Weights</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__WEIGHTS = GENERIC_TILE__WEIGHTS;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__HORIZONTAL = GENERIC_TILE__HORIZONTAL;

	/**
	 * The number of structural features of the '<em>Part Sash Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER_FEATURE_COUNT = GENERIC_TILE_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.WindowImpl <em>Window</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.WindowImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getWindow()
	 * @generated
	 */
	int WINDOW = 27;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__NAME = UI_ITEM__NAME;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__ICON_URI = UI_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__TOOLTIP = UI_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__ID = UI_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__WIDGET = UI_ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__FACTORY = UI_ITEM_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__VISIBLE = UI_ITEM_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__PARENT = UI_ITEM_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__CHILDREN = UI_ITEM_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__ACTIVE_CHILD = UI_ITEM_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__CONTEXT = UI_ITEM_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__VARIABLES = UI_ITEM_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Main Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__MAIN_MENU = UI_ITEM_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__X = UI_ITEM_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__Y = UI_ITEM_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__WIDTH = UI_ITEM_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__HEIGHT = UI_ITEM_FEATURE_COUNT + 13;

	/**
	 * The number of structural features of the '<em>Window</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_FEATURE_COUNT = UI_ITEM_FEATURE_COUNT + 14;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV______________IDE_______________V <em>VIDE V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV______________IDE_______________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________IDE_______________V()
	 * @generated
	 */
	int VIDE_V = 28;

	/**
	 * The number of structural features of the '<em>VIDE V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIDE_V_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MVSCElement <em>VSC Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MVSCElement
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getVSCElement()
	 * @generated
	 */
	int VSC_ELEMENT = 29;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VSC_ELEMENT__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VSC_ELEMENT__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VSC_ELEMENT__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VSC_ELEMENT__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VSC_ELEMENT__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The number of structural features of the '<em>VSC Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VSC_ELEMENT_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ViewImpl <em>View</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ViewImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getView()
	 * @generated
	 */
	int VIEW = 30;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__ID = PART__ID;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__URI = PART__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__OBJECT = PART__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__PERSISTED_STATE = PART__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__CONTEXT = PART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__VARIABLES = PART__VARIABLES;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__WIDGET = PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__FACTORY = PART__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__VISIBLE = PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__PARENT = PART__PARENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__NAME = PART__NAME;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__ICON_URI = PART__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__TOOLTIP = PART__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__MENUS = PART__MENUS;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__TOOLBAR = PART__TOOLBAR;

	/**
	 * The number of structural features of the '<em>View</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_FEATURE_COUNT = PART_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ViewStackImpl <em>View Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ViewStackImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getViewStack()
	 * @generated
	 */
	int VIEW_STACK = 31;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_STACK__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_STACK__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_STACK__FACTORY = ELEMENT_CONTAINER__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_STACK__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_STACK__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_STACK__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_STACK__ACTIVE_CHILD = ELEMENT_CONTAINER__ACTIVE_CHILD;

	/**
	 * The number of structural features of the '<em>View Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_STACK_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ViewSashContainerImpl <em>View Sash Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ViewSashContainerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getViewSashContainer()
	 * @generated
	 */
	int VIEW_SASH_CONTAINER = 32;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__ID = GENERIC_TILE__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__WIDGET = GENERIC_TILE__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__FACTORY = GENERIC_TILE__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__VISIBLE = GENERIC_TILE__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__PARENT = GENERIC_TILE__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__CHILDREN = GENERIC_TILE__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__ACTIVE_CHILD = GENERIC_TILE__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Weights</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__WEIGHTS = GENERIC_TILE__WEIGHTS;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER__HORIZONTAL = GENERIC_TILE__HORIZONTAL;

	/**
	 * The number of structural features of the '<em>View Sash Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_SASH_CONTAINER_FEATURE_COUNT = GENERIC_TILE_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.EditorImpl <em>Editor</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.EditorImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getEditor()
	 * @generated
	 */
	int EDITOR = 33;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__ID = PART__ID;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__URI = PART__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__OBJECT = PART__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__PERSISTED_STATE = PART__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__CONTEXT = PART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__VARIABLES = PART__VARIABLES;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__WIDGET = PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__FACTORY = PART__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__VISIBLE = PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__PARENT = PART__PARENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__NAME = PART__NAME;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__ICON_URI = PART__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__TOOLTIP = PART__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__MENUS = PART__MENUS;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__TOOLBAR = PART__TOOLBAR;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__INPUT_URI = PART_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR__DIRTY = PART_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Editor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_FEATURE_COUNT = PART_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.MultiEditorImpl <em>Multi Editor</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.MultiEditorImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMultiEditor()
	 * @generated
	 */
	int MULTI_EDITOR = 34;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__ID = EDITOR__ID;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__URI = EDITOR__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__OBJECT = EDITOR__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__PERSISTED_STATE = EDITOR__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__CONTEXT = EDITOR__CONTEXT;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__VARIABLES = EDITOR__VARIABLES;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__WIDGET = EDITOR__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__FACTORY = EDITOR__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__VISIBLE = EDITOR__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__PARENT = EDITOR__PARENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__NAME = EDITOR__NAME;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__ICON_URI = EDITOR__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__TOOLTIP = EDITOR__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__MENUS = EDITOR__MENUS;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__TOOLBAR = EDITOR__TOOLBAR;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__INPUT_URI = EDITOR__INPUT_URI;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__DIRTY = EDITOR__DIRTY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__CHILDREN = EDITOR_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR__ACTIVE_CHILD = EDITOR_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Multi Editor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MULTI_EDITOR_FEATURE_COUNT = EDITOR_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MESCElement <em>ESC Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MESCElement
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getESCElement()
	 * @generated
	 */
	int ESC_ELEMENT = 35;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ESC_ELEMENT__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ESC_ELEMENT__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ESC_ELEMENT__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ESC_ELEMENT__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ESC_ELEMENT__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The number of structural features of the '<em>ESC Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ESC_ELEMENT_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.EditorStackImpl <em>Editor Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.EditorStackImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getEditorStack()
	 * @generated
	 */
	int EDITOR_STACK = 36;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK__FACTORY = ELEMENT_CONTAINER__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK__ACTIVE_CHILD = ELEMENT_CONTAINER__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK__INPUT_URI = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Editor Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_STACK_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.EditorSashContainerImpl <em>Editor Sash Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.EditorSashContainerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getEditorSashContainer()
	 * @generated
	 */
	int EDITOR_SASH_CONTAINER = 37;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__ID = GENERIC_TILE__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__WIDGET = GENERIC_TILE__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__FACTORY = GENERIC_TILE__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__VISIBLE = GENERIC_TILE__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__PARENT = GENERIC_TILE__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__CHILDREN = GENERIC_TILE__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__ACTIVE_CHILD = GENERIC_TILE__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Weights</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__WEIGHTS = GENERIC_TILE__WEIGHTS;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER__HORIZONTAL = GENERIC_TILE__HORIZONTAL;

	/**
	 * The number of structural features of the '<em>Editor Sash Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDITOR_SASH_CONTAINER_FEATURE_COUNT = GENERIC_TILE_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PerspectiveImpl <em>Perspective</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PerspectiveImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPerspective()
	 * @generated
	 */
	int PERSPECTIVE = 38;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__NAME = UI_ITEM__NAME;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ICON_URI = UI_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__TOOLTIP = UI_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ID = UI_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__WIDGET = UI_ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__FACTORY = UI_ITEM_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__VISIBLE = UI_ITEM_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__PARENT = UI_ITEM_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__CHILDREN = UI_ITEM_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ACTIVE_CHILD = UI_ITEM_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__CONTEXT = UI_ITEM_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__VARIABLES = UI_ITEM_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>Perspective</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_FEATURE_COUNT = UI_ITEM_FEATURE_COUNT + 9;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PerspectiveStackImpl <em>Perspective Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PerspectiveStackImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPerspectiveStack()
	 * @generated
	 */
	int PERSPECTIVE_STACK = 39;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__FACTORY = UI_ELEMENT__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__CHILDREN = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__ACTIVE_CHILD = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Perspective Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.IDEWindowImpl <em>IDE Window</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.IDEWindowImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getIDEWindow()
	 * @generated
	 */
	int IDE_WINDOW = 40;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__ID = TRIM_STRUCTURE__ID;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__WIDGET = TRIM_STRUCTURE__WIDGET;

	/**
	 * The feature id for the '<em><b>Factory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__FACTORY = TRIM_STRUCTURE__FACTORY;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__VISIBLE = TRIM_STRUCTURE__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__PARENT = TRIM_STRUCTURE__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__CHILDREN = TRIM_STRUCTURE__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__ACTIVE_CHILD = TRIM_STRUCTURE__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Top</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__TOP = TRIM_STRUCTURE__TOP;

	/**
	 * The feature id for the '<em><b>Bottom</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__BOTTOM = TRIM_STRUCTURE__BOTTOM;

	/**
	 * The feature id for the '<em><b>Left</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__LEFT = TRIM_STRUCTURE__LEFT;

	/**
	 * The feature id for the '<em><b>Right</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__RIGHT = TRIM_STRUCTURE__RIGHT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__NAME = TRIM_STRUCTURE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__ICON_URI = TRIM_STRUCTURE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__TOOLTIP = TRIM_STRUCTURE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__CONTEXT = TRIM_STRUCTURE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__VARIABLES = TRIM_STRUCTURE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Main Menu</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW__MAIN_MENU = TRIM_STRUCTURE_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>IDE Window</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDE_WINDOW_FEATURE_COUNT = TRIM_STRUCTURE_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '<em>IEclipse Context</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.core.services.context.IEclipseContext
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getIEclipseContext()
	 * @generated
	 */
	int IECLIPSE_CONTEXT = 41;

	/**
	 * The meta object id for the '<em>Parameterized Command</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.core.commands.ParameterizedCommand
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getParameterizedCommand()
	 * @generated
	 */
	int PARAMETERIZED_COMMAND = 42;


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV____________Abstract_____________V <em>VAbstract V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VAbstract V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV____________Abstract_____________V
	 * @generated
	 */
	EClass getV____________Abstract_____________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MApplicationElement <em>Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement
	 * @generated
	 */
	EClass getApplicationElement();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement#getId()
	 * @see #getApplicationElement()
	 * @generated
	 */
	EAttribute getApplicationElement_Id();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MContribution <em>Contribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Contribution</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution
	 * @generated
	 */
	EClass getContribution();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContribution#getURI <em>URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getURI()
	 * @see #getContribution()
	 * @generated
	 */
	EAttribute getContribution_URI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContribution#getObject <em>Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getObject()
	 * @see #getContribution()
	 * @generated
	 */
	EAttribute getContribution_Object();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContribution#getPersistedState <em>Persisted State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Persisted State</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getPersistedState()
	 * @see #getContribution()
	 * @generated
	 */
	EAttribute getContribution_PersistedState();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand
	 * @generated
	 */
	EClass getCommand();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommand#getCommandURI <em>Command URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Command URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand#getCommandURI()
	 * @see #getCommand()
	 * @generated
	 */
	EAttribute getCommand_CommandURI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommand#getImpl <em>Impl</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Impl</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand#getImpl()
	 * @see #getCommand()
	 * @generated
	 */
	EAttribute getCommand_Impl();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.e4.ui.model.application.MCommand#getArgs <em>Args</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Args</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand#getArgs()
	 * @see #getCommand()
	 * @generated
	 */
	EAttribute getCommand_Args();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommand#getCommandName <em>Command Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Command Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand#getCommandName()
	 * @see #getCommand()
	 * @generated
	 */
	EAttribute getCommand_CommandName();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MInput <em>Input</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Input</em>'.
	 * @see org.eclipse.e4.ui.model.application.MInput
	 * @generated
	 */
	EClass getInput();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MInput#getInputURI <em>Input URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Input URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MInput#getInputURI()
	 * @see #getInput()
	 * @generated
	 */
	EAttribute getInput_InputURI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MInput#isDirty <em>Dirty</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dirty</em>'.
	 * @see org.eclipse.e4.ui.model.application.MInput#isDirty()
	 * @see #getInput()
	 * @generated
	 */
	EAttribute getInput_Dirty();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MParameter <em>Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter</em>'.
	 * @see org.eclipse.e4.ui.model.application.MParameter
	 * @generated
	 */
	EClass getParameter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MParameter#getTag <em>Tag</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tag</em>'.
	 * @see org.eclipse.e4.ui.model.application.MParameter#getTag()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Tag();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MParameter#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.e4.ui.model.application.MParameter#getValue()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MUIItem <em>UI Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>UI Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIItem
	 * @generated
	 */
	EClass getUIItem();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIItem#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIItem#getName()
	 * @see #getUIItem()
	 * @generated
	 */
	EAttribute getUIItem_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIItem#getIconURI <em>Icon URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Icon URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIItem#getIconURI()
	 * @see #getUIItem()
	 * @generated
	 */
	EAttribute getUIItem_IconURI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIItem#getTooltip <em>Tooltip</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tooltip</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIItem#getTooltip()
	 * @see #getUIItem()
	 * @generated
	 */
	EAttribute getUIItem_Tooltip();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MUIElement <em>UI Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>UI Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement
	 * @generated
	 */
	EClass getUIElement();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#getWidget <em>Widget</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Widget</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#getWidget()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_Widget();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#getFactory <em>Factory</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Factory</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#getFactory()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_Factory();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#isVisible <em>Visible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visible</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#isVisible()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_Visible();

	/**
	 * Returns the meta object for the container reference '{@link org.eclipse.e4.ui.model.application.MUIElement#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Parent</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#getParent()
	 * @see #getUIElement()
	 * @generated
	 */
	EReference getUIElement_Parent();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MContext <em>Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContext
	 * @generated
	 */
	EClass getContext();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContext#getContext <em>Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContext#getContext()
	 * @see #getContext()
	 * @generated
	 */
	EAttribute getContext_Context();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.e4.ui.model.application.MContext#getVariables <em>Variables</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Variables</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContext#getVariables()
	 * @see #getContext()
	 * @generated
	 */
	EAttribute getContext_Variables();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV_________Testing__________V <em>VTesting V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VTesting V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV_________Testing__________V
	 * @generated
	 */
	EClass getV_________Testing__________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MTestHarness <em>Test Harness</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Test Harness</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTestHarness
	 * @generated
	 */
	EClass getTestHarness();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V <em>VAbstract Containers V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VAbstract Containers V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V
	 * @generated
	 */
	EClass getV_________AbstractContainers__________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MElementContainer <em>Element Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Element Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MElementContainer
	 * @generated
	 */
	EClass getElementContainer();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MElementContainer#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.e4.ui.model.application.MElementContainer#getChildren()
	 * @see #getElementContainer()
	 * @generated
	 */
	EReference getElementContainer_Children();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MElementContainer#getActiveChild <em>Active Child</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Active Child</em>'.
	 * @see org.eclipse.e4.ui.model.application.MElementContainer#getActiveChild()
	 * @see #getElementContainer()
	 * @generated
	 */
	EReference getElementContainer_ActiveChild();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MGenericTile <em>Generic Tile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Generic Tile</em>'.
	 * @see org.eclipse.e4.ui.model.application.MGenericTile
	 * @generated
	 */
	EClass getGenericTile();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.e4.ui.model.application.MGenericTile#getWeights <em>Weights</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Weights</em>'.
	 * @see org.eclipse.e4.ui.model.application.MGenericTile#getWeights()
	 * @see #getGenericTile()
	 * @generated
	 */
	EAttribute getGenericTile_Weights();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MGenericTile#isHorizontal <em>Horizontal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Horizontal</em>'.
	 * @see org.eclipse.e4.ui.model.application.MGenericTile#isHorizontal()
	 * @see #getGenericTile()
	 * @generated
	 */
	EAttribute getGenericTile_Horizontal();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MTrimStructure <em>Trim Structure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Trim Structure</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimStructure
	 * @generated
	 */
	EClass getTrimStructure();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MTrimStructure#getTop <em>Top</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Top</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimStructure#getTop()
	 * @see #getTrimStructure()
	 * @generated
	 */
	EReference getTrimStructure_Top();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MTrimStructure#getBottom <em>Bottom</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Bottom</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimStructure#getBottom()
	 * @see #getTrimStructure()
	 * @generated
	 */
	EReference getTrimStructure_Bottom();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MTrimStructure#getLeft <em>Left</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Left</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimStructure#getLeft()
	 * @see #getTrimStructure()
	 * @generated
	 */
	EReference getTrimStructure_Left();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MTrimStructure#getRight <em>Right</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Right</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimStructure#getRight()
	 * @see #getTrimStructure()
	 * @generated
	 */
	EReference getTrimStructure_Right();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV______________RCP_______________V <em>VRCP V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VRCP V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV______________RCP_______________V
	 * @generated
	 */
	EClass getV______________RCP_______________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MApplication <em>Application</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Application</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplication
	 * @generated
	 */
	EClass getApplication();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MApplication#getCommands <em>Commands</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Commands</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplication#getCommands()
	 * @see #getApplication()
	 * @generated
	 */
	EReference getApplication_Commands();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MItem <em>Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem
	 * @generated
	 */
	EClass getItem();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#isEnabled <em>Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Enabled</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#isEnabled()
	 * @see #getItem()
	 * @generated
	 */
	EAttribute getItem_Enabled();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#isSelected <em>Selected</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Selected</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#isSelected()
	 * @see #getItem()
	 * @generated
	 */
	EAttribute getItem_Selected();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#isSeparator <em>Separator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Separator</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#isSeparator()
	 * @see #getItem()
	 * @generated
	 */
	EAttribute getItem_Separator();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MHandledItem <em>Handled Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handled Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem
	 * @generated
	 */
	EClass getHandledItem();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MHandledItem#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getCommand()
	 * @see #getHandledItem()
	 * @generated
	 */
	EReference getHandledItem_Command();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MHandledItem#getMenu <em>Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getMenu()
	 * @see #getHandledItem()
	 * @generated
	 */
	EReference getHandledItem_Menu();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MHandledItem#getWbCommand <em>Wb Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Wb Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getWbCommand()
	 * @see #getHandledItem()
	 * @generated
	 */
	EAttribute getHandledItem_WbCommand();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.e4.ui.model.application.MHandledItem#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Parameters</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getParameters()
	 * @see #getHandledItem()
	 * @generated
	 */
	EReference getHandledItem_Parameters();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MMenuItem <em>Menu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Menu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMenuItem
	 * @generated
	 */
	EClass getMenuItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MMenu <em>Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMenu
	 * @generated
	 */
	EClass getMenu();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MToolItem <em>Tool Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolItem
	 * @generated
	 */
	EClass getToolItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MToolBar <em>Tool Bar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Bar</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolBar
	 * @generated
	 */
	EClass getToolBar();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPSCElement <em>PSC Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>PSC Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPSCElement
	 * @generated
	 */
	EClass getPSCElement();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPart <em>Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart
	 * @generated
	 */
	EClass getPart();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.e4.ui.model.application.MPart#getMenus <em>Menus</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Menus</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getMenus()
	 * @see #getPart()
	 * @generated
	 */
	EReference getPart_Menus();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MPart#getToolbar <em>Toolbar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Toolbar</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getToolbar()
	 * @see #getPart()
	 * @generated
	 */
	EReference getPart_Toolbar();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPartStack <em>Part Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartStack
	 * @generated
	 */
	EClass getPartStack();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPartSashContainer <em>Part Sash Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Sash Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartSashContainer
	 * @generated
	 */
	EClass getPartSashContainer();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MWindow <em>Window</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Window</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow
	 * @generated
	 */
	EClass getWindow();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MWindow#getMainMenu <em>Main Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Main Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getMainMenu()
	 * @see #getWindow()
	 * @generated
	 */
	EReference getWindow_MainMenu();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getX <em>X</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>X</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getX()
	 * @see #getWindow()
	 * @generated
	 */
	EAttribute getWindow_X();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getY <em>Y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Y</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getY()
	 * @see #getWindow()
	 * @generated
	 */
	EAttribute getWindow_Y();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getWidth <em>Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Width</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getWidth()
	 * @see #getWindow()
	 * @generated
	 */
	EAttribute getWindow_Width();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getHeight <em>Height</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Height</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getHeight()
	 * @see #getWindow()
	 * @generated
	 */
	EAttribute getWindow_Height();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV______________IDE_______________V <em>VIDE V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VIDE V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV______________IDE_______________V
	 * @generated
	 */
	EClass getV______________IDE_______________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MVSCElement <em>VSC Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VSC Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MVSCElement
	 * @generated
	 */
	EClass getVSCElement();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MView <em>View</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>View</em>'.
	 * @see org.eclipse.e4.ui.model.application.MView
	 * @generated
	 */
	EClass getView();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MViewStack <em>View Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>View Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.MViewStack
	 * @generated
	 */
	EClass getViewStack();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MViewSashContainer <em>View Sash Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>View Sash Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MViewSashContainer
	 * @generated
	 */
	EClass getViewSashContainer();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MEditor <em>Editor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Editor</em>'.
	 * @see org.eclipse.e4.ui.model.application.MEditor
	 * @generated
	 */
	EClass getEditor();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MMultiEditor <em>Multi Editor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Multi Editor</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMultiEditor
	 * @generated
	 */
	EClass getMultiEditor();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MESCElement <em>ESC Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ESC Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MESCElement
	 * @generated
	 */
	EClass getESCElement();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MEditorStack <em>Editor Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Editor Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.MEditorStack
	 * @generated
	 */
	EClass getEditorStack();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MEditorStack#getInputURI <em>Input URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Input URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MEditorStack#getInputURI()
	 * @see #getEditorStack()
	 * @generated
	 */
	EAttribute getEditorStack_InputURI();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MEditorSashContainer <em>Editor Sash Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Editor Sash Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MEditorSashContainer
	 * @generated
	 */
	EClass getEditorSashContainer();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPerspective <em>Perspective</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Perspective</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPerspective
	 * @generated
	 */
	EClass getPerspective();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPerspectiveStack <em>Perspective Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Perspective Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPerspectiveStack
	 * @generated
	 */
	EClass getPerspectiveStack();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MIDEWindow <em>IDE Window</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDE Window</em>'.
	 * @see org.eclipse.e4.ui.model.application.MIDEWindow
	 * @generated
	 */
	EClass getIDEWindow();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MIDEWindow#getMainMenu <em>Main Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Main Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MIDEWindow#getMainMenu()
	 * @see #getIDEWindow()
	 * @generated
	 */
	EReference getIDEWindow_MainMenu();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.e4.core.services.context.IEclipseContext <em>IEclipse Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>IEclipse Context</em>'.
	 * @see org.eclipse.e4.core.services.context.IEclipseContext
	 * @model instanceClass="org.eclipse.e4.core.services.context.IEclipseContext" serializeable="false"
	 * @generated
	 */
	EDataType getIEclipseContext();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.core.commands.ParameterizedCommand <em>Parameterized Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Parameterized Command</em>'.
	 * @see org.eclipse.core.commands.ParameterizedCommand
	 * @model instanceClass="org.eclipse.core.commands.ParameterizedCommand" serializeable="false"
	 * @generated
	 */
	EDataType getParameterizedCommand();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	MApplicationFactory getApplicationFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV____________Abstract_____________V <em>VAbstract V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV____________Abstract_____________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV____________Abstract_____________V()
		 * @generated
		 */
		EClass VABSTRACT_V = eINSTANCE.getV____________Abstract_____________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl <em>Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getApplicationElement()
		 * @generated
		 */
		EClass APPLICATION_ELEMENT = eINSTANCE.getApplicationElement();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute APPLICATION_ELEMENT__ID = eINSTANCE.getApplicationElement_Id();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ContributionImpl <em>Contribution</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ContributionImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getContribution()
		 * @generated
		 */
		EClass CONTRIBUTION = eINSTANCE.getContribution();

		/**
		 * The meta object literal for the '<em><b>URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTRIBUTION__URI = eINSTANCE.getContribution_URI();

		/**
		 * The meta object literal for the '<em><b>Object</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTRIBUTION__OBJECT = eINSTANCE.getContribution_Object();

		/**
		 * The meta object literal for the '<em><b>Persisted State</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTRIBUTION__PERSISTED_STATE = eINSTANCE.getContribution_PersistedState();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.CommandImpl <em>Command</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.CommandImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getCommand()
		 * @generated
		 */
		EClass COMMAND = eINSTANCE.getCommand();

		/**
		 * The meta object literal for the '<em><b>Command URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND__COMMAND_URI = eINSTANCE.getCommand_CommandURI();

		/**
		 * The meta object literal for the '<em><b>Impl</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND__IMPL = eINSTANCE.getCommand_Impl();

		/**
		 * The meta object literal for the '<em><b>Args</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND__ARGS = eINSTANCE.getCommand_Args();

		/**
		 * The meta object literal for the '<em><b>Command Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND__COMMAND_NAME = eINSTANCE.getCommand_CommandName();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.InputImpl <em>Input</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.InputImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getInput()
		 * @generated
		 */
		EClass INPUT = eINSTANCE.getInput();

		/**
		 * The meta object literal for the '<em><b>Input URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT__INPUT_URI = eINSTANCE.getInput_InputURI();

		/**
		 * The meta object literal for the '<em><b>Dirty</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT__DIRTY = eINSTANCE.getInput_Dirty();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ParameterImpl <em>Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ParameterImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getParameter()
		 * @generated
		 */
		EClass PARAMETER = eINSTANCE.getParameter();

		/**
		 * The meta object literal for the '<em><b>Tag</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER__TAG = eINSTANCE.getParameter_Tag();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER__VALUE = eINSTANCE.getParameter_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.UIItemImpl <em>UI Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.UIItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getUIItem()
		 * @generated
		 */
		EClass UI_ITEM = eINSTANCE.getUIItem();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ITEM__NAME = eINSTANCE.getUIItem_Name();

		/**
		 * The meta object literal for the '<em><b>Icon URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ITEM__ICON_URI = eINSTANCE.getUIItem_IconURI();

		/**
		 * The meta object literal for the '<em><b>Tooltip</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ITEM__TOOLTIP = eINSTANCE.getUIItem_Tooltip();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.UIElementImpl <em>UI Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.UIElementImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getUIElement()
		 * @generated
		 */
		EClass UI_ELEMENT = eINSTANCE.getUIElement();

		/**
		 * The meta object literal for the '<em><b>Widget</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__WIDGET = eINSTANCE.getUIElement_Widget();

		/**
		 * The meta object literal for the '<em><b>Factory</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__FACTORY = eINSTANCE.getUIElement_Factory();

		/**
		 * The meta object literal for the '<em><b>Visible</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__VISIBLE = eINSTANCE.getUIElement_Visible();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference UI_ELEMENT__PARENT = eINSTANCE.getUIElement_Parent();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ContextImpl <em>Context</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ContextImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getContext()
		 * @generated
		 */
		EClass CONTEXT = eINSTANCE.getContext();

		/**
		 * The meta object literal for the '<em><b>Context</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTEXT__CONTEXT = eINSTANCE.getContext_Context();

		/**
		 * The meta object literal for the '<em><b>Variables</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTEXT__VARIABLES = eINSTANCE.getContext_Variables();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV_________Testing__________V <em>VTesting V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV_________Testing__________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV_________Testing__________V()
		 * @generated
		 */
		EClass VTESTING_V = eINSTANCE.getV_________Testing__________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl <em>Test Harness</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.TestHarnessImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getTestHarness()
		 * @generated
		 */
		EClass TEST_HARNESS = eINSTANCE.getTestHarness();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V <em>VAbstract Containers V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV_________AbstractContainers__________V()
		 * @generated
		 */
		EClass VABSTRACT_CONTAINERS_V = eINSTANCE.getV_________AbstractContainers__________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ElementContainerImpl <em>Element Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ElementContainerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getElementContainer()
		 * @generated
		 */
		EClass ELEMENT_CONTAINER = eINSTANCE.getElementContainer();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ELEMENT_CONTAINER__CHILDREN = eINSTANCE.getElementContainer_Children();

		/**
		 * The meta object literal for the '<em><b>Active Child</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ELEMENT_CONTAINER__ACTIVE_CHILD = eINSTANCE.getElementContainer_ActiveChild();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.GenericTileImpl <em>Generic Tile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.GenericTileImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getGenericTile()
		 * @generated
		 */
		EClass GENERIC_TILE = eINSTANCE.getGenericTile();

		/**
		 * The meta object literal for the '<em><b>Weights</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERIC_TILE__WEIGHTS = eINSTANCE.getGenericTile_Weights();

		/**
		 * The meta object literal for the '<em><b>Horizontal</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERIC_TILE__HORIZONTAL = eINSTANCE.getGenericTile_Horizontal();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.TrimStructureImpl <em>Trim Structure</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.TrimStructureImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getTrimStructure()
		 * @generated
		 */
		EClass TRIM_STRUCTURE = eINSTANCE.getTrimStructure();

		/**
		 * The meta object literal for the '<em><b>Top</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRIM_STRUCTURE__TOP = eINSTANCE.getTrimStructure_Top();

		/**
		 * The meta object literal for the '<em><b>Bottom</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRIM_STRUCTURE__BOTTOM = eINSTANCE.getTrimStructure_Bottom();

		/**
		 * The meta object literal for the '<em><b>Left</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRIM_STRUCTURE__LEFT = eINSTANCE.getTrimStructure_Left();

		/**
		 * The meta object literal for the '<em><b>Right</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRIM_STRUCTURE__RIGHT = eINSTANCE.getTrimStructure_Right();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV______________RCP_______________V <em>VRCP V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV______________RCP_______________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________RCP_______________V()
		 * @generated
		 */
		EClass VRCP_V = eINSTANCE.getV______________RCP_______________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl <em>Application</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getApplication()
		 * @generated
		 */
		EClass APPLICATION = eINSTANCE.getApplication();

		/**
		 * The meta object literal for the '<em><b>Commands</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference APPLICATION__COMMANDS = eINSTANCE.getApplication_Commands();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ItemImpl <em>Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getItem()
		 * @generated
		 */
		EClass ITEM = eINSTANCE.getItem();

		/**
		 * The meta object literal for the '<em><b>Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEM__ENABLED = eINSTANCE.getItem_Enabled();

		/**
		 * The meta object literal for the '<em><b>Selected</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEM__SELECTED = eINSTANCE.getItem_Selected();

		/**
		 * The meta object literal for the '<em><b>Separator</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEM__SEPARATOR = eINSTANCE.getItem_Separator();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.HandledItemImpl <em>Handled Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.HandledItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandledItem()
		 * @generated
		 */
		EClass HANDLED_ITEM = eINSTANCE.getHandledItem();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HANDLED_ITEM__COMMAND = eINSTANCE.getHandledItem_Command();

		/**
		 * The meta object literal for the '<em><b>Menu</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HANDLED_ITEM__MENU = eINSTANCE.getHandledItem_Menu();

		/**
		 * The meta object literal for the '<em><b>Wb Command</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HANDLED_ITEM__WB_COMMAND = eINSTANCE.getHandledItem_WbCommand();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HANDLED_ITEM__PARAMETERS = eINSTANCE.getHandledItem_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.MenuItemImpl <em>Menu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.MenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMenuItem()
		 * @generated
		 */
		EClass MENU_ITEM = eINSTANCE.getMenuItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.MenuImpl <em>Menu</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.MenuImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMenu()
		 * @generated
		 */
		EClass MENU = eINSTANCE.getMenu();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ToolItemImpl <em>Tool Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ToolItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getToolItem()
		 * @generated
		 */
		EClass TOOL_ITEM = eINSTANCE.getToolItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ToolBarImpl <em>Tool Bar</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ToolBarImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getToolBar()
		 * @generated
		 */
		EClass TOOL_BAR = eINSTANCE.getToolBar();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MPSCElement <em>PSC Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MPSCElement
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPSCElement()
		 * @generated
		 */
		EClass PSC_ELEMENT = eINSTANCE.getPSCElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PartImpl <em>Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PartImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPart()
		 * @generated
		 */
		EClass PART = eINSTANCE.getPart();

		/**
		 * The meta object literal for the '<em><b>Menus</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PART__MENUS = eINSTANCE.getPart_Menus();

		/**
		 * The meta object literal for the '<em><b>Toolbar</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PART__TOOLBAR = eINSTANCE.getPart_Toolbar();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PartStackImpl <em>Part Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PartStackImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartStack()
		 * @generated
		 */
		EClass PART_STACK = eINSTANCE.getPartStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PartSashContainerImpl <em>Part Sash Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PartSashContainerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartSashContainer()
		 * @generated
		 */
		EClass PART_SASH_CONTAINER = eINSTANCE.getPartSashContainer();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.WindowImpl <em>Window</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.WindowImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getWindow()
		 * @generated
		 */
		EClass WINDOW = eINSTANCE.getWindow();

		/**
		 * The meta object literal for the '<em><b>Main Menu</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WINDOW__MAIN_MENU = eINSTANCE.getWindow_MainMenu();

		/**
		 * The meta object literal for the '<em><b>X</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW__X = eINSTANCE.getWindow_X();

		/**
		 * The meta object literal for the '<em><b>Y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW__Y = eINSTANCE.getWindow_Y();

		/**
		 * The meta object literal for the '<em><b>Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW__WIDTH = eINSTANCE.getWindow_Width();

		/**
		 * The meta object literal for the '<em><b>Height</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW__HEIGHT = eINSTANCE.getWindow_Height();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV______________IDE_______________V <em>VIDE V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV______________IDE_______________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________IDE_______________V()
		 * @generated
		 */
		EClass VIDE_V = eINSTANCE.getV______________IDE_______________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MVSCElement <em>VSC Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MVSCElement
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getVSCElement()
		 * @generated
		 */
		EClass VSC_ELEMENT = eINSTANCE.getVSCElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ViewImpl <em>View</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ViewImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getView()
		 * @generated
		 */
		EClass VIEW = eINSTANCE.getView();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ViewStackImpl <em>View Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ViewStackImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getViewStack()
		 * @generated
		 */
		EClass VIEW_STACK = eINSTANCE.getViewStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ViewSashContainerImpl <em>View Sash Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ViewSashContainerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getViewSashContainer()
		 * @generated
		 */
		EClass VIEW_SASH_CONTAINER = eINSTANCE.getViewSashContainer();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.EditorImpl <em>Editor</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.EditorImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getEditor()
		 * @generated
		 */
		EClass EDITOR = eINSTANCE.getEditor();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.MultiEditorImpl <em>Multi Editor</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.MultiEditorImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMultiEditor()
		 * @generated
		 */
		EClass MULTI_EDITOR = eINSTANCE.getMultiEditor();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MESCElement <em>ESC Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MESCElement
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getESCElement()
		 * @generated
		 */
		EClass ESC_ELEMENT = eINSTANCE.getESCElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.EditorStackImpl <em>Editor Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.EditorStackImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getEditorStack()
		 * @generated
		 */
		EClass EDITOR_STACK = eINSTANCE.getEditorStack();

		/**
		 * The meta object literal for the '<em><b>Input URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EDITOR_STACK__INPUT_URI = eINSTANCE.getEditorStack_InputURI();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.EditorSashContainerImpl <em>Editor Sash Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.EditorSashContainerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getEditorSashContainer()
		 * @generated
		 */
		EClass EDITOR_SASH_CONTAINER = eINSTANCE.getEditorSashContainer();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PerspectiveImpl <em>Perspective</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PerspectiveImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPerspective()
		 * @generated
		 */
		EClass PERSPECTIVE = eINSTANCE.getPerspective();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PerspectiveStackImpl <em>Perspective Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PerspectiveStackImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPerspectiveStack()
		 * @generated
		 */
		EClass PERSPECTIVE_STACK = eINSTANCE.getPerspectiveStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.IDEWindowImpl <em>IDE Window</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.IDEWindowImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getIDEWindow()
		 * @generated
		 */
		EClass IDE_WINDOW = eINSTANCE.getIDEWindow();

		/**
		 * The meta object literal for the '<em><b>Main Menu</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDE_WINDOW__MAIN_MENU = eINSTANCE.getIDEWindow_MainMenu();

		/**
		 * The meta object literal for the '<em>IEclipse Context</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.core.services.context.IEclipseContext
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getIEclipseContext()
		 * @generated
		 */
		EDataType IECLIPSE_CONTEXT = eINSTANCE.getIEclipseContext();

		/**
		 * The meta object literal for the '<em>Parameterized Command</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.core.commands.ParameterizedCommand
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getParameterizedCommand()
		 * @generated
		 */
		EDataType PARAMETERIZED_COMMAND = eINSTANCE.getParameterizedCommand();

	}

} //MApplicationPackage
