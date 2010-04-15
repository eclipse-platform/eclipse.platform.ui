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
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MModelComponent;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;

import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;

import org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorContainerImpl;

import org.eclipse.e4.ui.model.application.ui.MUIElement;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Model Component</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getElementId <em>Element Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getTags <em>Tags</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getBindingTables <em>Binding Tables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getRootContext <em>Root Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getPositionInParent <em>Position In Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getParentID <em>Parent ID</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getCommands <em>Commands</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getProcessor <em>Processor</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl#getBindings <em>Bindings</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModelComponentImpl extends PartDescriptorContainerImpl implements MModelComponent {
	/**
	 * The default value of the '{@link #getElementId() <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElementId()
	 * @generated
	 * @ordered
	 */
	protected static final String ELEMENT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getElementId() <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElementId()
	 * @generated
	 * @ordered
	 */
	protected String elementId = ELEMENT_ID_EDEFAULT;

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
	 * The cached value of the '{@link #getHandlers() <em>Handlers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHandlers()
	 * @generated
	 * @ordered
	 */
	protected EList<MHandler> handlers;

	/**
	 * The cached value of the '{@link #getBindingTables() <em>Binding Tables</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingTables()
	 * @generated
	 * @ordered
	 */
	protected EList<MBindingTable> bindingTables;

	/**
	 * The cached value of the '{@link #getRootContext() <em>Root Context</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRootContext()
	 * @generated
	 * @ordered
	 */
	protected MBindingContext rootContext;

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
	 * The cached value of the '{@link #getBindings() <em>Bindings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindings()
	 * @generated
	 * @ordered
	 */
	protected EList<MKeyBinding> bindings;

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
		return ApplicationPackageImpl.Literals.MODEL_COMPONENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setElementId(String newElementId) {
		String oldElementId = elementId;
		elementId = newElementId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.MODEL_COMPONENT__ELEMENT_ID, oldElementId, elementId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<String> getTags() {
		if (tags == null) {
			tags = new EDataTypeUniqueEList<String>(String.class, this, ApplicationPackageImpl.MODEL_COMPONENT__TAGS);
		}
		return tags;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, ApplicationPackageImpl.MODEL_COMPONENT__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MBindingTable> getBindingTables() {
		if (bindingTables == null) {
			bindingTables = new EObjectContainmentEList<MBindingTable>(MBindingTable.class, this, ApplicationPackageImpl.MODEL_COMPONENT__BINDING_TABLES);
		}
		return bindingTables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MBindingContext getRootContext() {
		return rootContext;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRootContext(MBindingContext newRootContext, NotificationChain msgs) {
		MBindingContext oldRootContext = rootContext;
		rootContext = newRootContext;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT, oldRootContext, newRootContext);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRootContext(MBindingContext newRootContext) {
		if (newRootContext != rootContext) {
			NotificationChain msgs = null;
			if (rootContext != null)
				msgs = ((InternalEObject)rootContext).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT, null, msgs);
			if (newRootContext != null)
				msgs = ((InternalEObject)newRootContext).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT, null, msgs);
			msgs = basicSetRootContext(newRootContext, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT, newRootContext, newRootContext));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.MODEL_COMPONENT__POSITION_IN_PARENT, oldPositionInParent, positionInParent));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.MODEL_COMPONENT__PARENT_ID, oldParentID, parentID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MUIElement> getChildren() {
		if (children == null) {
			children = new EObjectContainmentEList<MUIElement>(MUIElement.class, this, ApplicationPackageImpl.MODEL_COMPONENT__CHILDREN);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MCommand> getCommands() {
		if (commands == null) {
			commands = new EObjectContainmentEList<MCommand>(MCommand.class, this, ApplicationPackageImpl.MODEL_COMPONENT__COMMANDS);
		}
		return commands;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.MODEL_COMPONENT__PROCESSOR, oldProcessor, processor));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MKeyBinding> getBindings() {
		if (bindings == null) {
			bindings = new EObjectContainmentEList<MKeyBinding>(MKeyBinding.class, this, ApplicationPackageImpl.MODEL_COMPONENT__BINDINGS);
		}
		return bindings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplicationPackageImpl.MODEL_COMPONENT__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDING_TABLES:
				return ((InternalEList<?>)getBindingTables()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT:
				return basicSetRootContext(null, msgs);
			case ApplicationPackageImpl.MODEL_COMPONENT__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.MODEL_COMPONENT__COMMANDS:
				return ((InternalEList<?>)getCommands()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDINGS:
				return ((InternalEList<?>)getBindings()).basicRemove(otherEnd, msgs);
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
			case ApplicationPackageImpl.MODEL_COMPONENT__ELEMENT_ID:
				return getElementId();
			case ApplicationPackageImpl.MODEL_COMPONENT__TAGS:
				return getTags();
			case ApplicationPackageImpl.MODEL_COMPONENT__HANDLERS:
				return getHandlers();
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDING_TABLES:
				return getBindingTables();
			case ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT:
				return getRootContext();
			case ApplicationPackageImpl.MODEL_COMPONENT__POSITION_IN_PARENT:
				return getPositionInParent();
			case ApplicationPackageImpl.MODEL_COMPONENT__PARENT_ID:
				return getParentID();
			case ApplicationPackageImpl.MODEL_COMPONENT__CHILDREN:
				return getChildren();
			case ApplicationPackageImpl.MODEL_COMPONENT__COMMANDS:
				return getCommands();
			case ApplicationPackageImpl.MODEL_COMPONENT__PROCESSOR:
				return getProcessor();
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDINGS:
				return getBindings();
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
			case ApplicationPackageImpl.MODEL_COMPONENT__ELEMENT_ID:
				setElementId((String)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__TAGS:
				getTags().clear();
				getTags().addAll((Collection<? extends String>)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDING_TABLES:
				getBindingTables().clear();
				getBindingTables().addAll((Collection<? extends MBindingTable>)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT:
				setRootContext((MBindingContext)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__POSITION_IN_PARENT:
				setPositionInParent((String)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__PARENT_ID:
				setParentID((String)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends MUIElement>)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__COMMANDS:
				getCommands().clear();
				getCommands().addAll((Collection<? extends MCommand>)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__PROCESSOR:
				setProcessor((String)newValue);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDINGS:
				getBindings().clear();
				getBindings().addAll((Collection<? extends MKeyBinding>)newValue);
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
			case ApplicationPackageImpl.MODEL_COMPONENT__ELEMENT_ID:
				setElementId(ELEMENT_ID_EDEFAULT);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__TAGS:
				getTags().clear();
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__HANDLERS:
				getHandlers().clear();
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDING_TABLES:
				getBindingTables().clear();
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT:
				setRootContext((MBindingContext)null);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__POSITION_IN_PARENT:
				setPositionInParent(POSITION_IN_PARENT_EDEFAULT);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__PARENT_ID:
				setParentID(PARENT_ID_EDEFAULT);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__CHILDREN:
				getChildren().clear();
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__COMMANDS:
				getCommands().clear();
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__PROCESSOR:
				setProcessor(PROCESSOR_EDEFAULT);
				return;
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDINGS:
				getBindings().clear();
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
			case ApplicationPackageImpl.MODEL_COMPONENT__ELEMENT_ID:
				return ELEMENT_ID_EDEFAULT == null ? elementId != null : !ELEMENT_ID_EDEFAULT.equals(elementId);
			case ApplicationPackageImpl.MODEL_COMPONENT__TAGS:
				return tags != null && !tags.isEmpty();
			case ApplicationPackageImpl.MODEL_COMPONENT__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDING_TABLES:
				return bindingTables != null && !bindingTables.isEmpty();
			case ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT:
				return rootContext != null;
			case ApplicationPackageImpl.MODEL_COMPONENT__POSITION_IN_PARENT:
				return POSITION_IN_PARENT_EDEFAULT == null ? positionInParent != null : !POSITION_IN_PARENT_EDEFAULT.equals(positionInParent);
			case ApplicationPackageImpl.MODEL_COMPONENT__PARENT_ID:
				return PARENT_ID_EDEFAULT == null ? parentID != null : !PARENT_ID_EDEFAULT.equals(parentID);
			case ApplicationPackageImpl.MODEL_COMPONENT__CHILDREN:
				return children != null && !children.isEmpty();
			case ApplicationPackageImpl.MODEL_COMPONENT__COMMANDS:
				return commands != null && !commands.isEmpty();
			case ApplicationPackageImpl.MODEL_COMPONENT__PROCESSOR:
				return PROCESSOR_EDEFAULT == null ? processor != null : !PROCESSOR_EDEFAULT.equals(processor);
			case ApplicationPackageImpl.MODEL_COMPONENT__BINDINGS:
				return bindings != null && !bindings.isEmpty();
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
				case ApplicationPackageImpl.MODEL_COMPONENT__ELEMENT_ID: return ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;
				case ApplicationPackageImpl.MODEL_COMPONENT__TAGS: return ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.MODEL_COMPONENT__HANDLERS: return CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingTableContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.MODEL_COMPONENT__BINDING_TABLES: return CommandsPackageImpl.BINDING_TABLE_CONTAINER__BINDING_TABLES;
				case ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT: return CommandsPackageImpl.BINDING_TABLE_CONTAINER__ROOT_CONTEXT;
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
				case ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID: return ApplicationPackageImpl.MODEL_COMPONENT__ELEMENT_ID;
				case ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS: return ApplicationPackageImpl.MODEL_COMPONENT__TAGS;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS: return ApplicationPackageImpl.MODEL_COMPONENT__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingTableContainer.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.BINDING_TABLE_CONTAINER__BINDING_TABLES: return ApplicationPackageImpl.MODEL_COMPONENT__BINDING_TABLES;
				case CommandsPackageImpl.BINDING_TABLE_CONTAINER__ROOT_CONTEXT: return ApplicationPackageImpl.MODEL_COMPONENT__ROOT_CONTEXT;
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
		result.append(" (elementId: "); //$NON-NLS-1$
		result.append(elementId);
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
