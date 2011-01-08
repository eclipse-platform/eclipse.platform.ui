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

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.swt.widgets.Composite;

public class ApplicationModelEditor extends ModelEditor {
	@Inject
	public ApplicationModelEditor(Composite composite, IEclipseContext context, IModelResource modelProvider, @Named("org.eclipse.e4.tools.emf.ui.editorproject") @Optional IProject project, IResourcePool resourcePool) {
		super(composite, context, modelProvider, project, resourcePool);
	}
}
