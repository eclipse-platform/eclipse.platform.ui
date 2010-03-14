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
import org.eclipse.e4.ui.model.application.MToolItem;

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
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.MToolItem} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ToolItemItemProvider
	extends ItemItemProvider
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
	public ToolItemItemProvider(AdapterFactory adapterFactory) {
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

			addSelectedElementPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Selected Element feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSelectedElementPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ElementContainer_selectedElement_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ElementContainer_selectedElement_feature", "_UI_ElementContainer_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.ELEMENT_CONTAINER__SELECTED_ELEMENT,
				 true,
				 false,
				 true,
				 null,
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
			childrenFeatures.add(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN);
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
	 * This returns ToolItem.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/ToolItem")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((MToolItem)object).getLabel();
		return label == null || label.length() == 0 ?
			getString("_UI_ToolItem_type") : //$NON-NLS-1$
			getString("_UI_ToolItem_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(MToolItem.class)) {
			case MApplicationPackage.TOOL_ITEM__CHILDREN:
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
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createInputPart()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartDescriptor()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createWindowTrim()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createTestHarness()));
	}

}
