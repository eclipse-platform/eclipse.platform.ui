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

public class FederatedSearchPart extends AbstractFormPart implements IHelpPart, IHelpUIConstants {
	private ReusableHelpPart parent;
	protected static java.util.List previousSearchQueryData = new java.util.ArrayList(
			20);
	private String id;
	private Composite container;
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
	public FederatedSearchPart(Composite parent, FormToolkit toolkit) {
		container = toolkit.createComposite(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		// Search Expression
		Label expressionLabel = toolkit.createLabel(container, null, SWT.WRAP);
		expressionLabel.setText(HelpUIResources.getString("expression")); //$NON-NLS-1$
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		expressionLabel.setLayoutData(td);
		// Pattern combo
		searchWordCombo = new Combo(container, SWT.SINGLE | SWT.BORDER);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		//td.widthHint = 50;//convertWidthInCharsToPixels(30);
		searchWordCombo.setLayoutData(td);
		// Not done here to prevent page from resizing
		// fPattern.setItems(getPreviousSearchPatterns());
		searchWordCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (searchWordCombo.getSelectionIndex() < 0)
					return;
				int index = previousSearchQueryData.size() - 1
						- searchWordCombo.getSelectionIndex();
				/*
				searchQueryData = (SearchQueryData) previousSearchQueryData
						.get(index);
				searchWordCombo.setText(searchQueryData.getSearchWord());
				all.setSelection(!searchQueryData.isBookFiltering());
				selected.setSelection(searchQueryData.isBookFiltering());
				includeDisabledActivities.setSelection(!searchQueryData
						.isActivityFiltering());
				displaySelectedBooks();
				// headingsButton.setSelection(searchOperation.getQueryData().isFieldsSearch());
				 * 
				 */
			}
		});
		goButton = toolkit.createButton(container,
				"Go", SWT.PUSH); //$NON-NLS-1$
		goButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSearch(searchWordCombo.getText());
			}
		});
		goButton.setEnabled(false);
		searchWordCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				goButton.setEnabled(searchWordCombo.getText().length()>0);
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
		// Space
		//toolkit.createLabel(control, null);
		// Syntax description
		Label label = toolkit.createLabel(container, null, SWT.WRAP);
		label.setText(HelpUIResources.getString("expression_label").replace('\n', ' ')); //$NON-NLS-1$
		td = new TableWrapData(TableWrapData.FILL);
		label.setLayoutData(td);
		
		toolkit.createLabel(container, null);
		
		// space
		//label = toolkit.createLabel(container, null);
		//td = new TableWrapData();
		//td.colspan = 2;	
		//label.setLayoutData(td);
		// Filtering group		
		scopeSection = toolkit.createSection(container, 
				Section.TWISTIE|Section.COMPACT|Section.LEFT_TEXT_CLIENT_ALIGNMENT);
		scopeSection.setText(HelpUIResources.getString("limit_to"));
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
		Hyperlink advanced = toolkit.createHyperlink(filteringGroup, "Advanced Settings", SWT.NULL);
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
		scopeSetLink.setToolTipText("Change the current scope set");
		section.setTextClient(scopeSetLink);
		ScopeSet active = scopeSetManager.getActiveSet();
		setActiveScopeSet(active);
	}

	private void setActiveScopeSet(ScopeSet set) {
		scopeSetLink.setText(set.getName());
		scopeSetManager.setActiveSet(set);
		updateMasters(set);
		scopeSection.layout();
		if (parent!=null)
			parent.reflow();
	}
	
	private void updateMasters(ScopeSet set) {
		Control [] masters = ((Composite)scopeSection.getClient()).getChildren();
		for (int i=0; i<masters.length; i++) {
			Control master = masters[i];
			Object data = master.getData();
			if (data!=null && data instanceof EngineDescriptor) {
				EngineDescriptor ed = (EngineDescriptor)data;
				Button button = (Button)master;
				button.setSelection(set.getEngineEnabled(ed));
			}
		}
	}

	private void loadEngines(Composite container, FormToolkit toolkit) {
		engineDescriptors = new ArrayList();
		IConfigurationElement [] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(ENGINE_EXP_ID);
		for (int i=0; i<elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("engine")) {
				EngineDescriptor desc = loadEngine(element, container, toolkit);
				engineDescriptors.add(desc);
			}
		}
		updateMasters(scopeSetManager.getActiveSet());
	}
	
	private EngineDescriptor loadEngine(IConfigurationElement element, Composite container, FormToolkit toolkit) {
		final EngineDescriptor edesc = new EngineDescriptor(element);
		Label ilabel = toolkit.createLabel(container, null);
		ilabel.setImage(edesc.getIconImage());
		final Button master = toolkit.createButton(container, edesc.getLabel(), SWT.CHECK);
		master.setData(edesc);
		master.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				scopeSetManager.getActiveSet().setEngineEnabled(edesc, master.getSelection());
			}
		});
		String desc = edesc.getDescription();
		if (desc!=null) {
			toolkit.createLabel(container, null);
			Label dlabel = toolkit.createLabel(container, desc, SWT.WRAP);
			dlabel.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
			dlabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		}
		return edesc;
	}

	private void doSearch(String text) {
		ScopeSet set = scopeSetManager.getActiveSet();
		ArrayList entries = new ArrayList();
		for (int i=0; i<engineDescriptors.size(); i++) {
			EngineDescriptor ed = (EngineDescriptor)engineDescriptors.get(i);
			if (set.getEngineEnabled(ed) && ed.getEngine() != null) {
				ISearchScope scope = ed.createSearchScope(set.getPreferenceStore());
				FederatedSearchEntry entry = new FederatedSearchEntry(ed.getId(), scope, ed.getEngine());
				entries.add(entry);
			}
		}
		if (entries.size()==0)
			return;
		FederatedSearchEntry [] array = (FederatedSearchEntry[])entries.toArray(new FederatedSearchEntry[entries.size()]);
		FederatedSearchResultsPart results = (FederatedSearchResultsPart)parent.findPart(IHelpUIConstants.HV_FSEARCH_RESULT);
		BaseHelpSystem.getSearchManager().search(text, array, results);   
	}
	
	private void doAdvanced() {
		ScopeSet set = scopeSetManager.getActiveSet();		
		PreferenceManager manager = new ScopePreferenceManager(engineDescriptors, set);
		PreferenceDialog dialog = new PreferenceDialog(container.getShell(), manager);
		dialog.setPreferenceStore(set.getPreferenceStore());
		dialog.open();
		updateMasters(set);
	}
	
	private void doChangeScopeSet() {
		ScopeSetDialog dialog = new ScopeSetDialog(container.getShell(), scopeSetManager, engineDescriptors);
		dialog.setInput(scopeSetManager);
		if (dialog.open()==ScopeSetDialog.OK) {
			ScopeSet set = dialog.getActiveSet();
			if (set!=null)
				setActiveScopeSet(set);
		}
	}
	
	public void dispose() {
		ScopeSet activeSet = scopeSetManager.getActiveSet();
		if (activeSet!=null)
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
}