/**
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.fragment.impl;

import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.fragment.MFragmentFactory
 * @model kind="package"
 * @generated
 */
public class FragmentPackageImpl extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "fragment"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "http://www.eclipse.org/ui/2010/UIModel/fragment"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "fragment"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final FragmentPackageImpl eINSTANCE = org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.fragment.impl.ModelFragmentsImpl <em>Model Fragments</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.fragment.impl.ModelFragmentsImpl
	 * @see org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl#getModelFragments()
	 * @generated
	 */
	public static final int MODEL_FRAGMENTS = 0;

	/**
	 * The feature id for the '<em><b>Imports</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MODEL_FRAGMENTS__IMPORTS = 0;

	/**
	 * The feature id for the '<em><b>Fragments</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MODEL_FRAGMENTS__FRAGMENTS = 1;

	/**
	 * The number of structural features of the '<em>Model Fragments</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MODEL_FRAGMENTS_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.fragment.impl.ModelFragmentImpl <em>Model Fragment</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.fragment.impl.ModelFragmentImpl
	 * @see org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl#getModelFragment()
	 * @generated
	 */
	public static final int MODEL_FRAGMENT = 1;

	/**
	 * The feature id for the '<em><b>Elements</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MODEL_FRAGMENT__ELEMENTS = 0;

	/**
	 * The number of structural features of the '<em>Model Fragment</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int MODEL_FRAGMENT_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl <em>String Model Fragment</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl
	 * @see org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl#getStringModelFragment()
	 * @generated
	 */
	public static final int STRING_MODEL_FRAGMENT = 2;

	/**
	 * The feature id for the '<em><b>Elements</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STRING_MODEL_FRAGMENT__ELEMENTS = MODEL_FRAGMENT__ELEMENTS;

	/**
	 * The feature id for the '<em><b>Featurename</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STRING_MODEL_FRAGMENT__FEATURENAME = MODEL_FRAGMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Parent Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID = MODEL_FRAGMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Position In List</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STRING_MODEL_FRAGMENT__POSITION_IN_LIST = MODEL_FRAGMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>String Model Fragment</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int STRING_MODEL_FRAGMENT_FEATURE_COUNT = MODEL_FRAGMENT_FEATURE_COUNT + 3;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass modelFragmentsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass modelFragmentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stringModelFragmentEClass = null;

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
	 * @see org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private FragmentPackageImpl() {
		super(eNS_URI, ((EFactory)MFragmentFactory.INSTANCE));
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
	 * <p>This method is used to initialize {@link FragmentPackageImpl#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static FragmentPackageImpl init() {
		if (isInited) return (FragmentPackageImpl)EPackage.Registry.INSTANCE.getEPackage(FragmentPackageImpl.eNS_URI);

		// Obtain or create and register package
		FragmentPackageImpl theFragmentPackage = (FragmentPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof FragmentPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new FragmentPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		ApplicationPackageImpl.eINSTANCE.eClass();

		// Create package meta-data objects
		theFragmentPackage.createPackageContents();

		// Initialize created meta-data
		theFragmentPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theFragmentPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(FragmentPackageImpl.eNS_URI, theFragmentPackage);
		return theFragmentPackage;
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.fragment.MModelFragments <em>Model Fragments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model Fragments</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MModelFragments
	 * @generated
	 */
	public EClass getModelFragments() {
		return modelFragmentsEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.fragment.MModelFragments#getImports <em>Imports</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Imports</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MModelFragments#getImports()
	 * @see #getModelFragments()
	 * @generated
	 */
	public EReference getModelFragments_Imports() {
		return (EReference)modelFragmentsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.fragment.MModelFragments#getFragments <em>Fragments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Fragments</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MModelFragments#getFragments()
	 * @see #getModelFragments()
	 * @generated
	 */
	public EReference getModelFragments_Fragments() {
		return (EReference)modelFragmentsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.fragment.MModelFragment <em>Model Fragment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model Fragment</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MModelFragment
	 * @generated
	 */
	public EClass getModelFragment() {
		return modelFragmentEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.fragment.MModelFragment#getElements <em>Elements</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Elements</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MModelFragment#getElements()
	 * @see #getModelFragment()
	 * @generated
	 */
	public EReference getModelFragment_Elements() {
		return (EReference)modelFragmentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment <em>String Model Fragment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String Model Fragment</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MStringModelFragment
	 * @generated
	 */
	public EClass getStringModelFragment() {
		return stringModelFragmentEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getFeaturename <em>Featurename</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Featurename</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MStringModelFragment#getFeaturename()
	 * @see #getStringModelFragment()
	 * @generated
	 */
	public EAttribute getStringModelFragment_Featurename() {
		return (EAttribute)stringModelFragmentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getParentElementId <em>Parent Element Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parent Element Id</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MStringModelFragment#getParentElementId()
	 * @see #getStringModelFragment()
	 * @generated
	 */
	public EAttribute getStringModelFragment_ParentElementId() {
		return (EAttribute)stringModelFragmentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getPositionInList <em>Position In List</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position In List</em>'.
	 * @see org.eclipse.e4.ui.model.fragment.MStringModelFragment#getPositionInList()
	 * @see #getStringModelFragment()
	 * @generated
	 */
	public EAttribute getStringModelFragment_PositionInList() {
		return (EAttribute)stringModelFragmentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public MFragmentFactory getFragmentFactory() {
		return (MFragmentFactory)getEFactoryInstance();
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
		modelFragmentsEClass = createEClass(MODEL_FRAGMENTS);
		createEReference(modelFragmentsEClass, MODEL_FRAGMENTS__IMPORTS);
		createEReference(modelFragmentsEClass, MODEL_FRAGMENTS__FRAGMENTS);

		modelFragmentEClass = createEClass(MODEL_FRAGMENT);
		createEReference(modelFragmentEClass, MODEL_FRAGMENT__ELEMENTS);

		stringModelFragmentEClass = createEClass(STRING_MODEL_FRAGMENT);
		createEAttribute(stringModelFragmentEClass, STRING_MODEL_FRAGMENT__FEATURENAME);
		createEAttribute(stringModelFragmentEClass, STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID);
		createEAttribute(stringModelFragmentEClass, STRING_MODEL_FRAGMENT__POSITION_IN_LIST);
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
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		stringModelFragmentEClass.getESuperTypes().add(this.getModelFragment());

		// Initialize classes and features; add operations and parameters
		initEClass(modelFragmentsEClass, MModelFragments.class, "ModelFragments", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getModelFragments_Imports(), theApplicationPackage.getApplicationElement(), null, "imports", null, 0, -1, MModelFragments.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getModelFragments_Fragments(), this.getModelFragment(), null, "fragments", null, 0, -1, MModelFragments.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(modelFragmentEClass, MModelFragment.class, "ModelFragment", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getModelFragment_Elements(), theApplicationPackage.getApplicationElement(), null, "elements", null, 0, -1, MModelFragment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		EOperation op = addEOperation(modelFragmentEClass, theApplicationPackage.getApplicationElement(), "merge", 0, -1, IS_UNIQUE, IS_ORDERED); //$NON-NLS-1$
		addEParameter(op, theApplicationPackage.getApplication(), "application", 0, 1, IS_UNIQUE, IS_ORDERED); //$NON-NLS-1$

		initEClass(stringModelFragmentEClass, MStringModelFragment.class, "StringModelFragment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getStringModelFragment_Featurename(), ecorePackage.getEString(), "featurename", null, 0, 1, MStringModelFragment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getStringModelFragment_ParentElementId(), ecorePackage.getEString(), "parentElementId", null, 0, 1, MStringModelFragment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getStringModelFragment_PositionInList(), ecorePackage.getEString(), "positionInList", null, 0, 1, MStringModelFragment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Create resource
		createResource(eNS_URI);
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
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.fragment.impl.ModelFragmentsImpl <em>Model Fragments</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.fragment.impl.ModelFragmentsImpl
		 * @see org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl#getModelFragments()
		 * @generated
		 */
		public static final EClass MODEL_FRAGMENTS = eINSTANCE.getModelFragments();

		/**
		 * The meta object literal for the '<em><b>Imports</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference MODEL_FRAGMENTS__IMPORTS = eINSTANCE.getModelFragments_Imports();

		/**
		 * The meta object literal for the '<em><b>Fragments</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference MODEL_FRAGMENTS__FRAGMENTS = eINSTANCE.getModelFragments_Fragments();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.fragment.impl.ModelFragmentImpl <em>Model Fragment</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.fragment.impl.ModelFragmentImpl
		 * @see org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl#getModelFragment()
		 * @generated
		 */
		public static final EClass MODEL_FRAGMENT = eINSTANCE.getModelFragment();

		/**
		 * The meta object literal for the '<em><b>Elements</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference MODEL_FRAGMENT__ELEMENTS = eINSTANCE.getModelFragment_Elements();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl <em>String Model Fragment</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl
		 * @see org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl#getStringModelFragment()
		 * @generated
		 */
		public static final EClass STRING_MODEL_FRAGMENT = eINSTANCE.getStringModelFragment();

		/**
		 * The meta object literal for the '<em><b>Featurename</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute STRING_MODEL_FRAGMENT__FEATURENAME = eINSTANCE.getStringModelFragment_Featurename();

		/**
		 * The meta object literal for the '<em><b>Parent Element Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID = eINSTANCE.getStringModelFragment_ParentElementId();

		/**
		 * The meta object literal for the '<em><b>Position In List</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute STRING_MODEL_FRAGMENT__POSITION_IN_LIST = eINSTANCE.getStringModelFragment_PositionInList();

	}

} //FragmentPackageImpl
