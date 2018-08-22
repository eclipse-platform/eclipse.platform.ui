/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Internal annotation super type hierarchy cache.
 * TODO this cache is currently unbound, i.e. only limited by the number of annotation types
 *
 * @since 3.0
 */
public final class AnnotationTypeHierarchy {

	private Map<String, String> fTypeMap;
	private Map<String, AnnotationType> fTypesCache= new HashMap<>();

	public AnnotationTypeHierarchy() {
	}

	public AnnotationType getAnnotationType(String typeName) {
		AnnotationType type= fTypesCache.get(typeName);
		if (type == null) {
			String[] superTypes= computeSuperTypes(typeName);
			type= new AnnotationType(typeName, superTypes);
			fTypesCache.put(typeName, type);
		}
		return type;
	}

	public boolean isSubtype(String superType, String subtypeCandidate) {
		AnnotationType type= getAnnotationType(subtypeCandidate);
		return type.isSubtype(superType);
	}

	private String[] computeSuperTypes(String typeName) {
		ArrayList<String> types= new ArrayList<>();
		append(types, getDirectSuperType(typeName));
		int index= 0;
		while (index < types.size()) {
			String type= types.get(index++);
			append(types, getDirectSuperType(type));
		}

		String[] superTypes= new String[types.size()];
		types.toArray(superTypes);
		return superTypes;
	}

	private String getDirectSuperType(String typeName) {
		return getTypeMap().get(typeName);
	}

	private void append(List<String> list, String string) {
		if (string == null || string.trim().length() == 0)
			return;

		if (!list.contains(string))
			list.add(string);
	}

	private Map<String, String> getTypeMap() {
		if (fTypeMap == null)
			fTypeMap= readTypes();
		return fTypeMap;
	}

	private Map<String, String> readTypes() {
		HashMap<String, String> allTypes= new HashMap<>();

		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(EditorsUI.PLUGIN_ID, "annotationTypes"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
			for (int i= 0; i < elements.length; i++) {
				IConfigurationElement element= elements[i];

				String name= element.getAttribute("name");  //$NON-NLS-1$
				if (name == null || name.trim().length() == 0)
					continue;

				String parent= element.getAttribute("super");  //$NON-NLS-1$
				if (parent == null || parent.trim().length() == 0)
					parent= ""; //$NON-NLS-1$

				allTypes.put(name, parent);
			}
		}

		return allTypes;
	}
}
