/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.ITextOperationTarget;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Abstract Handler to delegate to an operation of {@link ITextOperationTarget}.
 */
public abstract class TextOperationActionHandler extends AbstractHandler {

	protected final int operationCode;

	public TextOperationActionHandler(int operationCode) {
		this.operationCode= operationCode;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (operationCode < 0) {
			return null;
		}
		IWorkbenchPart part= HandlerUtil.getActivePart(event);
		ITextOperationTarget target= part.getAdapter(ITextOperationTarget.class);
		if (target == null) {
			return null;
		}

		IWorkbenchPartSite site= part.getSite();
		Shell shell= site.getShell();
		if (shell != null && !shell.isDisposed()) {
			Display display= shell.getDisplay();
			BusyIndicator.showWhile(display, () -> target.doOperation(operationCode));
			return Status.OK_STATUS;
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		IWorkbench workbench= PlatformUI.getWorkbench();
		if (workbench == null) {
			setBaseEnabled(false);
			return;
		}
		IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
		if (window == null) {
			setBaseEnabled(false);
			return;
		}
		IWorkbenchPage page= window.getActivePage();
		if (page == null) {
			setBaseEnabled(false);
			return;
		}
		IWorkbenchPart part= page.getActivePart();
		setBaseEnabled(part != null && part.getAdapter(ITextOperationTarget.class) != null);
	}
}
