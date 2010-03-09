/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.impl;

import java.util.Collection;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MModelComponent;
import org.eclipse.e4.ui.model.application.MUIElement;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Model Component</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getTags <em>Tags</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getPositionInParent <em>Position In Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getParentID <em>Parent ID</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getCommands <em>Commands</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getProcessor <em>Processor</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModelComponentImpl extends PartDescriptorContainerImpl implements MModelComponent {
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
	 * The cached value of the '{@link #getTags() <em>Tags</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTags()
	 * @generated
	 * @ordered
	 */
	protected EList<String> tags;

	/**
	 * The default value of the '{@link #getPositionInParent() <em>Position In Parent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPositionInParent()
	 * @generated
	 * @ordered
	 */
	protected static final String POSITION_IN_PARENT_EDEFAULT = ""; //$NON-NLS-1$

	/**
	 * The cached value of the '{@link #getPositionInParent() <em>Position In Parent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPositionInParent()
	 * @generated
	 * @ordered
	 */
	protected String positionInParent = POSITION_IN_PARENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getParentID() <em>Parent ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentID()
	 * @generated
	 * @ordered
	 */
	protected static final String PARENT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getParentID() <em>Parent ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentID()
	 * @generated
	 * @ordered
	 */
	protected String parentID = PARENT_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<MUIElement> children;

	/**
	 * The cached value of the '{@link #getCommands() <em>Commands</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommands()
	 * @generated
	 * @ordered
	 */
	protected EList<MCommand> commands;

	/**
	 * The cached value of the '{@link #getHandlers() <em>Handlers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHandlers()
	 * @generated
	 * @ordered
	 */
	protected EList<MHandler> handlers;

	/**
	 * The default value of the '{@link #getProcessor() <em>Processor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProcessor()
	 * @generated
	 * @ordered
	 */
	protected static final String PROCESSOR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getProcessor() <em>Processor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProcessor()
	 * @generated
	 * @ordered
	 */
	protected String processor = PROCESSOR_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelComponentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MApplicationPackage.Literals.MODEL_COMPONENT;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.MODEL_COMPONENT__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getTags() {
		if (tags == null) {
			tags = new EDataTypeUniqueEList<String>(String.class, this, MApplicationPackage.MODEL_COMPONENT__TAGS);
		}
		return tags;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getPositionInParent() {
		return positionInParent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPositionInParent(String newPositionInParent) {
		String oldPositionInParent = positionInParent;
		positionInParent = newPositionInParent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.MODEL_COMPONENT__POSITION_IN_PARENT, oldPositionInParent, positionInParent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getParentID() {
		return parentID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParentID(String newParentID) {
		String oldParentID = parentID;
		parentID = newParentID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.MODEL_COMPONENT__PARENT_ID, oldParentID, parentID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MUIElement> getChildren() {
		if (children == null) {
			children = new EObjectContainmentEList<MUIElement>(MUIElement.class, this, MApplicationPackage.MODEL_COMPONENT__CHILDREN);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MCommand> getCommands() {
		if (commands == null) {
			commands = new EObjectContainmentEList<MCommand>(MCommand.class, this, MApplicationPackage.MODEL_COMPONENT__COMMANDS);
		}
		return commands;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, MApplicationPackage.MODEL_COMPONENT__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getProcessor() {
		return processor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProcessor(String newProcessor) {
		String oldProcessor = processor;
		processor = newProcessor;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.MODEL_COMPONENT__PROCESSOR, oldProcessor, processor));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MApplicationPackage.MODEL_COMPONENT__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.MODEL_COMPONENT__COMMANDS:
				return ((InternalEList<?>)getCommands()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.MODEL_COMPONENT__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MApplicationPackage.MODEL_COMPONENT__ID:
				return getId();
			case MApplicationPackage.MODEL_COMPONENT__TAGS:
				return getTags();
			case MApplicationPackage.MODEL_COMPONENT__POSITION_IN_PARENT:
				return getPositionInParent();
			case MApplicationPackage.MODEL_COMPONENT__PARENT_ID:
				return getParentID();
			case MApplicationPackage.MODEL_COMPONENT__CHILDREN:
				return getChildren();
			case MApplicationPackage.MODEL_COMPONENT__COMMANDS:
				return getCommands();
			case MApplicationPackage.MODEL_COMPONENT__HANDLERS:
				return getHandlers();
			case MApplicationPackage.MODEL_COMPONENT__PROCESSOR:
				return getProcessor();
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
			case MApplicationPackage.MODEL_COMPONENT__ID:
				setId((String)newValue);
				return;
			case MApplicationPackage.MODEL_COMPONENT__TAGS:
				getTags().clear();
				getTags().addAll((Collection<? extends String>)newValue);
				return;
			case MApplicationPackage.MODEL_COMPONENT__POSITION_IN_PARENT:
				setPositionInParent((String)newValue);
				return;
			case MApplicationPackage.MODEL_COMPONENT__PARENT_ID:
				setParentID((String)newValue);
				return;
			case MApplicationPackage.MODEL_COMPONENT__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends MUIElement>)newValue);
				return;
			case MApplicationPackage.MODEL_COMPONENT__COMMANDS:
				getCommands().clear();
				getCommands().addAll((Collection<? extends MCommand>)newValue);
				return;
			case MApplicationPackage.MODEL_COMPONENT__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case MApplicationPackage.MODEL_COMPONENT__PROCESSOR:
				setProcessor((String)newValue);
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
			case MApplicationPackage.MODEL_COMPONENT__ID:
				setId(ID_EDEFAULT);
				return;
			case MApplicationPackage.MODEL_COMPONENT__TAGS:
				getTags().clear();
				return;
			case MApplicationPackage.MODEL_COMPONENT__POSITION_IN_PARENT:
				setPositionInParent(POSITION_IN_PARENT_EDEFAULT);
				return;
			case MApplicationPackage.MODEL_COMPONENT__PARENT_ID:
				setParentID(PARENT_ID_EDEFAULT);
				return;
			case MApplicationPackage.MODEL_COMPONENT__CHILDREN:
				getChildren().clear();
				return;
			case MApplicationPackage.MODEL_COMPONENT__COMMANDS:
				getCommands().clear();
				return;
			case MApplicationPackage.MODEL_COMPONENT__HANDLERS:
				getHandlers().clear();
				return;
			case MApplicationPackage.MODEL_COMPONENT__PROCESSOR:
				setProcessor(PROCESSOR_EDEFAULT);
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
			case MApplicationPackage.MODEL_COMPONENT__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case MApplicationPackage.MODEL_COMPONENT__TAGS:
				return tags != null && !tags.isEmpty();
			case MApplicationPackage.MODEL_COMPONENT__POSITION_IN_PARENT:
				return POSITION_IN_PARENT_EDEFAULT == null ? positionInParent != null : !POSITION_IN_PARENT_EDEFAULT.equals(positionInParent);
			case MApplicationPackage.MODEL_COMPONENT__PARENT_ID:
				return PARENT_ID_EDEFAULT == null ? parentID != null : !PARENT_ID_EDEFAULT.equals(parentID);
			case MApplicationPackage.MODEL_COMPONENT__CHILDREN:
				return children != null && !children.isEmpty();
			case MApplicationPackage.MODEL_COMPONENT__COMMANDS:
				return commands != null && !commands.isEmpty();
			case MApplicationPackage.MODEL_COMPONENT__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case MApplicationPackage.MODEL_COMPONENT__PROCESSOR:
				return PROCESSOR_EDEFAULT == null ? processor != null : !PROCESSOR_EDEFAULT.equals(processor);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MApplicationElement.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.MODEL_COMPONENT__ID: return MApplicationPackage.APPLICATION_ELEMENT__ID;
				case MApplicationPackage.MODEL_COMPONENT__TAGS: return MApplicationPackage.APPLICATION_ELEMENT__TAGS;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == MApplicationElement.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.APPLICATION_ELEMENT__ID: return MApplicationPackage.MODEL_COMPONENT__ID;
				case MApplicationPackage.APPLICATION_ELEMENT__TAGS: return MApplicationPackage.MODEL_COMPONENT__TAGS;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
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
		result.append(" (id: "); //$NON-NLS-1$
		result.append(id);
		result.append(", tags: "); //$NON-NLS-1$
		result.append(tags);
		result.append(", positionInParent: "); //$NON-NLS-1$
		result.append(positionInParent);
		result.append(", parentID: "); //$NON-NLS-1$
		result.append(parentID);
		result.append(", processor: "); //$NON-NLS-1$
		result.append(processor);
		result.append(')');
		return result.toString();
	}

} //ModelComponentImpl
