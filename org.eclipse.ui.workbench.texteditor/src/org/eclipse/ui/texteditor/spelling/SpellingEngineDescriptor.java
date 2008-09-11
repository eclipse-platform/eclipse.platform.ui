/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.spelling;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.internal.texteditor.spelling.EmptySpellingPreferenceBlock;

/**
 * Describes an extension to the <code>spellingEngine</code> extension point.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SpellingEngineDescriptor {

	/** Name of the <code>label</code> attribute. */
	private static final String LABEL_ATTRIBUTE= "label"; //$NON-NLS-1$
	/** Name of the <code>class</code> attribute. */
	private static final String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	/** Name of the <code>id</code> attribute. */
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	/** Name of the <code>default</code> attribute. */
	private static final String DEFAULT_ATTRIBUTE= "default"; //$NON-NLS-1$
	/** Name of the <code>preferencesClass</code> attribute. */
	private static final String PREFERENCES_CLASS_ATTRIBUTE= "preferencesClass"; //$NON-NLS-1$

	/** The configuration element describing this extension. */
	private IConfigurationElement fConfiguration;
	/** The value of the <code>label</code> attribute, if read. */
	private String fLabel;
	/** The value of the <code>id</code> attribute, if read. */
	private String fId;
	/** The value of the <code>default</code> attribute, if read. */
	private Boolean fDefault;
	/** The bundle where this extension was defined. */
	private Bundle fBundle;
	/** <code>true</code> iff a preferences class has been specified */
	private Boolean fHasPreferences;

	/**
	 * Creates a new descriptor for <code>element</code>.
	 * <p>
	 * This method is for internal use only.
	 * </p>
	 *
	 * @param element the extension point element to be described.
	 */
	public SpellingEngineDescriptor(IConfigurationElement element) {
		Assert.isLegal(element != null);
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
	 * Creates a spelling engine as described in the extension's xml.
	 *
	 * @return the created spelling engine
	 * @throws CoreException if the creation failed
	 */
	public ISpellingEngine createEngine() throws CoreException {
		return (ISpellingEngine)fConfiguration.createExecutableExtension(CLASS_ATTRIBUTE);
	}

	/**
	 * Returns <code>true</code> iff a preferences class has been
	 * specified for this engine.
	 *
	 * @return <code>true</code> iff a preferences class has been
	 *         specified for this engine
	 */
	private boolean hasPreferences() {
		if (fHasPreferences == null)
			if (fConfiguration.getAttribute(PREFERENCES_CLASS_ATTRIBUTE) == null)
				fHasPreferences= Boolean.FALSE;
			else
				fHasPreferences= Boolean.TRUE;
		return fHasPreferences.booleanValue();
	}

	/**
	 * Creates a spelling preferences block as described in the extension's xml.
	 *
	 * @return the created spelling preferences block
	 * @throws CoreException if the creation failed
	 */
	public ISpellingPreferenceBlock createPreferences() throws CoreException {
		if (hasPreferences())
			return (ISpellingPreferenceBlock) fConfiguration.createExecutableExtension(PREFERENCES_CLASS_ATTRIBUTE);
		return new EmptySpellingPreferenceBlock();
	}

	/**
	 * States whether the plug-in declaring this extension has been loaded already.
	 *
	 * @return <code>true</code> if the extension point's plug-in has been loaded, <code>false</code> otherwise.
	 */
	public boolean isPluginLoaded() {
		if (fBundle == null)
			fBundle= Platform.getBundle(fConfiguration.getContributor().getName());
		return (fBundle != null && fBundle.getState() == Bundle.ACTIVE);
	}

	/**
	 * Reads (if needed) and returns the default attribute value of this extension.
	 *
	 * @return the default attribute value for this extension.
	 */
	public boolean isDefault() {
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
