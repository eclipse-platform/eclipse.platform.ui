/**
 * <copyright>
 * </copyright>
 *
 * $Id: TestFactoryImpl.java,v 1.3 2010, 2015/06/04 20:22:20 johna Exp $
 */
package org.eclipse.e4.ui.tests.model.test.impl;

import org.eclipse.e4.ui.tests.model.test.MTestFactory;
import org.eclipse.e4.ui.tests.model.test.MTestHarness;
import org.eclipse.e4.ui.tests.model.test.MTestPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!--
 * end-user-doc -->
 *
 * @generated
 */
public class TestFactoryImpl extends EFactoryImpl implements MTestFactory {
	/**
	 * Creates the default factory implementation. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 */
	public static MTestFactory init() {
		try {
			MTestFactory theTestFactory = (MTestFactory) EPackage.Registry.INSTANCE
					.getEFactory(MTestPackage.eNS_URI);
			if (theTestFactory != null) {
				return theTestFactory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new TestFactoryImpl();
	}

	/**
	 * Creates an instance of the factory. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 */
	public TestFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
		case MTestPackage.TEST_HARNESS:
			return (EObject) createTestHarness();
		default:
			throw new IllegalArgumentException("The class '" + eClass.getName()
					+ "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public MTestHarness createTestHarness() {
		TestHarnessImpl testHarness = new TestHarnessImpl();
		return testHarness;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public MTestPackage getTestPackage() {
		return (MTestPackage) getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static MTestPackage getPackage() {
		return MTestPackage.eINSTANCE;
	}

} // TestFactoryImpl
