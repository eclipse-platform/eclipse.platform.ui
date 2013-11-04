/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.e4.emf.xpath.test.model.xpathtest;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getRoot <em>Root</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getCat <em>Cat</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getValue <em>Value</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getInrefs <em>Inrefs</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getOutrefs <em>Outrefs</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode()
 * @model
 * @generated
 */
public interface Node extends MenuContainer {
	/**
	 * Returns the value of the '<em><b>Parent</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' container reference.
	 * @see #setParent(Node)
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode_Parent()
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getChildren
	 * @model opposite="children" transient="false"
	 * @generated
	 */
	Node getParent();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getParent <em>Parent</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' container reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(Node value);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode_Children()
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	EList<Node> getChildren();

	/**
	 * Returns the value of the '<em><b>Root</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Root</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Root</em>' reference.
	 * @see #setRoot(Root)
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode_Root()
	 * @model
	 * @generated
	 */
	Root getRoot();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getRoot <em>Root</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Root</em>' reference.
	 * @see #getRoot()
	 * @generated
	 */
	void setRoot(Root value);

	/**
	 * Returns the value of the '<em><b>Cat</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cat</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cat</em>' attribute.
	 * @see #setCat(String)
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode_Cat()
	 * @model
	 * @generated
	 */
	String getCat();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getCat <em>Cat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cat</em>' attribute.
	 * @see #getCat()
	 * @generated
	 */
	void setCat(String value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(String)
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode_Value()
	 * @model
	 * @generated
	 */
	String getValue();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(String value);

	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Inrefs</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getOutrefs <em>Outrefs</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Inrefs</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Inrefs</em>' reference list.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode_Inrefs()
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getOutrefs
	 * @model opposite="outrefs"
	 * @generated
	 */
	EList<Node> getInrefs();

	/**
	 * Returns the value of the '<em><b>Outrefs</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getInrefs <em>Inrefs</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Outrefs</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Outrefs</em>' reference list.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage#getNode_Outrefs()
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getInrefs
	 * @model opposite="inrefs"
	 * @generated
	 */
	EList<Node> getOutrefs();

} // Node
