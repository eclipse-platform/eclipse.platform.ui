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

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.search.federated.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;

public class FederatedSearchResultsPart extends AbstractFormPart implements IHelpPart { 
	private ReusableHelpPart parent;
	private Composite separator;
	private Composite container;
	private ScrolledForm innerForm;
	private FormText searchResults;
	private String id;
	private Action showCategoriesAction;
	private Action showDescriptionAction;
	private ArrayList results;

	private String phrase;
	
	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public FederatedSearchResultsPart(Composite parent, FormToolkit toolkit, IToolBarManager tbm) {
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
		innerForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableWrapLayout tlayout = new TableWrapLayout();
		//tlayout.leftMargin = tlayout.rightMargin = 0;
		tlayout.topMargin = tlayout.bottomMargin = 0;
		//tlayout.verticalSpacing = 0;
		innerForm.getBody().setLayout(tlayout);
		searchResults = toolkit.createFormText(innerForm.getBody(), true);
		searchResults.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		searchResults.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if (href.toString().equals("__cancel__")) { //$NON-NLS-1$
					Platform.getJobManager().cancel(FederatedSearchJob.FAMILY);
					clearResults();
				}
			}
		});
		searchResults.setText("", false, false); //$NON-NLS-1$
		results = new ArrayList();
		contributeToToolBar(tbm);
	}
	
	private void contributeToToolBar(IToolBarManager tbm) {
		showCategoriesAction = new Action() {
			public void run() {
				toggleShowCategories(showCategoriesAction.isChecked());
			}
		};
		showCategoriesAction.setImageDescriptor(HelpUIResources.getImageDescriptor(IHelpUIConstants.IMAGE_SHOW_CATEGORIES));
		showCategoriesAction.setChecked(true);
		tbm.add(showCategoriesAction);
		
		showDescriptionAction = new Action() {
			public void run() {
				toggleShowDescription(showDescriptionAction.isChecked());
			}
		};
		showDescriptionAction.setImageDescriptor(HelpUIResources.getImageDescriptor(IHelpUIConstants.IMAGE_SHOW_DESC));
		showDescriptionAction.setChecked(false);
		tbm.add(showDescriptionAction);
	}
	
	private void toggleShowCategories(boolean checked) {
		for (int i=0; i<results.size(); i++) {
			EngineResultSection section = (EngineResultSection)results.get(i);
			section.setShowCategories(checked);
		}
		reflow();
		markThisState();
	}
	
	private void toggleShowDescription(boolean checked) {
		for (int i=0; i<results.size(); i++) {
			EngineResultSection section = (EngineResultSection)results.get(i);
			section.setShowDescription(checked);
		}
		reflow();
		markThisState();
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
		searchResults.setText("", false, false); //$NON-NLS-1$
		parent.reflow();
		separator.setVisible(false);
	}
	void clearResultSections() {
		for (int i=0; i<results.size(); i++) {
			EngineResultSection section = (EngineResultSection)results.get(i);
			section.dispose();
		}
		results.clear();
	}
	
	
	void startNewSearch(String phrase) {
		this.phrase = phrase;
		StringBuffer buff = new StringBuffer();
		buff.append("<form>"); //$NON-NLS-1$
		buff.append("<p><span color=\""); //$NON-NLS-1$
		buff.append(FormColors.TITLE);
		buff.append("\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.progress")); //$NON-NLS-1$
		buff.append("</span>"); //$NON-NLS-1$
		buff.append("<a href=\"__cancel__\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.cancel")); //$NON-NLS-1$
		buff.append("</a></p>"); //$NON-NLS-1$
		buff.append("</form>"); //$NON-NLS-1$
		searchResults.setText(buff.toString(), true, false);
		separator.setVisible(true);
		markThisState();
	}
	
	private void markThisState() {
		parent.addPageHistoryEntry(parent.getCurrentPageId(), 
				new FederatedSearchResultData(phrase,
						showDescriptionAction.isChecked(),
						showCategoriesAction.isChecked()));		
	}
	
	private void asyncUpdateResults() {
		searchResults.getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateResults();
			}
		});
	}
	
	private void updateResults() {
		searchResults.setText("", false, false); //$NON-NLS-1$
		searchResults.getParent().layout();
	}

	void doOpenLink(Object href) {
		String url = (String) href;

		if (url.startsWith("nw:")) { //$NON-NLS-1$
			WorkbenchHelp.displayHelpResource(url.substring(3));
		} else
			parent.showURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return parent.fillFormContextMenu(searchResults, manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return searchResults.equals(control);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.search.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.ISearchEngineResult)
	 */
	public synchronized void add(EngineDescriptor ed, ISearchEngineResult match) {
		asyncUpdateResults();
		EngineResultSection ers = findEngineResult(ed);
		ers.add(match);
	}
 
    /* (non-Javadoc)
     * @see org.eclipse.help.internal.search.federated.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.federated.ISearchEngineResult[])
     */
    public synchronized void add(EngineDescriptor ed, ISearchEngineResult[] matches) {
    	asyncUpdateResults(); 	
    	EngineResultSection ers = findEngineResult(ed);
    	ers.add(matches);
     }

    private synchronized EngineResultSection findEngineResult(EngineDescriptor ed) {
    	for (int i=0; i<results.size(); i++) {
    		EngineResultSection er = (EngineResultSection)results.get(i);
    		if (er.matches(ed))
    			return er;
    	}
    	final EngineResultSection er = new EngineResultSection(this, ed, true);
    	Display display = parent.getForm().getToolkit().getColors().getDisplay();
    	display.syncExec(new Runnable() {
    		public void run() {
    			Control c = er.createControl(innerForm.getBody(), parent.getForm().getToolkit());
    			c.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
    		}
    	});
    	results.add(er);
    	return er;
    }
    void reflow() {
    	innerForm.reflow(true);
     	parent.reflow();
    }
	public boolean setFormInput(Object input) {
		if (input instanceof FederatedSearchResultData) {
			FederatedSearchResultData data = (FederatedSearchResultData)input;
			showCategoriesAction.setChecked(data.showCategories);
			showDescriptionAction.setChecked(data.showDescription);
			FederatedSearchPart part = (FederatedSearchPart)parent.findPart(IHelpUIConstants.HV_FSEARCH);
			part.startSearch(data.expression);
			return true;
		}
		return false;
	}    
}