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
package org.eclipse.e4.tools.compat.internal;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.services.IClipboardService;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.WorkbenchPart;

public class PartHelper {
	public static IEclipseContext createPartContext(WorkbenchPart part) throws PartInitException {
		IWorkbenchPartSite site = part.getSite();
		IEclipseContext parentContext = (IEclipseContext) site.getService(IEclipseContext.class);
		
		// Check if running in 4.x
		if( parentContext.get("org.eclipse.e4.ui.workbench.IPresentationEngine") != null ) {
			// Hack to get the MPart-Context
			try {
				Class<?> clazz = Util.getBundle("org.eclipse.e4.ui.model.workbench").loadClass("org.eclipse.e4.ui.model.application.ui.basic.MPart");
				Object instance = site.getService(clazz);
				Method m = clazz.getMethod("getContext", new Class[0]);
				IEclipseContext ctx = (IEclipseContext) m.invoke(instance);
				IEclipseContext rv = ctx;
				while( ctx.getParent() != null ) {
					ctx = ctx.getParent();
				}
				ctx.set(IClipboardService.class, new ClipboardServiceImpl());
				return rv;
			} catch (Exception e) {
				throw new PartInitException("Could not create context",e);
			}
		} else {
			return parentContext.createChild("EditPart('"+part.getPartName()+"')"); //$NON-NLS-1$	
		}

	}
	
	public static <C> C createComponent(Composite parent, IEclipseContext context, Class<C> clazz, WorkbenchPart part) {
		ISelectionProvider s = new SelectionProviderImpl();
		context.set(ISelectionProvider.class, s);
		part.getSite().setSelectionProvider(s);
		
		IStylingEngine styleEngine = context.get(IStylingEngine.class);
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		//FIXME This should be read from the CSS
		FillLayout layout = new FillLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		comp.setLayout(layout);
		
		context.set(Composite.class.getName(), comp);
		C component = ContextInjectionFactory.make(clazz, context);
		
		styleEngine.setClassname(comp, part.getClass().getSimpleName());
		
		return component;
	}
	
	static class SelectionProviderImpl implements ISelectionProvider {
		private ISelection currentSelection = StructuredSelection.EMPTY;
		
		private ListenerList listeners = new ListenerList();
		
		@Override
		public void setSelection(ISelection selection) {
			currentSelection = selection;
			SelectionChangedEvent evt = new SelectionChangedEvent(this, selection);
			
			for( Object l : listeners.getListeners() ) {
				((ISelectionChangedListener)l).selectionChanged(evt);
			}
		}
				
		@Override
		public void removeSelectionChangedListener(
				ISelectionChangedListener listener) {
			listeners.remove(listener);
		}
		
		@Override
		public ISelection getSelection() {
			return currentSelection;
		}
		
		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}
	}

	public static void disposeContextIfE3(IEclipseContext parentContext,
			IEclipseContext context) {
			// Check if running in 3.x, otherwise there was no dedicated context
			// created
			if (parentContext
					.get("org.eclipse.e4.ui.workbench.IPresentationEngine") == null) {
				context.dispose();
				context = null;
		}
		
	}
}
