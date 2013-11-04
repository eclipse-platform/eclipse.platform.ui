/**
 */
package org.eclipse.e4.emf.xpath.test.model.xpathtest;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Menu Item</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.MenuItem#getMnemonic <em>Mnemonic</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getMenuItem()
 * @model
 * @generated
 */
public interface MenuItem extends MenuElement {
	/**
	 * Returns the value of the '<em><b>Mnemonic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mnemonic</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mnemonic</em>' attribute.
	 * @see #setMnemonic(char)
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getMenuItem_Mnemonic()
	 * @model
	 * @generated
	 */
	char getMnemonic();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.MenuItem#getMnemonic <em>Mnemonic</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mnemonic</em>' attribute.
	 * @see #getMnemonic()
	 * @generated
	 */
	void setMnemonic(char value);

} // MenuItem
