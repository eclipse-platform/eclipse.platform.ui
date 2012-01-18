/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

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

import com.ibm.icu.text.MessageFormat;


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

	private IConfigurationElement fElement;


	/**
	 * Returns descriptors for all editor association override extensions.
	 * 
	 * @return an array with the contributed editor association overrides
	 */
	public static EditorAssociationOverrideDescriptor[] getContributedEditorAssociationOverrides() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IConfigurationElement[] elements= registry.getConfigurationElementsFor(EDITOR_ASSOCIATION_OVERRIDE_EXTENSION_POINT);
		EditorAssociationOverrideDescriptor[] editorAssociationOverrideDescs= createDescriptors(elements);
		return editorAssociationOverrideDescs;
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
		String message= MessageFormat.format(IDEWorkbenchMessages.editorAssociationOverride_error_couldNotCreate_message, new String[] { getId(), fElement.getContributor().getName() });
		ISafeRunnable code= new SafeRunnable(message) {
			/*
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			public void run() throws Exception {
//		 		String pluginId = fElement.getContributor().getName();
				result[0]= (IEditorAssociationOverride)fElement.createExecutableExtension(CLASS_ATTRIBUTE);
			}
			/*
			 * @see org.eclipse.jface.util.SafeRunnable#handleException(java.lang.Throwable)
			 */
			public void handleException(Throwable ex) {
				super.handleException(ex);
				exception[0]= ex;
			}

		};

		SafeRunner.run(code);

		if (exception[0] == null)
			return result[0];
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

	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass()) || getId() == null)
			return false;
		return getId().equals(((EditorAssociationOverrideDescriptor)obj).getId());
	}

	public int hashCode() {
		return getId().hashCode();
	}

	private static EditorAssociationOverrideDescriptor[] createDescriptors(IConfigurationElement[] elements) {
		List result= new ArrayList(elements.length);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (EDITOR_ASSOCIATION_OVERRIDE_ELEMENT.equals(element.getName())) {
				EditorAssociationOverrideDescriptor desc= new EditorAssociationOverrideDescriptor(element);
				result.add(desc);
			} else {
				String message= MessageFormat.format(IDEWorkbenchMessages.editorAssociationOverride_error_invalidElementName_message,
						new String[] { element.getContributor().getName(), element.getName() });
				IDEWorkbenchPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.OK, message, null));
			}
		}
		return (EditorAssociationOverrideDescriptor[])result.toArray(new EditorAssociationOverrideDescriptor[result.size()]);
	}

}
