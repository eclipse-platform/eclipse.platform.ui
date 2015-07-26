/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Managed form wraps a form widget and adds life cycle methods for form parts.
 * A form part is a portion of the form that participates in form life cycle
 * events.
 * <p>
 * There is requirement for 1/1 mapping between widgets and form parts. A widget
 * like Section can be a part by itself, but a number of widgets can join around
 * one form part.
 * <p>
 * Note to developers: this class is left public to allow its use beyond the
 * original intention (inside a multi-page editor's page). You should limit the
 * use of this class to make new instances inside a form container (wizard page,
 * dialog etc.). Clients that need access to the class should not do it
 * directly. Instead, they should do it through IManagedForm interface as much
 * as possible.
 *
 * @since 3.0
 */
public class ManagedForm implements IManagedForm {
	private Object input;

	private ScrolledForm form;

	private FormToolkit toolkit;

	private Object container;

	private boolean ownsToolkit;

	private boolean initialized;

	private Vector<IFormPart> parts = new Vector<>();

	/**
	 * Creates a managed form in the provided parent. Form toolkit and widget
	 * will be created and owned by this object.
	 *
	 * @param parent
	 *            the parent widget
	 */
	public ManagedForm(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		ownsToolkit = true;
		form = toolkit.createScrolledForm(parent);
	}

	/**
	 * Creates a managed form that will use the provided toolkit and
	 *
	 * @param toolkit
	 * @param form
	 */
	public ManagedForm(FormToolkit toolkit, ScrolledForm form) {
		this.form = form;
		this.toolkit = toolkit;
	}

	@Override
	public void addPart(IFormPart part) {
		parts.add(part);
		part.initialize(this);
	}

	@Override
	public void removePart(IFormPart part) {
		parts.remove(part);
	}

	@Override
	public IFormPart[] getParts() {
		return parts.toArray(new IFormPart[parts.size()]);
	}

	@Override
	public FormToolkit getToolkit() {
		return toolkit;
	}

	@Override
	public ScrolledForm getForm() {
		return form;
	}

	@Override
	public void reflow(boolean changed) {
		form.reflow(changed);
	}

	/**
	 * A part can use this method to notify other parts that implement
	 * IPartSelectionListener about selection changes.
	 *
	 * @param part
	 *            the part that broadcasts the selection
	 * @param selection
	 *            the selection in the part
	 * @see IPartSelectionListener
	 */
	@Override
	public void fireSelectionChanged(IFormPart part, ISelection selection) {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart cpart = parts.get(i);
			if (part.equals(cpart))
				continue;
			if (cpart instanceof IPartSelectionListener) {
				((IPartSelectionListener) cpart).selectionChanged(part,
						selection);
			}
		}
	}

	/**
	 * Initializes the form by looping through the managed parts and
	 * initializing them. Has no effect if already called once.
	 */
	@Override
	public void initialize() {
		if (initialized)
			return;
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = parts.get(i);
			part.initialize(this);
		}
		initialized = true;
	}

	/**
	 * Disposes all the parts in this form.
	 */
	public void dispose() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = parts.get(i);
			part.dispose();
		}
		if (ownsToolkit) {
			toolkit.dispose();
		}
	}

	/**
	 * Refreshes the form by refreshes all the stale parts. Since 3.1, this
	 * method is performed on a UI thread when called from another thread so it
	 * is not needed to wrap the call in <code>Display.syncExec</code> or
	 * <code>asyncExec</code>.
	 */
	@Override
	public void refresh() {
		Thread t = Thread.currentThread();
		Thread dt = toolkit.getColors().getDisplay().getThread();
		if (t.equals(dt))
			doRefresh();
		else {
			toolkit.getColors().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					doRefresh();
				}
			});
		}
	}

	private void doRefresh() {
		int nrefreshed = 0;
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = parts.get(i);
			if (part.isStale()) {
				part.refresh();
				nrefreshed++;
			}
		}
		if (nrefreshed > 0)
			form.reflow(true);
	}

	@Override
	public void commit(boolean onSave) {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = parts.get(i);
			if (part.isDirty())
				part.commit(onSave);
		}
	}

	@Override
	public boolean setInput(Object input) {
		boolean pageResult = false;

		this.input = input;
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = parts.get(i);
			boolean result = part.setFormInput(input);
			if (result)
				pageResult = true;
		}
		return pageResult;
	}

	@Override
	public Object getInput() {
		return input;
	}

	/**
	 * Transfers the focus to the first form part.
	 */
	public void setFocus() {
		if (parts.size() > 0) {
			IFormPart part = parts.get(0);
			part.setFocus();
		}
	}

	@Override
	public boolean isDirty() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = parts.get(i);
			if (part.isDirty())
				return true;
		}
		return false;
	}

	@Override
	public boolean isStale() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = parts.get(i);
			if (part.isStale())
				return true;
		}
		return false;
	}

	@Override
	public void dirtyStateChanged() {
	}

	@Override
	public void staleStateChanged() {
	}

	@Override
	public Object getContainer() {
		return container;
	}

	@Override
	public void setContainer(Object container) {
		this.container = container;
	}

	@Override
	public IMessageManager getMessageManager() {
		return form.getMessageManager();
	}
}
