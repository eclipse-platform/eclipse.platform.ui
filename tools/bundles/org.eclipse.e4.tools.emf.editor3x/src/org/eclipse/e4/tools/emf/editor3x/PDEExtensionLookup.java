/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.tools.emf.ui.common.IExtensionLookup;
import org.eclipse.pde.internal.core.PDEExtensionRegistry;

@SuppressWarnings("restriction")
public class PDEExtensionLookup implements IExtensionLookup {

	@Override
	public IExtension[] findExtensions(String extensionPointId, boolean liveModel) {
		if (liveModel) {
			final IExtensionRegistry registry = RegistryFactory.getRegistry();
			return registry.getExtensionPoint(extensionPointId).getExtensions();
		}
		final PDEExtensionRegistry reg = new PDEExtensionRegistry();
		return reg.findExtensions(extensionPointId, true);
	}

}