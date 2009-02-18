/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.about;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.about.IInstallationPageSources;
import org.eclipse.ui.about.InstallationPage;

/**
 * A registered source provider that can update variables associated with an
 * InstallationDialog.
 * 
 * @since 3.5
 */
public class InstallationDialogSourceProvider extends AbstractSourceProvider {
	Map currentState;

	/**
	 * These variables are internal to the about framework. They are only needed
	 * because some of the platform about pages can be launched into their own
	 * nested ProductInfoDialog from another platform about page. We need
	 * different variables for the visibility expressions for those cases,
	 * because the launching InstallationDialog is still open and using the
	 * regular variables.
	 */
	public static final String ACTIVE_PRODUCT_DIALOG_PAGE = "org.eclipse.ui.internal.about.activeProductDialogPage"; //$NON-NLS-1$
	public static final String ACTIVE_PRODUCT_DIALOG_PAGE_ID = "org.eclipse.ui.internal.about.activeProductDialogPageId"; //$NON-NLS-1$
	public static final String ACTIVE_PRODUCT_DIALOG_PAGE_SELECTION = "org.eclipse.ui.internal.about.activeProductDialogSelection"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#dispose()
	 */
	public void dispose() {
		currentState = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	public Map getCurrentState() {
		if (currentState == null) {
			currentState = new HashMap();
			currentState.put(IInstallationPageSources.ACTIVE_PAGE,
					IEvaluationContext.UNDEFINED_VARIABLE);
			currentState.put(IInstallationPageSources.ACTIVE_PAGE_ID,
					IEvaluationContext.UNDEFINED_VARIABLE);
			currentState.put(IInstallationPageSources.ACTIVE_PAGE_SELECTION,
					IEvaluationContext.UNDEFINED_VARIABLE);
			currentState.put(ACTIVE_PRODUCT_DIALOG_PAGE,
					IEvaluationContext.UNDEFINED_VARIABLE);
			currentState.put(ACTIVE_PRODUCT_DIALOG_PAGE_ID,
					IEvaluationContext.UNDEFINED_VARIABLE);
			currentState.put(ACTIVE_PRODUCT_DIALOG_PAGE_SELECTION,
					IEvaluationContext.UNDEFINED_VARIABLE);
		}
		return currentState;
	}

	public void setCurrentPage(String id, InstallationPage page) {
		Map map = getCurrentState();
		map.put(IInstallationPageSources.ACTIVE_PAGE_ID, id);
		map.put(IInstallationPageSources.ACTIVE_PAGE, page);
		fireSourceChanged(ISources.WORKBENCH,
				IInstallationPageSources.ACTIVE_PAGE_ID, id);
		fireSourceChanged(ISources.WORKBENCH,
				IInstallationPageSources.ACTIVE_PAGE, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	public String[] getProvidedSourceNames() {
		return new String[] { IInstallationPageSources.ACTIVE_PAGE,
				IInstallationPageSources.ACTIVE_PAGE_ID,
				IInstallationPageSources.ACTIVE_PAGE_SELECTION,
				ACTIVE_PRODUCT_DIALOG_PAGE, ACTIVE_PRODUCT_DIALOG_PAGE_ID,
				ACTIVE_PRODUCT_DIALOG_PAGE_SELECTION };
	}

	public void setProductDialogPage(Object page) {
		Map map = getCurrentState();
		Object pageValue, idValue;
		if (page instanceof ProductInfoPage) {
			map.put(ACTIVE_PRODUCT_DIALOG_PAGE, pageValue = page);
			map.put(ACTIVE_PRODUCT_DIALOG_PAGE_ID,
					idValue = ((ProductInfoPage) page).getId());
		} else {
			map.put(ACTIVE_PRODUCT_DIALOG_PAGE,
					pageValue = IEvaluationContext.UNDEFINED_VARIABLE);
			map.put(ACTIVE_PRODUCT_DIALOG_PAGE_ID,
					idValue = IEvaluationContext.UNDEFINED_VARIABLE);
		}
		fireSourceChanged(ISources.WORKBENCH, ACTIVE_PRODUCT_DIALOG_PAGE,
				pageValue);
		fireSourceChanged(ISources.WORKBENCH, ACTIVE_PRODUCT_DIALOG_PAGE_ID,
				idValue);
	}

	public void setPageSelection(Object selection) {
		Map map = getCurrentState();
		map.put(IInstallationPageSources.ACTIVE_PAGE_SELECTION, selection);
		fireSourceChanged(ISources.WORKBENCH,
				IInstallationPageSources.ACTIVE_PAGE_SELECTION, selection);
	}

	public void setProductDialogPageSelection(Object selection) {
		Map map = getCurrentState();
		map.put(ACTIVE_PRODUCT_DIALOG_PAGE_SELECTION, selection);
		fireSourceChanged(ISources.WORKBENCH,
				ACTIVE_PRODUCT_DIALOG_PAGE_SELECTION, selection);
	}

	public void resetAll() {
		currentState = null;
		Map map = getCurrentState();
		fireSourceChanged(ISources.WORKBENCH, map);
	}
}
