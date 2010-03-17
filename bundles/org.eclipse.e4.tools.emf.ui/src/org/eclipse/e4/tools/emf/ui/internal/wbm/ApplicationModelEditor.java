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
package org.eclipse.e4.tools.emf.ui.internal.wbm;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.swt.widgets.Composite;

public class ApplicationModelEditor extends ModelEditor {
	public ApplicationModelEditor(Composite composite,
			IModelResource modelProvider, IProject project) {
		super(composite, modelProvider, project);
	}

	
}
