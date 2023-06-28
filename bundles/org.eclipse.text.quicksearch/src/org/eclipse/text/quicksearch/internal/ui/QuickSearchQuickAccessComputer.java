/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.quicksearch.internal.core.LineItem;
import org.eclipse.text.quicksearch.internal.core.QuickTextQuery;
import org.eclipse.text.quicksearch.internal.core.QuickTextSearchRequestor;
import org.eclipse.text.quicksearch.internal.core.QuickTextSearcher;
import org.eclipse.text.quicksearch.internal.core.priority.PriorityFunction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.quickaccess.IQuickAccessComputer;
import org.eclipse.ui.quickaccess.IQuickAccessComputerExtension;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.eclipse.ui.texteditor.ITextEditor;

public class QuickSearchQuickAccessComputer extends QuickTextSearchRequestor implements IQuickAccessComputer, IQuickAccessComputerExtension {

	private static final int MAX_ENTRIES = 20;
	private static final long TIMEOUT = 200;
	private PriorityFunction priorities;

	public QuickSearchQuickAccessComputer() {
		priorities = new QuickSearchContext(PlatformUI.getWorkbench().getActiveWorkbenchWindow()).createPriorityFun();
	}

	@Override public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor) {
		List<LineItem> matches = Collections.synchronizedList(new ArrayList<>());
		QuickTextQuery newQuery = new QuickTextQuery(query, true);
		QuickTextSearcher searcher = new QuickTextSearcher(newQuery, priorities, QuickSearchActivator.getDefault().getPreferences().getMaxLineLen(), new QuickTextSearchRequestor() {
			@Override public void add(LineItem match) {
				if (matches.size() < MAX_ENTRIES) {
					matches.add(match);
				}
			}

			@Override public void clear() {
				matches.clear();
			}

			@Override public void revoke(LineItem line) {
				matches.remove(line);
			}
		});
		searcher.setMaxResults(MAX_ENTRIES);
		long start = System.currentTimeMillis();
		while (matches.size() < MAX_ENTRIES && !searcher.isDone() && System.currentTimeMillis() - start < TIMEOUT) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				QuickSearchActivator.log(e);
			}
		}
		searcher.cancel();
		return matches.stream().map(LineItemQuickAccessElement::new).toArray(QuickAccessElement[]::new);
	}

	@Override public QuickAccessElement[] computeElements() {
		return new QuickAccessElement[0];
	}

	@Override public void resetState() {
		// stateless

	}

	@Override public boolean needsRefresh() {
		return false;
	}

	private static class LineItemQuickAccessElement extends QuickAccessElement {

		private final LineItem item;

		public LineItemQuickAccessElement(LineItem item) {
			this.item = item;
		}

		@Override public String getLabel() {
			return NLS.bind(Messages.quickAccessMatch, item.getText(), item.getFile().getName());
		}

		@Override public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override public String getId() {
			return item.getFile().getFullPath().toString() + '[' + item.getOffset() + ':' + (item.getOffset() + item.getText().length() - 1) + ']';
		}

		@Override public void execute() {
			IEditorPart part;
			try {
				part = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), item.getFile());
				if (part instanceof ITextEditor) {
					((ITextEditor) part).getSelectionProvider().setSelection(new TextSelection(item.getOffset(), item.getText().length()));
				}
			} catch (PartInitException e) {
				QuickSearchActivator.log(e);
			}
		}

	}

}
