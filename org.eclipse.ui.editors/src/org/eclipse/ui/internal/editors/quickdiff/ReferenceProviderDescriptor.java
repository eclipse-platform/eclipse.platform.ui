/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.quickdiff;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.jface.util.Assert;

import org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation;


/**
 * Describes an extension to the <code>quickdiff.referenceprovider</code> extension point.
 * 
 * @since 3.0
 * @see ReferenceSelectionAction
 * @see QuickDiffTestPlugin
 */
public class ReferenceProviderDescriptor {
	
	/** Name of the <code>label</code> attribute. */
	private static final String LABEL_ATTRIBUTE= "label"; //$NON-NLS-1$
	/** Name of the <code>class</code> attribute. */
	private static final String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	/** Name of the <code>id</code> attribute. */
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	/** Name of the <code>default</code> attribute. */
	private static final String DEFAULT_ATTRIBUTE= "default"; //$NON-NLS-1$

	/** The configuration element describing this extension. */
	private IConfigurationElement fConfiguration;
	/** The value of the <code>label</code> attribute, if read. */
	private String fLabel;
	/** The value of the <code>id</code> attribute, if read. */
	private String fId;
	/** The value of the <code>default</code> attribute, if read. */
	private Boolean fDefault;
	/** States whether the extension has been loaded. */
	private boolean fLoaded;

	/**
	 * Creates a new descriptor for <code>element</code>.
	 * 
	 * @param element the extension point element to be described.
	 */
	public ReferenceProviderDescriptor(IConfigurationElement element) {
		fConfiguration= element;
	}

	/**
	 * Reads (if needed) and returns the label of this extension.
	 * 
	 * @return the label for this extension.
	 */
	public String getLabel() {
		if (fLabel == null) {
			fLabel= fConfiguration.getAttribute(LABEL_ATTRIBUTE);
			Assert.isNotNull(fLabel);
		}
		return fLabel;
	}

	/**
	 * Reads (if needed) and returns the id of this extension.
	 * 
	 * @return the id for this extension.
	 */
	public String getId() {
		if (fId == null) {
			fId= fConfiguration.getAttribute(ID_ATTRIBUTE);
			Assert.isNotNull(fId);
		}
		return fId;
	}

	/**
	 * Creates a referenceprovider as described in the extension's xml. Sets the id on the provider.
	 * @return a new instance of the reference provider described by this descriptor.
	 */
	public IQuickDiffProviderImplementation createProvider() {
		try {
			IQuickDiffProviderImplementation impl= (IQuickDiffProviderImplementation)fConfiguration.createExecutableExtension(CLASS_ATTRIBUTE);
			impl.setId(getId());
			fLoaded= true;
			return impl;
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * States whether this extension has been loaded already.
	 * 
	 * @return <code>true</code> if the extension point has been loaded, <code>false</code> otherwise.
	 */
	public boolean isPluginLoaded() {
		return fLoaded;
	}

	/**
	 * Reads (if needed) and returns the default attribute value of this extension.
	 * 
	 * @return the default attribute value for this extension.
	 */
	public boolean getDefault() {
		if (fDefault == null) {
			String def= fConfiguration.getAttribute(DEFAULT_ATTRIBUTE);
			if ("true".equalsIgnoreCase(def)) //$NON-NLS-1$
				fDefault= Boolean.TRUE;
			else
				fDefault= Boolean.FALSE;
		}
		return fDefault.booleanValue();
	}

}
