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
 * $Id: WorkbenchPackage.java,v 1.1 2008/11/11 18:19:11 bbokowski Exp $
 */
package org.eclipse.e4.ui.model.workbench;

import org.eclipse.e4.ui.model.application.ApplicationPackage;

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
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.workbench.WorkbenchFactory
 * @model kind="package"
 * @generated
 */
public interface WorkbenchPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "workbench"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/ui/2008/Workbench"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "workbench"; //$NON-NLS-1$

	/**
	 * The package content type ID.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eCONTENT_TYPE = "org.eclipse.e4.ui.model.workbench"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	WorkbenchPackage eINSTANCE = org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.workbench.WorkbenchWindowImpl <em>Window</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchWindowImpl
	 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl#getWorkbenchWindow()
	 * @generated
	 */
	int WORKBENCH_WINDOW = 0;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__OWNER = ApplicationPackage.WINDOW__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__ID = ApplicationPackage.WINDOW__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__MENU = ApplicationPackage.WINDOW__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__TOOL_BAR = ApplicationPackage.WINDOW__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__POLICY = ApplicationPackage.WINDOW__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__CHILDREN = ApplicationPackage.WINDOW__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__ACTIVE_CHILD = ApplicationPackage.WINDOW__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__HANDLERS = ApplicationPackage.WINDOW__HANDLERS;

	/**
	 * The feature id for the '<em><b>Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__TRIM = ApplicationPackage.WINDOW__TRIM;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__WIDGET = ApplicationPackage.WINDOW__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__PARENT = ApplicationPackage.WINDOW__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__VISIBLE = ApplicationPackage.WINDOW__VISIBLE;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__ICON_URI = ApplicationPackage.WINDOW__ICON_URI;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__NAME = ApplicationPackage.WINDOW__NAME;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__TOOLTIP = ApplicationPackage.WINDOW__TOOLTIP;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__X = ApplicationPackage.WINDOW__X;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__Y = ApplicationPackage.WINDOW__Y;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__WIDTH = ApplicationPackage.WINDOW__WIDTH;

	/**
	 * The feature id for the '<em><b>Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__HEIGHT = ApplicationPackage.WINDOW__HEIGHT;

	/**
	 * The feature id for the '<em><b>Shared Parts</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW__SHARED_PARTS = ApplicationPackage.WINDOW_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Window</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_WINDOW_FEATURE_COUNT = ApplicationPackage.WINDOW_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.workbench.ProxyPartImpl <em>Proxy Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.workbench.ProxyPartImpl
	 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl#getProxyPart()
	 * @generated
	 */
	int PROXY_PART = 1;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__OWNER = ApplicationPackage.PART__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__ID = ApplicationPackage.PART__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__MENU = ApplicationPackage.PART__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__TOOL_BAR = ApplicationPackage.PART__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__POLICY = ApplicationPackage.PART__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__CHILDREN = ApplicationPackage.PART__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__ACTIVE_CHILD = ApplicationPackage.PART__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__HANDLERS = ApplicationPackage.PART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__TRIM = ApplicationPackage.PART__TRIM;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__WIDGET = ApplicationPackage.PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__PARENT = ApplicationPackage.PART__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__VISIBLE = ApplicationPackage.PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Part</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART__PART = ApplicationPackage.PART_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Proxy Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROXY_PART_FEATURE_COUNT = ApplicationPackage.PART_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.workbench.PerspectiveImpl <em>Perspective</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.workbench.PerspectiveImpl
	 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl#getPerspective()
	 * @generated
	 */
	int PERSPECTIVE = 2;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__OWNER = ApplicationPackage.ITEM_PART__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ID = ApplicationPackage.ITEM_PART__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__MENU = ApplicationPackage.ITEM_PART__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__TOOL_BAR = ApplicationPackage.ITEM_PART__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__POLICY = ApplicationPackage.ITEM_PART__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__CHILDREN = ApplicationPackage.ITEM_PART__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ACTIVE_CHILD = ApplicationPackage.ITEM_PART__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__HANDLERS = ApplicationPackage.ITEM_PART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__TRIM = ApplicationPackage.ITEM_PART__TRIM;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__WIDGET = ApplicationPackage.ITEM_PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__PARENT = ApplicationPackage.ITEM_PART__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__VISIBLE = ApplicationPackage.ITEM_PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ICON_URI = ApplicationPackage.ITEM_PART__ICON_URI;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__NAME = ApplicationPackage.ITEM_PART__NAME;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__TOOLTIP = ApplicationPackage.ITEM_PART__TOOLTIP;

	/**
	 * The number of structural features of the '<em>Perspective</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_FEATURE_COUNT = ApplicationPackage.ITEM_PART_FEATURE_COUNT + 0;


	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.workbench.WorkbenchModelImpl <em>Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchModelImpl
	 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl#getWorkbenchModel()
	 * @generated
	 */
	int WORKBENCH_MODEL = 3;

	/**
	 * The feature id for the '<em><b>Wb Windows</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_MODEL__WB_WINDOWS = 0;

	/**
	 * The feature id for the '<em><b>Cur WBW</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_MODEL__CUR_WBW = 1;

	/**
	 * The number of structural features of the '<em>Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKBENCH_MODEL_FEATURE_COUNT = 2;


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.workbench.WorkbenchWindow <em>Window</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Window</em>'.
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchWindow
	 * @generated
	 */
	EClass getWorkbenchWindow();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.workbench.WorkbenchWindow#getSharedParts <em>Shared Parts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Shared Parts</em>'.
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchWindow#getSharedParts()
	 * @see #getWorkbenchWindow()
	 * @generated
	 */
	EReference getWorkbenchWindow_SharedParts();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.workbench.ProxyPart <em>Proxy Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Proxy Part</em>'.
	 * @see org.eclipse.e4.ui.model.workbench.ProxyPart
	 * @generated
	 */
	EClass getProxyPart();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.workbench.ProxyPart#getPart <em>Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Part</em>'.
	 * @see org.eclipse.e4.ui.model.workbench.ProxyPart#getPart()
	 * @see #getProxyPart()
	 * @generated
	 */
	EReference getProxyPart_Part();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.workbench.Perspective <em>Perspective</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Perspective</em>'.
	 * @see org.eclipse.e4.ui.model.workbench.Perspective
	 * @generated
	 */
	EClass getPerspective();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.workbench.WorkbenchModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model</em>'.
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchModel
	 * @generated
	 */
	EClass getWorkbenchModel();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.workbench.WorkbenchModel#getWbWindows <em>Wb Windows</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Wb Windows</em>'.
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchModel#getWbWindows()
	 * @see #getWorkbenchModel()
	 * @generated
	 */
	EReference getWorkbenchModel_WbWindows();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.workbench.WorkbenchModel#getCurWBW <em>Cur WBW</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Cur WBW</em>'.
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchModel#getCurWBW()
	 * @see #getWorkbenchModel()
	 * @generated
	 */
	EReference getWorkbenchModel_CurWBW();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	WorkbenchFactory getWorkbenchFactory();

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
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.workbench.WorkbenchWindowImpl <em>Window</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchWindowImpl
		 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl#getWorkbenchWindow()
		 * @generated
		 */
		EClass WORKBENCH_WINDOW = eINSTANCE.getWorkbenchWindow();

		/**
		 * The meta object literal for the '<em><b>Shared Parts</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WORKBENCH_WINDOW__SHARED_PARTS = eINSTANCE.getWorkbenchWindow_SharedParts();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.workbench.ProxyPartImpl <em>Proxy Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.workbench.ProxyPartImpl
		 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl#getProxyPart()
		 * @generated
		 */
		EClass PROXY_PART = eINSTANCE.getProxyPart();

		/**
		 * The meta object literal for the '<em><b>Part</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROXY_PART__PART = eINSTANCE.getProxyPart_Part();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.workbench.PerspectiveImpl <em>Perspective</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.workbench.PerspectiveImpl
		 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl#getPerspective()
		 * @generated
		 */
		EClass PERSPECTIVE = eINSTANCE.getPerspective();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.workbench.WorkbenchModelImpl <em>Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchModelImpl
		 * @see org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl#getWorkbenchModel()
		 * @generated
		 */
		EClass WORKBENCH_MODEL = eINSTANCE.getWorkbenchModel();

		/**
		 * The meta object literal for the '<em><b>Wb Windows</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WORKBENCH_MODEL__WB_WINDOWS = eINSTANCE.getWorkbenchModel_WbWindows();

		/**
		 * The meta object literal for the '<em><b>Cur WBW</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WORKBENCH_MODEL__CUR_WBW = eINSTANCE.getWorkbenchModel_CurWBW();

	}

} //WorkbenchPackage
