/**
 */
package org.eclipse.e4.emf.xpath.test.model.xpathtest;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Menu</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Menu#getChildren <em>Children</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getMenu()
 * @model
 * @generated
 */
public interface Menu extends MenuElement {
	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.emf.xpath.test.model.xpathtest.MenuElement}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getMenu_Children()
	 * @model containment="true"
	 * @generated
	 */
	EList<MenuElement> getChildren();

} // Menu
