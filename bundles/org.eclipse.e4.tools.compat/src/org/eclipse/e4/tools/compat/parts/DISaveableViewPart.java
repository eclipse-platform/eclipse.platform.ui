/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 421453
 ******************************************************************************/
package org.eclipse.e4.tools.compat.parts;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

public abstract class DISaveableViewPart<C> extends DIViewPart<C> implements ISaveablePart2, IDirtyProviderService {
	private boolean dirtyState;

	public DISaveableViewPart(Class<C> clazz) {
		super(clazz);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IEclipseContext saveContext = getContext().createChild();
		ContextInjectionFactory.invoke(getComponent(), Persist.class, saveContext);
		saveContext.dispose();
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setDirtyState(boolean dirtyState) {
		if( dirtyState != this.dirtyState ) {
			this.dirtyState = dirtyState;
			firePropertyChange(PROP_DIRTY);
		}
	}

	@Override
	public boolean isDirty() {
		return dirtyState;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		getContext().declareModifiable(IDirtyProviderService.class);
		getContext().set(IDirtyProviderService.class, this);
	}
}