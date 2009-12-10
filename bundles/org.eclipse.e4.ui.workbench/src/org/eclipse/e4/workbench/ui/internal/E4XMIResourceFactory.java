/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class E4XMIResourceFactory extends XMIResourceFactoryImpl {

	@Override
	public Resource createResource(URI uri) {
		return new XMIResourceImpl(uri) {

			private int count = 0;

			@Override
			protected boolean useIDs() {
				return true;
			}

			@Override
			public void setID(EObject eObject, String id) {
				if (eObject instanceof MKeyBinding) {
					if (id != null) {
						super.setID(eObject, id);
					}
				} else {
					super.setID(eObject, id);
				}
			}

			@Override
			public String getID(EObject eObject) {
				String id = super.getID(eObject);
				if (id != null) {
					return id;
				}

				if (eObject instanceof MApplicationElement) {
					MApplicationElement element = (MApplicationElement) eObject;
					id = element.getId();
					if (id != null) {
						setID((EObject) element, id);
						return id;
					}

					id = "element." + count; //$NON-NLS-1$
					element.setId(id);
					setID((EObject) element, id);
					count++;
					return id;
				}

				MKeyBinding keyBinding = (MKeyBinding) eObject;
				id = "element." + count; //$NON-NLS-1$
				setID((EObject) keyBinding, id);
				count++;
				return id;
			}
		};
	}
}
