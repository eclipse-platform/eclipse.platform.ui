/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.help.search.ISearchEngineResult;
import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class SearchResultsPart extends AbstractFormPart implements IHelpPart {
	private ReusableHelpPart parent;

	private Composite separator;

	private Composite container;

	private ScrolledForm innerForm;

	private String id;

	private Action removeAllAction;

	private Action showCategoriesAction;

	private Action showDescriptionAction;

	private ArrayList results;

	private String phrase;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public SearchResultsPart(Composite parent, FormToolkit toolkit,
			IToolBarManager tbm) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container = toolkit.createComposite(parent);
		container.setLayout(layout);
		separator = toolkit.createCompositeSeparator(container);
		separator.setVisible(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 1;
		separator.setLayoutData(gd);
		innerForm = toolkit.createScrolledForm(container);
		innerForm.setDelayedReflow(true);
		innerForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableWrapLayout tlayout = new TableWrapLayout();
		tlayout.topMargin = 5;
		tlayout.bottomMargin = 0;
		innerForm.getBody().setLayout(tlayout);
		results = new ArrayList();
		contributeToToolBar(tbm);
	}

	private void contributeToToolBar(IToolBarManager tbm) {
		/*
		 * removeAllAction = new Action() { public void run() { clearResults(); } };
		 * removeAllAction.setImageDescriptor(HelpUIResources
		 * .getImageDescriptor(IHelpUIConstants.IMAGE_REMOVE_ALL));
		 * removeAllAction.setToolTipText("Remove all hits");
		 * removeAllAction.setId("removeAll"); tbm.insertBefore("back",
		 * removeAllAction); tbm.insertAfter("removeAll", new Separator());
		 */

		showCategoriesAction = new Action() {
			public void run() {
				updateResultSections();
			}
		};
		showCategoriesAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_SHOW_CATEGORIES));
		showCategoriesAction.setChecked(false);
		showCategoriesAction.setToolTipText("Show result categories");
		showCategoriesAction.setId("categories");
		tbm.insertBefore("back", showCategoriesAction);

		showDescriptionAction = new Action() {
			public void run() {
				updateResultSections();
			}
		};
		showDescriptionAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_SHOW_DESC));
		showDescriptionAction.setChecked(true);
		showDescriptionAction.setToolTipText("Show result descriptions");
		showDescriptionAction.setId("description");
		tbm.insertAfter("categories", showDescriptionAction);
		tbm.insertAfter("description", new Separator());
	}

	private void updateResultSections() {
		for (int i = 0; i < results.size(); i++) {
			EngineResultSection section = (EngineResultSection) results.get(i);
			section.updateResults(false);
		}
		reflow();
	}

	boolean getShowCategories() {
		return showCategoriesAction.isChecked();
	}

	boolean getShowDescription() {
		return showDescriptionAction.isChecked();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		container.setVisible(visible);
	}

	void clearResults() {
		clearResultSections();
		separator.setVisible(false);
		reflow();
	}

	void clearResultSections() {
		for (int i = 0; i < results.size(); i++) {
			EngineResultSection section = (EngineResultSection) results.get(i);
			section.dispose();
		}
		results.clear();
	}

	void startNewSearch(String phrase, ArrayList eds) {
		this.phrase = phrase;
		separator.setVisible(true);
		for (int i = 0; i < eds.size(); i++) {
			add((EngineDescriptor) eds.get(i));
		}
		reflow();
	}

	void doOpenLink(Object href) {
		String url = (String) href;

		if (url.startsWith("nw:")) { //$NON-NLS-1$
			parent.showExternalURL(url.substring(3));
		} else
			parent.showURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		Control focusControl = container.getDisplay().getFocusControl();
		if (focusControl != null && focusControl instanceof FormText) {
			return parent.fillFormContextMenu((FormText) focusControl, manager);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		for (int i = 0; i < results.size(); i++) {
			EngineResultSection er = (EngineResultSection) results.get(i);
			if (er.hasControl(control))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.search.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.ISearchEngineResult)
	 */
	public synchronized void add(EngineDescriptor ed, ISearchEngineResult match) {
		EngineResultSection ers = findEngineResult(ed);
		if (match != null)
			ers.add(match);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.search.federated.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.federated.ISearchEngineResult[])
	 */
	public synchronized void add(EngineDescriptor ed,
			ISearchEngineResult[] matches) {
		EngineResultSection ers = findEngineResult(ed);
		ers.add(matches);
	}

	private synchronized EngineResultSection findEngineResult(
			EngineDescriptor ed) {
		for (int i = 0; i < results.size(); i++) {
			EngineResultSection er = (EngineResultSection) results.get(i);
			if (er.matches(ed))
				return er;
		}
		final EngineResultSection er = new EngineResultSection(this, ed);
		Display display = parent.getForm().getToolkit().getColors()
				.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				Control c = er.createControl(innerForm.getBody(), parent
						.getForm().getToolkit());
				c.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			}
		});
		results.add(er);
		return er;
	}

	private void add(EngineDescriptor ed) {
		final EngineResultSection er = new EngineResultSection(this, ed);
		Control c = er.createControl(innerForm.getBody(), parent.getForm()
				.getToolkit());
		c.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		results.add(er);
	}

	void reflow() {
		innerForm.reflow(true);
		parent.reflow();
	}

	public boolean setFormInput(Object input) {
		return false;
	}
	void scrollToBeginning() {
		innerForm.setOrigin(0, 0);
	}
}