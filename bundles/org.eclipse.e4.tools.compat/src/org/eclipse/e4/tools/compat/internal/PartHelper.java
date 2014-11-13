/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
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
	private static final String ORG_ECLIPSE_E4_UI_WORKBENCH_I_PRESENTATION_ENGINE = "org.eclipse.e4.ui.workbench.IPresentationEngine"; //$NON-NLS-1$

	public static IEclipseContext createPartContext(WorkbenchPart part) throws PartInitException {
		final IWorkbenchPartSite site = part.getSite();
		final IEclipseContext parentContext = (IEclipseContext) site.getService(IEclipseContext.class);

		// Check if running in 4.x
		if (parentContext.get(ORG_ECLIPSE_E4_UI_WORKBENCH_I_PRESENTATION_ENGINE) != null) {
			// Hack to get the MPart-Context
			try {
				final Class<?> clazz = Util.getBundle("org.eclipse.e4.ui.model.workbench").loadClass( //$NON-NLS-1$
					"org.eclipse.e4.ui.model.application.ui.basic.MPart"); //$NON-NLS-1$
				final Object instance = site.getService(clazz);
				final Method m = clazz.getMethod("getContext", new Class[0]); //$NON-NLS-1$
				IEclipseContext ctx = (IEclipseContext) m.invoke(instance);
				final IEclipseContext rv = ctx;
				while (ctx.getParent() != null) {
					ctx = ctx.getParent();
				}
				ctx.set(IClipboardService.class, new ClipboardServiceImpl());
				return rv;
			} catch (final Exception e) {
				throw new PartInitException("Could not create context", e); //$NON-NLS-1$
			}
		}
		return parentContext.createChild("EditPart('" + part.getPartName() + "')"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static <C> C createComponent(Composite parent, IEclipseContext context, Class<C> clazz, WorkbenchPart part) {
		final ISelectionProvider s = new SelectionProviderImpl();
		context.set(ISelectionProvider.class, s);
		part.getSite().setSelectionProvider(s);

		final IStylingEngine styleEngine = context.get(IStylingEngine.class);
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);

		// FIXME This should be read from the CSS
		final FillLayout layout = new FillLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		comp.setLayout(layout);

		context.set(Composite.class.getName(), comp);
		final C component = ContextInjectionFactory.make(clazz, context);

		styleEngine.setClassname(comp, part.getClass().getSimpleName());

		return component;
	}

	static class SelectionProviderImpl implements ISelectionProvider {
		private ISelection currentSelection = StructuredSelection.EMPTY;

		private final ListenerList listeners = new ListenerList();

		@Override
		public void setSelection(ISelection selection) {
			currentSelection = selection;
			final SelectionChangedEvent evt = new SelectionChangedEvent(this, selection);

			for (final Object l : listeners.getListeners()) {
				((ISelectionChangedListener) l).selectionChanged(evt);
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
			.get(ORG_ECLIPSE_E4_UI_WORKBENCH_I_PRESENTATION_ENGINE) == null) {
			context.dispose();
			context = null;
		}

	}
}
