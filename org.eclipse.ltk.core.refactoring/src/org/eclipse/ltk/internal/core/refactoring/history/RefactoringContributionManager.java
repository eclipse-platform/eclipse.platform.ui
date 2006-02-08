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

import org.eclipse.ltk.core.refactoring.IRefactoringContribution;
import org.eclipse.ltk.core.refactoring.IRefactoringContributionManager;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Contribution manager for refactorings.
 * 
 * @since 3.2
 */
public final class RefactoringContributionManager implements IRegistryChangeListener, IRefactoringContributionManager {

	/** The class attribute */
	private static final String ATTRIBUTE_CLASS= "class"; //$NON-NLS-1$

	/** The id attribute */
	private static final String ATTRIBUTE_ID= "id"; //$NON-NLS-1$

	/** The singleton instance */
	private static RefactoringContributionManager fInstance= null;

	/** The refactoring contributions extension point */
	private static final String REFACTORING_CONTRIBUTIONS_EXTENSION_POINT= "refactoringContributions"; //$NON-NLS-1$

	/**
	 * Returns the singleton instance of the refactoring contribution manager.
	 * 
	 * @return the singleton instance
	 */
	public static RefactoringContributionManager getInstance() {
		if (fInstance == null)
			fInstance= new RefactoringContributionManager();
		return fInstance;
	}

	/**
	 * The refactoring contribution cache (element type: &lt;String,
	 * <code>IRefactoringContribution&gt;</code>)
	 */
	private Map fContributionCache= null;

	/**
	 * Creates a new refactoring contribution manager.
	 */
	private RefactoringContributionManager() {
		// Not instantiatable
	}

	/**
	 * Connects this manager to the platform's extension registry.
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
			final IRefactoringContribution contribution= getRefactoringContribution(id);
			if (contribution != null)
				return contribution.createArguments(descriptor);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringDescriptor createDescriptor(final String id, final String project, final String description, final String comment, final Map arguments, final int flags) {
		Assert.isNotNull(id);
		Assert.isNotNull(description);
		Assert.isNotNull(arguments);
		Assert.isLegal(flags >= RefactoringDescriptor.NONE);
		final IRefactoringContribution contribution= getRefactoringContribution(id);
		if (contribution != null)
			return contribution.createDescriptor(id, project, description, comment, arguments, flags);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Refactoring createRefactoring(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final String id= descriptor.getID();
		if (id != null) {
			final IRefactoringContribution contribution= getRefactoringContribution(id);
			if (contribution != null)
				return contribution.createRefactoring(descriptor);
		}
		return null;
	}

	/**
	 * Disconnects this manager from the platform's extension registry.
	 */
	public void disconnect() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public IRefactoringContribution getRefactoringContribution(final String id) {
		Assert.isNotNull(id);
		Assert.isTrue(!"".equals(id)); //$NON-NLS-1$
		if (fContributionCache == null) {
			fContributionCache= new HashMap();
			final IConfigurationElement[] elements= Platform.getExtensionRegistry().getConfigurationElementsFor(RefactoringCore.ID_PLUGIN, REFACTORING_CONTRIBUTIONS_EXTENSION_POINT);
			for (int index= 0; index < elements.length; index++) {
				final IConfigurationElement element= elements[index];
				final String attributeId= element.getAttribute(ATTRIBUTE_ID);
				final String point= RefactoringCore.ID_PLUGIN + "." + REFACTORING_CONTRIBUTIONS_EXTENSION_POINT; //$NON-NLS-1$
				if (attributeId != null && !"".equals(attributeId)) { //$NON-NLS-1$
					final String className= element.getAttribute(ATTRIBUTE_CLASS);
					if (className != null && !"".equals(className)) { //$NON-NLS-1$
						try {
							final Object implementation= element.createExecutableExtension(ATTRIBUTE_CLASS);
							if (implementation instanceof IRefactoringContribution) {
								if (fContributionCache.get(attributeId) != null)
									RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages.RefactoringCorePlugin_duplicate_warning, new String[] { attributeId, point}));
								fContributionCache.put(attributeId, implementation);
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
		return (IRefactoringContribution) fContributionCache.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registryChanged(final IRegistryChangeEvent event) {
		fContributionCache= null;
	}
}