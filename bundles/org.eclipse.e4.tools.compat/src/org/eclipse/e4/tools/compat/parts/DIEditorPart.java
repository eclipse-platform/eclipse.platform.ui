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

import java.lang.reflect.Method;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.compat.internal.Util;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
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
	
//FIXME once @Persist is out of ui.workbench
//	@Override
//	public void doSave(IProgressMonitor monitor) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void doSaveAs() {
//		// TODO Auto-generated method stub
//	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		
		IEclipseContext parentContext = (IEclipseContext) getSite().getService(IEclipseContext.class);
		
		// Check if running in 4.x
		if( parentContext.get("org.eclipse.e4.workbench.ui.IPresentationEngine") != null ) {
			// Hack to get the MPart-Context
			try {
				Class<?> clazz = Util.getBundle("org.eclipse.e4.ui.model.workbench").loadClass("org.eclipse.e4.ui.model.application.ui.basic.MPart");
				Object instance = getSite().getService(clazz);
				Method m = clazz.getMethod("getContext", new Class[0]);
				context = (IEclipseContext) m.invoke(instance);				
			} catch (Exception e) {
				throw new PartInitException("Could not create context",e);
			}
		} else {
			context = parentContext.createChild("EditPart('"+getPartName()+"')"); //$NON-NLS-1$	
		}
		
		context.declareModifiable(IEditorInput.class);
		context.declareModifiable(EditorPart.class);
		context.declareModifiable(IDirtyProviderService.class);
		
		context.set(EditorPart.class,this);
		context.set(IDirtyProviderService.class,this);
		context.set(IEditorInput.class, input);
	}

	
//
//	@Override
//	public boolean isSaveAsAllowed() {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public void createPartControl(Composite parent) {
		ISelectionProvider s = new SelectionProviderImpl();
		context.set(ISelectionProvider.class, s);
		getSite().setSelectionProvider(s);
		
		IStylingEngine styleEngine = context.get(IStylingEngine.class);
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		FillLayout layout = new FillLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		comp.setLayout(layout);
		
		context.set(Composite.class.getName(), comp);
		component = ContextInjectionFactory.make(clazz, context);
		
		styleEngine.setClassname(comp, getClass().getSimpleName());
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

	private class SelectionProviderImpl implements ISelectionProvider {
		private ISelection currentSelection = StructuredSelection.EMPTY;
		
		private ListenerList listeners = new ListenerList();
		
		public void setSelection(ISelection selection) {
			currentSelection = selection;
			SelectionChangedEvent evt = new SelectionChangedEvent(this, selection);
			
			for( Object l : listeners.getListeners() ) {
				((ISelectionChangedListener)l).selectionChanged(evt);
			}
		}
				
		public void removeSelectionChangedListener(
				ISelectionChangedListener listener) {
			listeners.remove(listener);
		}
		
		public ISelection getSelection() {
			return currentSelection;
		}
		
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}
	}
}
