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
package org.eclipse.e4.ui.model.application.ui.basic.impl;

import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
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
 * @see org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory
 * @model kind="package"
 * @generated
 */
public class BasicPackageImpl extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "basic"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "http://www.eclipse.org/ui/2010/UIModel/application/ui/basic"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "basic"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final BasicPackageImpl eINSTANCE = org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl
			.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl <em>Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getPart()
	 * @since 1.0
	 * @generated
	 */
	public static final int PART = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.CompositePartImpl <em>Composite Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.CompositePartImpl
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getCompositePart()
	 * @since 1.1
	 * @generated
	 */
	public static final int COMPOSITE_PART = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.PartStackImpl <em>Part Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.PartStackImpl
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getPartStack()
	 * @since 1.0
	 * @generated
	 */
	public static final int PART_STACK = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.PartSashContainerImpl <em>Part Sash Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.PartSashContainerImpl
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getPartSashContainer()
	 * @since 1.0
	 * @generated
	 */
	public static final int PART_SASH_CONTAINER = 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl <em>Window</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getWindow()
	 * @since 1.0
	 * @generated
	 */
	public static final int WINDOW = 4;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.TrimmedWindowImpl <em>Trimmed Window</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.TrimmedWindowImpl
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getTrimmedWindow()
	 * @since 1.0
	 * @generated
	 */
	public static final int TRIMMED_WINDOW = 5;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimElement <em>Trim Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MTrimElement
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getTrimElement()
	 * @since 1.0
	 * @generated
	 */
	public static final int TRIM_ELEMENT = 6;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement <em>Part Sash Container Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getPartSashContainerElement()
	 * @since 1.0
	 * @generated
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT = 7;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__ELEMENT_ID = UiPackageImpl.UI_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__PERSISTED_STATE = UiPackageImpl.UI_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__TAGS = UiPackageImpl.UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__CONTRIBUTOR_URI = UiPackageImpl.UI_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__TRANSIENT_DATA = UiPackageImpl.UI_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__WIDGET = UiPackageImpl.UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__RENDERER = UiPackageImpl.UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__TO_BE_RENDERED = UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__ON_TOP = UiPackageImpl.UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__VISIBLE = UiPackageImpl.UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__PARENT = UiPackageImpl.UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__CONTAINER_DATA = UiPackageImpl.UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__CUR_SHARED_REF = UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__VISIBLE_WHEN = UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of structural features of the '<em>Part Sash Container Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT___UPDATE_LOCALIZATION = UiPackageImpl.UI_ELEMENT___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Part Sash Container Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_ELEMENT_OPERATION_COUNT = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__ELEMENT_ID = PART_SASH_CONTAINER_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__PERSISTED_STATE = PART_SASH_CONTAINER_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__TAGS = PART_SASH_CONTAINER_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__CONTRIBUTOR_URI = PART_SASH_CONTAINER_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__TRANSIENT_DATA = PART_SASH_CONTAINER_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__WIDGET = PART_SASH_CONTAINER_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__RENDERER = PART_SASH_CONTAINER_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__TO_BE_RENDERED = PART_SASH_CONTAINER_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__ON_TOP = PART_SASH_CONTAINER_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__VISIBLE = PART_SASH_CONTAINER_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__PARENT = PART_SASH_CONTAINER_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__CONTAINER_DATA = PART_SASH_CONTAINER_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__CUR_SHARED_REF = PART_SASH_CONTAINER_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__VISIBLE_WHEN = PART_SASH_CONTAINER_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__ACCESSIBILITY_PHRASE = PART_SASH_CONTAINER_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__LOCALIZED_ACCESSIBILITY_PHRASE = PART_SASH_CONTAINER_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__CONTRIBUTION_URI = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__OBJECT = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__CONTEXT = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__VARIABLES = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__PROPERTIES = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__LABEL = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__ICON_URI = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__TOOLTIP = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Localized Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__LOCALIZED_LABEL = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Localized Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__LOCALIZED_TOOLTIP = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__HANDLERS = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__DIRTY = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__BINDING_CONTEXTS = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__MENUS = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__TOOLBAR = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Closeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__CLOSEABLE = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__DESCRIPTION = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART__LOCALIZED_DESCRIPTION = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Trim Bars</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.3
	 * @generated
	 * @ordered
	 */
	public static final int PART__TRIM_BARS = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 18;

	/**
	 * The number of structural features of the '<em>Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_FEATURE_COUNT = PART_SASH_CONTAINER_ELEMENT_FEATURE_COUNT + 19;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int PART___UPDATE_LOCALIZATION = PART_SASH_CONTAINER_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_OPERATION_COUNT = PART_SASH_CONTAINER_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__ELEMENT_ID = PART__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__PERSISTED_STATE = PART__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__TAGS = PART__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__CONTRIBUTOR_URI = PART__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__TRANSIENT_DATA = PART__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__WIDGET = PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__RENDERER = PART__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__TO_BE_RENDERED = PART__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__ON_TOP = PART__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__VISIBLE = PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__PARENT = PART__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__CONTAINER_DATA = PART__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__CUR_SHARED_REF = PART__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__VISIBLE_WHEN = PART__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__ACCESSIBILITY_PHRASE = PART__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__LOCALIZED_ACCESSIBILITY_PHRASE = PART__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__CONTRIBUTION_URI = PART__CONTRIBUTION_URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__OBJECT = PART__OBJECT;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__CONTEXT = PART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__VARIABLES = PART__VARIABLES;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__PROPERTIES = PART__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__LABEL = PART__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__ICON_URI = PART__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__TOOLTIP = PART__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Localized Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__LOCALIZED_LABEL = PART__LOCALIZED_LABEL;

	/**
	 * The feature id for the '<em><b>Localized Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__LOCALIZED_TOOLTIP = PART__LOCALIZED_TOOLTIP;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__HANDLERS = PART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__DIRTY = PART__DIRTY;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__BINDING_CONTEXTS = PART__BINDING_CONTEXTS;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__MENUS = PART__MENUS;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__TOOLBAR = PART__TOOLBAR;

	/**
	 * The feature id for the '<em><b>Closeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__CLOSEABLE = PART__CLOSEABLE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__DESCRIPTION = PART__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__LOCALIZED_DESCRIPTION = PART__LOCALIZED_DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Trim Bars</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.3
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__TRIM_BARS = PART__TRIM_BARS;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__CHILDREN = PART_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__SELECTED_ELEMENT = PART_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART__HORIZONTAL = PART_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Composite Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART_FEATURE_COUNT = PART_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART___UPDATE_LOCALIZATION = PART___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Composite Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int COMPOSITE_PART_OPERATION_COUNT = PART_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__ELEMENT_ID = UiPackageImpl.GENERIC_STACK__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__PERSISTED_STATE = UiPackageImpl.GENERIC_STACK__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__TAGS = UiPackageImpl.GENERIC_STACK__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__CONTRIBUTOR_URI = UiPackageImpl.GENERIC_STACK__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__TRANSIENT_DATA = UiPackageImpl.GENERIC_STACK__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__WIDGET = UiPackageImpl.GENERIC_STACK__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__RENDERER = UiPackageImpl.GENERIC_STACK__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__TO_BE_RENDERED = UiPackageImpl.GENERIC_STACK__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__ON_TOP = UiPackageImpl.GENERIC_STACK__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__VISIBLE = UiPackageImpl.GENERIC_STACK__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__PARENT = UiPackageImpl.GENERIC_STACK__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__CONTAINER_DATA = UiPackageImpl.GENERIC_STACK__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__CUR_SHARED_REF = UiPackageImpl.GENERIC_STACK__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__VISIBLE_WHEN = UiPackageImpl.GENERIC_STACK__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__ACCESSIBILITY_PHRASE = UiPackageImpl.GENERIC_STACK__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.GENERIC_STACK__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__CHILDREN = UiPackageImpl.GENERIC_STACK__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK__SELECTED_ELEMENT = UiPackageImpl.GENERIC_STACK__SELECTED_ELEMENT;

	/**
	 * The number of structural features of the '<em>Part Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK_FEATURE_COUNT = UiPackageImpl.GENERIC_STACK_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK___UPDATE_LOCALIZATION = UiPackageImpl.GENERIC_STACK___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Part Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_STACK_OPERATION_COUNT = UiPackageImpl.GENERIC_STACK_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__ELEMENT_ID = UiPackageImpl.GENERIC_TILE__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__PERSISTED_STATE = UiPackageImpl.GENERIC_TILE__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__TAGS = UiPackageImpl.GENERIC_TILE__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__CONTRIBUTOR_URI = UiPackageImpl.GENERIC_TILE__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__TRANSIENT_DATA = UiPackageImpl.GENERIC_TILE__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__WIDGET = UiPackageImpl.GENERIC_TILE__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__RENDERER = UiPackageImpl.GENERIC_TILE__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__TO_BE_RENDERED = UiPackageImpl.GENERIC_TILE__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__ON_TOP = UiPackageImpl.GENERIC_TILE__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__VISIBLE = UiPackageImpl.GENERIC_TILE__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__PARENT = UiPackageImpl.GENERIC_TILE__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__CONTAINER_DATA = UiPackageImpl.GENERIC_TILE__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__CUR_SHARED_REF = UiPackageImpl.GENERIC_TILE__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__VISIBLE_WHEN = UiPackageImpl.GENERIC_TILE__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__ACCESSIBILITY_PHRASE = UiPackageImpl.GENERIC_TILE__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.GENERIC_TILE__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__CHILDREN = UiPackageImpl.GENERIC_TILE__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__SELECTED_ELEMENT = UiPackageImpl.GENERIC_TILE__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER__HORIZONTAL = UiPackageImpl.GENERIC_TILE__HORIZONTAL;

	/**
	 * The number of structural features of the '<em>Part Sash Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_FEATURE_COUNT = UiPackageImpl.GENERIC_TILE_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER___UPDATE_LOCALIZATION = UiPackageImpl.GENERIC_TILE___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Part Sash Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int PART_SASH_CONTAINER_OPERATION_COUNT = UiPackageImpl.GENERIC_TILE_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__ELEMENT_ID = UiPackageImpl.ELEMENT_CONTAINER__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__PERSISTED_STATE = UiPackageImpl.ELEMENT_CONTAINER__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__TAGS = UiPackageImpl.ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__CONTRIBUTOR_URI = UiPackageImpl.ELEMENT_CONTAINER__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__TRANSIENT_DATA = UiPackageImpl.ELEMENT_CONTAINER__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__WIDGET = UiPackageImpl.ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__RENDERER = UiPackageImpl.ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__TO_BE_RENDERED = UiPackageImpl.ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__ON_TOP = UiPackageImpl.ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__VISIBLE = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__PARENT = UiPackageImpl.ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__CONTAINER_DATA = UiPackageImpl.ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__CUR_SHARED_REF = UiPackageImpl.ELEMENT_CONTAINER__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__VISIBLE_WHEN = UiPackageImpl.ELEMENT_CONTAINER__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.ELEMENT_CONTAINER__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__CHILDREN = UiPackageImpl.ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__SELECTED_ELEMENT = UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__LABEL = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__ICON_URI = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__TOOLTIP = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Localized Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__LOCALIZED_LABEL = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Localized Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__LOCALIZED_TOOLTIP = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__CONTEXT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__VARIABLES = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__PROPERTIES = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__HANDLERS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__BINDING_CONTEXTS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Snippets</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__SNIPPETS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Main Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__MAIN_MENU = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__X = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__Y = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__WIDTH = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__HEIGHT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Windows</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__WINDOWS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Shared Elements</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW__SHARED_ELEMENTS = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 17;

	/**
	 * The number of structural features of the '<em>Window</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_FEATURE_COUNT = UiPackageImpl.ELEMENT_CONTAINER_FEATURE_COUNT + 18;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW___UPDATE_LOCALIZATION = UiPackageImpl.ELEMENT_CONTAINER_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Window</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_OPERATION_COUNT = UiPackageImpl.ELEMENT_CONTAINER_OPERATION_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__ELEMENT_ID = WINDOW__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__PERSISTED_STATE = WINDOW__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__TAGS = WINDOW__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__CONTRIBUTOR_URI = WINDOW__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__TRANSIENT_DATA = WINDOW__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__WIDGET = WINDOW__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__RENDERER = WINDOW__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__TO_BE_RENDERED = WINDOW__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__ON_TOP = WINDOW__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__VISIBLE = WINDOW__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__PARENT = WINDOW__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__CONTAINER_DATA = WINDOW__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__CUR_SHARED_REF = WINDOW__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__VISIBLE_WHEN = WINDOW__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__ACCESSIBILITY_PHRASE = WINDOW__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__LOCALIZED_ACCESSIBILITY_PHRASE = WINDOW__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__CHILDREN = WINDOW__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__SELECTED_ELEMENT = WINDOW__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__LABEL = WINDOW__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__ICON_URI = WINDOW__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__TOOLTIP = WINDOW__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Localized Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__LOCALIZED_LABEL = WINDOW__LOCALIZED_LABEL;

	/**
	 * The feature id for the '<em><b>Localized Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__LOCALIZED_TOOLTIP = WINDOW__LOCALIZED_TOOLTIP;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__CONTEXT = WINDOW__CONTEXT;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__VARIABLES = WINDOW__VARIABLES;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__PROPERTIES = WINDOW__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__HANDLERS = WINDOW__HANDLERS;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__BINDING_CONTEXTS = WINDOW__BINDING_CONTEXTS;

	/**
	 * The feature id for the '<em><b>Snippets</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__SNIPPETS = WINDOW__SNIPPETS;

	/**
	 * The feature id for the '<em><b>Main Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__MAIN_MENU = WINDOW__MAIN_MENU;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__X = WINDOW__X;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__Y = WINDOW__Y;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__WIDTH = WINDOW__WIDTH;

	/**
	 * The feature id for the '<em><b>Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__HEIGHT = WINDOW__HEIGHT;

	/**
	 * The feature id for the '<em><b>Windows</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__WINDOWS = WINDOW__WINDOWS;

	/**
	 * The feature id for the '<em><b>Shared Elements</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__SHARED_ELEMENTS = WINDOW__SHARED_ELEMENTS;

	/**
	 * The feature id for the '<em><b>Trim Bars</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW__TRIM_BARS = WINDOW_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Trimmed Window</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW_FEATURE_COUNT = WINDOW_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW___UPDATE_LOCALIZATION = WINDOW___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Trimmed Window</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIMMED_WINDOW_OPERATION_COUNT = WINDOW_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__ELEMENT_ID = UiPackageImpl.UI_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__PERSISTED_STATE = UiPackageImpl.UI_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__TAGS = UiPackageImpl.UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__CONTRIBUTOR_URI = UiPackageImpl.UI_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__TRANSIENT_DATA = UiPackageImpl.UI_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__WIDGET = UiPackageImpl.UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__RENDERER = UiPackageImpl.UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__TO_BE_RENDERED = UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__ON_TOP = UiPackageImpl.UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__VISIBLE = UiPackageImpl.UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__PARENT = UiPackageImpl.UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__CONTAINER_DATA = UiPackageImpl.UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__CUR_SHARED_REF = UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__VISIBLE_WHEN = UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of structural features of the '<em>Trim Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT_FEATURE_COUNT = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT___UPDATE_LOCALIZATION = UiPackageImpl.UI_ELEMENT___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Trim Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_ELEMENT_OPERATION_COUNT = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindowElement <em>Window Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindowElement
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getWindowElement()
	 * @since 1.0
	 * @generated
	 */
	public static final int WINDOW_ELEMENT = 8;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__ELEMENT_ID = UiPackageImpl.UI_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__PERSISTED_STATE = UiPackageImpl.UI_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__TAGS = UiPackageImpl.UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__CONTRIBUTOR_URI = UiPackageImpl.UI_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__TRANSIENT_DATA = UiPackageImpl.UI_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__WIDGET = UiPackageImpl.UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__RENDERER = UiPackageImpl.UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__TO_BE_RENDERED = UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__ON_TOP = UiPackageImpl.UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__VISIBLE = UiPackageImpl.UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__PARENT = UiPackageImpl.UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__CONTAINER_DATA = UiPackageImpl.UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__CUR_SHARED_REF = UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__VISIBLE_WHEN = UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of structural features of the '<em>Window Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT_FEATURE_COUNT = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT___UPDATE_LOCALIZATION = UiPackageImpl.UI_ELEMENT___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Window Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int WINDOW_ELEMENT_OPERATION_COUNT = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.TrimBarImpl <em>Trim Bar</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.TrimBarImpl
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getTrimBar()
	 * @since 1.0
	 * @generated
	 */
	public static final int TRIM_BAR = 9;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__ELEMENT_ID = UiPackageImpl.GENERIC_TRIM_CONTAINER__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__PERSISTED_STATE = UiPackageImpl.GENERIC_TRIM_CONTAINER__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__TAGS = UiPackageImpl.GENERIC_TRIM_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__CONTRIBUTOR_URI = UiPackageImpl.GENERIC_TRIM_CONTAINER__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__TRANSIENT_DATA = UiPackageImpl.GENERIC_TRIM_CONTAINER__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__WIDGET = UiPackageImpl.GENERIC_TRIM_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__RENDERER = UiPackageImpl.GENERIC_TRIM_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__TO_BE_RENDERED = UiPackageImpl.GENERIC_TRIM_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__ON_TOP = UiPackageImpl.GENERIC_TRIM_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__VISIBLE = UiPackageImpl.GENERIC_TRIM_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__PARENT = UiPackageImpl.GENERIC_TRIM_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__CONTAINER_DATA = UiPackageImpl.GENERIC_TRIM_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__CUR_SHARED_REF = UiPackageImpl.GENERIC_TRIM_CONTAINER__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__VISIBLE_WHEN = UiPackageImpl.GENERIC_TRIM_CONTAINER__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__ACCESSIBILITY_PHRASE = UiPackageImpl.GENERIC_TRIM_CONTAINER__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.GENERIC_TRIM_CONTAINER__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__CHILDREN = UiPackageImpl.GENERIC_TRIM_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__SELECTED_ELEMENT = UiPackageImpl.GENERIC_TRIM_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Side</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__SIDE = UiPackageImpl.GENERIC_TRIM_CONTAINER__SIDE;

	/**
	 * The feature id for the '<em><b>Pending Cleanup</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @noreference
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR__PENDING_CLEANUP = UiPackageImpl.GENERIC_TRIM_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Trim Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR_FEATURE_COUNT = UiPackageImpl.GENERIC_TRIM_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR___UPDATE_LOCALIZATION = UiPackageImpl.GENERIC_TRIM_CONTAINER___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Trim Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int TRIM_BAR_OPERATION_COUNT = UiPackageImpl.GENERIC_TRIM_CONTAINER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MStackElement <em>Stack Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MStackElement
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getStackElement()
	 * @since 1.0
	 * @generated
	 */
	public static final int STACK_ELEMENT = 10;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__ELEMENT_ID = UiPackageImpl.UI_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__PERSISTED_STATE = UiPackageImpl.UI_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__TAGS = UiPackageImpl.UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__CONTRIBUTOR_URI = UiPackageImpl.UI_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__TRANSIENT_DATA = UiPackageImpl.UI_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__WIDGET = UiPackageImpl.UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__RENDERER = UiPackageImpl.UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__TO_BE_RENDERED = UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__ON_TOP = UiPackageImpl.UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__VISIBLE = UiPackageImpl.UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__PARENT = UiPackageImpl.UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__CONTAINER_DATA = UiPackageImpl.UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__CUR_SHARED_REF = UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__VISIBLE_WHEN = UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE = UiPackageImpl.UI_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of structural features of the '<em>Stack Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT_FEATURE_COUNT = UiPackageImpl.UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT___UPDATE_LOCALIZATION = UiPackageImpl.UI_ELEMENT___UPDATE_LOCALIZATION;

	/**
	 * The number of operations of the '<em>Stack Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	public static final int STACK_ELEMENT_OPERATION_COUNT = UiPackageImpl.UI_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass partEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 */
	private EClass compositePartEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass partStackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass partSashContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass windowEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass trimmedWindowEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass trimElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass partSashContainerElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass windowElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass trimBarEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	private EClass stackElementEClass = null;

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
	 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private BasicPackageImpl() {
		super(eNS_URI, ((EFactory) MBasicFactory.INSTANCE));
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
	 * <p>This method is used to initialize {@link BasicPackageImpl#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static BasicPackageImpl init() {
		if (isInited) {
			return (BasicPackageImpl) EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI);
		}

		// Obtain or create and register package
		Object registeredBasicPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		BasicPackageImpl theBasicPackage = registeredBasicPackage instanceof BasicPackageImpl
				? (BasicPackageImpl) registeredBasicPackage
				: new BasicPackageImpl();

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
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(AdvancedPackageImpl.eNS_URI);
		AdvancedPackageImpl theAdvancedPackage = (AdvancedPackageImpl) (registeredPackage instanceof AdvancedPackageImpl
				? registeredPackage
				: AdvancedPackageImpl.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE
				.getEPackage(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI);
		org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl theBasicPackage_1 = (org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl) (registeredPackage instanceof org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl
				? registeredPackage
				: org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

		// Create package meta-data objects
		theBasicPackage.createPackageContents();
		theApplicationPackage.createPackageContents();
		theCommandsPackage.createPackageContents();
		theUiPackage.createPackageContents();
		theMenuPackage.createPackageContents();
		theAdvancedPackage.createPackageContents();
		theBasicPackage_1.createPackageContents();

		// Initialize created meta-data
		theBasicPackage.initializePackageContents();
		theApplicationPackage.initializePackageContents();
		theCommandsPackage.initializePackageContents();
		theUiPackage.initializePackageContents();
		theMenuPackage.initializePackageContents();
		theAdvancedPackage.initializePackageContents();
		theBasicPackage_1.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theBasicPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(BasicPackageImpl.eNS_URI, theBasicPackage);
		return theBasicPackage;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart <em>Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPart
	 * @since 1.0
	 * @generated
	 */
	public EClass getPart() {
		return partEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getMenus <em>Menus</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Menus</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPart#getMenus()
	 * @see #getPart()
	 * @since 1.0
	 * @generated
	 */
	public EReference getPart_Menus() {
		return (EReference) partEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getToolbar <em>Toolbar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Toolbar</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPart#getToolbar()
	 * @see #getPart()
	 * @since 1.0
	 * @generated
	 */
	public EReference getPart_Toolbar() {
		return (EReference) partEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#isCloseable <em>Closeable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Closeable</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPart#isCloseable()
	 * @see #getPart()
	 * @since 1.0
	 * @generated
	 */
	public EAttribute getPart_Closeable() {
		return (EAttribute) partEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPart#getDescription()
	 * @see #getPart()
	 * @since 1.0
	 * @generated
	 */
	public EAttribute getPart_Description() {
		return (EAttribute) partEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getLocalizedDescription <em>Localized Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Localized Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPart#getLocalizedDescription()
	 * @see #getPart()
	 * @since 1.0
	 * @generated
	 */
	public EAttribute getPart_LocalizedDescription() {
		return (EAttribute) partEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getTrimBars <em>Trim Bars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Trim Bars</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPart#getTrimBars()
	 * @see #getPart()
	 * @since 1.3
	 * @generated
	 */
	public EReference getPart_TrimBars() {
		return (EReference) partEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * Returns the meta object for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#updateLocalization() <em>Update Localization</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Localization</em>' operation.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPart#updateLocalization()
	 * @since 1.1
	 * @generated
	 */
	public EOperation getPart__UpdateLocalization() {
		return partEClass.getEOperations().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MCompositePart <em>Composite Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Composite Part</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MCompositePart
	 * @since 1.1
	 * @generated
	 */
	public EClass getCompositePart() {
		return compositePartEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MPartStack <em>Part Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPartStack
	 * @since 1.0
	 * @generated
	 */
	public EClass getPartStack() {
		return partStackEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer <em>Part Sash Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Sash Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer
	 * @since 1.0
	 * @generated
	 */
	public EClass getPartSashContainer() {
		return partSashContainerEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow <em>Window</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Window</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow
	 * @since 1.0
	 * @generated
	 */
	public EClass getWindow() {
		return windowEClass;
	}

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getMainMenu <em>Main Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Main Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow#getMainMenu()
	 * @see #getWindow()
	 * @since 1.0
	 * @generated
	 */
	public EReference getWindow_MainMenu() {
		return (EReference) windowEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getX <em>X</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>X</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow#getX()
	 * @see #getWindow()
	 * @since 1.0
	 * @generated
	 */
	public EAttribute getWindow_X() {
		return (EAttribute) windowEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getY <em>Y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Y</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow#getY()
	 * @see #getWindow()
	 * @since 1.0
	 * @generated
	 */
	public EAttribute getWindow_Y() {
		return (EAttribute) windowEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWidth <em>Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Width</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWidth()
	 * @see #getWindow()
	 * @since 1.0
	 * @generated
	 */
	public EAttribute getWindow_Width() {
		return (EAttribute) windowEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getHeight <em>Height</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Height</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow#getHeight()
	 * @see #getWindow()
	 * @since 1.0
	 * @generated
	 */
	public EAttribute getWindow_Height() {
		return (EAttribute) windowEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWindows <em>Windows</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Windows</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWindows()
	 * @see #getWindow()
	 * @since 1.0
	 * @generated
	 */
	public EReference getWindow_Windows() {
		return (EReference) windowEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getSharedElements <em>Shared Elements</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Shared Elements</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow#getSharedElements()
	 * @see #getWindow()
	 * @since 1.0
	 * @generated
	 */
	public EReference getWindow_SharedElements() {
		return (EReference) windowEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * Returns the meta object for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#updateLocalization() <em>Update Localization</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Localization</em>' operation.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindow#updateLocalization()
	 * @since 1.1
	 * @generated
	 */
	public EOperation getWindow__UpdateLocalization() {
		return windowEClass.getEOperations().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow <em>Trimmed Window</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Trimmed Window</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow
	 * @since 1.0
	 * @generated
	 */
	public EClass getTrimmedWindow() {
		return trimmedWindowEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow#getTrimBars <em>Trim Bars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Trim Bars</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow#getTrimBars()
	 * @see #getTrimmedWindow()
	 * @since 1.0
	 * @generated
	 */
	public EReference getTrimmedWindow_TrimBars() {
		return (EReference) trimmedWindowEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimElement <em>Trim Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Trim Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MTrimElement
	 * @since 1.0
	 * @generated
	 */
	public EClass getTrimElement() {
		return trimElementEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement <em>Part Sash Container Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Sash Container Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement
	 * @since 1.0
	 * @generated
	 */
	public EClass getPartSashContainerElement() {
		return partSashContainerElementEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindowElement <em>Window Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Window Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindowElement
	 * @since 1.0
	 * @generated
	 */
	public EClass getWindowElement() {
		return windowElementEClass;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimBar <em>Trim Bar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Trim Bar</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MTrimBar
	 * @since 1.0
	 * @generated
	 */
	public EClass getTrimBar() {
		return trimBarEClass;
	}

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimBar#getPendingCleanup <em>Pending Cleanup</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Pending Cleanup</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MTrimBar#getPendingCleanup()
	 * @see #getTrimBar()
	 * @since 1.0
	 * @noreference
	 * @generated
	 */
	public EReference getTrimBar_PendingCleanup() {
		return (EReference) trimBarEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.ui.basic.MStackElement <em>Stack Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stack Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MStackElement
	 * @since 1.0
	 * @generated
	 */
	public EClass getStackElement() {
		return stackElementEClass;
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public MBasicFactory getBasicFactory() {
		return (MBasicFactory) getEFactoryInstance();
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
		partEClass = createEClass(PART);
		createEReference(partEClass, PART__MENUS);
		createEReference(partEClass, PART__TOOLBAR);
		createEAttribute(partEClass, PART__CLOSEABLE);
		createEAttribute(partEClass, PART__DESCRIPTION);
		createEAttribute(partEClass, PART__LOCALIZED_DESCRIPTION);
		createEReference(partEClass, PART__TRIM_BARS);
		createEOperation(partEClass, PART___UPDATE_LOCALIZATION);

		compositePartEClass = createEClass(COMPOSITE_PART);

		partStackEClass = createEClass(PART_STACK);

		partSashContainerEClass = createEClass(PART_SASH_CONTAINER);

		windowEClass = createEClass(WINDOW);
		createEReference(windowEClass, WINDOW__MAIN_MENU);
		createEAttribute(windowEClass, WINDOW__X);
		createEAttribute(windowEClass, WINDOW__Y);
		createEAttribute(windowEClass, WINDOW__WIDTH);
		createEAttribute(windowEClass, WINDOW__HEIGHT);
		createEReference(windowEClass, WINDOW__WINDOWS);
		createEReference(windowEClass, WINDOW__SHARED_ELEMENTS);
		createEOperation(windowEClass, WINDOW___UPDATE_LOCALIZATION);

		trimmedWindowEClass = createEClass(TRIMMED_WINDOW);
		createEReference(trimmedWindowEClass, TRIMMED_WINDOW__TRIM_BARS);

		trimElementEClass = createEClass(TRIM_ELEMENT);

		partSashContainerElementEClass = createEClass(PART_SASH_CONTAINER_ELEMENT);

		windowElementEClass = createEClass(WINDOW_ELEMENT);

		trimBarEClass = createEClass(TRIM_BAR);
		createEReference(trimBarEClass, TRIM_BAR__PENDING_CLEANUP);

		stackElementEClass = createEClass(STACK_ELEMENT);
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
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl) EPackage.Registry.INSTANCE
				.getEPackage(ApplicationPackageImpl.eNS_URI);
		UiPackageImpl theUiPackage = (UiPackageImpl) EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI);
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl) EPackage.Registry.INSTANCE
				.getEPackage(CommandsPackageImpl.eNS_URI);
		MenuPackageImpl theMenuPackage = (MenuPackageImpl) EPackage.Registry.INSTANCE
				.getEPackage(MenuPackageImpl.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		partEClass.getESuperTypes().add(this.getPartSashContainerElement());
		partEClass.getESuperTypes().add(this.getStackElement());
		partEClass.getESuperTypes().add(theApplicationPackage.getContribution());
		partEClass.getESuperTypes().add(theUiPackage.getContext());
		partEClass.getESuperTypes().add(theUiPackage.getUILabel());
		partEClass.getESuperTypes().add(theCommandsPackage.getHandlerContainer());
		partEClass.getESuperTypes().add(theUiPackage.getDirtyable());
		partEClass.getESuperTypes().add(theCommandsPackage.getBindings());
		partEClass.getESuperTypes().add(this.getWindowElement());
		EGenericType g1 = createEGenericType(this.getPart());
		compositePartEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getGenericTile());
		EGenericType g2 = createEGenericType(this.getPartSashContainerElement());
		g1.getETypeArguments().add(g2);
		compositePartEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getGenericStack());
		g2 = createEGenericType(this.getStackElement());
		g1.getETypeArguments().add(g2);
		partStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPartSashContainerElement());
		partStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getWindowElement());
		partStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getGenericTile());
		g2 = createEGenericType(this.getPartSashContainerElement());
		g1.getETypeArguments().add(g2);
		partSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPartSashContainerElement());
		partSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getWindowElement());
		partSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getElementContainer());
		g2 = createEGenericType(this.getWindowElement());
		g1.getETypeArguments().add(g2);
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getUILabel());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getContext());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theCommandsPackage.getHandlerContainer());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theCommandsPackage.getBindings());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getSnippetContainer());
		windowEClass.getEGenericSuperTypes().add(g1);
		trimmedWindowEClass.getESuperTypes().add(this.getWindow());
		trimElementEClass.getESuperTypes().add(theUiPackage.getUIElement());
		partSashContainerElementEClass.getESuperTypes().add(theUiPackage.getUIElement());
		windowElementEClass.getESuperTypes().add(theUiPackage.getUIElement());
		g1 = createEGenericType(theUiPackage.getGenericTrimContainer());
		g2 = createEGenericType(this.getTrimElement());
		g1.getETypeArguments().add(g2);
		trimBarEClass.getEGenericSuperTypes().add(g1);
		stackElementEClass.getESuperTypes().add(theUiPackage.getUIElement());

		// Initialize classes, features, and operations; add parameters
		initEClass(partEClass, MPart.class, "Part", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPart_Menus(), theMenuPackage.getMenu(), null, "menus", null, 0, -1, MPart.class, //$NON-NLS-1$
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPart_Toolbar(), theMenuPackage.getToolBar(), null, "toolbar", null, 0, 1, MPart.class, //$NON-NLS-1$
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPart_Closeable(), ecorePackage.getEBoolean(), "closeable", "false", 0, 1, MPart.class, //$NON-NLS-1$//$NON-NLS-2$
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPart_Description(), ecorePackage.getEString(), "description", null, 0, 1, MPart.class, //$NON-NLS-1$
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPart_LocalizedDescription(), ecorePackage.getEString(), "localizedDescription", null, 0, 1, //$NON-NLS-1$
				MPart.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED,
				IS_ORDERED);
		initEReference(getPart_TrimBars(), this.getTrimBar(), null, "trimBars", null, 0, -1, MPart.class, !IS_TRANSIENT, //$NON-NLS-1$
				!IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED,
				IS_ORDERED);

		initEOperation(getPart__UpdateLocalization(), null, "updateLocalization", 0, 1, IS_UNIQUE, IS_ORDERED); //$NON-NLS-1$

		initEClass(compositePartEClass, MCompositePart.class, "CompositePart", !IS_ABSTRACT, !IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);

		initEClass(partStackEClass, MPartStack.class, "PartStack", !IS_ABSTRACT, !IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);

		initEClass(partSashContainerEClass, MPartSashContainer.class, "PartSashContainer", !IS_ABSTRACT, !IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);

		initEClass(windowEClass, MWindow.class, "Window", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getWindow_MainMenu(), theMenuPackage.getMenu(), null, "mainMenu", null, 0, 1, MWindow.class, //$NON-NLS-1$
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getWindow_X(), ecorePackage.getEInt(), "x", "-2147483648", 0, 1, MWindow.class, !IS_TRANSIENT, //$NON-NLS-1$//$NON-NLS-2$
				!IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getWindow_Y(), ecorePackage.getEInt(), "y", "-2147483648", 0, 1, MWindow.class, !IS_TRANSIENT, //$NON-NLS-1$//$NON-NLS-2$
				!IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getWindow_Width(), ecorePackage.getEInt(), "width", "-1", 0, 1, MWindow.class, !IS_TRANSIENT, //$NON-NLS-1$//$NON-NLS-2$
				!IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getWindow_Height(), ecorePackage.getEInt(), "height", "-1", 0, 1, MWindow.class, !IS_TRANSIENT, //$NON-NLS-1$//$NON-NLS-2$
				!IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getWindow_Windows(), this.getWindow(), null, "windows", null, 0, -1, MWindow.class, //$NON-NLS-1$
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getWindow_SharedElements(), theUiPackage.getUIElement(), null, "sharedElements", null, 0, -1, //$NON-NLS-1$
				MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEOperation(getWindow__UpdateLocalization(), null, "updateLocalization", 0, 1, IS_UNIQUE, IS_ORDERED); //$NON-NLS-1$

		initEClass(trimmedWindowEClass, MTrimmedWindow.class, "TrimmedWindow", !IS_ABSTRACT, !IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTrimmedWindow_TrimBars(), this.getTrimBar(), null, "trimBars", null, 0, -1, //$NON-NLS-1$
				MTrimmedWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(trimElementEClass, MTrimElement.class, "TrimElement", IS_ABSTRACT, IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);

		initEClass(partSashContainerElementEClass, MPartSashContainerElement.class, "PartSashContainerElement", //$NON-NLS-1$
				IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(windowElementEClass, MWindowElement.class, "WindowElement", IS_ABSTRACT, IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);

		initEClass(trimBarEClass, MTrimBar.class, "TrimBar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getTrimBar_PendingCleanup(), this.getTrimElement(), null, "pendingCleanup", null, 0, -1, //$NON-NLS-1$
				MTrimBar.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stackElementEClass, MStackElement.class, "StackElement", IS_ABSTRACT, IS_INTERFACE, //$NON-NLS-1$
				IS_GENERATED_INSTANCE_CLASS);
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
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl <em>Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getPart()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass PART = eINSTANCE.getPart();

		/**
		 * The meta object literal for the '<em><b>Menus</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EReference PART__MENUS = eINSTANCE.getPart_Menus();

		/**
		 * The meta object literal for the '<em><b>Toolbar</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EReference PART__TOOLBAR = eINSTANCE.getPart_Toolbar();

		/**
		 * The meta object literal for the '<em><b>Closeable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EAttribute PART__CLOSEABLE = eINSTANCE.getPart_Closeable();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EAttribute PART__DESCRIPTION = eINSTANCE.getPart_Description();

		/**
		 * The meta object literal for the '<em><b>Localized Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EAttribute PART__LOCALIZED_DESCRIPTION = eINSTANCE.getPart_LocalizedDescription();

		/**
		 * The meta object literal for the '<em><b>Trim Bars</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.3
		 * @generated
		 */
		public static final EReference PART__TRIM_BARS = eINSTANCE.getPart_TrimBars();

		/**
		 * The meta object literal for the '<em><b>Update Localization</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.1
		 * @generated
		 */
		public static final EOperation PART___UPDATE_LOCALIZATION = eINSTANCE.getPart__UpdateLocalization();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.CompositePartImpl <em>Composite Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.CompositePartImpl
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getCompositePart()
		 * @since 1.1
		 * @generated
		 */
		public static final EClass COMPOSITE_PART = eINSTANCE.getCompositePart();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.PartStackImpl <em>Part Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.PartStackImpl
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getPartStack()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass PART_STACK = eINSTANCE.getPartStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.PartSashContainerImpl <em>Part Sash Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.PartSashContainerImpl
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getPartSashContainer()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass PART_SASH_CONTAINER = eINSTANCE.getPartSashContainer();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl <em>Window</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getWindow()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass WINDOW = eINSTANCE.getWindow();

		/**
		 * The meta object literal for the '<em><b>Main Menu</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EReference WINDOW__MAIN_MENU = eINSTANCE.getWindow_MainMenu();

		/**
		 * The meta object literal for the '<em><b>X</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EAttribute WINDOW__X = eINSTANCE.getWindow_X();

		/**
		 * The meta object literal for the '<em><b>Y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EAttribute WINDOW__Y = eINSTANCE.getWindow_Y();

		/**
		 * The meta object literal for the '<em><b>Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EAttribute WINDOW__WIDTH = eINSTANCE.getWindow_Width();

		/**
		 * The meta object literal for the '<em><b>Height</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EAttribute WINDOW__HEIGHT = eINSTANCE.getWindow_Height();

		/**
		 * The meta object literal for the '<em><b>Windows</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EReference WINDOW__WINDOWS = eINSTANCE.getWindow_Windows();

		/**
		 * The meta object literal for the '<em><b>Shared Elements</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EReference WINDOW__SHARED_ELEMENTS = eINSTANCE.getWindow_SharedElements();

		/**
		 * The meta object literal for the '<em><b>Update Localization</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.1
		 * @generated
		 */
		public static final EOperation WINDOW___UPDATE_LOCALIZATION = eINSTANCE.getWindow__UpdateLocalization();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.TrimmedWindowImpl <em>Trimmed Window</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.TrimmedWindowImpl
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getTrimmedWindow()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass TRIMMED_WINDOW = eINSTANCE.getTrimmedWindow();

		/**
		 * The meta object literal for the '<em><b>Trim Bars</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @generated
		 */
		public static final EReference TRIMMED_WINDOW__TRIM_BARS = eINSTANCE.getTrimmedWindow_TrimBars();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimElement <em>Trim Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.MTrimElement
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getTrimElement()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass TRIM_ELEMENT = eINSTANCE.getTrimElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement <em>Part Sash Container Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getPartSashContainerElement()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass PART_SASH_CONTAINER_ELEMENT = eINSTANCE.getPartSashContainerElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindowElement <em>Window Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindowElement
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getWindowElement()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass WINDOW_ELEMENT = eINSTANCE.getWindowElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.impl.TrimBarImpl <em>Trim Bar</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.TrimBarImpl
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getTrimBar()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass TRIM_BAR = eINSTANCE.getTrimBar();

		/**
		 * The meta object literal for the '<em><b>Pending Cleanup</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @since 1.0
		 * @noreference
		 * @generated
		 */
		public static final EReference TRIM_BAR__PENDING_CLEANUP = eINSTANCE.getTrimBar_PendingCleanup();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ui.basic.MStackElement <em>Stack Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ui.basic.MStackElement
		 * @see org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl#getStackElement()
		 * @since 1.0
		 * @generated
		 */
		public static final EClass STACK_ELEMENT = eINSTANCE.getStackElement();

	}

} //BasicPackageImpl
