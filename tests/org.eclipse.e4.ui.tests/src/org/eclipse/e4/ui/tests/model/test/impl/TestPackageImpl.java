/**
 * <copyright>
 * </copyright>
 *
 * $Id: TestPackageImpl.java,v 1.3 2010/05/14 19:39:11 emoffatt Exp $
 */
package org.eclipse.e4.ui.tests.model.test.impl;

import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.tests.model.test.MTestFactory;
import org.eclipse.e4.ui.tests.model.test.MTestHarness;
import org.eclipse.e4.ui.tests.model.test.MTestPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!--
 * end-user-doc -->
 * @generated
 */
public class TestPackageImpl extends EPackageImpl implements MTestPackage {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass testHarnessEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the
	 * package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory
	 * method {@link #init init()}, which also performs initialization of the
	 * package, or returns the registered package, if one already exists. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.e4.ui.tests.model.test.MTestPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TestPackageImpl() {
		super(eNS_URI, MTestFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model,
	 * and for any others upon which it depends.
	 *
	 * <p>
	 * This method is used to initialize {@link MTestPackage#eINSTANCE} when
	 * that field is accessed. Clients should not invoke it directly. Instead,
	 * they should simply access that field to obtain the package. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static MTestPackage init() {
		if (isInited) return (MTestPackage)EPackage.Registry.INSTANCE.getEPackage(MTestPackage.eNS_URI);

		// Obtain or create and register package
		TestPackageImpl theTestPackage = (TestPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof TestPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new TestPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		ApplicationPackageImpl.eINSTANCE.eClass();

		// Create package meta-data objects
		theTestPackage.createPackageContents();

		// Initialize created meta-data
		theTestPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTestPackage.freeze();


		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(MTestPackage.eNS_URI, theTestPackage);
		return theTestPackage;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTestHarness() {
		return testHarnessEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MTestFactory getTestFactory() {
		return (MTestFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
		testHarnessEClass = createEClass(TEST_HARNESS);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model. This
	 * method is guarded to have no affect on any invocation but its first. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
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
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl)EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI);
		UiPackageImpl theUiPackage = (UiPackageImpl)EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		EGenericType g1 = createEGenericType(theApplicationPackage.getApplicationElement());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theCommandsPackage.getCommand());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getContext());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theApplicationPackage.getContribution());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getElementContainer());
		EGenericType g2 = createEGenericType(theUiPackage.getUIElement());
		g1.getETypeArguments().add(g2);
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theCommandsPackage.getParameter());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getInput());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getUILabel());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getDirtyable());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theUiPackage.getSnippetContainer());
		testHarnessEClass.getEGenericSuperTypes().add(g1);

		// Initialize classes, features, and operations; add parameters
		initEClass(testHarnessEClass, MTestHarness.class, "TestHarness", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} // TestPackageImpl
