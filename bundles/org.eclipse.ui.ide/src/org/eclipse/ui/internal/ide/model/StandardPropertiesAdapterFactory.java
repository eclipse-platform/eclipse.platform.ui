/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 23862
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;

/**
 * Dispenses an <code>IPropertySource</code> adapter for the core resource objects.
 */
public class StandardPropertiesAdapterFactory implements IAdapterFactory {

	@Override
	public <T> T getAdapter(Object o, Class<T> adapterType) {
		if (adapterType.isInstance(o)) {
			return adapterType.cast(o);
		}
		if (adapterType == IPropertySource.class) {
			if (o instanceof IResource) {
				IResource resource = (IResource) o;
				if (resource.getType() == IResource.FILE) {
					return adapterType.cast(new FilePropertySource((IFile) o));
				}
				return adapterType.cast(new ResourcePropertySource((IResource) o));
			} else if (o instanceof IEditorPart) {
				IEditorInput input = ((IEditorPart) o).getEditorInput();
				if (input instanceof IFileEditorInput) {
					return adapterType.cast(new FilePropertySource(((IFileEditorInput) input).getFile()));
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		// org.eclipe.ui.views is an optional dependency
		try {
			Class.forName("org.eclipse.ui.views.properties.IPropertySource"); //$NON-NLS-1$
		} catch(ClassNotFoundException e) {
			return new Class[0];
		}
		return new Class[] { IPropertySource.class };
	}
}
