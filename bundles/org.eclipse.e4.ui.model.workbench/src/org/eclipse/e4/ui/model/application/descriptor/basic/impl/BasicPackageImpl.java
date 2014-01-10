/**
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.descriptor.basic.impl;

import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
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
 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MBasicFactory
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
	public static final String eNS_URI = "http://www.eclipse.org/ui/2010/UIModel/application/descriptor/basic"; //$NON-NLS-1$

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
	public static final BasicPackageImpl eINSTANCE = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl <em>Part Descriptor</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl#getPartDescriptor()
	 * @generated
	 */
	public static final int PART_DESCRIPTOR = 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__LABEL = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__ICON_URI = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__TOOLTIP = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Localized Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__LOCALIZED_LABEL = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Localized Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__LOCALIZED_TOOLTIP = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__HANDLERS = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__BINDING_CONTEXTS = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Allow Multiple</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__ALLOW_MULTIPLE = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Category</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__CATEGORY = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__MENUS = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__TOOLBAR = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Closeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__CLOSEABLE = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Dirtyable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__DIRTYABLE = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__CONTRIBUTION_URI = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR__LOCALIZED_DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 15;

	/**
	 * The number of structural features of the '<em>Part Descriptor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 16;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR___UPDATE_LOCALIZATION = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Part Descriptor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer <em>Part Descriptor Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl#getPartDescriptorContainer()
	 * @generated
	 */
	public static final int PART_DESCRIPTOR_CONTAINER = 1;

	/**
	 * The feature id for the '<em><b>Descriptors</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR_CONTAINER__DESCRIPTORS = 0;

	/**
	 * The number of structural features of the '<em>Part Descriptor Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Part Descriptor Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PART_DESCRIPTOR_CONTAINER_OPERATION_COUNT = 0;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass partDescriptorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass partDescriptorContainerEClass = null;

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
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private BasicPackageImpl() {
		super(eNS_URI, ((EFactory)MBasicFactory.INSTANCE));
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
		if (isInited) return (BasicPackageImpl)EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI);

		// Obtain or create and register package
		BasicPackageImpl theBasicPackage = (BasicPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof BasicPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new BasicPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI) instanceof ApplicationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI) : ApplicationPackageImpl.eINSTANCE);
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI) instanceof CommandsPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI) : CommandsPackageImpl.eINSTANCE);
		UiPackageImpl theUiPackage = (UiPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI) instanceof UiPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI) : UiPackageImpl.eINSTANCE);
		MenuPackageImpl theMenuPackage = (MenuPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(MenuPackageImpl.eNS_URI) instanceof MenuPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(MenuPackageImpl.eNS_URI) : MenuPackageImpl.eINSTANCE);
		org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl theBasicPackage_1 = (org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.eNS_URI) instanceof org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.eNS_URI) : org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.eINSTANCE);
		AdvancedPackageImpl theAdvancedPackage = (AdvancedPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(AdvancedPackageImpl.eNS_URI) instanceof AdvancedPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(AdvancedPackageImpl.eNS_URI) : AdvancedPackageImpl.eINSTANCE);

		// Create package meta-data objects
		theBasicPackage.createPackageContents();
		theApplicationPackage.createPackageContents();
		theCommandsPackage.createPackageContents();
		theUiPackage.createPackageContents();
		theMenuPackage.createPackageContents();
		theBasicPackage_1.createPackageContents();
		theAdvancedPackage.createPackageContents();

		// Initialize created meta-data
		theBasicPackage.initializePackageContents();
		theApplicationPackage.initializePackageContents();
		theCommandsPackage.initializePackageContents();
		theUiPackage.initializePackageContents();
		theMenuPackage.initializePackageContents();
		theBasicPackage_1.initializePackageContents();
		theAdvancedPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theBasicPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(BasicPackageImpl.eNS_URI, theBasicPackage);
		return theBasicPackage;
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor <em>Part Descriptor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Descriptor</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor
	 * @generated
	 */
	public EClass getPartDescriptor() {
		return partDescriptorEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isAllowMultiple <em>Allow Multiple</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Allow Multiple</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isAllowMultiple()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EAttribute getPartDescriptor_AllowMultiple() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Category</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getCategory()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EAttribute getPartDescriptor_Category() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getMenus <em>Menus</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Menus</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getMenus()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EReference getPartDescriptor_Menus() {
		return (EReference)partDescriptorEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getToolbar <em>Toolbar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Toolbar</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getToolbar()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EReference getPartDescriptor_Toolbar() {
		return (EReference)partDescriptorEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isCloseable <em>Closeable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Closeable</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isCloseable()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EAttribute getPartDescriptor_Closeable() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable <em>Dirtyable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dirtyable</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EAttribute getPartDescriptor_Dirtyable() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getContributionURI <em>Contribution URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Contribution URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getContributionURI()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EAttribute getPartDescriptor_ContributionURI() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getDescription()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EAttribute getPartDescriptor_Description() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(7);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getLocalizedDescription <em>Localized Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Localized Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getLocalizedDescription()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	public EAttribute getPartDescriptor_LocalizedDescription() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(8);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer <em>Part Descriptor Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Descriptor Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer
	 * @generated
	 */
	public EClass getPartDescriptorContainer() {
		return partDescriptorContainerEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer#getDescriptors <em>Descriptors</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Descriptors</em>'.
	 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer#getDescriptors()
	 * @see #getPartDescriptorContainer()
	 * @generated
	 */
	public EReference getPartDescriptorContainer_Descriptors() {
		return (EReference)partDescriptorContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public MBasicFactory getBasicFactory() {
		return (MBasicFactory)getEFactoryInstance();
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
		partDescriptorEClass = createEClass(PART_DESCRIPTOR);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__ALLOW_MULTIPLE);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__CATEGORY);
		createEReference(partDescriptorEClass, PART_DESCRIPTOR__MENUS);
		createEReference(partDescriptorEClass, PART_DESCRIPTOR__TOOLBAR);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__CLOSEABLE);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__DIRTYABLE);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__CONTRIBUTION_URI);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__DESCRIPTION);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__LOCALIZED_DESCRIPTION);

		partDescriptorContainerEClass = createEClass(PART_DESCRIPTOR_CONTAINER);
		createEReference(partDescriptorContainerEClass, PART_DESCRIPTOR_CONTAINER__DESCRIPTORS);
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
		UiPackageImpl theUiPackage = (UiPackageImpl)EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI);
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl)EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI);
		MenuPackageImpl theMenuPackage = (MenuPackageImpl)EPackage.Registry.INSTANCE.getEPackage(MenuPackageImpl.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		partDescriptorEClass.getESuperTypes().add(theApplicationPackage.getApplicationElement());
		partDescriptorEClass.getESuperTypes().add(theUiPackage.getUILabel());
		partDescriptorEClass.getESuperTypes().add(theCommandsPackage.getHandlerContainer());
		partDescriptorEClass.getESuperTypes().add(theCommandsPackage.getBindings());

		// Initialize classes, features, and operations; add parameters
		initEClass(partDescriptorEClass, MPartDescriptor.class, "PartDescriptor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPartDescriptor_AllowMultiple(), ecorePackage.getEBoolean(), "allowMultiple", null, 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPartDescriptor_Category(), ecorePackage.getEString(), "category", null, 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getPartDescriptor_Menus(), theMenuPackage.getMenu(), null, "menus", null, 0, -1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getPartDescriptor_Toolbar(), theMenuPackage.getToolBar(), null, "toolbar", null, 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPartDescriptor_Closeable(), ecorePackage.getEBoolean(), "closeable", "false", 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getPartDescriptor_Dirtyable(), ecorePackage.getEBoolean(), "dirtyable", null, 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPartDescriptor_ContributionURI(), ecorePackage.getEString(), "contributionURI", null, 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPartDescriptor_Description(), ecorePackage.getEString(), "description", null, 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPartDescriptor_LocalizedDescription(), ecorePackage.getEString(), "localizedDescription", null, 0, 1, MPartDescriptor.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(partDescriptorContainerEClass, MPartDescriptorContainer.class, "PartDescriptorContainer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPartDescriptorContainer_Descriptors(), this.getPartDescriptor(), null, "descriptors", null, 0, -1, MPartDescriptorContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
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
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl <em>Part Descriptor</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl
		 * @see org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl#getPartDescriptor()
		 * @generated
		 */
		public static final EClass PART_DESCRIPTOR = eINSTANCE.getPartDescriptor();

		/**
		 * The meta object literal for the '<em><b>Allow Multiple</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PART_DESCRIPTOR__ALLOW_MULTIPLE = eINSTANCE.getPartDescriptor_AllowMultiple();

		/**
		 * The meta object literal for the '<em><b>Category</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PART_DESCRIPTOR__CATEGORY = eINSTANCE.getPartDescriptor_Category();

		/**
		 * The meta object literal for the '<em><b>Menus</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference PART_DESCRIPTOR__MENUS = eINSTANCE.getPartDescriptor_Menus();

		/**
		 * The meta object literal for the '<em><b>Toolbar</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference PART_DESCRIPTOR__TOOLBAR = eINSTANCE.getPartDescriptor_Toolbar();

		/**
		 * The meta object literal for the '<em><b>Closeable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PART_DESCRIPTOR__CLOSEABLE = eINSTANCE.getPartDescriptor_Closeable();

		/**
		 * The meta object literal for the '<em><b>Dirtyable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PART_DESCRIPTOR__DIRTYABLE = eINSTANCE.getPartDescriptor_Dirtyable();

		/**
		 * The meta object literal for the '<em><b>Contribution URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PART_DESCRIPTOR__CONTRIBUTION_URI = eINSTANCE.getPartDescriptor_ContributionURI();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PART_DESCRIPTOR__DESCRIPTION = eINSTANCE.getPartDescriptor_Description();

		/**
		 * The meta object literal for the '<em><b>Localized Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PART_DESCRIPTOR__LOCALIZED_DESCRIPTION = eINSTANCE.getPartDescriptor_LocalizedDescription();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer <em>Part Descriptor Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer
		 * @see org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl#getPartDescriptorContainer()
		 * @generated
		 */
		public static final EClass PART_DESCRIPTOR_CONTAINER = eINSTANCE.getPartDescriptorContainer();

		/**
		 * The meta object literal for the '<em><b>Descriptors</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference PART_DESCRIPTOR_CONTAINER__DESCRIPTORS = eINSTANCE.getPartDescriptorContainer_Descriptors();

	}

} //BasicPackageImpl
