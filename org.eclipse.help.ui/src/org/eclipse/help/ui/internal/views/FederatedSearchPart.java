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

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.search.federated.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.*;

public class FederatedSearchPart extends AbstractFormPart implements IHelpPart,
		IHelpUIConstants {
	private ReusableHelpPart parent;

	protected static java.util.List previousSearchQueryData = new java.util.ArrayList(
			20);

	private static final String HREF_TOGGLE = "__toggle__";

	private static final String HREF_SEARCH_HELP = "/org.eclipse.platform.doc.user/tasks/tsearch.htm";

	private String id;

	private Composite container;

	private FormText searchWordText;

	private boolean searchWordTextExpanded = false;

	private Combo searchWordCombo;

	private Section scopeSection;

	private Button goButton;

	private Hyperlink scopeSetLink;

	private ScopeSetManager scopeSetManager;

	private ArrayList engineDescriptors;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public FederatedSearchPart(final Composite parent, FormToolkit toolkit) {
		container = toolkit.createComposite(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		// Search Expression
		searchWordText = toolkit.createFormText(container, true);
		searchWordText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if (href.equals(HREF_TOGGLE)) {
					parent.getDisplay().asyncExec(new Runnable() {
						public void run() {
							toggleSearchWordText();
						}
					});
				} else
					FederatedSearchPart.this.parent.showURL(HREF_SEARCH_HELP,
							true);
			}
		});
		searchWordText.setImage(IHelpUIConstants.IMAGE_FILE_F1TOPIC,
				HelpUIResources.getImage(IHelpUIConstants.IMAGE_FILE_F1TOPIC));
		updateSearchWordText();
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		searchWordText.setLayoutData(td);
		// Pattern combo
		searchWordCombo = new Combo(container, SWT.SINGLE | SWT.BORDER);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.maxWidth = 100;
		searchWordCombo.setLayoutData(td);
		searchWordCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (searchWordCombo.getSelectionIndex() < 0)
					return;
				int index = previousSearchQueryData.size() - 1
						- searchWordCombo.getSelectionIndex();
				/*
				 * searchQueryData = (SearchQueryData) previousSearchQueryData
				 * .get(index);
				 * searchWordCombo.setText(searchQueryData.getSearchWord());
				 * all.setSelection(!searchQueryData.isBookFiltering());
				 * selected.setSelection(searchQueryData.isBookFiltering());
				 * includeDisabledActivities.setSelection(!searchQueryData
				 * .isActivityFiltering()); displaySelectedBooks(); //
				 * headingsButton.setSelection(searchOperation.getQueryData().isFieldsSearch());
				 * 
				 */
			}
		});
		goButton = toolkit.createButton(container, "Go", SWT.PUSH); //$NON-NLS-1$
		goButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSearch(searchWordCombo.getText());
			}
		});
		goButton.setEnabled(false);
		searchWordCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				goButton.setEnabled(searchWordCombo.getText().length() > 0);
			}
		});
		searchWordCombo.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r') {
					if (goButton.isEnabled())
						doSearch(searchWordCombo.getText());
				}
			}
		});
		scopeSection = toolkit.createSection(container, Section.TWISTIE
				| Section.COMPACT | Section.LEFT_TEXT_CLIENT_ALIGNMENT);
		scopeSection.setText(HelpUIResources.getString("limit_to")); //$NON-NLS-1$
		td = new TableWrapData();
		td.colspan = 2;
		td.align = TableWrapData.FILL;
		scopeSection.setLayoutData(td);
		Composite filteringGroup = toolkit.createComposite(scopeSection);
		scopeSection.setClient(filteringGroup);
		createScopeSet(scopeSection, toolkit);
		TableWrapLayout flayout = new TableWrapLayout();
		flayout.numColumns = 2;
		filteringGroup.setLayout(flayout);

		toolkit.paintBordersFor(filteringGroup);
		loadEngines(filteringGroup, toolkit);
		Hyperlink advanced = toolkit
				.createHyperlink(filteringGroup, HelpUIResources
						.getString("FederatedSearchPart.advanced"), SWT.NULL); //$NON-NLS-1$
		advanced.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doAdvanced();
			}
		});
		td = new TableWrapData();
		td.colspan = 2;
		advanced.setLayoutData(td);
	}

	private void createScopeSet(Section section, FormToolkit toolkit) {
		scopeSetManager = new ScopeSetManager();
		scopeSetLink = toolkit.createHyperlink(section, null, SWT.NULL);
		scopeSetLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doChangeScopeSet();
			}
		});
		scopeSetLink.setToolTipText(HelpUIResources
				.getString("FederatedSearchPart.changeScopeSet")); //$NON-NLS-1$
		section.setTextClient(scopeSetLink);
		ScopeSet active = scopeSetManager.getActiveSet();
		setActiveScopeSet(active);
	}

	private void toggleSearchWordText() {
		searchWordTextExpanded = !searchWordTextExpanded;
		updateSearchWordText();
		FederatedSearchPart.this.parent.reflow();
		searchWordText.setFocus();
	}

	private void updateSearchWordText() {
		StringBuffer buff = new StringBuffer();
		buff.append("<form>");
		buff.append("<p>");
		buff.append(HelpUIResources.getString("expression"));
		if (searchWordTextExpanded) {
			buff.append(" <a href=\"");
			buff.append(HREF_TOGGLE);
			buff.append("\" alt=\"");
			buff.append("Collapse");
			buff.append("\">&lt;&lt;</a>");
			buff.append("</p><p>");
			buff.append(HelpUIResources.getString("expression_label"));
			buff.append("</p><p>");
			buff.append("<img href=\"");
			buff.append(IHelpUIConstants.IMAGE_FILE_F1TOPIC);
			buff.append("\"/> ");
			buff.append("<a href=\"");
			buff.append(HREF_SEARCH_HELP);
			buff.append("\">Learn more");
			buff.append("</a>");
		} else {
			buff.append(" <a href=\"");
			buff.append(HREF_TOGGLE);
			buff.append("\" alt=\"");
			buff.append("Expand");
			buff.append("\">&gt;&gt;</a>");
		}
		buff.append("</p>");
		buff.append("</form>");
		searchWordText.setText(buff.toString(), true, false);
	}

	private void setActiveScopeSet(ScopeSet set) {
		scopeSetLink.setText(set.getName());
		scopeSetManager.setActiveSet(set);
		updateMasters(set);
		scopeSection.layout();
		if (parent != null)
			parent.reflow();
	}

	private void updateMasters(ScopeSet set) {
		Control[] masters = ((Composite) scopeSection.getClient())
				.getChildren();
		for (int i = 0; i < masters.length; i++) {
			Control master = masters[i];
			Object data = master.getData();
			if (data != null && data instanceof EngineDescriptor) {
				EngineDescriptor ed = (EngineDescriptor) data;
				Button button = (Button) master;
				button.setSelection(set.getEngineEnabled(ed));
			}
		}
	}

	private void loadEngines(Composite container, FormToolkit toolkit) {
		engineDescriptors = new ArrayList();
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(ENGINE_EXP_ID);
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("engine")) { //$NON-NLS-1$
				EngineDescriptor desc = loadEngine(element, container, toolkit);
				engineDescriptors.add(desc);
			}
		}
		updateMasters(scopeSetManager.getActiveSet());
	}

	private EngineDescriptor loadEngine(IConfigurationElement element,
			Composite container, FormToolkit toolkit) {
		final EngineDescriptor edesc = new EngineDescriptor(element);
		Label ilabel = toolkit.createLabel(container, null);
		ilabel.setImage(edesc.getIconImage());
		final Button master = toolkit.createButton(container, edesc.getLabel(),
				SWT.CHECK);
		master.setData(edesc);
		master.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				scopeSetManager.getActiveSet().setEngineEnabled(edesc,
						master.getSelection());
			}
		});
		String desc = edesc.getDescription();
		if (desc != null) {
			toolkit.createLabel(container, null);
			Label dlabel = toolkit.createLabel(container, desc, SWT.WRAP);
			dlabel
					.setForeground(toolkit.getColors().getColor(
							FormColors.TITLE));
			dlabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		}
		return edesc;
	}

	public void startSearch(String text) {
		searchWordCombo.setText(text);
		doSearch(text);
	}

	private void doSearch(String text) {
		ScopeSet set = scopeSetManager.getActiveSet();
		ArrayList entries = new ArrayList();
		final FederatedSearchResultsPart results = (FederatedSearchResultsPart) parent
				.findPart(IHelpUIConstants.HV_FSEARCH_RESULT);
		ArrayList eds = new ArrayList();
		for (int i = 0; i < engineDescriptors.size(); i++) {
			final EngineDescriptor ed = (EngineDescriptor) engineDescriptors
					.get(i);
			if (set.getEngineEnabled(ed) && ed.getEngine() != null) {
				ISearchScope scope = ed.createSearchScope(set
						.getPreferenceStore());
				FederatedSearchEntry entry = new FederatedSearchEntry(ed
						.getId(), ed.getLabel(), scope, ed.getEngine(),
						new ISearchEngineResultCollector() {
							public void add(ISearchEngineResult searchResult) {
								results.add(ed, searchResult);
							}

							public void add(ISearchEngineResult[] searchResults) {
								results.add(ed, searchResults);
							}
						});
				entries.add(entry);
				eds.add(ed);
			}
		}
		if (entries.size() == 0)
			return;
		FederatedSearchEntry[] array = (FederatedSearchEntry[]) entries
				.toArray(new FederatedSearchEntry[entries.size()]);
		if (scopeSection.isExpanded()) {
			scopeSection.setExpanded(false);
			parent.reflow();
		}
		results.clearResults();
		results.startNewSearch(text, eds);
		BaseHelpSystem.getSearchManager().search(text, array);
	}

	private void doAdvanced() {
		ScopeSet set = scopeSetManager.getActiveSet();
		PreferenceManager manager = new ScopePreferenceManager(
				engineDescriptors, set);
		PreferenceDialog dialog = new PreferenceDialog(container.getShell(),
				manager);
		dialog.setPreferenceStore(set.getPreferenceStore());
		dialog.open();
		updateMasters(set);
	}

	private void doChangeScopeSet() {
		ScopeSetDialog dialog = new ScopeSetDialog(container.getShell(),
				scopeSetManager, engineDescriptors);
		dialog.setInput(scopeSetManager);
		if (dialog.open() == ScopeSetDialog.OK) {
			ScopeSet set = dialog.getActiveSet();
			if (set != null)
				setActiveScopeSet(set);
		}
	}

	public void dispose() {
		ScopeSet activeSet = scopeSetManager.getActiveSet();
		if (activeSet != null)
			activeSet.save();
		super.dispose();
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
		getControl().setVisible(visible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return false;
	}

	public void setFocus() {
		searchWordCombo.setFocus();
	}
}