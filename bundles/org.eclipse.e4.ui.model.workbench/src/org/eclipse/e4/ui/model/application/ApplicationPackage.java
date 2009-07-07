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
 * $Id: ApplicationPackage.java,v 1.10 2009/06/15 19:13:16 pwebster Exp $
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
 * @see org.eclipse.e4.ui.model.application.ApplicationFactory
 * @model kind="package"
 * @generated
 */
public interface ApplicationPackage extends EPackage {
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
	String eNS_URI = "http://www.eclipse.org/ui/2008/Application"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "application"; //$NON-NLS-1$

	/**
	 * The package content type ID.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eCONTENT_TYPE = "org.eclipse.e4.ui.model.application"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ApplicationPackage eINSTANCE = org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MApplicationElementImpl <em>MApplication Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MApplicationElementImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMApplicationElement()
	 * @generated
	 */
	int MAPPLICATION_ELEMENT = 0;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION_ELEMENT__OWNER = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION_ELEMENT__ID = 1;

	/**
	 * The number of structural features of the '<em>MApplication Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION_ELEMENT_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MApplicationImpl <em>MApplication</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MApplicationImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMApplication()
	 * @generated
	 */
	int MAPPLICATION = 1;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION__OWNER = MAPPLICATION_ELEMENT__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION__ID = MAPPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Windows</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION__WINDOWS = MAPPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Command</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION__COMMAND = MAPPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION__CONTEXT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>MApplication</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAPPLICATION_FEATURE_COUNT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MPartImpl <em>MPart</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MPartImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMPart()
	 * @generated
	 */
	int MPART = 2;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__OWNER = MAPPLICATION_ELEMENT__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__ID = MAPPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__MENU = MAPPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__TOOL_BAR = MAPPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__POLICY = MAPPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__CHILDREN = MAPPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__ACTIVE_CHILD = MAPPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__HANDLERS = MAPPLICATION_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__WIDGET = MAPPLICATION_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__PARENT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__VISIBLE = MAPPLICATION_ELEMENT_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART__CONTEXT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 9;

	/**
	 * The number of structural features of the '<em>MPart</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPART_FEATURE_COUNT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 10;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MStackImpl <em>MStack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MStackImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMStack()
	 * @generated
	 */
	int MSTACK = 3;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__OWNER = MPART__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__ID = MPART__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__MENU = MPART__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__TOOL_BAR = MPART__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__POLICY = MPART__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__CHILDREN = MPART__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__ACTIVE_CHILD = MPART__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__HANDLERS = MPART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__WIDGET = MPART__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__PARENT = MPART__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__VISIBLE = MPART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK__CONTEXT = MPART__CONTEXT;

	/**
	 * The number of structural features of the '<em>MStack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSTACK_FEATURE_COUNT = MPART_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MSashFormImpl <em>MSash Form</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MSashFormImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMSashForm()
	 * @generated
	 */
	int MSASH_FORM = 4;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__OWNER = MPART__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__ID = MPART__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__MENU = MPART__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__TOOL_BAR = MPART__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__POLICY = MPART__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__CHILDREN = MPART__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__ACTIVE_CHILD = MPART__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__HANDLERS = MPART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__WIDGET = MPART__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__PARENT = MPART__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__VISIBLE = MPART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__CONTEXT = MPART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Weights</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM__WEIGHTS = MPART_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>MSash Form</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MSASH_FORM_FEATURE_COUNT = MPART_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MItemPartImpl <em>MItem Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MItemPartImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMItemPart()
	 * @generated
	 */
	int MITEM_PART = 16;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__OWNER = MPART__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__ID = MPART__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__MENU = MPART__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__TOOL_BAR = MPART__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__POLICY = MPART__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__CHILDREN = MPART__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__ACTIVE_CHILD = MPART__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__HANDLERS = MPART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__WIDGET = MPART__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__PARENT = MPART__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__VISIBLE = MPART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__CONTEXT = MPART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__ICON_URI = MPART_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__NAME = MPART_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART__TOOLTIP = MPART_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>MItem Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_PART_FEATURE_COUNT = MPART_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MContributedPartImpl <em>MContributed Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MContributedPartImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMContributedPart()
	 * @generated
	 */
	int MCONTRIBUTED_PART = 5;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__OWNER = MITEM_PART__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__ID = MITEM_PART__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__MENU = MITEM_PART__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__TOOL_BAR = MITEM_PART__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__POLICY = MITEM_PART__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__CHILDREN = MITEM_PART__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__ACTIVE_CHILD = MITEM_PART__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__HANDLERS = MITEM_PART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__WIDGET = MITEM_PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__PARENT = MITEM_PART__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__VISIBLE = MITEM_PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__CONTEXT = MITEM_PART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__ICON_URI = MITEM_PART__ICON_URI;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__NAME = MITEM_PART__NAME;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__TOOLTIP = MITEM_PART__TOOLTIP;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__URI = MITEM_PART_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__OBJECT = MITEM_PART_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART__PERSISTED_STATE = MITEM_PART_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>MContributed Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTED_PART_FEATURE_COUNT = MITEM_PART_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MContributionImpl <em>MContribution</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MContributionImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMContribution()
	 * @generated
	 */
	int MCONTRIBUTION = 6;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTION__OWNER = MAPPLICATION_ELEMENT__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTION__ID = MAPPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTION__URI = MAPPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTION__OBJECT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTION__PERSISTED_STATE = MAPPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>MContribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCONTRIBUTION_FEATURE_COUNT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MHandlerImpl <em>MHandler</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MHandlerImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMHandler()
	 * @generated
	 */
	int MHANDLER = 7;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLER__OWNER = MCONTRIBUTION__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLER__ID = MCONTRIBUTION__ID;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLER__URI = MCONTRIBUTION__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLER__OBJECT = MCONTRIBUTION__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLER__PERSISTED_STATE = MCONTRIBUTION__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLER__COMMAND = MCONTRIBUTION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>MHandler</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLER_FEATURE_COUNT = MCONTRIBUTION_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MItemImpl <em>MItem</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MItemImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMItem()
	 * @generated
	 */
	int MITEM = 8;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM__OWNER = MAPPLICATION_ELEMENT__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM__ID = MAPPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM__ICON_URI = MAPPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM__NAME = MAPPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM__TOOLTIP = MAPPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>MItem</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_FEATURE_COUNT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MHandledItemImpl <em>MHandled Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MHandledItemImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMHandledItem()
	 * @generated
	 */
	int MHANDLED_ITEM = 9;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLED_ITEM__OWNER = MITEM__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLED_ITEM__ID = MITEM__ID;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLED_ITEM__ICON_URI = MITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLED_ITEM__NAME = MITEM__NAME;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLED_ITEM__TOOLTIP = MITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLED_ITEM__COMMAND = MITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLED_ITEM__MENU = MITEM_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>MHandled Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MHANDLED_ITEM_FEATURE_COUNT = MITEM_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MMenuItemImpl <em>MMenu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MMenuItemImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMMenuItem()
	 * @generated
	 */
	int MMENU_ITEM = 10;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__OWNER = MHANDLED_ITEM__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__ID = MHANDLED_ITEM__ID;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__ICON_URI = MHANDLED_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__NAME = MHANDLED_ITEM__NAME;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__TOOLTIP = MHANDLED_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__COMMAND = MHANDLED_ITEM__COMMAND;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__MENU = MHANDLED_ITEM__MENU;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__SEPARATOR = MHANDLED_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM__VISIBLE = MHANDLED_ITEM_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>MMenu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_ITEM_FEATURE_COUNT = MHANDLED_ITEM_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MToolBarItemImpl <em>MTool Bar Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MToolBarItemImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMToolBarItem()
	 * @generated
	 */
	int MTOOL_BAR_ITEM = 11;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_ITEM__OWNER = MHANDLED_ITEM__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_ITEM__ID = MHANDLED_ITEM__ID;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_ITEM__ICON_URI = MHANDLED_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_ITEM__NAME = MHANDLED_ITEM__NAME;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_ITEM__TOOLTIP = MHANDLED_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_ITEM__COMMAND = MHANDLED_ITEM__COMMAND;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_ITEM__MENU = MHANDLED_ITEM__MENU;

	/**
	 * The number of structural features of the '<em>MTool Bar Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_ITEM_FEATURE_COUNT = MHANDLED_ITEM_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MItemContainerImpl <em>MItem Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MItemContainerImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMItemContainer()
	 * @generated
	 */
	int MITEM_CONTAINER = 12;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_CONTAINER__OWNER = MAPPLICATION_ELEMENT__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_CONTAINER__ID = MAPPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Items</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_CONTAINER__ITEMS = MAPPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>MItem Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MITEM_CONTAINER_FEATURE_COUNT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MMenuImpl <em>MMenu</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MMenuImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMMenu()
	 * @generated
	 */
	int MMENU = 13;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU__OWNER = MITEM_CONTAINER__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU__ID = MITEM_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Items</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU__ITEMS = MITEM_CONTAINER__ITEMS;

	/**
	 * The number of structural features of the '<em>MMenu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MMENU_FEATURE_COUNT = MITEM_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MToolBarImpl <em>MTool Bar</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MToolBarImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMToolBar()
	 * @generated
	 */
	int MTOOL_BAR = 14;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR__OWNER = MITEM_CONTAINER__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR__ID = MITEM_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Items</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR__ITEMS = MITEM_CONTAINER__ITEMS;

	/**
	 * The number of structural features of the '<em>MTool Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_FEATURE_COUNT = MITEM_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl <em>MTrimmed Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMTrimmedPart()
	 * @generated
	 */
	int MTRIMMED_PART = 15;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__OWNER = MPART__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__ID = MPART__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__MENU = MPART__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__TOOL_BAR = MPART__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__POLICY = MPART__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__CHILDREN = MPART__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__ACTIVE_CHILD = MPART__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__HANDLERS = MPART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__WIDGET = MPART__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__PARENT = MPART__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__VISIBLE = MPART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__CONTEXT = MPART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Top Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__TOP_TRIM = MPART_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Left Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__LEFT_TRIM = MPART_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Right Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__RIGHT_TRIM = MPART_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Bottom Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__BOTTOM_TRIM = MPART_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Client Area</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART__CLIENT_AREA = MPART_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>MTrimmed Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTRIMMED_PART_FEATURE_COUNT = MPART_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MWindowImpl <em>MWindow</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MWindowImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMWindow()
	 * @generated
	 */
	int MWINDOW = 17;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__OWNER = MITEM_PART__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__ID = MITEM_PART__ID;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__MENU = MITEM_PART__MENU;

	/**
	 * The feature id for the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__TOOL_BAR = MITEM_PART__TOOL_BAR;

	/**
	 * The feature id for the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__POLICY = MITEM_PART__POLICY;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__CHILDREN = MITEM_PART__CHILDREN;

	/**
	 * The feature id for the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__ACTIVE_CHILD = MITEM_PART__ACTIVE_CHILD;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__HANDLERS = MITEM_PART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__WIDGET = MITEM_PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__PARENT = MITEM_PART__PARENT;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__VISIBLE = MITEM_PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__CONTEXT = MITEM_PART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__ICON_URI = MITEM_PART__ICON_URI;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__NAME = MITEM_PART__NAME;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__TOOLTIP = MITEM_PART__TOOLTIP;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__X = MITEM_PART_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__Y = MITEM_PART_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__WIDTH = MITEM_PART_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW__HEIGHT = MITEM_PART_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>MWindow</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MWINDOW_FEATURE_COUNT = MITEM_PART_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MCommandImpl <em>MCommand</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MCommandImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMCommand()
	 * @generated
	 */
	int MCOMMAND = 18;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCOMMAND__OWNER = MAPPLICATION_ELEMENT__OWNER;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCOMMAND__ID = MAPPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCOMMAND__NAME = MAPPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>MCommand</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MCOMMAND_FEATURE_COUNT = MAPPLICATION_ELEMENT_FEATURE_COUNT + 1;


	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.internal.application.MToolBarContainerImpl <em>MTool Bar Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.internal.application.MToolBarContainerImpl
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMToolBarContainer()
	 * @generated
	 */
	int MTOOL_BAR_CONTAINER = 19;

	/**
	 * The feature id for the '<em><b>Toolbars</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_CONTAINER__TOOLBARS = 0;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_CONTAINER__HORIZONTAL = 1;

	/**
	 * The number of structural features of the '<em>MTool Bar Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MTOOL_BAR_CONTAINER_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '<em>IEclipse Context</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.core.services.context.IEclipseContext
	 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getIEclipseContext()
	 * @generated
	 */
	int IECLIPSE_CONTEXT = 20;


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MApplicationElement <em>MApplication Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MApplication Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement
	 * @generated
	 */
	EClass getMApplicationElement();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Owner</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement#getOwner()
	 * @see #getMApplicationElement()
	 * @generated
	 */
	EAttribute getMApplicationElement_Owner();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement#getId()
	 * @see #getMApplicationElement()
	 * @generated
	 */
	EAttribute getMApplicationElement_Id();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MApplication <em>MApplication</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MApplication</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplication
	 * @generated
	 */
	EClass getMApplication();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MApplication#getWindows <em>Windows</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Windows</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplication#getWindows()
	 * @see #getMApplication()
	 * @generated
	 */
	EReference getMApplication_Windows();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MApplication#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplication#getCommand()
	 * @see #getMApplication()
	 * @generated
	 */
	EReference getMApplication_Command();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MApplication#getContext <em>Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplication#getContext()
	 * @see #getMApplication()
	 * @generated
	 */
	EAttribute getMApplication_Context();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPart <em>MPart</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MPart</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart
	 * @generated
	 */
	EClass getMPart();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MPart#getMenu <em>Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getMenu()
	 * @see #getMPart()
	 * @generated
	 */
	EReference getMPart_Menu();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MPart#getToolBar <em>Tool Bar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Tool Bar</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getToolBar()
	 * @see #getMPart()
	 * @generated
	 */
	EReference getMPart_ToolBar();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MPart#getPolicy <em>Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Policy</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getPolicy()
	 * @see #getMPart()
	 * @generated
	 */
	EAttribute getMPart_Policy();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MPart#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getChildren()
	 * @see #getMPart()
	 * @generated
	 */
	EReference getMPart_Children();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MPart#getActiveChild <em>Active Child</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Active Child</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getActiveChild()
	 * @see #getMPart()
	 * @generated
	 */
	EReference getMPart_ActiveChild();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MPart#getHandlers <em>Handlers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Handlers</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getHandlers()
	 * @see #getMPart()
	 * @generated
	 */
	EReference getMPart_Handlers();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MPart#getWidget <em>Widget</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Widget</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getWidget()
	 * @see #getMPart()
	 * @generated
	 */
	EAttribute getMPart_Widget();

	/**
	 * Returns the meta object for the container reference '{@link org.eclipse.e4.ui.model.application.MPart#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Parent</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getParent()
	 * @see #getMPart()
	 * @generated
	 */
	EReference getMPart_Parent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MPart#isVisible <em>Visible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visible</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#isVisible()
	 * @see #getMPart()
	 * @generated
	 */
	EAttribute getMPart_Visible();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MPart#getContext <em>Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getContext()
	 * @see #getMPart()
	 * @generated
	 */
	EAttribute getMPart_Context();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MStack <em>MStack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MStack</em>'.
	 * @see org.eclipse.e4.ui.model.application.MStack
	 * @generated
	 */
	EClass getMStack();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MSashForm <em>MSash Form</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MSash Form</em>'.
	 * @see org.eclipse.e4.ui.model.application.MSashForm
	 * @generated
	 */
	EClass getMSashForm();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.e4.ui.model.application.MSashForm#getWeights <em>Weights</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Weights</em>'.
	 * @see org.eclipse.e4.ui.model.application.MSashForm#getWeights()
	 * @see #getMSashForm()
	 * @generated
	 */
	EAttribute getMSashForm_Weights();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MContributedPart <em>MContributed Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MContributed Part</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContributedPart
	 * @generated
	 */
	EClass getMContributedPart();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MContribution <em>MContribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MContribution</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution
	 * @generated
	 */
	EClass getMContribution();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContribution#getURI <em>URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getURI()
	 * @see #getMContribution()
	 * @generated
	 */
	EAttribute getMContribution_URI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContribution#getObject <em>Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getObject()
	 * @see #getMContribution()
	 * @generated
	 */
	EAttribute getMContribution_Object();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContribution#getPersistedState <em>Persisted State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Persisted State</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getPersistedState()
	 * @see #getMContribution()
	 * @generated
	 */
	EAttribute getMContribution_PersistedState();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MHandler <em>MHandler</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MHandler</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandler
	 * @generated
	 */
	EClass getMHandler();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MHandler#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandler#getCommand()
	 * @see #getMHandler()
	 * @generated
	 */
	EReference getMHandler_Command();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MItem <em>MItem</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MItem</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem
	 * @generated
	 */
	EClass getMItem();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#getIconURI <em>Icon URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Icon URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#getIconURI()
	 * @see #getMItem()
	 * @generated
	 */
	EAttribute getMItem_IconURI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#getName()
	 * @see #getMItem()
	 * @generated
	 */
	EAttribute getMItem_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#getTooltip <em>Tooltip</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tooltip</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#getTooltip()
	 * @see #getMItem()
	 * @generated
	 */
	EAttribute getMItem_Tooltip();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MHandledItem <em>MHandled Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MHandled Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem
	 * @generated
	 */
	EClass getMHandledItem();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MHandledItem#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getCommand()
	 * @see #getMHandledItem()
	 * @generated
	 */
	EReference getMHandledItem_Command();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MHandledItem#getMenu <em>Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getMenu()
	 * @see #getMHandledItem()
	 * @generated
	 */
	EReference getMHandledItem_Menu();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MMenuItem <em>MMenu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MMenu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMenuItem
	 * @generated
	 */
	EClass getMMenuItem();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MMenuItem#isSeparator <em>Separator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Separator</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMenuItem#isSeparator()
	 * @see #getMMenuItem()
	 * @generated
	 */
	EAttribute getMMenuItem_Separator();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MMenuItem#isVisible <em>Visible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visible</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMenuItem#isVisible()
	 * @see #getMMenuItem()
	 * @generated
	 */
	EAttribute getMMenuItem_Visible();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MToolBarItem <em>MTool Bar Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MTool Bar Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolBarItem
	 * @generated
	 */
	EClass getMToolBarItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MItemContainer <em>MItem Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MItem Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItemContainer
	 * @generated
	 */
	EClass getMItemContainer();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MItemContainer#getItems <em>Items</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Items</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItemContainer#getItems()
	 * @see #getMItemContainer()
	 * @generated
	 */
	EReference getMItemContainer_Items();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MMenu <em>MMenu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MMenu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMenu
	 * @generated
	 */
	EClass getMMenu();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MToolBar <em>MTool Bar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MTool Bar</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolBar
	 * @generated
	 */
	EClass getMToolBar();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MTrimmedPart <em>MTrimmed Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MTrimmed Part</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimmedPart
	 * @generated
	 */
	EClass getMTrimmedPart();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getTopTrim <em>Top Trim</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Top Trim</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimmedPart#getTopTrim()
	 * @see #getMTrimmedPart()
	 * @generated
	 */
	EReference getMTrimmedPart_TopTrim();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getLeftTrim <em>Left Trim</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Left Trim</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimmedPart#getLeftTrim()
	 * @see #getMTrimmedPart()
	 * @generated
	 */
	EReference getMTrimmedPart_LeftTrim();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getRightTrim <em>Right Trim</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Right Trim</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimmedPart#getRightTrim()
	 * @see #getMTrimmedPart()
	 * @generated
	 */
	EReference getMTrimmedPart_RightTrim();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getBottomTrim <em>Bottom Trim</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Bottom Trim</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimmedPart#getBottomTrim()
	 * @see #getMTrimmedPart()
	 * @generated
	 */
	EReference getMTrimmedPart_BottomTrim();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MTrimmedPart#getClientArea <em>Client Area</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Client Area</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimmedPart#getClientArea()
	 * @see #getMTrimmedPart()
	 * @generated
	 */
	EReference getMTrimmedPart_ClientArea();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MItemPart <em>MItem Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MItem Part</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItemPart
	 * @generated
	 */
	EClass getMItemPart();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MWindow <em>MWindow</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MWindow</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow
	 * @generated
	 */
	EClass getMWindow();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getX <em>X</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>X</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getX()
	 * @see #getMWindow()
	 * @generated
	 */
	EAttribute getMWindow_X();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getY <em>Y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Y</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getY()
	 * @see #getMWindow()
	 * @generated
	 */
	EAttribute getMWindow_Y();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getWidth <em>Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Width</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getWidth()
	 * @see #getMWindow()
	 * @generated
	 */
	EAttribute getMWindow_Width();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getHeight <em>Height</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Height</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getHeight()
	 * @see #getMWindow()
	 * @generated
	 */
	EAttribute getMWindow_Height();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MCommand <em>MCommand</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MCommand</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand
	 * @generated
	 */
	EClass getMCommand();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommand#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand#getName()
	 * @see #getMCommand()
	 * @generated
	 */
	EAttribute getMCommand_Name();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MToolBarContainer <em>MTool Bar Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>MTool Bar Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolBarContainer
	 * @generated
	 */
	EClass getMToolBarContainer();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MToolBarContainer#getToolbars <em>Toolbars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Toolbars</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolBarContainer#getToolbars()
	 * @see #getMToolBarContainer()
	 * @generated
	 */
	EReference getMToolBarContainer_Toolbars();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MToolBarContainer#isHorizontal <em>Horizontal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Horizontal</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolBarContainer#isHorizontal()
	 * @see #getMToolBarContainer()
	 * @generated
	 */
	EAttribute getMToolBarContainer_Horizontal();

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
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ApplicationFactory getApplicationFactory();

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
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MApplicationElementImpl <em>MApplication Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MApplicationElementImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMApplicationElement()
		 * @generated
		 */
		EClass MAPPLICATION_ELEMENT = eINSTANCE.getMApplicationElement();

		/**
		 * The meta object literal for the '<em><b>Owner</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MAPPLICATION_ELEMENT__OWNER = eINSTANCE.getMApplicationElement_Owner();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MAPPLICATION_ELEMENT__ID = eINSTANCE.getMApplicationElement_Id();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MApplicationImpl <em>MApplication</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MApplicationImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMApplication()
		 * @generated
		 */
		EClass MAPPLICATION = eINSTANCE.getMApplication();

		/**
		 * The meta object literal for the '<em><b>Windows</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MAPPLICATION__WINDOWS = eINSTANCE.getMApplication_Windows();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MAPPLICATION__COMMAND = eINSTANCE.getMApplication_Command();

		/**
		 * The meta object literal for the '<em><b>Context</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MAPPLICATION__CONTEXT = eINSTANCE.getMApplication_Context();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MPartImpl <em>MPart</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MPartImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMPart()
		 * @generated
		 */
		EClass MPART = eINSTANCE.getMPart();

		/**
		 * The meta object literal for the '<em><b>Menu</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MPART__MENU = eINSTANCE.getMPart_Menu();

		/**
		 * The meta object literal for the '<em><b>Tool Bar</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MPART__TOOL_BAR = eINSTANCE.getMPart_ToolBar();

		/**
		 * The meta object literal for the '<em><b>Policy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPART__POLICY = eINSTANCE.getMPart_Policy();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MPART__CHILDREN = eINSTANCE.getMPart_Children();

		/**
		 * The meta object literal for the '<em><b>Active Child</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MPART__ACTIVE_CHILD = eINSTANCE.getMPart_ActiveChild();

		/**
		 * The meta object literal for the '<em><b>Handlers</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MPART__HANDLERS = eINSTANCE.getMPart_Handlers();

		/**
		 * The meta object literal for the '<em><b>Widget</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPART__WIDGET = eINSTANCE.getMPart_Widget();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MPART__PARENT = eINSTANCE.getMPart_Parent();

		/**
		 * The meta object literal for the '<em><b>Visible</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPART__VISIBLE = eINSTANCE.getMPart_Visible();

		/**
		 * The meta object literal for the '<em><b>Context</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPART__CONTEXT = eINSTANCE.getMPart_Context();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MStackImpl <em>MStack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MStackImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMStack()
		 * @generated
		 */
		EClass MSTACK = eINSTANCE.getMStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MSashFormImpl <em>MSash Form</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MSashFormImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMSashForm()
		 * @generated
		 */
		EClass MSASH_FORM = eINSTANCE.getMSashForm();

		/**
		 * The meta object literal for the '<em><b>Weights</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MSASH_FORM__WEIGHTS = eINSTANCE.getMSashForm_Weights();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MContributedPartImpl <em>MContributed Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MContributedPartImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMContributedPart()
		 * @generated
		 */
		EClass MCONTRIBUTED_PART = eINSTANCE.getMContributedPart();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MContributionImpl <em>MContribution</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MContributionImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMContribution()
		 * @generated
		 */
		EClass MCONTRIBUTION = eINSTANCE.getMContribution();

		/**
		 * The meta object literal for the '<em><b>URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MCONTRIBUTION__URI = eINSTANCE.getMContribution_URI();

		/**
		 * The meta object literal for the '<em><b>Object</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MCONTRIBUTION__OBJECT = eINSTANCE.getMContribution_Object();

		/**
		 * The meta object literal for the '<em><b>Persisted State</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MCONTRIBUTION__PERSISTED_STATE = eINSTANCE.getMContribution_PersistedState();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MHandlerImpl <em>MHandler</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MHandlerImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMHandler()
		 * @generated
		 */
		EClass MHANDLER = eINSTANCE.getMHandler();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MHANDLER__COMMAND = eINSTANCE.getMHandler_Command();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MItemImpl <em>MItem</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MItemImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMItem()
		 * @generated
		 */
		EClass MITEM = eINSTANCE.getMItem();

		/**
		 * The meta object literal for the '<em><b>Icon URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MITEM__ICON_URI = eINSTANCE.getMItem_IconURI();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MITEM__NAME = eINSTANCE.getMItem_Name();

		/**
		 * The meta object literal for the '<em><b>Tooltip</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MITEM__TOOLTIP = eINSTANCE.getMItem_Tooltip();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MHandledItemImpl <em>MHandled Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MHandledItemImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMHandledItem()
		 * @generated
		 */
		EClass MHANDLED_ITEM = eINSTANCE.getMHandledItem();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MHANDLED_ITEM__COMMAND = eINSTANCE.getMHandledItem_Command();

		/**
		 * The meta object literal for the '<em><b>Menu</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MHANDLED_ITEM__MENU = eINSTANCE.getMHandledItem_Menu();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MMenuItemImpl <em>MMenu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MMenuItemImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMMenuItem()
		 * @generated
		 */
		EClass MMENU_ITEM = eINSTANCE.getMMenuItem();

		/**
		 * The meta object literal for the '<em><b>Separator</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MMENU_ITEM__SEPARATOR = eINSTANCE.getMMenuItem_Separator();

		/**
		 * The meta object literal for the '<em><b>Visible</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MMENU_ITEM__VISIBLE = eINSTANCE.getMMenuItem_Visible();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MToolBarItemImpl <em>MTool Bar Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MToolBarItemImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMToolBarItem()
		 * @generated
		 */
		EClass MTOOL_BAR_ITEM = eINSTANCE.getMToolBarItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MItemContainerImpl <em>MItem Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MItemContainerImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMItemContainer()
		 * @generated
		 */
		EClass MITEM_CONTAINER = eINSTANCE.getMItemContainer();

		/**
		 * The meta object literal for the '<em><b>Items</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MITEM_CONTAINER__ITEMS = eINSTANCE.getMItemContainer_Items();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MMenuImpl <em>MMenu</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MMenuImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMMenu()
		 * @generated
		 */
		EClass MMENU = eINSTANCE.getMMenu();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MToolBarImpl <em>MTool Bar</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MToolBarImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMToolBar()
		 * @generated
		 */
		EClass MTOOL_BAR = eINSTANCE.getMToolBar();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl <em>MTrimmed Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMTrimmedPart()
		 * @generated
		 */
		EClass MTRIMMED_PART = eINSTANCE.getMTrimmedPart();

		/**
		 * The meta object literal for the '<em><b>Top Trim</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MTRIMMED_PART__TOP_TRIM = eINSTANCE.getMTrimmedPart_TopTrim();

		/**
		 * The meta object literal for the '<em><b>Left Trim</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MTRIMMED_PART__LEFT_TRIM = eINSTANCE.getMTrimmedPart_LeftTrim();

		/**
		 * The meta object literal for the '<em><b>Right Trim</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MTRIMMED_PART__RIGHT_TRIM = eINSTANCE.getMTrimmedPart_RightTrim();

		/**
		 * The meta object literal for the '<em><b>Bottom Trim</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MTRIMMED_PART__BOTTOM_TRIM = eINSTANCE.getMTrimmedPart_BottomTrim();

		/**
		 * The meta object literal for the '<em><b>Client Area</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MTRIMMED_PART__CLIENT_AREA = eINSTANCE.getMTrimmedPart_ClientArea();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MItemPartImpl <em>MItem Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MItemPartImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMItemPart()
		 * @generated
		 */
		EClass MITEM_PART = eINSTANCE.getMItemPart();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MWindowImpl <em>MWindow</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MWindowImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMWindow()
		 * @generated
		 */
		EClass MWINDOW = eINSTANCE.getMWindow();

		/**
		 * The meta object literal for the '<em><b>X</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MWINDOW__X = eINSTANCE.getMWindow_X();

		/**
		 * The meta object literal for the '<em><b>Y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MWINDOW__Y = eINSTANCE.getMWindow_Y();

		/**
		 * The meta object literal for the '<em><b>Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MWINDOW__WIDTH = eINSTANCE.getMWindow_Width();

		/**
		 * The meta object literal for the '<em><b>Height</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MWINDOW__HEIGHT = eINSTANCE.getMWindow_Height();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MCommandImpl <em>MCommand</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MCommandImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMCommand()
		 * @generated
		 */
		EClass MCOMMAND = eINSTANCE.getMCommand();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MCOMMAND__NAME = eINSTANCE.getMCommand_Name();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.internal.application.MToolBarContainerImpl <em>MTool Bar Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.internal.application.MToolBarContainerImpl
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getMToolBarContainer()
		 * @generated
		 */
		EClass MTOOL_BAR_CONTAINER = eINSTANCE.getMToolBarContainer();

		/**
		 * The meta object literal for the '<em><b>Toolbars</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MTOOL_BAR_CONTAINER__TOOLBARS = eINSTANCE.getMToolBarContainer_Toolbars();

		/**
		 * The meta object literal for the '<em><b>Horizontal</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MTOOL_BAR_CONTAINER__HORIZONTAL = eINSTANCE.getMToolBarContainer_Horizontal();

		/**
		 * The meta object literal for the '<em>IEclipse Context</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.core.services.context.IEclipseContext
		 * @see org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl#getIEclipseContext()
		 * @generated
		 */
		EDataType IECLIPSE_CONTEXT = eINSTANCE.getIEclipseContext();

	}

} //ApplicationPackage
