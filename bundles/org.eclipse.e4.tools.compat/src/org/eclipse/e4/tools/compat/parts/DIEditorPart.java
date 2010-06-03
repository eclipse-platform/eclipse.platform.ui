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
package org.eclipse.e4.tools.compat.parts;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.compat.internal.PartHelper;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public abstract class DIEditorPart<C> extends EditorPart implements IDirtyProviderService {
	private IEclipseContext context;
	private C component;
	private Class<C> clazz;
	private boolean dirtyState;
	
	public DIEditorPart(Class<C> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		IEclipseContext saveContext = context.createChild();
		ContextInjectionFactory.invoke(component, Persist.class, saveContext);
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
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		
		context = PartHelper.createPartContext(this);
		context.declareModifiable(IEditorInput.class);
		context.declareModifiable(IEditorPart.class);
		context.declareModifiable(IDirtyProviderService.class);
		
		context.set(IEditorPart.class,this);
		context.set(IDirtyProviderService.class,this);
		context.set(IEditorInput.class, input);
	}

	
	@Override
	public void createPartControl(Composite parent) {
		component = PartHelper.creatComponent(parent, context, clazz, this);
	}
	
	public C getComponent() {
		return component;
	}
	
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
	
//FIXME Once we have an @Focus we can implement it
//	@Override
//	public void setFocus() {
//		// TODO Auto-generated method stub
//		
//	}
	
	@Override
	public void dispose() {
		context.dispose();
		context = null;
		super.dispose();
	}
}
