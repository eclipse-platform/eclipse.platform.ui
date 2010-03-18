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
package org.eclipse.e4.ui.model.application.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MModelComponent;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.MModelComponent} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelComponentItemProvider
	extends PartDescriptorContainerItemProvider
	implements
		IEditingDomainItemProvider,
		IStructuredItemContentProvider,
		ITreeItemContentProvider,
		IItemLabelProvider,
		IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelComponentItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addIdPropertyDescriptor(object);
			addTagsPropertyDescriptor(object);
			addPositionInParentPropertyDescriptor(object);
			addParentIDPropertyDescriptor(object);
			addProcessorPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Id feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addIdPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ApplicationElement_id_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ApplicationElement_id_feature", "_UI_ApplicationElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.APPLICATION_ELEMENT__ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Tags feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addTagsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ApplicationElement_tags_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ApplicationElement_tags_feature", "_UI_ApplicationElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.APPLICATION_ELEMENT__TAGS,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Position In Parent feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addPositionInParentPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ModelComponent_positionInParent_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ModelComponent_positionInParent_feature", "_UI_ModelComponent_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.MODEL_COMPONENT__POSITION_IN_PARENT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Parent ID feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addParentIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ModelComponent_parentID_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ModelComponent_parentID_feature", "_UI_ModelComponent_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.MODEL_COMPONENT__PARENT_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Processor feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addProcessorPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ModelComponent_processor_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ModelComponent_processor_feature", "_UI_ModelComponent_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.MODEL_COMPONENT__PROCESSOR,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS);
			childrenFeatures.add(MApplicationPackage.Literals.BINDING_CONTAINER__BINDING_TABLES);
			childrenFeatures.add(MApplicationPackage.Literals.BINDING_CONTAINER__ROOT_CONTEXT);
			childrenFeatures.add(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN);
			childrenFeatures.add(MApplicationPackage.Literals.MODEL_COMPONENT__COMMANDS);
			childrenFeatures.add(MApplicationPackage.Literals.MODEL_COMPONENT__BINDINGS);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns ModelComponent.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/ModelComponent")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((MModelComponent)object).getId();
		return label == null || label.length() == 0 ?
			getString("_UI_ModelComponent_type") : //$NON-NLS-1$
			getString("_UI_ModelComponent_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(MModelComponent.class)) {
			case MApplicationPackage.MODEL_COMPONENT__ID:
			case MApplicationPackage.MODEL_COMPONENT__TAGS:
			case MApplicationPackage.MODEL_COMPONENT__POSITION_IN_PARENT:
			case MApplicationPackage.MODEL_COMPONENT__PARENT_ID:
			case MApplicationPackage.MODEL_COMPONENT__PROCESSOR:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case MApplicationPackage.MODEL_COMPONENT__HANDLERS:
			case MApplicationPackage.MODEL_COMPONENT__BINDING_TABLES:
			case MApplicationPackage.MODEL_COMPONENT__ROOT_CONTEXT:
			case MApplicationPackage.MODEL_COMPONENT__CHILDREN:
			case MApplicationPackage.MODEL_COMPONENT__COMMANDS:
			case MApplicationPackage.MODEL_COMPONENT__BINDINGS:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
	 * that can be created under this object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS,
				 MApplicationFactory.eINSTANCE.createHandler()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.BINDING_CONTAINER__BINDING_TABLES,
				 MApplicationFactory.eINSTANCE.createBindingTable()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.BINDING_CONTAINER__ROOT_CONTEXT,
				 MApplicationFactory.eINSTANCE.createBindingContext()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createInputPart()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartDescriptor()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createWindowTrim()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN,
				 MApplicationFactory.eINSTANCE.createTestHarness()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__COMMANDS,
				 MApplicationFactory.eINSTANCE.createCommand()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__COMMANDS,
				 MApplicationFactory.eINSTANCE.createTestHarness()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.MODEL_COMPONENT__BINDINGS,
				 MApplicationFactory.eINSTANCE.createKeyBinding()));
	}

	/**
	 * This returns the label text for {@link org.eclipse.emf.edit.command.CreateChildCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCreateChildText(Object owner, Object feature, Object child, Collection<?> selection) {
		Object childFeature = feature;
		Object childObject = child;

		boolean qualify =
			childFeature == MApplicationPackage.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS ||
			childFeature == MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN ||
			childFeature == MApplicationPackage.Literals.MODEL_COMPONENT__COMMANDS;

		if (qualify) {
			return getString
				("_UI_CreateChild_text2", //$NON-NLS-1$
				 new Object[] { getTypeText(childObject), getFeatureText(childFeature), getTypeText(owner) });
		}
		return super.getCreateChildText(owner, feature, child, selection);
	}

}
