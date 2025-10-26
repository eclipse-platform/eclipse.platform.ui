/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Sebastian Thomschke (Vegard IT GmbH) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.OpenFileWithReuseAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Installs a wrapper OPEN handler in Project Explorer that applies last opened
 * editor reuse for single {@link IFile} selections, and delegates to the
 * previous OPEN handler otherwise (e.g., JDT OpenAction for Java elements).
 */
public class OpenFileWithReuseActionProvider extends CommonActionProvider {

	private ICommonViewerWorkbenchSite viewSite;
	private OpenFileWithReuseAction reuseAction;
	private IWorkbenchPage reusePage;

	@Override
	public void init(ICommonActionExtensionSite aConfig) {
		if (aConfig.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			viewSite = (ICommonViewerWorkbenchSite) aConfig.getViewSite();
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (viewSite == null) {
			return;
		}
		// Scope strictly to Project Explorer (including secondary ids)
		String partId = viewSite.getSite().getId();
		if (partId == null || !partId.startsWith(IPageLayout.ID_PROJECT_EXPLORER)) {
			return;
		}

		// Capture current OPEN delegate (e.g., JDT's OpenAction) to call when reuse
		// doesn't apply
		IAction delegate = actionBars.getGlobalActionHandler(ICommonActionConstants.OPEN);

		IAction wrapper = new Action() {
			@Override
			public void run() {
				ISelection sel = getSelection();
				if (!(sel instanceof IStructuredSelection)) {
					if (delegate != null) {
						delegate.run();
					}
					return;
				}
				IStructuredSelection ss = (IStructuredSelection) sel;
				if (ss.size() == 1) {
					Object element = ss.getFirstElement();
					IFile file = Adapters.adapt(element, IFile.class);
					if (file != null) {
						IWorkbenchPage page = viewSite.getPage();
						if (reuseAction == null || reusePage != page) {
							reuseAction = new OpenFileWithReuseAction(page, IPageLayout.ID_PROJECT_EXPLORER);
							reusePage = page;
						}
						reuseAction.selectionChanged(new StructuredSelection(file));
						reuseAction.run();
						return;
					}
				}
				// Fallback: delegate to prior handler (e.g., JDT)
				if (delegate != null) {
					delegate.run();
				}
			}

			@Override
			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				if (delegate != null) {
					delegate.setEnabled(enabled);
				}
			}
		};

		// Install synchronously; plugin.xml dependsOn ensures we run after JDT's
		// provider
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, wrapper);
		actionBars.updateActionBars();
	}

	private ISelection getSelection() {
		ActionContext ctx = getContext();
		if (ctx != null && ctx.getSelection() != null) {
			return ctx.getSelection();
		}
		return viewSite.getSelectionProvider() != null //
				? viewSite.getSelectionProvider().getSelection()
				: StructuredSelection.EMPTY;
	}
}
