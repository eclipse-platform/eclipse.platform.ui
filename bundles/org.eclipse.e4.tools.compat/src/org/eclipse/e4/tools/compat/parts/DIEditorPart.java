/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Jonas Helming <jhelming@eclipsesource.com>
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 421453
 *     Steven Spungin <steven@spungin.tv> - Bug 436889
 ******************************************************************************/
package org.eclipse.e4.tools.compat.parts;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.compat.internal.CopyAction;
import org.eclipse.e4.tools.compat.internal.CutAction;
import org.eclipse.e4.tools.compat.internal.PartHelper;
import org.eclipse.e4.tools.compat.internal.PasteAction;
import org.eclipse.e4.tools.services.IClipboardService;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

/**
 * This class provides an adapter enabling to plug editors, which are
 * implemented following the e4 programming model into a 3.x workbench. This
 * class is supposed to be sub classed by clients.
 *
 * @author Jonas
 *
 * @param <C>
 */
public abstract class DIEditorPart<C> extends EditorPart implements
		IDirtyProviderService {
	private IEclipseContext context;
	private C component;
	private Class<C> clazz;
	private boolean dirtyState;

	private int features;

	protected static final int COPY = 1;
	protected static final int PASTE = 1 << 1;
	protected static final int CUT = 1 << 2;

	public DIEditorPart(Class<C> clazz) {
		this(clazz, SWT.NONE);
	}

	public DIEditorPart(Class<C> clazz, int features) {
		this.clazz = clazz;
		this.features = features;
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

		context.set(IEditorPart.class, this);
		context.set(IDirtyProviderService.class, this);
		context.set(IEditorInput.class, input);
	}

	@Override
	public void createPartControl(Composite parent) {
		component = PartHelper.createComponent(parent, context, clazz, this);
		makeActions();
	}

	@PersistState
	public void persistState() {
		ContextInjectionFactory.invoke(component, PersistState.class, context);
	}

	protected IEclipseContext getContext() {
		return context;
	}

	public C getComponent() {
		return component;
	}

	protected void makeActions() {
		if ((features & COPY) == COPY) {
			IClipboardService clipboard = context.get(IClipboardService.class);
			getEditorSite().getActionBars().setGlobalActionHandler(
					ActionFactory.COPY.getId(), new CopyAction(clipboard));
		}

		if ((features & PASTE) == PASTE) {
			IClipboardService clipboard = context.get(IClipboardService.class);
			getEditorSite().getActionBars().setGlobalActionHandler(
					ActionFactory.PASTE.getId(), new PasteAction(clipboard));
		}

		if ((features & CUT) == CUT) {
			IClipboardService clipboard = context.get(IClipboardService.class);
			getEditorSite().getActionBars().setGlobalActionHandler(
					ActionFactory.CUT.getId(), new CutAction(clipboard));
		}
	}

	@Override
	public void setDirtyState(boolean dirtyState) {
		if (dirtyState != this.dirtyState) {
			this.dirtyState = dirtyState;
			firePropertyChange(PROP_DIRTY);
		}
	}

	@Override
	public boolean isDirty() {
		return dirtyState;
	}

	@Override
	public void setFocus() {
		ContextInjectionFactory.invoke(component, Focus.class, context);
	}

	@Override
	public void dispose() {
		PartHelper.disposeContextIfE3((IEclipseContext) getSite().getService(
				IEclipseContext.class), context);
		super.dispose();
	}


}
