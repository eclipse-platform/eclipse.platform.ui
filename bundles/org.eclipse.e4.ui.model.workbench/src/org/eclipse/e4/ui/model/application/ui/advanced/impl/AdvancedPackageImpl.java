/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.advanced.impl;

import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
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
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory
 * @model kind="package"
 * @generated
 */
public class AdvancedPackageImpl extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "advanced"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "http://www.eclipse.org/ui/2010/UIModel/application/ui/advanced"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "advanced"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final AdvancedPackageImpl eINSTANCE = org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl
			.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PlaceholderImpl <em>Placeholder</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.PlaceholderImpl
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#getPlaceholder()
	 * @since 1.0
	 * @generated
	 */
	public static final int PLACEHOLDER = 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__ELEMENT_ID = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__PERSISTED_STATE = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__TAGS = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__CONTRIBUTOR_URI = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__TRANSIENT_DATA = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__WIDGET = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__RENDERER = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__TO_BE_RENDERED = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__ON_TOP = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__VISIBLE = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__PARENT = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__CONTAINER_DATA = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__CUR_SHARED_REF = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__VISIBLE_WHEN = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__ACCESSIBILITY_PHRASE = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__LOCALIZED_ACCESSIBILITY_PHRASE = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__REF = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Closeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER__CLOSEABLE = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Placeholder</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER_FEATURE_COUNT = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER___UPDATE_LOCALIZATION = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Placeholder</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PLACEHOLDER_OPERATION_COUNT = BasicPackageImpl.PART_SASH_CONTAINER_ELEMENT_OPERATION_COUNT
			+ 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl <em>Perspective</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#getPerspective()
	 * @since 1.0
	 * @generated
	 */
	public static final int PERSPECTIVE = 1;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__ELEMENT_ID = UiPackageImpl.ELEMENT_CONTAINER__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__PERSISTED_STATE = UiPackageImpl.ELEMENT_CONTAINER__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__TAGS = UiPackageImpl.ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__CONTRIBUTOR_URI = UiPackageImpl.ELEMENT_CONTAINER__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__TRANSIENT_DATA = UiPackageImpl.ELEMENT_CONTAINER__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__WIDGET = UiPackageImpl.ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__RENDERER = UiPackageImpl.ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__TO_BE_RENDERED = UiPackageImpl.ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__ON_TOP = UiPackageImpl.ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__VISIBLE = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__PARENT = UiPackageImpl.ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__CONTAINER_DATA = UiPackageImpl.ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__CUR_SHARED_REF = UiPackageImpl.ELEMENT_CONTAINER__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__VISIBLE_WHEN = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__CHILDREN = UiPackageImpl.ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__SELECTED_ELEMENT = UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__LABEL = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__ICON_URI = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__TOOLTIP = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Localized Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__LOCALIZED_LABEL = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Localized Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__LOCALIZED_TOOLTIP = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__CONTEXT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__VARIABLES = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__PROPERTIES = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__HANDLERS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__BINDING_CONTEXTS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Windows</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__WINDOWS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Trim Bars</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.3
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE__TRIM_BARS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 11;

	/**
	 * The number of structural features of the '<em>Perspective</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_FEATURE_COUNT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 12;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE___UPDATE_LOCALIZATION = UiPackageImpl.ELEMENT_CONTAINER_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Perspective</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_OPERATION_COUNT = UiPackageImpl.ELEMENT_CONTAINER_OPERATION_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveStackImpl <em>Perspective Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveStackImpl
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#getPerspectiveStack()
	 * @since 1.0
	 * @generated
	 */
	public static final int PERSPECTIVE_STACK = 2;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__ELEMENT_ID = UiPackageImpl.GENERIC_STACK__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__PERSISTED_STATE = UiPackageImpl.GENERIC_STACK__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__TAGS = UiPackageImpl.GENERIC_STACK__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__CONTRIBUTOR_URI = UiPackageImpl.GENERIC_STACK__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__TRANSIENT_DATA = UiPackageImpl.GENERIC_STACK__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__WIDGET = UiPackageImpl.GENERIC_STACK__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__RENDERER = UiPackageImpl.GENERIC_STACK__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__TO_BE_RENDERED = UiPackageImpl.GENERIC_STACK__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__ON_TOP = UiPackageImpl.GENERIC_STACK__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__VISIBLE = UiPackageImpl.GENERIC_STACK__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__PARENT = UiPackageImpl.GENERIC_STACK__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__CONTAINER_DATA = UiPackageImpl.GENERIC_STACK__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__CUR_SHARED_REF = UiPackageImpl.GENERIC_STACK__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__VISIBLE_WHEN = UiPackageImpl.GENERIC_STACK__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__ACCESSIBILITY_PHRASE = UiPackageImpl.GENERIC_STACK__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.GENERIC_STACK__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__CHILDREN = UiPackageImpl.GENERIC_STACK__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK__SELECTED_ELEMENT = UiPackageImpl.GENERIC_STACK__SELECTED_ELEMENT;

	/**
	 * The number of structural features of the '<em>Perspective Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK_FEATURE_COUNT = UiPackageImpl.GENERIC_STACK_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK___UPDATE_LOCALIZATION = UiPackageImpl.GENERIC_STACK___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Perspective Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PERSPECTIVE_STACK_OPERATION_COUNT = UiPackageImpl.GENERIC_STACK_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.AreaImpl <em>Area</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AreaImpl
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#getArea()
	 * @since 1.0
	 * @generated
	 */
	public static final int AREA = 3;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__ELEMENT_ID = BasicPackageImpl.PART_SASH_CONTAINER__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__PERSISTED_STATE = BasicPackageImpl.PART_SASH_CONTAINER__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__TAGS = BasicPackageImpl.PART_SASH_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__CONTRIBUTOR_URI = BasicPackageImpl.PART_SASH_CONTAINER__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__TRANSIENT_DATA = BasicPackageImpl.PART_SASH_CONTAINER__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__WIDGET = BasicPackageImpl.PART_SASH_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__RENDERER = BasicPackageImpl.PART_SASH_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__TO_BE_RENDERED = BasicPackageImpl.PART_SASH_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__ON_TOP = BasicPackageImpl.PART_SASH_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__VISIBLE = BasicPackageImpl.PART_SASH_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__PARENT = BasicPackageImpl.PART_SASH_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__CONTAINER_DATA = BasicPackageImpl.PART_SASH_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__CUR_SHARED_REF = BasicPackageImpl.PART_SASH_CONTAINER__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__VISIBLE_WHEN = BasicPackageImpl.PART_SASH_CONTAINER__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__ACCESSIBILITY_PHRASE = BasicPackageImpl.PART_SASH_CONTAINER__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__LOCALIZED_ACCESSIBILITY_PHRASE = BasicPackageImpl.PART_SASH_CONTAINER__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__CHILDREN = BasicPackageImpl.PART_SASH_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__SELECTED_ELEMENT = BasicPackageImpl.PART_SASH_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__HORIZONTAL = BasicPackageImpl.PART_SASH_CONTAINER__HORIZONTAL;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__LABEL = BasicPackageImpl.PART_SASH_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__ICON_URI = BasicPackageImpl.PART_SASH_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__TOOLTIP = BasicPackageImpl.PART_SASH_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Localized Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__LOCALIZED_LABEL = BasicPackageImpl.PART_SASH_CONTAINER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Localized Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA__LOCALIZED_TOOLTIP = BasicPackageImpl.PART_SASH_CONTAINER_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Area</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA_FEATURE_COUNT = BasicPackageImpl.PART_SASH_CONTAINER_FEATURE_COUNT + 5;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int AREA___UPDATE_LOCALIZATION = BasicPackageImpl.PART_SASH_CONTAINER_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Area</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int AREA_OPERATION_COUNT = BasicPackageImpl.PART_SASH_CONTAINER_OPERATION_COUNT + 1;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass placeholderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass perspectiveEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass perspectiveStackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass areaEClass = null;

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
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private AdvancedPackageImpl() {
		super(eNS_URI, ((EFactory) MAdvancedFactory.INSTANCE));
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
	 * <p>This method is used to initialize {@link AdvancedPackageImpl#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static AdvancedPackageImpl init() {
		if (isInited) {
			return (AdvancedPackageImpl) EPackage.Registry.INSTANCE.getEPackage(AdvancedPackageImpl.eNS_URI);
		}

		// Obtain or create and register package
		Object registeredAdvancedPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		AdvancedPackageImpl theAdvancedPackage = registeredAdvancedPackage instanceof AdvancedPackageImpl
				? (AdvancedPackageImpl) registeredAdvancedPackage
				: new AdvancedPackageImpl();

		isInited = true;

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI);
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl) (registeredPackage instanceof ApplicationPackageImpl
				? registeredPackage
				: ApplicationPackageImpl.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI);
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl) (registeredPackage instanceof CommandsPackageImpl
				? registeredPackage
				: CommandsPackageImpl.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI);
		UiPackageImpl theUiPackage = (UiPackageImpl) (registeredPackage instanceof UiPackageImpl ? registeredPackage
				: UiPackageImpl.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(MenuPackageImpl.eNS_URI);
		MenuPackageImpl theMenuPackage = (MenuPackageImpl) (registeredPackage instanceof MenuPackageImpl
				? registeredPackage
				: MenuPackageImpl.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI);
		BasicPackageImpl theBasicPackage = (BasicPackageImpl) (registeredPackage instanceof BasicPackageImpl
				? registeredPackage
				: BasicPackageImpl.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE
				.getEPackage(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI);
		org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl theBasicPackage_1 = (org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl) (registeredPackage instanceof org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl
				? registeredPackage
				: org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

		// Create package meta-data objects
		theAdvancedPackage.createPackageContents();
		theApplicationPackage.createPackageContents();
		theCommandsPackage.createPackageContents();
		theUiPackage.createPackageContents();
		theMenuPackage.createPackageContents();
		theBasicPackage.createPackageContents();
		theBasicPackage_1.createPackageContents();

		// Initialize created meta-data
		theAdvancedPackage.initializePackageContents();
		theApplicationPackage.initializePackageContents();
		theCommandsPackage.initializePackageContents();
		theUiPackage.initializePackageContents();
		theMenuPackage.initializePackageContents();
		theBasicPackage.initializePackageContents();
		theBasicPackage_1.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theAdvancedPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(AdvancedPackageImpl.eNS_URI, theAdvancedPackage);
		return theAdvancedPackage;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder <em>Placeholder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Placeholder</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder
	 * @since 1.0
	 * @generated
	 */
	public EClass getPlaceholder() {
		return placeholderEClass;
	}

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder#getRef <em>Ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Ref</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder#getRef()
	 * @see #getPlaceholder()
	 * @since 1.0
	 * @generated
	 */
	public EReference getPlaceholder_Ref() {
		return (EReference) placeholderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder#isCloseable <em>Closeable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Closeable</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder#isCloseable()
	 * @see #getPlaceholder()
	 * @since 1.0
	 * @generated
	 */
	public EAttribute getPlaceholder_Closeable() {
		return (EAttribute) placeholderEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspective <em>Perspective</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Perspective</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPerspective
	 * @since 1.0
	 * @generated
	 */
	public EClass getPerspective() {
		return perspectiveEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspective#getWindows <em>Windows</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Windows</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPerspective#getWindows()
	 * @see #getPerspective()
	 * @since 1.0
	 * @generated
	 */
	public EReference getPerspective_Windows() {
		return (EReference) perspectiveEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspective#getTrimBars <em>Trim Bars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Trim Bars</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPerspective#getTrimBars()
	 * @see #getPerspective()
	 * @since 1.3
	 * @generated
	 */
	public EReference getPerspective_TrimBars() {
		return (EReference) perspectiveEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspective#updateLocalization() <em>Update Localization</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Localization</em>' operation.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPerspective#updateLocalization()
	 * @since 1.1
	 * @generated
	 */
	public EOperation getPerspective__UpdateLocalization() {
		return perspectiveEClass.getEOperations().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack <em>Perspective Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Perspective Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack
	 * @since 1.0
	 * @generated
	 */
	public EClass getPerspectiveStack() {
		return perspectiveStackEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.advanced.MArea <em>Area</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Area</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MArea
	 * @since 1.0
	 * @generated
	 */
	public EClass getArea() {
		return areaEClass;
	}

	/**
	 * Returns the meta object for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.MArea#updateLocalization() <em>Update Localization</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Localization</em>' operation.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MArea#updateLocalization()
	 * @since 1.1
	 * @generated
	 */
	public EOperation getArea__UpdateLocalization() {
		return areaEClass.getEOperations().get(0);
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public MAdvancedFactory getAdvancedFactory() {
		return (MAdvancedFactory) getEFactoryInstance();
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
		if (isCreated) {
			return;
		}
		isCreated = true;

		// Create classes and their features
		placeholderEClass = createEClass(PLACEHOLDER);
		createEReference(placeholderEClass, PLACEHOLDER__REF);
		createEAttribute(placeholderEClass, PLACEHOLDER__CLOSEABLE);

		perspectiveEClass = createEClass(PERSPECTIVE);
		createEReference(perspectiveEClass, PERSPECTIVE__WINDOWS);
		createEReference(perspectiveEClass, PERSPECTIVE__TRIM_BARS);
		createEOperation(perspectiveEClass, PERSPECTIVE___UPDATE_LOCALIZATION);

		perspectiveStackEClass = createEClass(PERSPECTIVE_STACK);

		areaEClass = createEClass(AREA);
		createEOperation(areaEClass, AREA___UPDATE_LOCALIZATION);
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
		if (isInitialized) {
			return;
		}
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		BasicPackageImpl theBasicPackage = (BasicPackageImpl) EPackage.Registry.INSTANCE
				.getEPackage(BasicPackageImpl.eNS_URI);
		UiPackageImpl theUiPackage = (UiPackageImpl) EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI);
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl) EPackage.Registry.INSTANCE
				.getEPackage(CommandsPackageImpl.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		placeholderEClass.getESuperTypes().add(theBasicPackage.getPartSashContainerElement());
		placeholderEClass.getESuperTypes().add(theBasicPackage.getStackElement());
		EGenericType g1 = createEGenericType(theUiPackage.getElementContainer());
		EGenericType g2 = createEGenericType(theBasicPackage.getPartSashContainerElement());
		g1.getETypeArguments().add(g2);
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getUILabel());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getContext());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theCommandsPackage.getHandlerContainer());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theCommandsPackage.getBindings());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getGenericStack());
		g2 = createEGenericType(this.getPerspective());
		g1.getETypeArguments().add(g2);
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theBasicPackage.getPartSashContainerElement());
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theBasicPackage.getWindowElement());
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		areaEClass.getESuperTypes().add(theBasicPackage.getPartSashContainer());
		areaEClass.getESuperTypes().add(theUiPackage.getUILabel());

		// Initialize classes, features, and operations; add parameters
		initEClass(placeholderEClass, MPlaceholder.class, "Placeholder", !IS_ABSTRACT, !IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPlaceholder_Ref(), theUiPackage.getUIElement(), null, "ref", null, 1, 1, MPlaceholder.class, //$NON-NLS-1$
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPlaceholder_Closeable(), ecorePackage.getEBoolean(), "closeable", "false", 0, 1, //$NON-NLS-1$//$NON-NLS-2$
				MPlaceholder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);

		initEClass(perspectiveEClass, MPerspective.class, "Perspective", !IS_ABSTRACT, !IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPerspective_Windows(), theBasicPackage.getWindow(), null, "windows", null, 0, -1, //$NON-NLS-1$
				MPerspective.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPerspective_TrimBars(), theBasicPackage.getTrimBar(), null, "trimBars", null, 0, -1, //$NON-NLS-1$
				MPerspective.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEOperation(getPerspective__UpdateLocalization(), null, "updateLocalization", 0, 1, IS_UNIQUE, IS_ORDERED); //$NON-NLS-1$

		initEClass(perspectiveStackEClass, MPerspectiveStack.class, "PerspectiveStack", !IS_ABSTRACT, !IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);

		initEClass(areaEClass, MArea.class, "Area", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEOperation(getArea__UpdateLocalization(), null, "updateLocalization", 0, 1, IS_UNIQUE, IS_ORDERED); //$NON-NLS-1$
	}

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
	public interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PlaceholderImpl <em>Placeholder</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.PlaceholderImpl
		 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#getPlaceholder()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass PLACEHOLDER = eINSTANCE.getPlaceholder();

		/**
		 * The meta object literal for the '<em><b>Ref</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EReference PLACEHOLDER__REF = eINSTANCE.getPlaceholder_Ref();

		/**
		 * The meta object literal for the '<em><b>Closeable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EAttribute PLACEHOLDER__CLOSEABLE = eINSTANCE.getPlaceholder_Closeable();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl <em>Perspective</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl
		 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#getPerspective()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass PERSPECTIVE = eINSTANCE.getPerspective();

		/**
		 * The meta object literal for the '<em><b>Windows</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EReference PERSPECTIVE__WINDOWS = eINSTANCE.getPerspective_Windows();

		/**
		 * The meta object literal for the '<em><b>Trim Bars</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.3
		 * @generated
		 */
		public static final EReference PERSPECTIVE__TRIM_BARS = eINSTANCE.getPerspective_TrimBars();

		/**
		 * The meta object literal for the '<em><b>Update Localization</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.1
		 * @generated
		 */
		public static final EOperation PERSPECTIVE___UPDATE_LOCALIZATION = eINSTANCE
				.getPerspective__UpdateLocalization();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveStackImpl <em>Perspective Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveStackImpl
		 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#getPerspectiveStack()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass PERSPECTIVE_STACK = eINSTANCE.getPerspectiveStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.AreaImpl <em>Area</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AreaImpl
		 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl#getArea()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass AREA = eINSTANCE.getArea();

		/**
		 * The meta object literal for the '<em><b>Update Localization</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.1
		 * @generated
		 */
		public static final EOperation AREA___UPDATE_LOCALIZATION = eINSTANCE.getArea__UpdateLocalization();

	}

} //AdvancedPackageImpl
