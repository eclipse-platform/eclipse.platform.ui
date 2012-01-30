/**
 * <copyright>
 * </copyright>
 *
 * $Id: MTestPackage.java,v 1.10 2011/04/16 10:39:14 tschindl Exp $
 */
package org.eclipse.e4.ui.tests.model.test;

import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains
 * accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.tests.model.test.MTestFactory
 * @model kind="package"
 * @generated
 */
public interface MTestPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "test";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/ui/2010/Test/UIModel/test";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "test";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	MTestPackage eINSTANCE = org.eclipse.e4.ui.tests.model.test.impl.TestPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.tests.model.test.impl.TestHarnessImpl <em>Harness</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.tests.model.test.impl.TestHarnessImpl
	 * @see org.eclipse.e4.ui.tests.model.test.impl.TestPackageImpl#getTestHarness()
	 * @generated
	 */
	int TEST_HARNESS = 0;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Command Name</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__COMMAND_NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PARAMETERS = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CATEGORY = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CONTEXT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VARIABLES = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PROPERTIES = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Contribution URI</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CONTRIBUTION_URI = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__OBJECT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__WIDGET = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__RENDERER = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TO_BE_RENDERED = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ON_TOP = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VISIBLE = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PARENT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CONTAINER_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CUR_SHARED_REF = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VISIBLE_WHEN = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ACCESSIBILITY_PHRASE = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 18;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CHILDREN = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 19;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__SELECTED_ELEMENT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 20;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 21;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VALUE = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 22;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__INPUT_URI = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 23;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__LABEL = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 24;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ICON_URI = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 25;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TOOLTIP = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 26;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__DIRTY = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 27;

	/**
	 * The feature id for the '<em><b>Snippets</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__SNIPPETS = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 28;

	/**
	 * The number of structural features of the '<em>Harness</em>' class. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 29;

	/**
	 * The operation id for the '<em>Get Localized Command Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS___GET_LOCALIZED_COMMAND_NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Localized Description</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS___GET_LOCALIZED_DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Localized Accessibility Phrase</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS___GET_LOCALIZED_ACCESSIBILITY_PHRASE = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Localized Label</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS___GET_LOCALIZED_LABEL = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Localized Tooltip</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS___GET_LOCALIZED_TOOLTIP = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 4;

	/**
	 * The number of operations of the '<em>Harness</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 5;

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.tests.model.test.MTestHarness <em>Harness</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Harness</em>'.
	 * @see org.eclipse.e4.ui.tests.model.test.MTestHarness
	 * @generated
	 */
	EClass getTestHarness();

	/**
	 * Returns the factory that creates the instances of the model. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	MTestFactory getTestFactory();

	/**
	 * <!-- begin-user-doc --> Defines literals for the meta objects that
	 * represent
	 * <ul>
	 * <li>each class,</li>
	 * <li>each feature of each class,</li>
	 * <li>each enum,</li>
	 * <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '
		 * {@link org.eclipse.e4.ui.tests.model.test.impl.TestHarnessImpl
		 * <em>Harness</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
		 * -->
		 * 
		 * @see org.eclipse.e4.ui.tests.model.test.impl.TestHarnessImpl
		 * @see org.eclipse.e4.ui.tests.model.test.impl.TestPackageImpl#getTestHarness()
		 * @generated
		 */
		EClass TEST_HARNESS = eINSTANCE.getTestHarness();

	}

} // MTestPackage
