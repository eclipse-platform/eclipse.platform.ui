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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.*;

public class SearchPart extends SectionPart implements IHelpPart {
	private ReusableHelpPart parent;
	private Text phraseText;
	private Button goButton;
	private String id;
	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public SearchPart(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.EXPANDED|Section.TWISTIE|Section.TITLE_BAR);
		// configure section
		Section section = getSection();
		section.setText(HelpUIResources.getString("SearchPart.title")); //$NON-NLS-1$
		section.marginWidth = 5;
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				toggleSearchResults(e.getState());
			}
			public void expansionStateChanged(ExpansionEvent e) {
				if (e.getState()) {
					String phrase = phraseText.getText();
					if (phrase.length()>0)
						doSearch(phrase);
				}
			}
		});
		// create 'clear' hyperlink on the section tool bar
		ImageHyperlink clearLink = new ImageHyperlink(section, SWT.NULL);
		toolkit.adapt(clearLink, true, true);
		clearLink.setToolTipText(HelpUIResources.getString("SearchPart.clearResults")); //$NON-NLS-1$
		clearLink.setImage(HelpUIResources.getImage(IHelpUIConstants.IMAGE_CLEAR));
		clearLink.setBackground(section.getTitleBarGradientBackground());		
		clearLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doClear();
			}
		});
		section.setTextClient(clearLink);
		// create section client
		Composite helpContainer = toolkit.createComposite(section);
		section.setClient(helpContainer);
		GridLayout glayout = new GridLayout();
		glayout.numColumns = 3;
		glayout.marginWidth = glayout.marginHeight = 1;
		helpContainer.setLayout(glayout);
		helpContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		toolkit.paintBordersFor(helpContainer);
		Label label = toolkit.createLabel(helpContainer, HelpUIResources.getString("SearchPart.searchLabel")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		phraseText = toolkit.createText(helpContainer, ""); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 50;
		phraseText.setLayoutData(gd);
		goButton = toolkit.createButton(helpContainer,
				"Go", SWT.PUSH); //$NON-NLS-1$
		goButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSearch(phraseText.getText());
			}
		});
		goButton.setEnabled(false);
		phraseText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = phraseText.getText();
				goButton.setEnabled(text.length() > 0);
			}
		});
		phraseText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r') {
					if (goButton.isEnabled())
						doSearch(phraseText.getText());
				}
			}
		});
		toolkit.paintBordersFor(helpContainer);
	}
	public void setFocus() {
		if (phraseText!=null)
			phraseText.setFocus();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return getSection();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}
	public String getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		getSection().setVisible(visible);
	}

	public void startSearch(String phrase) {
		String currentPhrase = phraseText.getText();
		if (currentPhrase.equals(phrase))
			return;
		phraseText.setText(phrase);
		if (getSection().isExpanded())
			doSearch(phrase);
	}

	private void doSearch(String phrase) {
		/*
		SearchQueryData data = new SearchQueryData();
		//if (!parent.isInWorkbenchWindow()) 
		data.setMaxHits(parent.getNumberOfInPlaceHits());
		data.setSearchWord(phrase);
		HelpSearchQuery query = new HelpSearchQuery(data);
		startInPlaceSearch(phrase, query);
		*/
	}
	
/*
	public void startWorkbenchSearch(String phrase) {
		SearchQueryData data = new SearchQueryData();
		data.setSearchWord(phrase);
		HelpSearchQuery query = new HelpSearchQuery(data);
		startWorkbenchSearch(query);
	}
	
	private void startWorkbenchSearch(ISearchQuery query) {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(parent.getRunnableContext(), query);
	}
*/
/*
	private void startInPlaceSearch(final String phrase, final ISearchQuery query) {
		final ISearchResult result = query.getSearchResult();
		final StringBuffer resultBuffer = new StringBuffer();
		result.addListener(new ISearchResultListener() {
			public void searchResultChanged(SearchResultEvent e) {
				final HelpSearchResult hresult = (HelpSearchResult) result;
				getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateResults(phrase, resultBuffer, hresult);
					}
				});
			}
		});
		Job job = new Job("Quick search") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					return query.run(monitor);
				}
				catch (OperationCanceledException e) {
					return null;
				}
			}
		};
		doStartSearch(job);
	}
	private void updateResults(String phrase, StringBuffer buffer, HelpSearchResult hresult) {
		SearchResultsPart part = (SearchResultsPart)parent.findPart(IHelpUIConstants.HV_SEARCH_RESULT);
		if (part!=null)
			part.updateResults(phrase, buffer, hresult);
	}
	*/
	private void doClear() {
		SearchResultsPart part = (SearchResultsPart)parent.findPart(IHelpUIConstants.HV_SEARCH_RESULT);
		if (part!=null)
			part.clearResults();
	}
	private void doStartSearch(Job job) {
		SearchResultsPart part = (SearchResultsPart)parent.findPart(IHelpUIConstants.HV_SEARCH_RESULT);
		if (part!=null)
			part.startNewSearch(job);
	}
	private void toggleSearchResults(boolean visible) {
		SearchResultsPart part = (SearchResultsPart)parent.findPart(IHelpUIConstants.HV_SEARCH_RESULT);
		if (part!=null)
			part.setVisible(visible);		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return phraseText.equals(control) || goButton.equals(control);
	}
}