/**
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.fragment.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplicationFactory;

import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;

import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;

import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;

import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragments;

import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;

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
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.fragment.MModelFragments} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelFragmentsItemProvider
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
	public ModelFragmentsItemProvider(AdapterFactory adapterFactory) {
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
			childrenFeatures.add(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS);
			childrenFeatures.add(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS);
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
	 * This returns ModelFragments.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/ModelFragments")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		return getString("_UI_ModelFragments_type"); //$NON-NLS-1$
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

		switch (notification.getFeatureID(MModelFragments.class)) {
			case FragmentPackageImpl.MODEL_FRAGMENTS__IMPORTS:
			case FragmentPackageImpl.MODEL_FRAGMENTS__FRAGMENTS:
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
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MApplicationFactory.INSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MApplicationFactory.INSTANCE.createAddon()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createBindingContext()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createBindingTable()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createCommand()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createCommandParameter()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createHandler()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createKeyBinding()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createParameter()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createCategory()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createMenuContribution()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createPopupMenu()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createToolControl()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createToolBarSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createRenderedMenu()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createRenderedToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createToolBarContribution()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createTrimContribution()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createRenderedMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createInputPart()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createTrimmedWindow()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createTrimBar()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MAdvancedFactory.INSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MAdvancedFactory.INSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MAdvancedFactory.INSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 MAdvancedFactory.INSTANCE.createArea()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS,
				 org.eclipse.e4.ui.model.application.descriptor.basic.MBasicFactory.INSTANCE.createPartDescriptor()));

		newChildDescriptors.add
			(createChildParameter
				(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS,
				 MFragmentFactory.INSTANCE.createStringModelFragment()));
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return ModelFragmentEditPlugin.INSTANCE;
	}

}
