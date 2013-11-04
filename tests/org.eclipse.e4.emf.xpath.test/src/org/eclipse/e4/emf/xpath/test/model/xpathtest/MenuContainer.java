/**
 */
package org.eclipse.e4.emf.xpath.test.model.xpathtest;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Menu Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.MenuContainer#getMenus <em>Menus</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getMenuContainer()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MenuContainer extends EObject {
	/**
	 * Returns the value of the '<em><b>Menus</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.emf.xpath.test.model.xpathtest.MenuElement}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Menus</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Menus</em>' containment reference list.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getMenuContainer_Menus()
	 * @model containment="true"
	 * @generated
	 */
	EList<MenuElement> getMenus();

} // MenuContainer
