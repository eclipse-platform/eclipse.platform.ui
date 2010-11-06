/**
 * <copyright>
 * </copyright>
 *
 * $Id: XpathtestFactory.java,v 1.1 2010/11/06 13:43:10 tschindl Exp $
 */
package org.eclipse.e4.emf.xpath.test.model.xpathtest;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage
 * @generated
 */
public interface XpathtestFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	XpathtestFactory eINSTANCE = org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.XpathtestFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Root</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Root</em>'.
	 * @generated
	 */
	Root createRoot();

	/**
	 * Returns a new object of class '<em>Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Node</em>'.
	 * @generated
	 */
	Node createNode();

	/**
	 * Returns a new object of class '<em>Extended Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Extended Node</em>'.
	 * @generated
	 */
	ExtendedNode createExtendedNode();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	XpathtestPackage getXpathtestPackage();

} //XpathtestFactory
