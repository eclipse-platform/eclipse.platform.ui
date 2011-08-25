/**
 * <copyright>
 * </copyright>
 *
 * $Id: XpathtestPackage.java,v 1.1 2010/11/06 13:43:10 tschindl Exp $
 */
package org.eclipse.e4.emf.xpath.test.model.xpathtest;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

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
 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestFactory
 * @model kind="package"
 * @generated
 */
public interface XpathtestPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "xpathtest";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/emf/xpathtest";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "xpathtest";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	XpathtestPackage eINSTANCE = org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.XpathtestPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.RootImpl <em>Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.RootImpl
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.XpathtestPackageImpl#getRoot()
	 * @generated
	 */
	int ROOT = 0;

	/**
	 * The feature id for the '<em><b>Nodes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROOT__NODES = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROOT__ID = 1;

	/**
	 * The number of structural features of the '<em>Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROOT_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl <em>Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.XpathtestPackageImpl#getNode()
	 * @generated
	 */
	int NODE = 1;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__PARENT = 0;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__CHILDREN = 1;

	/**
	 * The feature id for the '<em><b>Root</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__ROOT = 2;

	/**
	 * The feature id for the '<em><b>Cat</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__CAT = 3;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__VALUE = 4;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__ID = 5;

	/**
	 * The feature id for the '<em><b>Inrefs</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__INREFS = 6;

	/**
	 * The feature id for the '<em><b>Outrefs</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__OUTREFS = 7;

	/**
	 * The number of structural features of the '<em>Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE_FEATURE_COUNT = 8;


	/**
	 * The meta object id for the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.ExtendedNodeImpl <em>Extended Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.ExtendedNodeImpl
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.XpathtestPackageImpl#getExtendedNode()
	 * @generated
	 */
	int EXTENDED_NODE = 2;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__PARENT = NODE__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__CHILDREN = NODE__CHILDREN;

	/**
	 * The feature id for the '<em><b>Root</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__ROOT = NODE__ROOT;

	/**
	 * The feature id for the '<em><b>Cat</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__CAT = NODE__CAT;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__VALUE = NODE__VALUE;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__ID = NODE__ID;

	/**
	 * The feature id for the '<em><b>Inrefs</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__INREFS = NODE__INREFS;

	/**
	 * The feature id for the '<em><b>Outrefs</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__OUTREFS = NODE__OUTREFS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE__NAME = NODE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Extended Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTENDED_NODE_FEATURE_COUNT = NODE_FEATURE_COUNT + 1;


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Root <em>Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Root</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Root
	 * @generated
	 */
	EClass getRoot();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Root#getNodes <em>Nodes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Nodes</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Root#getNodes()
	 * @see #getRoot()
	 * @generated
	 */
	EReference getRoot_Nodes();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Root#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Root#getId()
	 * @see #getRoot()
	 * @generated
	 */
	EAttribute getRoot_Id();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node <em>Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Node</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node
	 * @generated
	 */
	EClass getNode();

	/**
	 * Returns the meta object for the container reference '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Parent</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getParent()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Parent();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getChildren()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Children();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getRoot <em>Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Root</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getRoot()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Root();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getCat <em>Cat</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Cat</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getCat()
	 * @see #getNode()
	 * @generated
	 */
	EAttribute getNode_Cat();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getValue()
	 * @see #getNode()
	 * @generated
	 */
	EAttribute getNode_Value();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getId()
	 * @see #getNode()
	 * @generated
	 */
	EAttribute getNode_Id();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getInrefs <em>Inrefs</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Inrefs</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getInrefs()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Inrefs();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getOutrefs <em>Outrefs</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Outrefs</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.Node#getOutrefs()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Outrefs();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.ExtendedNode <em>Extended Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Extended Node</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.ExtendedNode
	 * @generated
	 */
	EClass getExtendedNode();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.ExtendedNode#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.ExtendedNode#getName()
	 * @see #getExtendedNode()
	 * @generated
	 */
	EAttribute getExtendedNode_Name();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	XpathtestFactory getXpathtestFactory();

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
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.RootImpl <em>Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.RootImpl
		 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.XpathtestPackageImpl#getRoot()
		 * @generated
		 */
		EClass ROOT = eINSTANCE.getRoot();

		/**
		 * The meta object literal for the '<em><b>Nodes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ROOT__NODES = eINSTANCE.getRoot_Nodes();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROOT__ID = eINSTANCE.getRoot_Id();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl <em>Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl
		 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.XpathtestPackageImpl#getNode()
		 * @generated
		 */
		EClass NODE = eINSTANCE.getNode();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__PARENT = eINSTANCE.getNode_Parent();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__CHILDREN = eINSTANCE.getNode_Children();

		/**
		 * The meta object literal for the '<em><b>Root</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__ROOT = eINSTANCE.getNode_Root();

		/**
		 * The meta object literal for the '<em><b>Cat</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NODE__CAT = eINSTANCE.getNode_Cat();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NODE__VALUE = eINSTANCE.getNode_Value();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NODE__ID = eINSTANCE.getNode_Id();

		/**
		 * The meta object literal for the '<em><b>Inrefs</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__INREFS = eINSTANCE.getNode_Inrefs();

		/**
		 * The meta object literal for the '<em><b>Outrefs</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__OUTREFS = eINSTANCE.getNode_Outrefs();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.ExtendedNodeImpl <em>Extended Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.ExtendedNodeImpl
		 * @see org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.XpathtestPackageImpl#getExtendedNode()
		 * @generated
		 */
		EClass EXTENDED_NODE = eINSTANCE.getExtendedNode();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXTENDED_NODE__NAME = eINSTANCE.getExtendedNode_Name();

	}

} //XpathtestPackage
