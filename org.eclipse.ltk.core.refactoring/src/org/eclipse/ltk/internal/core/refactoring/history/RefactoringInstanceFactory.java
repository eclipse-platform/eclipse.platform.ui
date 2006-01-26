/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ltk.core.refactoring.IRefactoringInstanceCreator;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Factory class to create refactoring instances from refactoring descriptors.
 * 
 * @since 3.2
 */
public final class RefactoringInstanceFactory implements IRegistryChangeListener, IRefactoringInstanceCreator {

	/** The class attribute */
	private static final String ATTRIBUTE_CLASS= "class"; //$NON-NLS-1$

	/** The id attribute */
	private static final String ATTRIBUTE_ID= "id"; //$NON-NLS-1$

	/** The singleton instance */
	private static RefactoringInstanceFactory fInstance= null;

	/** The refactoring creators extension point */
	private static final String REFACTORING_CREATORS_EXTENSION_POINT= "refactoringCreators"; //$NON-NLS-1$

	/**
	 * Returns the singleton instance of the refactoring instance factory.
	 * 
	 * @return the singleton instance
	 */
	public static RefactoringInstanceFactory getInstance() {
		if (fInstance == null)
			fInstance= new RefactoringInstanceFactory();
		return fInstance;
	}

	/**
	 * The refactoring creator cache (element type: &lt;String,
	 * <code>IRefactoringInstanceCreator&gt;</code>)
	 */
	private Map fCreatorCache= null;

	/**
	 * Creates a new refactoring instance factory.
	 */
	private RefactoringInstanceFactory() {
		// Not instantiatable
	}

	/**
	 * Connects this factory to the platform's extension registry.
	 */
	public void connect() {
		Platform.getExtensionRegistry().addRegistryChangeListener(this, RefactoringCore.ID_PLUGIN);
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringArguments createArguments(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		final String id= descriptor.getID();
		if (id != null) {
			final IRefactoringInstanceCreator creator= createRefactoringCreator(id);
			if (creator != null)
				return creator.createArguments(descriptor);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Refactoring createRefactoring(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final String id= descriptor.getID();
		if (id != null) {
			final IRefactoringInstanceCreator creator= createRefactoringCreator(id);
			if (creator != null)
				return creator.createRefactoring(descriptor);
		}
		return null;
	}

	/**
	 * Creates a refactoring instance creator for the specified id.
	 * 
	 * @param id
	 *            the refactoring id
	 * @return the refactoring instance creator, or <code>null</code>
	 */
	private IRefactoringInstanceCreator createRefactoringCreator(final String id) {
		Assert.isNotNull(id);
		Assert.isTrue(!"".equals(id)); //$NON-NLS-1$
		if (fCreatorCache == null) {
			fCreatorCache= new HashMap();
			final IConfigurationElement[] elements= Platform.getExtensionRegistry().getConfigurationElementsFor(RefactoringCore.ID_PLUGIN, REFACTORING_CREATORS_EXTENSION_POINT);
			for (int index= 0; index < elements.length; index++) {
				final IConfigurationElement element= elements[index];
				final String attributeId= element.getAttribute(ATTRIBUTE_ID);
				final String point= RefactoringCore.ID_PLUGIN + "." + REFACTORING_CREATORS_EXTENSION_POINT; //$NON-NLS-1$
				if (attributeId != null && !"".equals(attributeId)) { //$NON-NLS-1$
					final String className= element.getAttribute(ATTRIBUTE_CLASS);
					if (className != null && !"".equals(className)) { //$NON-NLS-1$
						try {
							final Object implementation= element.createExecutableExtension(ATTRIBUTE_CLASS);
							if (implementation instanceof IRefactoringInstanceCreator) {
								if (fCreatorCache.get(attributeId) != null)
									RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages.RefactoringCorePlugin_duplicate_warning, new String[] { attributeId, point}));
								fCreatorCache.put(attributeId, implementation);
							} else
								RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages.RefactoringCorePlugin_creation_error, new String[] { point, attributeId}));
						} catch (CoreException exception) {
							RefactoringCorePlugin.log(exception);
						}
					} else
						RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages.RefactoringCorePlugin_missing_class_attribute, new String[] { point, attributeId, ATTRIBUTE_CLASS}));
				} else
					RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages.RefactoringCorePlugin_missing_attribute, new String[] { point, ATTRIBUTE_ID}));
			}
		}
		return (IRefactoringInstanceCreator) fCreatorCache.get(id);
	}

	/**
	 * Disconnects this factory from the platform's extensionr registry.
	 */
	public void disconnect() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registryChanged(final IRegistryChangeEvent event) {
		fCreatorCache= null;
	}
}