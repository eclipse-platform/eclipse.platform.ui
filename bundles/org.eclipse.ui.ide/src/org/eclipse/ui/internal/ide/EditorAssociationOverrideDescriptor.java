/*******************************************************************************
 * Copyright (c) 2012, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug XXXXXX
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.ide.IEditorAssociationOverride;

/**
 * Describes a contribution to the 'org.eclipse.ui.ide.editorAssociationOverride' extension point.
 *
 * @since 3.8
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class EditorAssociationOverrideDescriptor {

	private static final String EDITOR_ASSOCIATION_OVERRIDE_EXTENSION_POINT= "org.eclipse.ui.ide.editorAssociationOverride"; //$NON-NLS-1$

	private static final String EDITOR_ASSOCIATION_OVERRIDE_ELEMENT= "editorAssociationOverride"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
	private static final String DESCRIPTION_ATTRIBUTE= "description"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$

	private final IConfigurationElement fElement;


	/**
	 * Returns descriptors for all editor association override extensions.
	 *
	 * @return an array with the contributed editor association overrides
	 */
	public static EditorAssociationOverrideDescriptor[] getContributedEditorAssociationOverrides() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IConfigurationElement[] elements= registry.getConfigurationElementsFor(EDITOR_ASSOCIATION_OVERRIDE_EXTENSION_POINT);
		return createDescriptors(elements);
	}

	/**
	 * Creates a new descriptor from the given configuration element.
	 *
	 * @param element the configuration element
	 */
	private EditorAssociationOverrideDescriptor(IConfigurationElement element) {
		Assert.isNotNull(element);
		fElement= element;
	}

	/**
	 * Creates a new {@link IEditorAssociationOverride}.
	 *
	 * @return the editor association override or <code>null</code> if the plug-in isn't loaded yet
	 * @throws CoreException if a failure occurred during creation
	 */
	public IEditorAssociationOverride createOverride() throws CoreException {
		final Throwable[] exception= new Throwable[1];
		final IEditorAssociationOverride[] result= new IEditorAssociationOverride[1];
		String message= MessageFormat.format(IDEWorkbenchMessages.editorAssociationOverride_error_couldNotCreate_message, getId(), fElement.getContributor().getName());
		ISafeRunnable code= new SafeRunnable(message) {
			/*
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			@Override
			public void run() throws Exception {
//				String pluginId = fElement.getContributor().getName();
				result[0]= (IEditorAssociationOverride)fElement.createExecutableExtension(CLASS_ATTRIBUTE);
			}
			/*
			 * @see org.eclipse.jface.util.SafeRunnable#handleException(java.lang.Throwable)
			 */
			@Override
			public void handleException(Throwable ex) {
				super.handleException(ex);
				exception[0]= ex;
			}

		};

		SafeRunner.run(code);

		if (exception[0] == null) {
			return result[0];
		}
		throw new CoreException(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.OK, message, exception[0]));

	}

	//---- XML Attribute accessors ---------------------------------------------

	/**
	 * Returns the editor association override's id.
	 *
	 * @return the editor association override's id
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}

	/**
	 * Returns the editor association override's name.
	 *
	 * @return the editor association override's name
	 */
	public String getName() {
		return fElement.getAttribute(NAME_ATTRIBUTE);
	}

	/**
	 * Returns the editor association override's description.
	 *
	 * @return the editor association override's description or <code>null</code> if not provided
	 */
	public String getDescription() {
		return fElement.getAttribute(DESCRIPTION_ATTRIBUTE);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass()) || getId() == null) {
			return false;
		}
		return getId().equals(((EditorAssociationOverrideDescriptor)obj).getId());
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	private static EditorAssociationOverrideDescriptor[] createDescriptors(IConfigurationElement[] elements) {
		List<EditorAssociationOverrideDescriptor> result = new ArrayList<>(elements.length);
		for (IConfigurationElement configElement : elements) {
			if (EDITOR_ASSOCIATION_OVERRIDE_ELEMENT.equals(configElement.getName())) {
				EditorAssociationOverrideDescriptor desc= new EditorAssociationOverrideDescriptor(configElement);
				result.add(desc);
			} else {
				String message= MessageFormat.format(IDEWorkbenchMessages.editorAssociationOverride_error_invalidElementName_message,
						configElement.getContributor().getName(), configElement.getName());
				IDEWorkbenchPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.OK, message, null));
			}
		}
		return result.toArray(new EditorAssociationOverrideDescriptor[result.size()]);
	}

}
