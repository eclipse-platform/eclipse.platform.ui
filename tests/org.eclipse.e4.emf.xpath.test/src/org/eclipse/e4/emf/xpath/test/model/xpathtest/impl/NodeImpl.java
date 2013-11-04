/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.e4.emf.xpath.test.model.xpathtest.impl;

import java.util.Collection;

import org.eclipse.e4.emf.xpath.test.model.xpathtest.MenuElement;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Node;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Root;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getMenus <em>Menus</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getRoot <em>Root</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getCat <em>Cat</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getValue <em>Value</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getInrefs <em>Inrefs</em>}</li>
 *   <li>{@link org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl#getOutrefs <em>Outrefs</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class NodeImpl extends EObjectImpl implements Node {
	/**
	 * The cached value of the '{@link #getMenus() <em>Menus</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenus()
	 * @generated
	 * @ordered
	 */
	protected EList<MenuElement> menus;

	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<Node> children;

	/**
	 * The cached value of the '{@link #getRoot() <em>Root</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRoot()
	 * @generated
	 * @ordered
	 */
	protected Root root;

	/**
	 * The default value of the '{@link #getCat() <em>Cat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCat()
	 * @generated
	 * @ordered
	 */
	protected static final String CAT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCat() <em>Cat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCat()
	 * @generated
	 * @ordered
	 */
	protected String cat = CAT_EDEFAULT;

	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final String VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected String value = VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getInrefs() <em>Inrefs</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInrefs()
	 * @generated
	 * @ordered
	 */
	protected EList<Node> inrefs;

	/**
	 * The cached value of the '{@link #getOutrefs() <em>Outrefs</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutrefs()
	 * @generated
	 * @ordered
	 */
	protected EList<Node> outrefs;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected NodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return XpathtestPackage.Literals.NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MenuElement> getMenus() {
		if (menus == null) {
			menus = new EObjectContainmentEList<MenuElement>(MenuElement.class, this, XpathtestPackage.NODE__MENUS);
		}
		return menus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node getParent() {
		if (eContainerFeatureID() != XpathtestPackage.NODE__PARENT) return null;
		return (Node)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(Node newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParent, XpathtestPackage.NODE__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(Node newParent) {
		if (newParent != eInternalContainer() || (eContainerFeatureID() != XpathtestPackage.NODE__PARENT && newParent != null)) {
			if (EcoreUtil.isAncestor(this, newParent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParent != null)
				msgs = ((InternalEObject)newParent).eInverseAdd(this, XpathtestPackage.NODE__CHILDREN, Node.class, msgs);
			msgs = basicSetParent(newParent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, XpathtestPackage.NODE__PARENT, newParent, newParent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Node> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<Node>(Node.class, this, XpathtestPackage.NODE__CHILDREN, XpathtestPackage.NODE__PARENT);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Root getRoot() {
		if (root != null && root.eIsProxy()) {
			InternalEObject oldRoot = (InternalEObject)root;
			root = (Root)eResolveProxy(oldRoot);
			if (root != oldRoot) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, XpathtestPackage.NODE__ROOT, oldRoot, root));
			}
		}
		return root;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Root basicGetRoot() {
		return root;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRoot(Root newRoot) {
		Root oldRoot = root;
		root = newRoot;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, XpathtestPackage.NODE__ROOT, oldRoot, root));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCat() {
		return cat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCat(String newCat) {
		String oldCat = cat;
		cat = newCat;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, XpathtestPackage.NODE__CAT, oldCat, cat));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValue(String newValue) {
		String oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, XpathtestPackage.NODE__VALUE, oldValue, value));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, XpathtestPackage.NODE__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Node> getInrefs() {
		if (inrefs == null) {
			inrefs = new EObjectWithInverseResolvingEList.ManyInverse<Node>(Node.class, this, XpathtestPackage.NODE__INREFS, XpathtestPackage.NODE__OUTREFS);
		}
		return inrefs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Node> getOutrefs() {
		if (outrefs == null) {
			outrefs = new EObjectWithInverseResolvingEList.ManyInverse<Node>(Node.class, this, XpathtestPackage.NODE__OUTREFS, XpathtestPackage.NODE__INREFS);
		}
		return outrefs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case XpathtestPackage.NODE__PARENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((Node)otherEnd, msgs);
			case XpathtestPackage.NODE__CHILDREN:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildren()).basicAdd(otherEnd, msgs);
			case XpathtestPackage.NODE__INREFS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getInrefs()).basicAdd(otherEnd, msgs);
			case XpathtestPackage.NODE__OUTREFS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getOutrefs()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case XpathtestPackage.NODE__MENUS:
				return ((InternalEList<?>)getMenus()).basicRemove(otherEnd, msgs);
			case XpathtestPackage.NODE__PARENT:
				return basicSetParent(null, msgs);
			case XpathtestPackage.NODE__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
			case XpathtestPackage.NODE__INREFS:
				return ((InternalEList<?>)getInrefs()).basicRemove(otherEnd, msgs);
			case XpathtestPackage.NODE__OUTREFS:
				return ((InternalEList<?>)getOutrefs()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case XpathtestPackage.NODE__PARENT:
				return eInternalContainer().eInverseRemove(this, XpathtestPackage.NODE__CHILDREN, Node.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case XpathtestPackage.NODE__MENUS:
				return getMenus();
			case XpathtestPackage.NODE__PARENT:
				return getParent();
			case XpathtestPackage.NODE__CHILDREN:
				return getChildren();
			case XpathtestPackage.NODE__ROOT:
				if (resolve) return getRoot();
				return basicGetRoot();
			case XpathtestPackage.NODE__CAT:
				return getCat();
			case XpathtestPackage.NODE__VALUE:
				return getValue();
			case XpathtestPackage.NODE__ID:
				return getId();
			case XpathtestPackage.NODE__INREFS:
				return getInrefs();
			case XpathtestPackage.NODE__OUTREFS:
				return getOutrefs();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case XpathtestPackage.NODE__MENUS:
				getMenus().clear();
				getMenus().addAll((Collection<? extends MenuElement>)newValue);
				return;
			case XpathtestPackage.NODE__PARENT:
				setParent((Node)newValue);
				return;
			case XpathtestPackage.NODE__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends Node>)newValue);
				return;
			case XpathtestPackage.NODE__ROOT:
				setRoot((Root)newValue);
				return;
			case XpathtestPackage.NODE__CAT:
				setCat((String)newValue);
				return;
			case XpathtestPackage.NODE__VALUE:
				setValue((String)newValue);
				return;
			case XpathtestPackage.NODE__ID:
				setId((String)newValue);
				return;
			case XpathtestPackage.NODE__INREFS:
				getInrefs().clear();
				getInrefs().addAll((Collection<? extends Node>)newValue);
				return;
			case XpathtestPackage.NODE__OUTREFS:
				getOutrefs().clear();
				getOutrefs().addAll((Collection<? extends Node>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case XpathtestPackage.NODE__MENUS:
				getMenus().clear();
				return;
			case XpathtestPackage.NODE__PARENT:
				setParent((Node)null);
				return;
			case XpathtestPackage.NODE__CHILDREN:
				getChildren().clear();
				return;
			case XpathtestPackage.NODE__ROOT:
				setRoot((Root)null);
				return;
			case XpathtestPackage.NODE__CAT:
				setCat(CAT_EDEFAULT);
				return;
			case XpathtestPackage.NODE__VALUE:
				setValue(VALUE_EDEFAULT);
				return;
			case XpathtestPackage.NODE__ID:
				setId(ID_EDEFAULT);
				return;
			case XpathtestPackage.NODE__INREFS:
				getInrefs().clear();
				return;
			case XpathtestPackage.NODE__OUTREFS:
				getOutrefs().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case XpathtestPackage.NODE__MENUS:
				return menus != null && !menus.isEmpty();
			case XpathtestPackage.NODE__PARENT:
				return getParent() != null;
			case XpathtestPackage.NODE__CHILDREN:
				return children != null && !children.isEmpty();
			case XpathtestPackage.NODE__ROOT:
				return root != null;
			case XpathtestPackage.NODE__CAT:
				return CAT_EDEFAULT == null ? cat != null : !CAT_EDEFAULT.equals(cat);
			case XpathtestPackage.NODE__VALUE:
				return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
			case XpathtestPackage.NODE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case XpathtestPackage.NODE__INREFS:
				return inrefs != null && !inrefs.isEmpty();
			case XpathtestPackage.NODE__OUTREFS:
				return outrefs != null && !outrefs.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (cat: ");
		result.append(cat);
		result.append(", value: ");
		result.append(value);
		result.append(", id: ");
		result.append(id);
		result.append(')');
		return result.toString();
	}

} //NodeImpl
