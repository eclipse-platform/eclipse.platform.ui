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
package org.eclipse.e4.ui.model.application.ui.menu.impl;

import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

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
 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory
 * @model kind="package"
 * @generated
 */
public class MenuPackageImpl extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "menu"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "http://www.eclipse.org/ui/2010/UIModel/application/ui/menu"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "menu"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final MenuPackageImpl eINSTANCE = org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl <em>Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getItem()
	 * @generated
	 */
	public static final int ITEM = 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__ELEMENT_ID = UiPackageImpl.UI_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__PERSISTED_STATE = UiPackageImpl.UI_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__TAGS = UiPackageImpl.UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__CONTRIBUTOR_URI = UiPackageImpl.UI_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__TRANSIENT_DATA = UiPackageImpl.UI_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__WIDGET = UiPackageImpl.UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__RENDERER = UiPackageImpl.UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__TO_BE_RENDERED = UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__ON_TOP = UiPackageImpl.UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__VISIBLE = UiPackageImpl.UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__PARENT = UiPackageImpl.UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__CONTAINER_DATA = UiPackageImpl.UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__CUR_SHARED_REF = UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__VISIBLE_WHEN = UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__LABEL = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__ICON_URI = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__TOOLTIP = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__ENABLED = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__SELECTED = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM__TYPE = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM_FEATURE_COUNT = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM___GET_LOCALIZED_LABEL = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM___GET_LOCALIZED_TOOLTIP = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The number of operations of the '<em>Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ITEM_OPERATION_COUNT = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledItemImpl <em>Handled Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.HandledItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getHandledItem()
	 * @generated
	 */
	public static final int HANDLED_ITEM = 1;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__ELEMENT_ID = ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__PERSISTED_STATE = ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__TAGS = ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__CONTRIBUTOR_URI = ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__TRANSIENT_DATA = ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__WIDGET = ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__RENDERER = ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__TO_BE_RENDERED = ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__ON_TOP = ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__VISIBLE = ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__PARENT = ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__CONTAINER_DATA = ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__CUR_SHARED_REF = ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__VISIBLE_WHEN = ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__ACCESSIBILITY_PHRASE = ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__LABEL = ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__ICON_URI = ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__TOOLTIP = ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__ENABLED = ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__SELECTED = ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__TYPE = ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__COMMAND = ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__WB_COMMAND = ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM__PARAMETERS = ITEM_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Handled Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM_FEATURE_COUNT = ITEM_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM___GET_LOCALIZED_LABEL = ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM___GET_LOCALIZED_TOOLTIP = ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The number of operations of the '<em>Handled Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_ITEM_OPERATION_COUNT = ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl <em>Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuElement()
	 * @generated
	 */
	public static final int MENU_ELEMENT = 2;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__ELEMENT_ID = UiPackageImpl.UI_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__PERSISTED_STATE = UiPackageImpl.UI_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__TAGS = UiPackageImpl.UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__CONTRIBUTOR_URI = UiPackageImpl.UI_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__TRANSIENT_DATA = UiPackageImpl.UI_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__WIDGET = UiPackageImpl.UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__RENDERER = UiPackageImpl.UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__TO_BE_RENDERED = UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__ON_TOP = UiPackageImpl.UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__VISIBLE = UiPackageImpl.UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__PARENT = UiPackageImpl.UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__CONTAINER_DATA = UiPackageImpl.UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__CUR_SHARED_REF = UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__VISIBLE_WHEN = UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__LABEL = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__ICON_URI = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__TOOLTIP = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT__MNEMONICS = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT_FEATURE_COUNT = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT___GET_LOCALIZED_LABEL = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT___GET_LOCALIZED_TOOLTIP = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT___GET_LOCALIZED_MNEMONICS = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 2;

	/**
	 * The number of operations of the '<em>Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ELEMENT_OPERATION_COUNT = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuItemImpl <em>Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuItem()
	 * @generated
	 */
	public static final int MENU_ITEM = 3;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__ELEMENT_ID = ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__PERSISTED_STATE = ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__TAGS = ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__CONTRIBUTOR_URI = ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__TRANSIENT_DATA = ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__WIDGET = ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__RENDERER = ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__TO_BE_RENDERED = ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__ON_TOP = ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__VISIBLE = ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__PARENT = ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__CONTAINER_DATA = ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__CUR_SHARED_REF = ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__VISIBLE_WHEN = ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__ACCESSIBILITY_PHRASE = ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__LABEL = ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__ICON_URI = ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__TOOLTIP = ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__ENABLED = ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__SELECTED = ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__TYPE = ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM__MNEMONICS = ITEM_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM_FEATURE_COUNT = ITEM_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM___GET_LOCALIZED_LABEL = ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM___GET_LOCALIZED_TOOLTIP = ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM___GET_LOCALIZED_MNEMONICS = ITEM_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_ITEM_OPERATION_COUNT = ITEM_OPERATION_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuSeparatorImpl <em>Separator</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuSeparatorImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuSeparator()
	 * @generated
	 */
	public static final int MENU_SEPARATOR = 4;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__ELEMENT_ID = MENU_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__PERSISTED_STATE = MENU_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__TAGS = MENU_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__CONTRIBUTOR_URI = MENU_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__TRANSIENT_DATA = MENU_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__WIDGET = MENU_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__RENDERER = MENU_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__TO_BE_RENDERED = MENU_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__ON_TOP = MENU_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__VISIBLE = MENU_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__PARENT = MENU_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__CONTAINER_DATA = MENU_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__CUR_SHARED_REF = MENU_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__VISIBLE_WHEN = MENU_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__ACCESSIBILITY_PHRASE = MENU_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__LABEL = MENU_ELEMENT__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__ICON_URI = MENU_ELEMENT__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__TOOLTIP = MENU_ELEMENT__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR__MNEMONICS = MENU_ELEMENT__MNEMONICS;

	/**
	 * The number of structural features of the '<em>Separator</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR_FEATURE_COUNT = MENU_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR___GET_LOCALIZED_LABEL = MENU_ELEMENT___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR___GET_LOCALIZED_TOOLTIP = MENU_ELEMENT___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR___GET_LOCALIZED_MNEMONICS = MENU_ELEMENT___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Separator</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_SEPARATOR_OPERATION_COUNT = MENU_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuImpl <em>Menu</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenu()
	 * @generated
	 */
	public static final int MENU = 5;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__ELEMENT_ID = MENU_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__PERSISTED_STATE = MENU_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__TAGS = MENU_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__CONTRIBUTOR_URI = MENU_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__TRANSIENT_DATA = MENU_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__WIDGET = MENU_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__RENDERER = MENU_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__TO_BE_RENDERED = MENU_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__ON_TOP = MENU_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__VISIBLE = MENU_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__PARENT = MENU_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__CONTAINER_DATA = MENU_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__CUR_SHARED_REF = MENU_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__VISIBLE_WHEN = MENU_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__ACCESSIBILITY_PHRASE = MENU_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__LABEL = MENU_ELEMENT__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__ICON_URI = MENU_ELEMENT__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__TOOLTIP = MENU_ELEMENT__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__MNEMONICS = MENU_ELEMENT__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__CHILDREN = MENU_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__SELECTED_ELEMENT = MENU_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU__ENABLED = MENU_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_FEATURE_COUNT = MENU_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU___GET_LOCALIZED_LABEL = MENU_ELEMENT___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU___GET_LOCALIZED_TOOLTIP = MENU_ELEMENT___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU___GET_LOCALIZED_MNEMONICS = MENU_ELEMENT___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_OPERATION_COUNT = MENU_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuContributionImpl <em>Contribution</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuContributionImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuContribution()
	 * @generated
	 */
	public static final int MENU_CONTRIBUTION = 6;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__ELEMENT_ID = UiPackageImpl.ELEMENT_CONTAINER__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__PERSISTED_STATE = UiPackageImpl.ELEMENT_CONTAINER__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__TAGS = UiPackageImpl.ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__CONTRIBUTOR_URI = UiPackageImpl.ELEMENT_CONTAINER__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__TRANSIENT_DATA = UiPackageImpl.ELEMENT_CONTAINER__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__WIDGET = UiPackageImpl.ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__RENDERER = UiPackageImpl.ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__TO_BE_RENDERED = UiPackageImpl.ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__ON_TOP = UiPackageImpl.ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__VISIBLE = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__PARENT = UiPackageImpl.ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__CONTAINER_DATA = UiPackageImpl.ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__CUR_SHARED_REF = UiPackageImpl.ELEMENT_CONTAINER__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__VISIBLE_WHEN = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__CHILDREN = UiPackageImpl.ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__SELECTED_ELEMENT = UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Position In Parent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__POSITION_IN_PARENT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Parent Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION__PARENT_ID = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Contribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION_FEATURE_COUNT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION___GET_LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Contribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTION_OPERATION_COUNT = UiPackageImpl.ELEMENT_CONTAINER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.PopupMenuImpl <em>Popup Menu</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.PopupMenuImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getPopupMenu()
	 * @generated
	 */
	public static final int POPUP_MENU = 7;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__ELEMENT_ID = MENU__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__PERSISTED_STATE = MENU__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__TAGS = MENU__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__CONTRIBUTOR_URI = MENU__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__TRANSIENT_DATA = MENU__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__WIDGET = MENU__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__RENDERER = MENU__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__TO_BE_RENDERED = MENU__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__ON_TOP = MENU__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__VISIBLE = MENU__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__PARENT = MENU__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__CONTAINER_DATA = MENU__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__CUR_SHARED_REF = MENU__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__VISIBLE_WHEN = MENU__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__ACCESSIBILITY_PHRASE = MENU__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__LABEL = MENU__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__ICON_URI = MENU__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__TOOLTIP = MENU__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__MNEMONICS = MENU__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__CHILDREN = MENU__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__SELECTED_ELEMENT = MENU__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__ENABLED = MENU__ENABLED;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__CONTEXT = MENU_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__VARIABLES = MENU_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU__PROPERTIES = MENU_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Popup Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU_FEATURE_COUNT = MENU_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU___GET_LOCALIZED_LABEL = MENU___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU___GET_LOCALIZED_TOOLTIP = MENU___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU___GET_LOCALIZED_MNEMONICS = MENU___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Popup Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int POPUP_MENU_OPERATION_COUNT = MENU_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.DirectMenuItemImpl <em>Direct Menu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.DirectMenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getDirectMenuItem()
	 * @generated
	 */
	public static final int DIRECT_MENU_ITEM = 8;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__ELEMENT_ID = MENU_ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__PERSISTED_STATE = MENU_ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__TAGS = MENU_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__CONTRIBUTOR_URI = MENU_ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__TRANSIENT_DATA = MENU_ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__WIDGET = MENU_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__RENDERER = MENU_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__TO_BE_RENDERED = MENU_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__ON_TOP = MENU_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__VISIBLE = MENU_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__PARENT = MENU_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__CONTAINER_DATA = MENU_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__CUR_SHARED_REF = MENU_ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__VISIBLE_WHEN = MENU_ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__ACCESSIBILITY_PHRASE = MENU_ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__LABEL = MENU_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__ICON_URI = MENU_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__TOOLTIP = MENU_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__ENABLED = MENU_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__SELECTED = MENU_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__TYPE = MENU_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__MNEMONICS = MENU_ITEM__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__CONTRIBUTION_URI = MENU_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM__OBJECT = MENU_ITEM_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Direct Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM_FEATURE_COUNT = MENU_ITEM_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM___GET_LOCALIZED_LABEL = MENU_ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM___GET_LOCALIZED_TOOLTIP = MENU_ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM___GET_LOCALIZED_MNEMONICS = MENU_ITEM___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Direct Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_MENU_ITEM_OPERATION_COUNT = MENU_ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledMenuItemImpl <em>Handled Menu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.HandledMenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getHandledMenuItem()
	 * @generated
	 */
	public static final int HANDLED_MENU_ITEM = 9;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__ELEMENT_ID = MENU_ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__PERSISTED_STATE = MENU_ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__TAGS = MENU_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__CONTRIBUTOR_URI = MENU_ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__TRANSIENT_DATA = MENU_ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__WIDGET = MENU_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__RENDERER = MENU_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__TO_BE_RENDERED = MENU_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__ON_TOP = MENU_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__VISIBLE = MENU_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__PARENT = MENU_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__CONTAINER_DATA = MENU_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__CUR_SHARED_REF = MENU_ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__VISIBLE_WHEN = MENU_ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__ACCESSIBILITY_PHRASE = MENU_ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__LABEL = MENU_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__ICON_URI = MENU_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__TOOLTIP = MENU_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__ENABLED = MENU_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__SELECTED = MENU_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__TYPE = MENU_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__MNEMONICS = MENU_ITEM__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__COMMAND = MENU_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__WB_COMMAND = MENU_ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM__PARAMETERS = MENU_ITEM_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Handled Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM_FEATURE_COUNT = MENU_ITEM_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM___GET_LOCALIZED_LABEL = MENU_ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM___GET_LOCALIZED_TOOLTIP = MENU_ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM___GET_LOCALIZED_MNEMONICS = MENU_ITEM___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Handled Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_MENU_ITEM_OPERATION_COUNT = MENU_ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolItemImpl <em>Tool Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolItem()
	 * @generated
	 */
	public static final int TOOL_ITEM = 10;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__ELEMENT_ID = ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__PERSISTED_STATE = ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__TAGS = ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__CONTRIBUTOR_URI = ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__TRANSIENT_DATA = ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__WIDGET = ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__RENDERER = ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__TO_BE_RENDERED = ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__ON_TOP = ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__VISIBLE = ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__PARENT = ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__CONTAINER_DATA = ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__CUR_SHARED_REF = ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__VISIBLE_WHEN = ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__ACCESSIBILITY_PHRASE = ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__LABEL = ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__ICON_URI = ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__TOOLTIP = ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__ENABLED = ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__SELECTED = ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__TYPE = ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM__MENU = ITEM_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM_FEATURE_COUNT = ITEM_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM___GET_LOCALIZED_LABEL = ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM___GET_LOCALIZED_TOOLTIP = ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The number of operations of the '<em>Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_ITEM_OPERATION_COUNT = ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarImpl <em>Tool Bar</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBar()
	 * @generated
	 */
	public static final int TOOL_BAR = 11;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__ELEMENT_ID = UiPackageImpl.ELEMENT_CONTAINER__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__PERSISTED_STATE = UiPackageImpl.ELEMENT_CONTAINER__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__TAGS = UiPackageImpl.ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__CONTRIBUTOR_URI = UiPackageImpl.ELEMENT_CONTAINER__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__TRANSIENT_DATA = UiPackageImpl.ELEMENT_CONTAINER__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__WIDGET = UiPackageImpl.ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__RENDERER = UiPackageImpl.ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__TO_BE_RENDERED = UiPackageImpl.ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__ON_TOP = UiPackageImpl.ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__VISIBLE = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__PARENT = UiPackageImpl.ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__CONTAINER_DATA = UiPackageImpl.ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__CUR_SHARED_REF = UiPackageImpl.ELEMENT_CONTAINER__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__VISIBLE_WHEN = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__CHILDREN = UiPackageImpl.ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR__SELECTED_ELEMENT = UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The number of structural features of the '<em>Tool Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_FEATURE_COUNT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR___GET_LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Tool Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_OPERATION_COUNT = UiPackageImpl.ELEMENT_CONTAINER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarElementImpl <em>Tool Bar Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarElementImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBarElement()
	 * @generated
	 */
	public static final int TOOL_BAR_ELEMENT = 12;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__ELEMENT_ID = UiPackageImpl.UI_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__PERSISTED_STATE = UiPackageImpl.UI_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__TAGS = UiPackageImpl.UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__CONTRIBUTOR_URI = UiPackageImpl.UI_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__TRANSIENT_DATA = UiPackageImpl.UI_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__WIDGET = UiPackageImpl.UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__RENDERER = UiPackageImpl.UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__TO_BE_RENDERED = UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__ON_TOP = UiPackageImpl.UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__VISIBLE = UiPackageImpl.UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__PARENT = UiPackageImpl.UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__CONTAINER_DATA = UiPackageImpl.UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__CUR_SHARED_REF = UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__VISIBLE_WHEN = UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT__ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The number of structural features of the '<em>Tool Bar Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT_FEATURE_COUNT = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Tool Bar Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_ELEMENT_OPERATION_COUNT = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolControlImpl <em>Tool Control</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolControlImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolControl()
	 * @generated
	 */
	public static final int TOOL_CONTROL = 13;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__ELEMENT_ID = TOOL_BAR_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__PERSISTED_STATE = TOOL_BAR_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__TAGS = TOOL_BAR_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__CONTRIBUTOR_URI = TOOL_BAR_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__TRANSIENT_DATA = TOOL_BAR_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__WIDGET = TOOL_BAR_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__RENDERER = TOOL_BAR_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__TO_BE_RENDERED = TOOL_BAR_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__ON_TOP = TOOL_BAR_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__VISIBLE = TOOL_BAR_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__PARENT = TOOL_BAR_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__CONTAINER_DATA = TOOL_BAR_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__CUR_SHARED_REF = TOOL_BAR_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__VISIBLE_WHEN = TOOL_BAR_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__ACCESSIBILITY_PHRASE = TOOL_BAR_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__CONTRIBUTION_URI = TOOL_BAR_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL__OBJECT = TOOL_BAR_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Tool Control</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL_FEATURE_COUNT = TOOL_BAR_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL___GET_LOCALIZED_ACCESSIBILITY_PHRASE = TOOL_BAR_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Tool Control</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_CONTROL_OPERATION_COUNT = TOOL_BAR_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl <em>Handled Tool Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getHandledToolItem()
	 * @generated
	 */
	public static final int HANDLED_TOOL_ITEM = 14;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__ELEMENT_ID = TOOL_ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__PERSISTED_STATE = TOOL_ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__TAGS = TOOL_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__CONTRIBUTOR_URI = TOOL_ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__TRANSIENT_DATA = TOOL_ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__WIDGET = TOOL_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__RENDERER = TOOL_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__TO_BE_RENDERED = TOOL_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__ON_TOP = TOOL_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__VISIBLE = TOOL_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__PARENT = TOOL_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__CONTAINER_DATA = TOOL_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__CUR_SHARED_REF = TOOL_ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__VISIBLE_WHEN = TOOL_ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__ACCESSIBILITY_PHRASE = TOOL_ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__LABEL = TOOL_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__ICON_URI = TOOL_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__TOOLTIP = TOOL_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__ENABLED = TOOL_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__SELECTED = TOOL_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__TYPE = TOOL_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__MENU = TOOL_ITEM__MENU;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__COMMAND = TOOL_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__WB_COMMAND = TOOL_ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM__PARAMETERS = TOOL_ITEM_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Handled Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM_FEATURE_COUNT = TOOL_ITEM_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = TOOL_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM___GET_LOCALIZED_LABEL = TOOL_ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM___GET_LOCALIZED_TOOLTIP = TOOL_ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The number of operations of the '<em>Handled Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLED_TOOL_ITEM_OPERATION_COUNT = TOOL_ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.DirectToolItemImpl <em>Direct Tool Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.DirectToolItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getDirectToolItem()
	 * @generated
	 */
	public static final int DIRECT_TOOL_ITEM = 15;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__ELEMENT_ID = TOOL_ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__PERSISTED_STATE = TOOL_ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__TAGS = TOOL_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__CONTRIBUTOR_URI = TOOL_ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__TRANSIENT_DATA = TOOL_ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__WIDGET = TOOL_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__RENDERER = TOOL_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__TO_BE_RENDERED = TOOL_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__ON_TOP = TOOL_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__VISIBLE = TOOL_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__PARENT = TOOL_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__CONTAINER_DATA = TOOL_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__CUR_SHARED_REF = TOOL_ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__VISIBLE_WHEN = TOOL_ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__ACCESSIBILITY_PHRASE = TOOL_ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__LABEL = TOOL_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__ICON_URI = TOOL_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__TOOLTIP = TOOL_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__ENABLED = TOOL_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__SELECTED = TOOL_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__TYPE = TOOL_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__MENU = TOOL_ITEM__MENU;

	/**
	 * The feature id for the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__CONTRIBUTION_URI = TOOL_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM__OBJECT = TOOL_ITEM_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Direct Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM_FEATURE_COUNT = TOOL_ITEM_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = TOOL_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM___GET_LOCALIZED_LABEL = TOOL_ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM___GET_LOCALIZED_TOOLTIP = TOOL_ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The number of operations of the '<em>Direct Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DIRECT_TOOL_ITEM_OPERATION_COUNT = TOOL_ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarSeparatorImpl <em>Tool Bar Separator</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarSeparatorImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBarSeparator()
	 * @generated
	 */
	public static final int TOOL_BAR_SEPARATOR = 16;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__ELEMENT_ID = TOOL_BAR_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__PERSISTED_STATE = TOOL_BAR_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__TAGS = TOOL_BAR_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__CONTRIBUTOR_URI = TOOL_BAR_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__TRANSIENT_DATA = TOOL_BAR_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__WIDGET = TOOL_BAR_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__RENDERER = TOOL_BAR_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__TO_BE_RENDERED = TOOL_BAR_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__ON_TOP = TOOL_BAR_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__VISIBLE = TOOL_BAR_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__PARENT = TOOL_BAR_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__CONTAINER_DATA = TOOL_BAR_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__CUR_SHARED_REF = TOOL_BAR_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__VISIBLE_WHEN = TOOL_BAR_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR__ACCESSIBILITY_PHRASE = TOOL_BAR_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The number of structural features of the '<em>Tool Bar Separator</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR_FEATURE_COUNT = TOOL_BAR_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR___GET_LOCALIZED_ACCESSIBILITY_PHRASE = TOOL_BAR_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Tool Bar Separator</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_SEPARATOR_OPERATION_COUNT = TOOL_BAR_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions <em>Contributions</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuContributions()
	 * @generated
	 */
	public static final int MENU_CONTRIBUTIONS = 17;

	/**
	 * The feature id for the '<em><b>Menu Contributions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS = 0;

	/**
	 * The number of structural features of the '<em>Contributions</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTIONS_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Contributions</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MENU_CONTRIBUTIONS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuImpl <em>Rendered Menu</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getRenderedMenu()
	 * @generated
	 */
	public static final int RENDERED_MENU = 18;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__ELEMENT_ID = MENU__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__PERSISTED_STATE = MENU__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__TAGS = MENU__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__CONTRIBUTOR_URI = MENU__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__TRANSIENT_DATA = MENU__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__WIDGET = MENU__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__RENDERER = MENU__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__TO_BE_RENDERED = MENU__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__ON_TOP = MENU__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__VISIBLE = MENU__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__PARENT = MENU__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__CONTAINER_DATA = MENU__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__CUR_SHARED_REF = MENU__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__VISIBLE_WHEN = MENU__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__ACCESSIBILITY_PHRASE = MENU__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__LABEL = MENU__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__ICON_URI = MENU__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__TOOLTIP = MENU__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__MNEMONICS = MENU__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__CHILDREN = MENU__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__SELECTED_ELEMENT = MENU__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__ENABLED = MENU__ENABLED;

	/**
	 * The feature id for the '<em><b>Contribution Manager</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU__CONTRIBUTION_MANAGER = MENU_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Rendered Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_FEATURE_COUNT = MENU_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU___GET_LOCALIZED_LABEL = MENU___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU___GET_LOCALIZED_TOOLTIP = MENU___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU___GET_LOCALIZED_MNEMONICS = MENU___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Rendered Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_OPERATION_COUNT = MENU_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedToolBarImpl <em>Rendered Tool Bar</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedToolBarImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getRenderedToolBar()
	 * @generated
	 */
	public static final int RENDERED_TOOL_BAR = 19;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__ELEMENT_ID = TOOL_BAR__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__PERSISTED_STATE = TOOL_BAR__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__TAGS = TOOL_BAR__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__CONTRIBUTOR_URI = TOOL_BAR__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__TRANSIENT_DATA = TOOL_BAR__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__WIDGET = TOOL_BAR__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__RENDERER = TOOL_BAR__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__TO_BE_RENDERED = TOOL_BAR__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__ON_TOP = TOOL_BAR__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__VISIBLE = TOOL_BAR__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__PARENT = TOOL_BAR__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__CONTAINER_DATA = TOOL_BAR__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__CUR_SHARED_REF = TOOL_BAR__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__VISIBLE_WHEN = TOOL_BAR__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__ACCESSIBILITY_PHRASE = TOOL_BAR__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__CHILDREN = TOOL_BAR__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__SELECTED_ELEMENT = TOOL_BAR__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Contribution Manager</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR__CONTRIBUTION_MANAGER = TOOL_BAR_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Rendered Tool Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR_FEATURE_COUNT = TOOL_BAR_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR___GET_LOCALIZED_ACCESSIBILITY_PHRASE = TOOL_BAR___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Rendered Tool Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_TOOL_BAR_OPERATION_COUNT = TOOL_BAR_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarContributionImpl <em>Tool Bar Contribution</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarContributionImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBarContribution()
	 * @generated
	 */
	public static final int TOOL_BAR_CONTRIBUTION = 20;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__ELEMENT_ID = UiPackageImpl.ELEMENT_CONTAINER__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__PERSISTED_STATE = UiPackageImpl.ELEMENT_CONTAINER__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__TAGS = UiPackageImpl.ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__CONTRIBUTOR_URI = UiPackageImpl.ELEMENT_CONTAINER__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__TRANSIENT_DATA = UiPackageImpl.ELEMENT_CONTAINER__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__WIDGET = UiPackageImpl.ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__RENDERER = UiPackageImpl.ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__TO_BE_RENDERED = UiPackageImpl.ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__ON_TOP = UiPackageImpl.ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__VISIBLE = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__PARENT = UiPackageImpl.ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__CONTAINER_DATA = UiPackageImpl.ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__CUR_SHARED_REF = UiPackageImpl.ELEMENT_CONTAINER__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__VISIBLE_WHEN = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__CHILDREN = UiPackageImpl.ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__SELECTED_ELEMENT = UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Parent Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__PARENT_ID = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Position In Parent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION__POSITION_IN_PARENT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Tool Bar Contribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION_FEATURE_COUNT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION___GET_LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Tool Bar Contribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTION_OPERATION_COUNT = UiPackageImpl.ELEMENT_CONTAINER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions <em>Tool Bar Contributions</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBarContributions()
	 * @generated
	 */
	public static final int TOOL_BAR_CONTRIBUTIONS = 21;

	/**
	 * The feature id for the '<em><b>Tool Bar Contributions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS = 0;

	/**
	 * The number of structural features of the '<em>Tool Bar Contributions</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTIONS_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Tool Bar Contributions</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TOOL_BAR_CONTRIBUTIONS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.TrimContributionImpl <em>Trim Contribution</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.TrimContributionImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getTrimContribution()
	 * @generated
	 */
	public static final int TRIM_CONTRIBUTION = 22;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__ELEMENT_ID = UiPackageImpl.ELEMENT_CONTAINER__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__PERSISTED_STATE = UiPackageImpl.ELEMENT_CONTAINER__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__TAGS = UiPackageImpl.ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__CONTRIBUTOR_URI = UiPackageImpl.ELEMENT_CONTAINER__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__TRANSIENT_DATA = UiPackageImpl.ELEMENT_CONTAINER__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__WIDGET = UiPackageImpl.ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__RENDERER = UiPackageImpl.ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__TO_BE_RENDERED = UiPackageImpl.ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__ON_TOP = UiPackageImpl.ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__VISIBLE = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__PARENT = UiPackageImpl.ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__CONTAINER_DATA = UiPackageImpl.ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__CUR_SHARED_REF = UiPackageImpl.ELEMENT_CONTAINER__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__VISIBLE_WHEN = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__CHILDREN = UiPackageImpl.ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__SELECTED_ELEMENT = UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Parent Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__PARENT_ID = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Position In Parent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION__POSITION_IN_PARENT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Trim Contribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION_FEATURE_COUNT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION___GET_LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Trim Contribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTION_OPERATION_COUNT = UiPackageImpl.ELEMENT_CONTAINER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions <em>Trim Contributions</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getTrimContributions()
	 * @generated
	 */
	public static final int TRIM_CONTRIBUTIONS = 23;

	/**
	 * The feature id for the '<em><b>Trim Contributions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS = 0;

	/**
	 * The number of structural features of the '<em>Trim Contributions</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTIONS_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Trim Contributions</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_CONTRIBUTIONS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuItemImpl <em>Rendered Menu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getRenderedMenuItem()
	 * @generated
	 */
	public static final int RENDERED_MENU_ITEM = 24;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__ELEMENT_ID = MENU_ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__PERSISTED_STATE = MENU_ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__TAGS = MENU_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__CONTRIBUTOR_URI = MENU_ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__TRANSIENT_DATA = MENU_ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__WIDGET = MENU_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__RENDERER = MENU_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__TO_BE_RENDERED = MENU_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__ON_TOP = MENU_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__VISIBLE = MENU_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__PARENT = MENU_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__CONTAINER_DATA = MENU_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__CUR_SHARED_REF = MENU_ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__VISIBLE_WHEN = MENU_ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__ACCESSIBILITY_PHRASE = MENU_ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__LABEL = MENU_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__ICON_URI = MENU_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__TOOLTIP = MENU_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__ENABLED = MENU_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__SELECTED = MENU_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__TYPE = MENU_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__MNEMONICS = MENU_ITEM__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Contribution Item</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM__CONTRIBUTION_ITEM = MENU_ITEM_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Rendered Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM_FEATURE_COUNT = MENU_ITEM_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM___GET_LOCALIZED_LABEL = MENU_ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM___GET_LOCALIZED_TOOLTIP = MENU_ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM___GET_LOCALIZED_MNEMONICS = MENU_ITEM___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Rendered Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int RENDERED_MENU_ITEM_OPERATION_COUNT = MENU_ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueToolItemImpl <em>Opaque Tool Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueToolItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getOpaqueToolItem()
	 * @generated
	 */
	public static final int OPAQUE_TOOL_ITEM = 25;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__ELEMENT_ID = TOOL_ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__PERSISTED_STATE = TOOL_ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__TAGS = TOOL_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__CONTRIBUTOR_URI = TOOL_ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__TRANSIENT_DATA = TOOL_ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__WIDGET = TOOL_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__RENDERER = TOOL_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__TO_BE_RENDERED = TOOL_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__ON_TOP = TOOL_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__VISIBLE = TOOL_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__PARENT = TOOL_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__CONTAINER_DATA = TOOL_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__CUR_SHARED_REF = TOOL_ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__VISIBLE_WHEN = TOOL_ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__ACCESSIBILITY_PHRASE = TOOL_ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__LABEL = TOOL_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__ICON_URI = TOOL_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__TOOLTIP = TOOL_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__ENABLED = TOOL_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__SELECTED = TOOL_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__TYPE = TOOL_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__MENU = TOOL_ITEM__MENU;

	/**
	 * The feature id for the '<em><b>Opaque Item</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM__OPAQUE_ITEM = TOOL_ITEM_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Opaque Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM_FEATURE_COUNT = TOOL_ITEM_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = TOOL_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM___GET_LOCALIZED_LABEL = TOOL_ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM___GET_LOCALIZED_TOOLTIP = TOOL_ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The number of operations of the '<em>Opaque Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_TOOL_ITEM_OPERATION_COUNT = TOOL_ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuItemImpl <em>Opaque Menu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getOpaqueMenuItem()
	 * @generated
	 */
	public static final int OPAQUE_MENU_ITEM = 26;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__ELEMENT_ID = MENU_ITEM__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__PERSISTED_STATE = MENU_ITEM__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__TAGS = MENU_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__CONTRIBUTOR_URI = MENU_ITEM__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__TRANSIENT_DATA = MENU_ITEM__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__WIDGET = MENU_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__RENDERER = MENU_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__TO_BE_RENDERED = MENU_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__ON_TOP = MENU_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__VISIBLE = MENU_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__PARENT = MENU_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__CONTAINER_DATA = MENU_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__CUR_SHARED_REF = MENU_ITEM__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__VISIBLE_WHEN = MENU_ITEM__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__ACCESSIBILITY_PHRASE = MENU_ITEM__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__LABEL = MENU_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__ICON_URI = MENU_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__TOOLTIP = MENU_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__ENABLED = MENU_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__SELECTED = MENU_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__TYPE = MENU_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__MNEMONICS = MENU_ITEM__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Opaque Item</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM__OPAQUE_ITEM = MENU_ITEM_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Opaque Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM_FEATURE_COUNT = MENU_ITEM_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU_ITEM___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM___GET_LOCALIZED_LABEL = MENU_ITEM___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM___GET_LOCALIZED_TOOLTIP = MENU_ITEM___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM___GET_LOCALIZED_MNEMONICS = MENU_ITEM___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Opaque Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_ITEM_OPERATION_COUNT = MENU_ITEM_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuSeparatorImpl <em>Opaque Menu Separator</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuSeparatorImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getOpaqueMenuSeparator()
	 * @generated
	 */
	public static final int OPAQUE_MENU_SEPARATOR = 27;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__ELEMENT_ID = MENU_SEPARATOR__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__PERSISTED_STATE = MENU_SEPARATOR__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__TAGS = MENU_SEPARATOR__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__CONTRIBUTOR_URI = MENU_SEPARATOR__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__TRANSIENT_DATA = MENU_SEPARATOR__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__WIDGET = MENU_SEPARATOR__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__RENDERER = MENU_SEPARATOR__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__TO_BE_RENDERED = MENU_SEPARATOR__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__ON_TOP = MENU_SEPARATOR__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__VISIBLE = MENU_SEPARATOR__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__PARENT = MENU_SEPARATOR__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__CONTAINER_DATA = MENU_SEPARATOR__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__CUR_SHARED_REF = MENU_SEPARATOR__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__VISIBLE_WHEN = MENU_SEPARATOR__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__ACCESSIBILITY_PHRASE = MENU_SEPARATOR__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__LABEL = MENU_SEPARATOR__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__ICON_URI = MENU_SEPARATOR__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__TOOLTIP = MENU_SEPARATOR__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__MNEMONICS = MENU_SEPARATOR__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Opaque Item</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR__OPAQUE_ITEM = MENU_SEPARATOR_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Opaque Menu Separator</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR_FEATURE_COUNT = MENU_SEPARATOR_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU_SEPARATOR___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR___GET_LOCALIZED_LABEL = MENU_SEPARATOR___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR___GET_LOCALIZED_TOOLTIP = MENU_SEPARATOR___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR___GET_LOCALIZED_MNEMONICS = MENU_SEPARATOR___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Opaque Menu Separator</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_SEPARATOR_OPERATION_COUNT = MENU_SEPARATOR_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuImpl <em>Opaque Menu</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuImpl
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getOpaqueMenu()
	 * @generated
	 */
	public static final int OPAQUE_MENU = 28;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__ELEMENT_ID = MENU__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__PERSISTED_STATE = MENU__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__TAGS = MENU__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__CONTRIBUTOR_URI = MENU__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__TRANSIENT_DATA = MENU__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__WIDGET = MENU__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__RENDERER = MENU__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__TO_BE_RENDERED = MENU__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__ON_TOP = MENU__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__VISIBLE = MENU__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__PARENT = MENU__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__CONTAINER_DATA = MENU__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__CUR_SHARED_REF = MENU__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__VISIBLE_WHEN = MENU__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__ACCESSIBILITY_PHRASE = MENU__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__LABEL = MENU__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__ICON_URI = MENU__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__TOOLTIP = MENU__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__MNEMONICS = MENU__MNEMONICS;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__CHILDREN = MENU__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__SELECTED_ELEMENT = MENU__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU__ENABLED = MENU__ENABLED;

	/**
	 * The number of structural features of the '<em>Opaque Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_FEATURE_COUNT = MENU_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU___GET_LOCALIZED_ACCESSIBILITY_PHRASE = MENU___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU___GET_LOCALIZED_LABEL = MENU___GET_LOCALIZED_LABEL;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU___GET_LOCALIZED_TOOLTIP = MENU___GET_LOCALIZED_TOOLTIP;

	/**
	 * The operation id for the '<em>Get Localized Mnemonics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU___GET_LOCALIZED_MNEMONICS = MENU___GET_LOCALIZED_MNEMONICS;

	/**
	 * The number of operations of the '<em>Opaque Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int OPAQUE_MENU_OPERATION_COUNT = MENU_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.menu.ItemType <em>Item Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.menu.ItemType
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getItemType()
	 * @generated
	 */
	public static final int ITEM_TYPE = 29;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass itemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass handledItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass menuElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass menuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass menuSeparatorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass menuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass menuContributionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass popupMenuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass directMenuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass handledMenuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolBarEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolBarElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolControlEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass handledToolItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass directToolItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolBarSeparatorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass menuContributionsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass renderedMenuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass renderedToolBarEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolBarContributionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolBarContributionsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass trimContributionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass trimContributionsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass renderedMenuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass opaqueToolItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass opaqueMenuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass opaqueMenuSeparatorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass opaqueMenuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum itemTypeEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private MenuPackageImpl() {
		super(eNS_URI, ((EFactory)MMenuFactory.INSTANCE));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link MenuPackageImpl#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static MenuPackageImpl init() {
		if (isInited) return (MenuPackageImpl)EPackage.Registry.INSTANCE.getEPackage(MenuPackageImpl.eNS_URI);

		// Obtain or create and register package
		MenuPackageImpl theMenuPackage = (MenuPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof MenuPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new MenuPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI) instanceof ApplicationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI) : ApplicationPackageImpl.eINSTANCE);
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI) instanceof CommandsPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI) : CommandsPackageImpl.eINSTANCE);
		UiPackageImpl theUiPackage = (UiPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI) instanceof UiPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI) : UiPackageImpl.eINSTANCE);
		BasicPackageImpl theBasicPackage = (BasicPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI) instanceof BasicPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI) : BasicPackageImpl.eINSTANCE);
		AdvancedPackageImpl theAdvancedPackage = (AdvancedPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(AdvancedPackageImpl.eNS_URI) instanceof AdvancedPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(AdvancedPackageImpl.eNS_URI) : AdvancedPackageImpl.eINSTANCE);
		org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl theBasicPackage_1 = (org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI) instanceof org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI) : org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

		// Create package meta-data objects
		theMenuPackage.createPackageContents();
		theApplicationPackage.createPackageContents();
		theCommandsPackage.createPackageContents();
		theUiPackage.createPackageContents();
		theBasicPackage.createPackageContents();
		theAdvancedPackage.createPackageContents();
		theBasicPackage_1.createPackageContents();

		// Initialize created meta-data
		theMenuPackage.initializePackageContents();
		theApplicationPackage.initializePackageContents();
		theCommandsPackage.initializePackageContents();
		theUiPackage.initializePackageContents();
		theBasicPackage.initializePackageContents();
		theAdvancedPackage.initializePackageContents();
		theBasicPackage_1.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theMenuPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(MenuPackageImpl.eNS_URI, theMenuPackage);
		return theMenuPackage;
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MItem <em>Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MItem
	 * @generated
	 */
	public EClass getItem() {
		return itemEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MItem#isEnabled <em>Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Enabled</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MItem#isEnabled()
	 * @see #getItem()
	 * @generated
	 */
	public EAttribute getItem_Enabled() {
		return (EAttribute)itemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MItem#isSelected <em>Selected</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Selected</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MItem#isSelected()
	 * @see #getItem()
	 * @generated
	 */
	public EAttribute getItem_Selected() {
		return (EAttribute)itemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MItem#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MItem#getType()
	 * @see #getItem()
	 * @generated
	 */
	public EAttribute getItem_Type() {
		return (EAttribute)itemEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem <em>Handled Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handled Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MHandledItem
	 * @generated
	 */
	public EClass getHandledItem() {
		return handledItemEClass;
	}

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getCommand()
	 * @see #getHandledItem()
	 * @generated
	 */
	public EReference getHandledItem_Command() {
		return (EReference)handledItemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getWbCommand <em>Wb Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Wb Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getWbCommand()
	 * @see #getHandledItem()
	 * @generated
	 */
	public EAttribute getHandledItem_WbCommand() {
		return (EAttribute)handledItemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getParameters()
	 * @see #getHandledItem()
	 * @generated
	 */
	public EReference getHandledItem_Parameters() {
		return (EReference)handledItemEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuElement <em>Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuElement
	 * @generated
	 */
	public EClass getMenuElement() {
		return menuElementEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuElement#getMnemonics <em>Mnemonics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mnemonics</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuElement#getMnemonics()
	 * @see #getMenuElement()
	 * @generated
	 */
	public EAttribute getMenuElement_Mnemonics() {
		return (EAttribute)menuElementEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for the '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuElement#getLocalizedMnemonics() <em>Get Localized Mnemonics</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Localized Mnemonics</em>' operation.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuElement#getLocalizedMnemonics()
	 * @generated
	 */
	public EOperation getMenuElement__GetLocalizedMnemonics() {
		return menuElementEClass.getEOperations().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuItem <em>Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuItem
	 * @generated
	 */
	public EClass getMenuItem() {
		return menuItemEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator <em>Separator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Separator</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator
	 * @generated
	 */
	public EClass getMenuSeparator() {
		return menuSeparatorEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenu <em>Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenu
	 * @generated
	 */
	public EClass getMenu() {
		return menuEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenu#isEnabled <em>Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Enabled</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenu#isEnabled()
	 * @see #getMenu()
	 * @generated
	 */
	public EAttribute getMenu_Enabled() {
		return (EAttribute)menuEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution <em>Contribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Contribution</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution
	 * @generated
	 */
	public EClass getMenuContribution() {
		return menuContributionEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution#getPositionInParent <em>Position In Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position In Parent</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution#getPositionInParent()
	 * @see #getMenuContribution()
	 * @generated
	 */
	public EAttribute getMenuContribution_PositionInParent() {
		return (EAttribute)menuContributionEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution#getParentId <em>Parent Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parent Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution#getParentId()
	 * @see #getMenuContribution()
	 * @generated
	 */
	public EAttribute getMenuContribution_ParentId() {
		return (EAttribute)menuContributionEClass.getEStructuralFeatures().get(1);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu <em>Popup Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Popup Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu
	 * @generated
	 */
	public EClass getPopupMenu() {
		return popupMenuEClass;
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem <em>Direct Menu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Direct Menu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem
	 * @generated
	 */
	public EClass getDirectMenuItem() {
		return directMenuItemEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem <em>Handled Menu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handled Menu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem
	 * @generated
	 */
	public EClass getHandledMenuItem() {
		return handledMenuItemEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolItem <em>Tool Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolItem
	 * @generated
	 */
	public EClass getToolItem() {
		return toolItemEClass;
	}

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolItem#getMenu <em>Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolItem#getMenu()
	 * @see #getToolItem()
	 * @generated
	 */
	public EReference getToolItem_Menu() {
		return (EReference)toolItemEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBar <em>Tool Bar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Bar</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBar
	 * @generated
	 */
	public EClass getToolBar() {
		return toolBarEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement <em>Tool Bar Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Bar Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement
	 * @generated
	 */
	public EClass getToolBarElement() {
		return toolBarElementEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolControl <em>Tool Control</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Control</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolControl
	 * @generated
	 */
	public EClass getToolControl() {
		return toolControlEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem <em>Handled Tool Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handled Tool Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem
	 * @generated
	 */
	public EClass getHandledToolItem() {
		return handledToolItemEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem <em>Direct Tool Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Direct Tool Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem
	 * @generated
	 */
	public EClass getDirectToolItem() {
		return directToolItemEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator <em>Tool Bar Separator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Bar Separator</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator
	 * @generated
	 */
	public EClass getToolBarSeparator() {
		return toolBarSeparatorEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions <em>Contributions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Contributions</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions
	 * @generated
	 */
	public EClass getMenuContributions() {
		return menuContributionsEClass;
	}


	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions#getMenuContributions <em>Menu Contributions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Menu Contributions</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions#getMenuContributions()
	 * @see #getMenuContributions()
	 * @generated
	 */
	public EReference getMenuContributions_MenuContributions() {
		return (EReference)menuContributionsEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu <em>Rendered Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Rendered Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu
	 * @generated
	 */
	public EClass getRenderedMenu() {
		return renderedMenuEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu#getContributionManager <em>Contribution Manager</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Contribution Manager</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu#getContributionManager()
	 * @see #getRenderedMenu()
	 * @generated
	 */
	public EAttribute getRenderedMenu_ContributionManager() {
		return (EAttribute)renderedMenuEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar <em>Rendered Tool Bar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Rendered Tool Bar</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar
	 * @generated
	 */
	public EClass getRenderedToolBar() {
		return renderedToolBarEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar#getContributionManager <em>Contribution Manager</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Contribution Manager</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar#getContributionManager()
	 * @see #getRenderedToolBar()
	 * @generated
	 */
	public EAttribute getRenderedToolBar_ContributionManager() {
		return (EAttribute)renderedToolBarEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution <em>Tool Bar Contribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Bar Contribution</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution
	 * @generated
	 */
	public EClass getToolBarContribution() {
		return toolBarContributionEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution#getParentId <em>Parent Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parent Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution#getParentId()
	 * @see #getToolBarContribution()
	 * @generated
	 */
	public EAttribute getToolBarContribution_ParentId() {
		return (EAttribute)toolBarContributionEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution#getPositionInParent <em>Position In Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position In Parent</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution#getPositionInParent()
	 * @see #getToolBarContribution()
	 * @generated
	 */
	public EAttribute getToolBarContribution_PositionInParent() {
		return (EAttribute)toolBarContributionEClass.getEStructuralFeatures().get(1);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions <em>Tool Bar Contributions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Bar Contributions</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions
	 * @generated
	 */
	public EClass getToolBarContributions() {
		return toolBarContributionsEClass;
	}


	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions#getToolBarContributions <em>Tool Bar Contributions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Tool Bar Contributions</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions#getToolBarContributions()
	 * @see #getToolBarContributions()
	 * @generated
	 */
	public EReference getToolBarContributions_ToolBarContributions() {
		return (EReference)toolBarContributionsEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution <em>Trim Contribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Trim Contribution</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution
	 * @generated
	 */
	public EClass getTrimContribution() {
		return trimContributionEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution#getParentId <em>Parent Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parent Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution#getParentId()
	 * @see #getTrimContribution()
	 * @generated
	 */
	public EAttribute getTrimContribution_ParentId() {
		return (EAttribute)trimContributionEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution#getPositionInParent <em>Position In Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position In Parent</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution#getPositionInParent()
	 * @see #getTrimContribution()
	 * @generated
	 */
	public EAttribute getTrimContribution_PositionInParent() {
		return (EAttribute)trimContributionEClass.getEStructuralFeatures().get(1);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions <em>Trim Contributions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Trim Contributions</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions
	 * @generated
	 */
	public EClass getTrimContributions() {
		return trimContributionsEClass;
	}


	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions#getTrimContributions <em>Trim Contributions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Trim Contributions</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions#getTrimContributions()
	 * @see #getTrimContributions()
	 * @generated
	 */
	public EReference getTrimContributions_TrimContributions() {
		return (EReference)trimContributionsEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem <em>Rendered Menu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Rendered Menu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem
	 * @generated
	 */
	public EClass getRenderedMenuItem() {
		return renderedMenuItemEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem#getContributionItem <em>Contribution Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Contribution Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem#getContributionItem()
	 * @see #getRenderedMenuItem()
	 * @generated
	 */
	public EAttribute getRenderedMenuItem_ContributionItem() {
		return (EAttribute)renderedMenuItemEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MOpaqueToolItem <em>Opaque Tool Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Opaque Tool Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MOpaqueToolItem
	 * @generated
	 */
	public EClass getOpaqueToolItem() {
		return opaqueToolItemEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MOpaqueToolItem#getOpaqueItem <em>Opaque Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Opaque Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MOpaqueToolItem#getOpaqueItem()
	 * @see #getOpaqueToolItem()
	 * @generated
	 */
	public EAttribute getOpaqueToolItem_OpaqueItem() {
		return (EAttribute)opaqueToolItemEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuItem <em>Opaque Menu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Opaque Menu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuItem
	 * @generated
	 */
	public EClass getOpaqueMenuItem() {
		return opaqueMenuItemEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuItem#getOpaqueItem <em>Opaque Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Opaque Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuItem#getOpaqueItem()
	 * @see #getOpaqueMenuItem()
	 * @generated
	 */
	public EAttribute getOpaqueMenuItem_OpaqueItem() {
		return (EAttribute)opaqueMenuItemEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator <em>Opaque Menu Separator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Opaque Menu Separator</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator
	 * @generated
	 */
	public EClass getOpaqueMenuSeparator() {
		return opaqueMenuSeparatorEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator#getOpaqueItem <em>Opaque Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Opaque Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator#getOpaqueItem()
	 * @see #getOpaqueMenuSeparator()
	 * @generated
	 */
	public EAttribute getOpaqueMenuSeparator_OpaqueItem() {
		return (EAttribute)opaqueMenuSeparatorEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenu <em>Opaque Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Opaque Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenu
	 * @generated
	 */
	public EClass getOpaqueMenu() {
		return opaqueMenuEClass;
	}


	/**
	 * Returns the meta object for enum '{@link org.eclipse.e4.ui.model.application.ui.menu.ItemType <em>Item Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Item Type</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.menu.ItemType
	 * @generated
	 */
	public EEnum getItemType() {
		return itemTypeEEnum;
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public MMenuFactory getMenuFactory() {
		return (MMenuFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		itemEClass = createEClass(ITEM);
		createEAttribute(itemEClass, ITEM__ENABLED);
		createEAttribute(itemEClass, ITEM__SELECTED);
		createEAttribute(itemEClass, ITEM__TYPE);

		handledItemEClass = createEClass(HANDLED_ITEM);
		createEReference(handledItemEClass, HANDLED_ITEM__COMMAND);
		createEAttribute(handledItemEClass, HANDLED_ITEM__WB_COMMAND);
		createEReference(handledItemEClass, HANDLED_ITEM__PARAMETERS);

		menuElementEClass = createEClass(MENU_ELEMENT);
		createEAttribute(menuElementEClass, MENU_ELEMENT__MNEMONICS);
		createEOperation(menuElementEClass, MENU_ELEMENT___GET_LOCALIZED_MNEMONICS);

		menuItemEClass = createEClass(MENU_ITEM);

		menuSeparatorEClass = createEClass(MENU_SEPARATOR);

		menuEClass = createEClass(MENU);
		createEAttribute(menuEClass, MENU__ENABLED);

		menuContributionEClass = createEClass(MENU_CONTRIBUTION);
		createEAttribute(menuContributionEClass, MENU_CONTRIBUTION__POSITION_IN_PARENT);
		createEAttribute(menuContributionEClass, MENU_CONTRIBUTION__PARENT_ID);

		popupMenuEClass = createEClass(POPUP_MENU);

		directMenuItemEClass = createEClass(DIRECT_MENU_ITEM);

		handledMenuItemEClass = createEClass(HANDLED_MENU_ITEM);

		toolItemEClass = createEClass(TOOL_ITEM);
		createEReference(toolItemEClass, TOOL_ITEM__MENU);

		toolBarEClass = createEClass(TOOL_BAR);

		toolBarElementEClass = createEClass(TOOL_BAR_ELEMENT);

		toolControlEClass = createEClass(TOOL_CONTROL);

		handledToolItemEClass = createEClass(HANDLED_TOOL_ITEM);

		directToolItemEClass = createEClass(DIRECT_TOOL_ITEM);

		toolBarSeparatorEClass = createEClass(TOOL_BAR_SEPARATOR);

		menuContributionsEClass = createEClass(MENU_CONTRIBUTIONS);
		createEReference(menuContributionsEClass, MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS);

		renderedMenuEClass = createEClass(RENDERED_MENU);
		createEAttribute(renderedMenuEClass, RENDERED_MENU__CONTRIBUTION_MANAGER);

		renderedToolBarEClass = createEClass(RENDERED_TOOL_BAR);
		createEAttribute(renderedToolBarEClass, RENDERED_TOOL_BAR__CONTRIBUTION_MANAGER);

		toolBarContributionEClass = createEClass(TOOL_BAR_CONTRIBUTION);
		createEAttribute(toolBarContributionEClass, TOOL_BAR_CONTRIBUTION__PARENT_ID);
		createEAttribute(toolBarContributionEClass, TOOL_BAR_CONTRIBUTION__POSITION_IN_PARENT);

		toolBarContributionsEClass = createEClass(TOOL_BAR_CONTRIBUTIONS);
		createEReference(toolBarContributionsEClass, TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS);

		trimContributionEClass = createEClass(TRIM_CONTRIBUTION);
		createEAttribute(trimContributionEClass, TRIM_CONTRIBUTION__PARENT_ID);
		createEAttribute(trimContributionEClass, TRIM_CONTRIBUTION__POSITION_IN_PARENT);

		trimContributionsEClass = createEClass(TRIM_CONTRIBUTIONS);
		createEReference(trimContributionsEClass, TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS);

		renderedMenuItemEClass = createEClass(RENDERED_MENU_ITEM);
		createEAttribute(renderedMenuItemEClass, RENDERED_MENU_ITEM__CONTRIBUTION_ITEM);

		opaqueToolItemEClass = createEClass(OPAQUE_TOOL_ITEM);
		createEAttribute(opaqueToolItemEClass, OPAQUE_TOOL_ITEM__OPAQUE_ITEM);

		opaqueMenuItemEClass = createEClass(OPAQUE_MENU_ITEM);
		createEAttribute(opaqueMenuItemEClass, OPAQUE_MENU_ITEM__OPAQUE_ITEM);

		opaqueMenuSeparatorEClass = createEClass(OPAQUE_MENU_SEPARATOR);
		createEAttribute(opaqueMenuSeparatorEClass, OPAQUE_MENU_SEPARATOR__OPAQUE_ITEM);

		opaqueMenuEClass = createEClass(OPAQUE_MENU);

		// Create enums
		itemTypeEEnum = createEEnum(ITEM_TYPE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		UiPackageImpl theUiPackage = (UiPackageImpl)EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI);
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl)EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI);
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI);
		BasicPackageImpl theBasicPackage = (BasicPackageImpl)EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		itemEClass.getESuperTypes().add(theUiPackage.getUIElement());
		itemEClass.getESuperTypes().add(theUiPackage.getUILabel());
		handledItemEClass.getESuperTypes().add(this.getItem());
		menuElementEClass.getESuperTypes().add(theUiPackage.getUIElement());
		menuElementEClass.getESuperTypes().add(theUiPackage.getUILabel());
		menuItemEClass.getESuperTypes().add(this.getItem());
		menuItemEClass.getESuperTypes().add(this.getMenuElement());
		menuSeparatorEClass.getESuperTypes().add(this.getMenuElement());
		EGenericType g1 = createEGenericType(this.getMenuElement());
		menuEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getElementContainer());
		EGenericType g2 = createEGenericType(this.getMenuElement());
		g1.getETypeArguments().add(g2);
		menuEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getElementContainer());
		g2 = createEGenericType(this.getMenuElement());
		g1.getETypeArguments().add(g2);
		menuContributionEClass.getEGenericSuperTypes().add(g1);
		popupMenuEClass.getESuperTypes().add(this.getMenu());
		popupMenuEClass.getESuperTypes().add(theUiPackage.getContext());
		directMenuItemEClass.getESuperTypes().add(this.getMenuItem());
		directMenuItemEClass.getESuperTypes().add(theApplicationPackage.getContribution());
		handledMenuItemEClass.getESuperTypes().add(this.getMenuItem());
		handledMenuItemEClass.getESuperTypes().add(this.getHandledItem());
		toolItemEClass.getESuperTypes().add(this.getItem());
		toolItemEClass.getESuperTypes().add(this.getToolBarElement());
		g1 = createEGenericType(theUiPackage.getElementContainer());
		g2 = createEGenericType(this.getToolBarElement());
		g1.getETypeArguments().add(g2);
		toolBarEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theBasicPackage.getTrimElement());
		toolBarEClass.getEGenericSuperTypes().add(g1);
		toolBarElementEClass.getESuperTypes().add(theUiPackage.getUIElement());
		toolControlEClass.getESuperTypes().add(this.getToolBarElement());
		toolControlEClass.getESuperTypes().add(theApplicationPackage.getContribution());
		toolControlEClass.getESuperTypes().add(theBasicPackage.getTrimElement());
		handledToolItemEClass.getESuperTypes().add(this.getToolItem());
		handledToolItemEClass.getESuperTypes().add(this.getHandledItem());
		directToolItemEClass.getESuperTypes().add(this.getToolItem());
		directToolItemEClass.getESuperTypes().add(theApplicationPackage.getContribution());
		toolBarSeparatorEClass.getESuperTypes().add(this.getToolBarElement());
		renderedMenuEClass.getESuperTypes().add(this.getMenu());
		renderedToolBarEClass.getESuperTypes().add(this.getToolBar());
		g1 = createEGenericType(theUiPackage.getElementContainer());
		g2 = createEGenericType(this.getToolBarElement());
		g1.getETypeArguments().add(g2);
		toolBarContributionEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getElementContainer());
		g2 = createEGenericType(theBasicPackage.getTrimElement());
		g1.getETypeArguments().add(g2);
		trimContributionEClass.getEGenericSuperTypes().add(g1);
		renderedMenuItemEClass.getESuperTypes().add(this.getMenuItem());
		opaqueToolItemEClass.getESuperTypes().add(this.getToolItem());
		opaqueMenuItemEClass.getESuperTypes().add(this.getMenuItem());
		opaqueMenuSeparatorEClass.getESuperTypes().add(this.getMenuSeparator());
		opaqueMenuEClass.getESuperTypes().add(this.getMenu());

		// Initialize classes, features, and operations; add parameters
		initEClass(itemEClass, MItem.class, "Item", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getItem_Enabled(), ecorePackage.getEBoolean(), "enabled", "true", 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getItem_Selected(), ecorePackage.getEBoolean(), "selected", null, 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getItem_Type(), this.getItemType(), "type", null, 1, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(handledItemEClass, MHandledItem.class, "HandledItem", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandledItem_Command(), theCommandsPackage.getCommand(), null, "command", null, 0, 1, MHandledItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getHandledItem_WbCommand(), theCommandsPackage.getParameterizedCommand(), "wbCommand", null, 0, 1, MHandledItem.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getHandledItem_Parameters(), theCommandsPackage.getParameter(), null, "parameters", null, 0, -1, MHandledItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(menuElementEClass, MMenuElement.class, "MenuElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMenuElement_Mnemonics(), ecorePackage.getEString(), "mnemonics", null, 0, 1, MMenuElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEOperation(getMenuElement__GetLocalizedMnemonics(), ecorePackage.getEString(), "getLocalizedMnemonics", 0, 1, IS_UNIQUE, IS_ORDERED); //$NON-NLS-1$

		initEClass(menuItemEClass, MMenuItem.class, "MenuItem", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(menuSeparatorEClass, MMenuSeparator.class, "MenuSeparator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(menuEClass, MMenu.class, "Menu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMenu_Enabled(), ecorePackage.getEBoolean(), "enabled", "true", 0, 1, MMenu.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$

		initEClass(menuContributionEClass, MMenuContribution.class, "MenuContribution", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMenuContribution_PositionInParent(), ecorePackage.getEString(), "positionInParent", "", 0, 1, MMenuContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getMenuContribution_ParentId(), ecorePackage.getEString(), "parentId", null, 1, 1, MMenuContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(popupMenuEClass, MPopupMenu.class, "PopupMenu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(directMenuItemEClass, MDirectMenuItem.class, "DirectMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(handledMenuItemEClass, MHandledMenuItem.class, "HandledMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(toolItemEClass, MToolItem.class, "ToolItem", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getToolItem_Menu(), this.getMenu(), null, "menu", null, 0, 1, MToolItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(toolBarEClass, MToolBar.class, "ToolBar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(toolBarElementEClass, MToolBarElement.class, "ToolBarElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(toolControlEClass, MToolControl.class, "ToolControl", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(handledToolItemEClass, MHandledToolItem.class, "HandledToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(directToolItemEClass, MDirectToolItem.class, "DirectToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(toolBarSeparatorEClass, MToolBarSeparator.class, "ToolBarSeparator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(menuContributionsEClass, MMenuContributions.class, "MenuContributions", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getMenuContributions_MenuContributions(), this.getMenuContribution(), null, "menuContributions", null, 0, -1, MMenuContributions.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(renderedMenuEClass, MRenderedMenu.class, "RenderedMenu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getRenderedMenu_ContributionManager(), ecorePackage.getEJavaObject(), "contributionManager", null, 0, 1, MRenderedMenu.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(renderedToolBarEClass, MRenderedToolBar.class, "RenderedToolBar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getRenderedToolBar_ContributionManager(), ecorePackage.getEJavaObject(), "contributionManager", null, 0, 1, MRenderedToolBar.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(toolBarContributionEClass, MToolBarContribution.class, "ToolBarContribution", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getToolBarContribution_ParentId(), ecorePackage.getEString(), "parentId", null, 0, 1, MToolBarContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getToolBarContribution_PositionInParent(), ecorePackage.getEString(), "positionInParent", null, 0, 1, MToolBarContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(toolBarContributionsEClass, MToolBarContributions.class, "ToolBarContributions", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getToolBarContributions_ToolBarContributions(), this.getToolBarContribution(), null, "toolBarContributions", null, 0, -1, MToolBarContributions.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(trimContributionEClass, MTrimContribution.class, "TrimContribution", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getTrimContribution_ParentId(), ecorePackage.getEString(), "parentId", null, 0, 1, MTrimContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getTrimContribution_PositionInParent(), ecorePackage.getEString(), "positionInParent", null, 0, 1, MTrimContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(trimContributionsEClass, MTrimContributions.class, "TrimContributions", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getTrimContributions_TrimContributions(), this.getTrimContribution(), null, "trimContributions", null, 0, -1, MTrimContributions.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(renderedMenuItemEClass, MRenderedMenuItem.class, "RenderedMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getRenderedMenuItem_ContributionItem(), ecorePackage.getEJavaObject(), "contributionItem", null, 0, 1, MRenderedMenuItem.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(opaqueToolItemEClass, MOpaqueToolItem.class, "OpaqueToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getOpaqueToolItem_OpaqueItem(), ecorePackage.getEJavaObject(), "opaqueItem", null, 0, 1, MOpaqueToolItem.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(opaqueMenuItemEClass, MOpaqueMenuItem.class, "OpaqueMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getOpaqueMenuItem_OpaqueItem(), ecorePackage.getEJavaObject(), "opaqueItem", null, 0, 1, MOpaqueMenuItem.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(opaqueMenuSeparatorEClass, MOpaqueMenuSeparator.class, "OpaqueMenuSeparator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getOpaqueMenuSeparator_OpaqueItem(), ecorePackage.getEJavaObject(), "opaqueItem", null, 0, 1, MOpaqueMenuSeparator.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(opaqueMenuEClass, MOpaqueMenu.class, "OpaqueMenu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		// Initialize enums and add enum literals
		initEEnum(itemTypeEEnum, ItemType.class, "ItemType"); //$NON-NLS-1$
		addEEnumLiteral(itemTypeEEnum, ItemType.PUSH);
		addEEnumLiteral(itemTypeEEnum, ItemType.CHECK);
		addEEnumLiteral(itemTypeEEnum, ItemType.RADIO);
	}

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
	public interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl <em>Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getItem()
		 * @generated
		 */
		public static final EClass ITEM = eINSTANCE.getItem();

		/**
		 * The meta object literal for the '<em><b>Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute ITEM__ENABLED = eINSTANCE.getItem_Enabled();

		/**
		 * The meta object literal for the '<em><b>Selected</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute ITEM__SELECTED = eINSTANCE.getItem_Selected();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute ITEM__TYPE = eINSTANCE.getItem_Type();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledItemImpl <em>Handled Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.HandledItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getHandledItem()
		 * @generated
		 */
		public static final EClass HANDLED_ITEM = eINSTANCE.getHandledItem();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference HANDLED_ITEM__COMMAND = eINSTANCE.getHandledItem_Command();

		/**
		 * The meta object literal for the '<em><b>Wb Command</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute HANDLED_ITEM__WB_COMMAND = eINSTANCE.getHandledItem_WbCommand();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference HANDLED_ITEM__PARAMETERS = eINSTANCE.getHandledItem_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl <em>Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuElement()
		 * @generated
		 */
		public static final EClass MENU_ELEMENT = eINSTANCE.getMenuElement();

		/**
		 * The meta object literal for the '<em><b>Mnemonics</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute MENU_ELEMENT__MNEMONICS = eINSTANCE.getMenuElement_Mnemonics();

		/**
		 * The meta object literal for the '<em><b>Get Localized Mnemonics</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EOperation MENU_ELEMENT___GET_LOCALIZED_MNEMONICS = eINSTANCE.getMenuElement__GetLocalizedMnemonics();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuItemImpl <em>Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuItem()
		 * @generated
		 */
		public static final EClass MENU_ITEM = eINSTANCE.getMenuItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuSeparatorImpl <em>Separator</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuSeparatorImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuSeparator()
		 * @generated
		 */
		public static final EClass MENU_SEPARATOR = eINSTANCE.getMenuSeparator();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuImpl <em>Menu</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenu()
		 * @generated
		 */
		public static final EClass MENU = eINSTANCE.getMenu();

		/**
		 * The meta object literal for the '<em><b>Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute MENU__ENABLED = eINSTANCE.getMenu_Enabled();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuContributionImpl <em>Contribution</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuContributionImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuContribution()
		 * @generated
		 */
		public static final EClass MENU_CONTRIBUTION = eINSTANCE.getMenuContribution();

		/**
		 * The meta object literal for the '<em><b>Position In Parent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute MENU_CONTRIBUTION__POSITION_IN_PARENT = eINSTANCE.getMenuContribution_PositionInParent();

		/**
		 * The meta object literal for the '<em><b>Parent Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute MENU_CONTRIBUTION__PARENT_ID = eINSTANCE.getMenuContribution_ParentId();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.PopupMenuImpl <em>Popup Menu</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.PopupMenuImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getPopupMenu()
		 * @generated
		 */
		public static final EClass POPUP_MENU = eINSTANCE.getPopupMenu();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.DirectMenuItemImpl <em>Direct Menu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.DirectMenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getDirectMenuItem()
		 * @generated
		 */
		public static final EClass DIRECT_MENU_ITEM = eINSTANCE.getDirectMenuItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledMenuItemImpl <em>Handled Menu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.HandledMenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getHandledMenuItem()
		 * @generated
		 */
		public static final EClass HANDLED_MENU_ITEM = eINSTANCE.getHandledMenuItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolItemImpl <em>Tool Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolItem()
		 * @generated
		 */
		public static final EClass TOOL_ITEM = eINSTANCE.getToolItem();

		/**
		 * The meta object literal for the '<em><b>Menu</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference TOOL_ITEM__MENU = eINSTANCE.getToolItem_Menu();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarImpl <em>Tool Bar</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBar()
		 * @generated
		 */
		public static final EClass TOOL_BAR = eINSTANCE.getToolBar();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarElementImpl <em>Tool Bar Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarElementImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBarElement()
		 * @generated
		 */
		public static final EClass TOOL_BAR_ELEMENT = eINSTANCE.getToolBarElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolControlImpl <em>Tool Control</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolControlImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolControl()
		 * @generated
		 */
		public static final EClass TOOL_CONTROL = eINSTANCE.getToolControl();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl <em>Handled Tool Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getHandledToolItem()
		 * @generated
		 */
		public static final EClass HANDLED_TOOL_ITEM = eINSTANCE.getHandledToolItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.DirectToolItemImpl <em>Direct Tool Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.DirectToolItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getDirectToolItem()
		 * @generated
		 */
		public static final EClass DIRECT_TOOL_ITEM = eINSTANCE.getDirectToolItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarSeparatorImpl <em>Tool Bar Separator</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarSeparatorImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBarSeparator()
		 * @generated
		 */
		public static final EClass TOOL_BAR_SEPARATOR = eINSTANCE.getToolBarSeparator();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions <em>Contributions</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getMenuContributions()
		 * @generated
		 */
		public static final EClass MENU_CONTRIBUTIONS = eINSTANCE.getMenuContributions();

		/**
		 * The meta object literal for the '<em><b>Menu Contributions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS = eINSTANCE.getMenuContributions_MenuContributions();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuImpl <em>Rendered Menu</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getRenderedMenu()
		 * @generated
		 */
		public static final EClass RENDERED_MENU = eINSTANCE.getRenderedMenu();

		/**
		 * The meta object literal for the '<em><b>Contribution Manager</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute RENDERED_MENU__CONTRIBUTION_MANAGER = eINSTANCE.getRenderedMenu_ContributionManager();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedToolBarImpl <em>Rendered Tool Bar</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedToolBarImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getRenderedToolBar()
		 * @generated
		 */
		public static final EClass RENDERED_TOOL_BAR = eINSTANCE.getRenderedToolBar();

		/**
		 * The meta object literal for the '<em><b>Contribution Manager</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute RENDERED_TOOL_BAR__CONTRIBUTION_MANAGER = eINSTANCE.getRenderedToolBar_ContributionManager();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarContributionImpl <em>Tool Bar Contribution</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarContributionImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBarContribution()
		 * @generated
		 */
		public static final EClass TOOL_BAR_CONTRIBUTION = eINSTANCE.getToolBarContribution();

		/**
		 * The meta object literal for the '<em><b>Parent Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute TOOL_BAR_CONTRIBUTION__PARENT_ID = eINSTANCE.getToolBarContribution_ParentId();

		/**
		 * The meta object literal for the '<em><b>Position In Parent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute TOOL_BAR_CONTRIBUTION__POSITION_IN_PARENT = eINSTANCE.getToolBarContribution_PositionInParent();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions <em>Tool Bar Contributions</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getToolBarContributions()
		 * @generated
		 */
		public static final EClass TOOL_BAR_CONTRIBUTIONS = eINSTANCE.getToolBarContributions();

		/**
		 * The meta object literal for the '<em><b>Tool Bar Contributions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS = eINSTANCE.getToolBarContributions_ToolBarContributions();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.TrimContributionImpl <em>Trim Contribution</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.TrimContributionImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getTrimContribution()
		 * @generated
		 */
		public static final EClass TRIM_CONTRIBUTION = eINSTANCE.getTrimContribution();

		/**
		 * The meta object literal for the '<em><b>Parent Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute TRIM_CONTRIBUTION__PARENT_ID = eINSTANCE.getTrimContribution_ParentId();

		/**
		 * The meta object literal for the '<em><b>Position In Parent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute TRIM_CONTRIBUTION__POSITION_IN_PARENT = eINSTANCE.getTrimContribution_PositionInParent();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions <em>Trim Contributions</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getTrimContributions()
		 * @generated
		 */
		public static final EClass TRIM_CONTRIBUTIONS = eINSTANCE.getTrimContributions();

		/**
		 * The meta object literal for the '<em><b>Trim Contributions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS = eINSTANCE.getTrimContributions_TrimContributions();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuItemImpl <em>Rendered Menu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getRenderedMenuItem()
		 * @generated
		 */
		public static final EClass RENDERED_MENU_ITEM = eINSTANCE.getRenderedMenuItem();

		/**
		 * The meta object literal for the '<em><b>Contribution Item</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute RENDERED_MENU_ITEM__CONTRIBUTION_ITEM = eINSTANCE.getRenderedMenuItem_ContributionItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueToolItemImpl <em>Opaque Tool Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueToolItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getOpaqueToolItem()
		 * @generated
		 */
		public static final EClass OPAQUE_TOOL_ITEM = eINSTANCE.getOpaqueToolItem();

		/**
		 * The meta object literal for the '<em><b>Opaque Item</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute OPAQUE_TOOL_ITEM__OPAQUE_ITEM = eINSTANCE.getOpaqueToolItem_OpaqueItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuItemImpl <em>Opaque Menu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getOpaqueMenuItem()
		 * @generated
		 */
		public static final EClass OPAQUE_MENU_ITEM = eINSTANCE.getOpaqueMenuItem();

		/**
		 * The meta object literal for the '<em><b>Opaque Item</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute OPAQUE_MENU_ITEM__OPAQUE_ITEM = eINSTANCE.getOpaqueMenuItem_OpaqueItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuSeparatorImpl <em>Opaque Menu Separator</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuSeparatorImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getOpaqueMenuSeparator()
		 * @generated
		 */
		public static final EClass OPAQUE_MENU_SEPARATOR = eINSTANCE.getOpaqueMenuSeparator();

		/**
		 * The meta object literal for the '<em><b>Opaque Item</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute OPAQUE_MENU_SEPARATOR__OPAQUE_ITEM = eINSTANCE.getOpaqueMenuSeparator_OpaqueItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuImpl <em>Opaque Menu</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuImpl
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getOpaqueMenu()
		 * @generated
		 */
		public static final EClass OPAQUE_MENU = eINSTANCE.getOpaqueMenu();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.menu.ItemType <em>Item Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.menu.ItemType
		 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl#getItemType()
		 * @generated
		 */
		public static final EEnum ITEM_TYPE = eINSTANCE.getItemType();

	}

} //MenuPackageImpl
