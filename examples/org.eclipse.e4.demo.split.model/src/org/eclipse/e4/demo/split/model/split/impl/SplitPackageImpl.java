/**
 * Copyright (c) 2012 IBM Corporation and BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *      Tom Schindl - initial API and implementation
 */
package org.eclipse.e4.demo.split.model.split.impl;

import org.eclipse.e4.demo.split.model.split.MSplitFactory;
import org.eclipse.e4.demo.split.model.split.MStackSashContainer;

import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;

import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;

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
 * @see org.eclipse.e4.demo.split.model.split.MSplitFactory
 * @model kind="package"
 * @generated
 */
public class SplitPackageImpl extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "split"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "http://www.eclipse.org/e4/demo/split/1.0"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "split"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final SplitPackageImpl eINSTANCE = org.eclipse.e4.demo.split.model.split.impl.SplitPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.demo.split.model.split.impl.StackSashContainerImpl <em>Stack Sash Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.demo.split.model.split.impl.StackSashContainerImpl
	 * @see org.eclipse.e4.demo.split.model.split.impl.SplitPackageImpl#getStackSashContainer()
	 * @generated
	 */
	public static final int STACK_SASH_CONTAINER = 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__ELEMENT_ID = BasicPackageImpl.STACK_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__PERSISTED_STATE = BasicPackageImpl.STACK_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__TAGS = BasicPackageImpl.STACK_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__CONTRIBUTOR_URI = BasicPackageImpl.STACK_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__TRANSIENT_DATA = BasicPackageImpl.STACK_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__WIDGET = BasicPackageImpl.STACK_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__RENDERER = BasicPackageImpl.STACK_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__TO_BE_RENDERED = BasicPackageImpl.STACK_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__ON_TOP = BasicPackageImpl.STACK_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__VISIBLE = BasicPackageImpl.STACK_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__PARENT = BasicPackageImpl.STACK_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__CONTAINER_DATA = BasicPackageImpl.STACK_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__CUR_SHARED_REF = BasicPackageImpl.STACK_ELEMENT__CUR_SHARED_REF;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__VISIBLE_WHEN = BasicPackageImpl.STACK_ELEMENT__VISIBLE_WHEN;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__ACCESSIBILITY_PHRASE = BasicPackageImpl.STACK_ELEMENT__ACCESSIBILITY_PHRASE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__CHILDREN = BasicPackageImpl.STACK_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__SELECTED_ELEMENT = BasicPackageImpl.STACK_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER__HORIZONTAL = BasicPackageImpl.STACK_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Stack Sash Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER_FEATURE_COUNT = BasicPackageImpl.STACK_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER___GET_LOCALIZED_ACCESSIBILITY_PHRASE = BasicPackageImpl.STACK_ELEMENT___GET_LOCALIZED_ACCESSIBILITY_PHRASE;

	/**
	 * The number of operations of the '<em>Stack Sash Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STACK_SASH_CONTAINER_OPERATION_COUNT = BasicPackageImpl.STACK_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stackSashContainerEClass = null;

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
	 * @see org.eclipse.e4.demo.split.model.split.impl.SplitPackageImpl#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private SplitPackageImpl() {
		super(eNS_URI, ((EFactory)MSplitFactory.INSTANCE));
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
	 * <p>This method is used to initialize {@link SplitPackageImpl#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static SplitPackageImpl init() {
		if (isInited) return (SplitPackageImpl)EPackage.Registry.INSTANCE.getEPackage(SplitPackageImpl.eNS_URI);

		// Obtain or create and register package
		SplitPackageImpl theSplitPackage = (SplitPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof SplitPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new SplitPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		ApplicationPackageImpl.eINSTANCE.eClass();

		// Create package meta-data objects
		theSplitPackage.createPackageContents();

		// Initialize created meta-data
		theSplitPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theSplitPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(SplitPackageImpl.eNS_URI, theSplitPackage);
		return theSplitPackage;
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.demo.split.model.split.MStackSashContainer <em>Stack Sash Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stack Sash Container</em>'.
	 * @see org.eclipse.e4.demo.split.model.split.MStackSashContainer
	 * @generated
	 */
	public EClass getStackSashContainer() {
		return stackSashContainerEClass;
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public MSplitFactory getSplitFactory() {
		return (MSplitFactory)getEFactoryInstance();
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
		stackSashContainerEClass = createEClass(STACK_SASH_CONTAINER);
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
		BasicPackageImpl theBasicPackage = (BasicPackageImpl)EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		stackSashContainerEClass.getESuperTypes().add(theBasicPackage.getStackElement());
		stackSashContainerEClass.getESuperTypes().add(theBasicPackage.getPartSashContainer());

		// Initialize classes, features, and operations; add parameters
		initEClass(stackSashContainerEClass, MStackSashContainer.class, "StackSashContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		// Create resource
		createResource(eNS_URI);
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
		 * The meta object literal for the '{@link org.eclipse.e4.demo.split.model.split.impl.StackSashContainerImpl <em>Stack Sash Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.demo.split.model.split.impl.StackSashContainerImpl
		 * @see org.eclipse.e4.demo.split.model.split.impl.SplitPackageImpl#getStackSashContainer()
		 * @generated
		 */
		public static final EClass STACK_SASH_CONTAINER = eINSTANCE.getStackSashContainer();

	}

} //SplitPackageImpl
