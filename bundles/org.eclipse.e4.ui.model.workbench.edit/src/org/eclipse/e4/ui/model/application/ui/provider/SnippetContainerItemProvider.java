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
package org.eclipse.e4.ui.model.application.ui.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplicationFactory;

import org.eclipse.e4.ui.model.application.provider.UIElementsEditPlugin;

import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;

import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;

import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;

import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;

import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.ui.MSnippetContainer} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class SnippetContainerItemProvider
	extends ItemProviderAdapter
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
	public SnippetContainerItemProvider(AdapterFactory adapterFactory) {
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

		}
		return itemPropertyDescriptors;
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
			childrenFeatures.add(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS);
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
	 * This returns SnippetContainer.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/SnippetContainer")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		return getString("_UI_SnippetContainer_type"); //$NON-NLS-1$
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

		switch (notification.getFeatureID(MSnippetContainer.class)) {
			case UiPackageImpl.SNIPPET_CONTAINER__SNIPPETS:
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
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MApplicationFactory.INSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenuContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createPopupMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolControl()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBarSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBarContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createTrimContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createInputPart()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createTrimmedWindow()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createTrimBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createArea()));
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return UIElementsEditPlugin.INSTANCE;
	}

}
