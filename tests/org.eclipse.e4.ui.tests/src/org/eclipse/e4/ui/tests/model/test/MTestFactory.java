/**
 * <copyright>
 * </copyright>
 *
 * $Id: MTestFactory.java,v 1.1.2.1 2010/04/16 12:24:24 tschindl Exp $
 */
package org.eclipse.e4.ui.tests.model.test;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.tests.model.test.MTestPackage
 * @generated
 */
public interface MTestFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MTestFactory eINSTANCE = org.eclipse.e4.ui.tests.model.test.impl.TestFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Harness</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Harness</em>'.
	 * @generated
	 */
	MTestHarness createTestHarness();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	MTestPackage getTestPackage();

} //MTestFactory
