/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.swt.widgets.Composite;

public class XMIFileEditor {
	private ApplicationModelEditor editor;
	
	@Inject
	public XMIFileEditor(Composite composite, final MPart part) {
		final XMIModelResource resource = new XMIModelResource(part.getPersistedState());
		resource.addModelListener(new ModelListener() {
			
			public void dirtyChanged() {
				part.setDirty(resource.isDirty());
			}
		});
		editor = new ApplicationModelEditor(composite, resource);
	}
	
	public void doSave(@Optional IProgressMonitor monitor) {
		System.err.println("We are saving");
		IStatus status = editor.save();
		if( ! status.isOK() ) {
			System.err.println("Saving failed");
		}
	}
	
}
