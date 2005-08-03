/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.federated.FederatedSearchEntry;
import org.eclipse.help.internal.search.federated.FederatedSearchJob;
import org.eclipse.help.search.ISearchEngineResult;
import org.eclipse.help.search.ISearchEngineResultCollector;
import org.eclipse.help.search.ISearchScope;
import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class SearchPart extends AbstractFormPart implements IHelpPart,
		IHelpUIConstants {
	private ReusableHelpPart parent;

	protected static java.util.List previousSearchQueryData = new java.util.ArrayList(
			20);

	private static final String HREF_TOGGLE = "__toggle__"; //$NON-NLS-1$

	private static final String HREF_SEARCH_HELP = "/org.eclipse.platform.doc.user/tasks/tsearch.htm"; //$NON-NLS-1$
	
	private static boolean SEARCH_HELP_AVAILABLE=false;

	static {
		InputStream is = HelpSystem.getHelpContent(HREF_SEARCH_HELP);
		if (is!=null) {
			//don't leak the input stream
			try {
				is.close();
				SEARCH_HELP_AVAILABLE = true;
			}
			catch (IOException e) {
				//ignore
			}
		}
	}

	private String id;

	private Composite container;

	private FormText searchWordText;

	private Chevron searchWordChevron;

	private ComboPart searchWordCombo;

	private Section scopeSection;

	private Button goButton;

	private Hyperlink scopeSetLink;

	private Hyperlink advancedLink;

	private ScopeSetManager scopeSetManager;

	private EngineDescriptorManager descManager;

	private static final int COMBO_HISTORY_SIZE = 10;

	private JobListener jobListener;
	
	private boolean searchPending;

	private class JobListener implements IJobChangeListener, Runnable {
		private boolean searchInProgress = false;

		public void aboutToRun(IJobChangeEvent event) {
		}

		public void awake(IJobChangeEvent event) {
		}

		public void done(IJobChangeEvent event) {
			if (event.getJob().belongsTo(FederatedSearchJob.FAMILY)) {
				Job[] searchJobs = Platform.getJobManager().find(
						FederatedSearchJob.FAMILY);
				if (searchJobs.length == 0) {
					// search finished
					searchInProgress = false;
					if (container.isDisposed())
						return;
					container.getDisplay().asyncExec(this);
					SearchResultsPart results = (SearchResultsPart) parent
							.findPart(IHelpUIConstants.HV_FSEARCH_RESULT);
					results.completed();
				}
			}
		}

		public void running(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
			if (!searchInProgress
					&& event.getJob().belongsTo(FederatedSearchJob.FAMILY)) {
				searchInProgress = true;
				container.getDisplay().asyncExec(this);
			}
		}

		public void sleeping(IJobChangeEvent event) {
		}

		public void run() {
			searchWordCombo.getControl().setEnabled(!searchInProgress);
			if (!searchInProgress)
				goButton.setEnabled(true);
			if (searchInProgress)
				goButton.setText(Messages.SearchPart_stop);
			else
				goButton.setText(Messages.SearchPart_go);
			goButton.getParent().layout();
		}
	}

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public SearchPart(final Composite parent, FormToolkit toolkit) {
		container = toolkit.createComposite(parent);
		scopeSetManager = new ScopeSetManager();
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		// Search Expression
		searchWordText = toolkit.createFormText(container, false);
		searchWordChevron = new Chevron(searchWordText, SWT.NULL);
		toolkit.adapt(searchWordChevron, true, true);
		searchWordChevron.setHoverDecorationColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		searchWordChevron.setDecorationColor(toolkit.getColors().getColor(
				FormColors.TB_TOGGLE));
		searchWordText.setControl(HREF_TOGGLE, searchWordChevron);
		searchWordText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				SearchPart.this.parent.showURL(HREF_SEARCH_HELP, true);
			}
		});
		searchWordChevron.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				parent.getDisplay().asyncExec(new Runnable() {
					public void run() {
						toggleSearchWordText();
					}
				});
			}
		});
		searchWordText.setImage(IHelpUIConstants.IMAGE_HELP, HelpUIResources
				.getImage(IHelpUIConstants.IMAGE_HELP));
		updateSearchWordText();
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		searchWordText.setLayoutData(td);
		// Pattern combo
		searchWordCombo = new ComboPart(container, toolkit, toolkit
				.getBorderStyle());
		updateSearchCombo(null);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.maxWidth = 100;
		td.valign = TableWrapData.MIDDLE;
		searchWordCombo.getControl().setLayoutData(td);
		searchWordCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (searchWordCombo.getSelectionIndex() < 0)
					return;
				searchFromHistory(searchWordCombo.getSelectionIndex());
			}
		});
		goButton = toolkit.createButton(container, Messages.SearchPart_go,
				SWT.PUSH);
		goButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleButtonPressed();
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
		scopeSection.setText(Messages.limit_to);
		td = new TableWrapData();
		td.colspan = 2;
		td.align = TableWrapData.FILL;
		scopeSection.setLayoutData(td);
		Composite filteringGroup = toolkit.createComposite(scopeSection);
		scopeSection.setClient(filteringGroup);
		TableWrapLayout flayout = new TableWrapLayout();
		flayout.numColumns = 2;
		filteringGroup.setLayout(flayout);
		createScopeSet(scopeSection, toolkit);
		toolkit.paintBordersFor(filteringGroup);
		toolkit.paintBordersFor(container);
		loadEngines(filteringGroup, toolkit);
		createAdvancedLink(filteringGroup, toolkit);
		jobListener = new JobListener();
		Platform.getJobManager().addJobChangeListener(jobListener);
	}

	private void createAdvancedLink(Composite parent, FormToolkit toolkit) {
		advancedLink = toolkit.createHyperlink(parent,
				Messages.FederatedSearchPart_advanced, SWT.NULL); 
		advancedLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doAdvanced();
			}
		});
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		advancedLink.setLayoutData(td);
	}

	private void createScopeSet(Section section, FormToolkit toolkit) {
		scopeSetLink = toolkit.createHyperlink(section, null, SWT.NULL);
		scopeSetLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doChangeScopeSet();
			}
		});
		scopeSetLink
				.setToolTipText(Messages.FederatedSearchPart_changeScopeSet); 
		section.setTextClient(scopeSetLink);
		ScopeSet active = scopeSetManager.getActiveSet();
		setActiveScopeSet(active);
	}

	private void toggleSearchWordText() {
		updateSearchWordText();
		SearchPart.this.parent.reflow();
	}

	private void updateSearchWordText() {
		StringBuffer buff = new StringBuffer();
		buff.append("<form>"); //$NON-NLS-1$
		buff.append("<p>"); //$NON-NLS-1$
		buff.append(Messages.expression);
		if (searchWordChevron.isExpanded()) {
			searchWordChevron.setToolTipText(Messages.SearchPart_collapse);
			buff.append("<control href=\""); //$NON-NLS-1$
			buff.append(HREF_TOGGLE);
			buff.append("\"/>"); //$NON-NLS-1$
			buff.append("</p><p>"); //$NON-NLS-1$
			buff.append(Messages.expression_label);
			//Only add the link if available
			if (SEARCH_HELP_AVAILABLE) {
				buff.append("</p><p>"); //$NON-NLS-1$
				buff.append("<img href=\""); //$NON-NLS-1$
				buff.append(IHelpUIConstants.IMAGE_HELP);
				buff.append("\"/> "); //$NON-NLS-1$
				buff.append("<a href=\""); //$NON-NLS-1$
				buff.append(HREF_SEARCH_HELP);
				buff.append("\">"); //$NON-NLS-1$
				buff.append(Messages.SearchPart_learnMore);
				buff.append("</a>"); //$NON-NLS-1$
			}
		} else {
			searchWordChevron.setToolTipText(Messages.SearchPart_expand);
			buff.append("<control href=\""); //$NON-NLS-1$
			buff.append(HREF_TOGGLE);
			buff.append("\"/>"); //$NON-NLS-1$
		}
		buff.append("</p>"); //$NON-NLS-1$
		buff.append("</form>"); //$NON-NLS-1$
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
		Control[] children = ((Composite) scopeSection.getClient())
				.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			if (child instanceof Button) {
				Button master = (Button) child;
				Object data = master.getData();
				if (data != null && data instanceof EngineDescriptor) {
					EngineDescriptor ed = (EngineDescriptor) data;
					master.setSelection(set.getEngineEnabled(ed));
				}
			}
		}
	}

	private void loadEngines(final Composite container,
			final FormToolkit toolkit) {
		descManager = new EngineDescriptorManager();
		EngineDescriptor[] descriptors = descManager.getDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			EngineDescriptor desc = descriptors[i];
			loadEngine(desc, container, toolkit);
		}
		descManager.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				EngineDescriptorManager.DescriptorEvent event = (EngineDescriptorManager.DescriptorEvent) arg;
				int kind = event.getKind();
				EngineDescriptor desc = event.getDescriptor();
				if (kind == IHelpUIConstants.ADD) {
					advancedLink.dispose();
					loadEngine(desc, container, toolkit);
					createAdvancedLink(container, toolkit);
					parent.reflow();
				} else if (kind == IHelpUIConstants.REMOVE) {
					removeEngine(desc);
				} else {
					updateEngine(desc);
				}
			}
		});
		updateMasters(scopeSetManager.getActiveSet());
	}

	private EngineDescriptor loadEngine(final EngineDescriptor edesc,
			Composite container, FormToolkit toolkit) {
		Label ilabel = toolkit.createLabel(container, null);
		ilabel.setImage(edesc.getIconImage());
		ilabel.setData(edesc);
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
			Label spacer = toolkit.createLabel(container, null);
			spacer.setData(edesc);
			Label dlabel = toolkit.createLabel(container, desc, SWT.WRAP);
			dlabel
					.setForeground(toolkit.getColors().getColor(
							FormColors.TITLE));
			dlabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			dlabel.setMenu(container.getMenu());
			dlabel.setData(edesc);
		}
		return edesc;
	}

	private void removeEngine(EngineDescriptor desc) {
		boolean reflowNeeded = false;
		Control[] children = ((Composite) scopeSection.getClient())
				.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			EngineDescriptor ed = (EngineDescriptor) child.getData();
			if (ed == desc) {
				child.setMenu(null);
				child.dispose();
				reflowNeeded = true;
			}
		}
		if (reflowNeeded)
			parent.reflow();
	}

	private void updateEngine(EngineDescriptor desc) {
		Control[] children = ((Composite) scopeSection.getClient())
				.getChildren();
		boolean reflowNeeded = false;
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			EngineDescriptor ed = (EngineDescriptor) child.getData();
			if (ed == desc) {
				Button b = (Button) children[i + 1];
				b.setText(desc.getLabel());
				Label d = (Label) children[i + 3];
				d.setText(desc.getDescription());
				d.getParent().layout();
				reflowNeeded = true;
				break;
			}
		}
		if (reflowNeeded)
			parent.reflow();
	}

	public void startSearch(String text) {
		searchWordCombo.setText(text);
		doSearch(text);
	}

	private void storeSearchHistory(String expression) {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95479
		HistoryScopeSet sset = scopeSetManager.findSearchSet(expression);
		if (sset == null) {
			sset = new HistoryScopeSet(expression);
			scopeSetManager.add(sset);
		}
		ScopeSet activeSet = scopeSetManager.getActiveSet();
		sset.copyFrom(activeSet);
		sset.save();
		updateSearchCombo(sset);
		searchWordCombo.setText(expression);
	}

	private void updateSearchCombo(HistoryScopeSet current) {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95479
		ScopeSet[] sets = scopeSetManager.getScopeSets(true);
		ArrayList items = new ArrayList();
		ArrayList toDelete = new ArrayList();
		// if (current!=null)
		// items.add(current.getExpression());
		for (int i = sets.length - 1; i >= 0; i--) {
			HistoryScopeSet sset = (HistoryScopeSet) sets[i];
			if (current != null && sset == current)
				continue;
			if (sets.length - i > COMBO_HISTORY_SIZE)
				toDelete.add(sset);
			items.add(sset.getExpression());
		}
		for (int i = 0; i < toDelete.size(); i++) {
			HistoryScopeSet sset = (HistoryScopeSet) toDelete.get(i);
			scopeSetManager.remove(sset);
		}
		if (items.size() > 0)
			searchWordCombo.setItems((String[]) items.toArray(new String[items
					.size()]));
	}

	private void searchFromHistory(int index) {
		String expression = searchWordCombo.getSelection();
		ScopeSet set = scopeSetManager.findSearchSet(expression);
		if (set == null)
			return;
		setActiveScopeSet(set);
		doSearch(expression, true);
	}

	private void handleButtonPressed() {
		if (searchWordCombo.getControl().isEnabled())
			doSearch(searchWordCombo.getText());
		else {
			goButton.setEnabled(false);
			stop();
		}
	}
	
	private void doSearch(String text) {
		doSearch(text, false);
	}

	private void doSearch(String text, boolean fromHistory) {
		ScopeSet set = scopeSetManager.getActiveSet();
		if (!fromHistory && set instanceof HistoryScopeSet) {
			String setExpression = ((HistoryScopeSet)set).getExpression();
			if (setExpression.equals(text))
				fromHistory=true;
		}
		if (!fromHistory) {
			storeSearchHistory(text);
			boolean switchedSet = scopeSetManager.restoreLastExplicitSet();
			set = scopeSetManager.getActiveSet();
			if (switchedSet)
				setActiveScopeSet(set);
		}
		ArrayList entries = new ArrayList();
		final SearchResultsPart results = (SearchResultsPart) parent
				.findPart(IHelpUIConstants.HV_FSEARCH_RESULT);
		ArrayList eds = new ArrayList();
		EngineDescriptor[] engineDescriptors = descManager.getDescriptors();
		for (int i = 0; i < engineDescriptors.length; i++) {
			final EngineDescriptor ed = engineDescriptors[i];
			if (set.getEngineEnabled(ed) && ed.getEngine() != null) {
				ISearchScope scope = ed.createSearchScope(set
						.getPreferenceStore());
				FederatedSearchEntry entry = new FederatedSearchEntry(ed
						.getId(), ed.getLabel(), scope, ed.getEngine(),
						new ISearchEngineResultCollector() {
							public void accept(ISearchEngineResult searchResult) {
								results.add(ed, searchResult);
							}

							public void accept(
									ISearchEngineResult[] searchResults) {
								results.add(ed, searchResults);
							}

							public void error(IStatus status) {
								results.error(ed, status);
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
		PreferenceManager manager = new ScopePreferenceManager(descManager, set);
		PreferenceDialog dialog = new ScopePreferenceDialog(container
				.getShell(), manager, descManager, set.isEditable());
		dialog.setPreferenceStore(set.getPreferenceStore());
		dialog.create();
		dialog.getShell().setText(
				NLS.bind(Messages.ScopePreferenceDialog_wtitle, set.getName()));
		dialog.open();
		updateMasters(set);
	}

	private void doChangeScopeSet() {
		ScopeSetDialog dialog = new ScopeSetDialog(container.getShell(),
				scopeSetManager, descManager);
		dialog.setInput(scopeSetManager);
		dialog.create();
		dialog.getShell().setText(Messages.ScopeSetDialog_wtitle);
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
		Platform.getJobManager().removeJobChangeListener(jobListener);
		stop();
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
	public void init(ReusableHelpPart parent, String id, IMemento memento) {
		this.parent = parent;
		this.id = id;
		parent.hookFormText(searchWordText);
		if (memento != null)
			restorePart(memento);
	}

	private void restorePart(IMemento memento) {
		String setName = memento.getString("activeSet"); //$NON-NLS-1$
		if (setName != null) {
			ScopeSet sset = scopeSetManager.findSet(setName);
			if (sset != null)
				scopeSetManager.setActiveSet(sset);
		}
		String expression = memento.getString("expression"); //$NON-NLS-1$
		if (expression!=null && expression.length()>0) {
			searchWordCombo.setText(expression);
			searchPending=true;
			markStale();
		}
	}

	public void refresh() {
		super.refresh();
		if (searchPending) {
			searchPending=false;
			doSearch(searchWordCombo.getText());
		}
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
		return parent.fillFormContextMenu(searchWordText, manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return control == searchWordText
				|| control == searchWordCombo.getControl()
				|| scopeSection.getClient() == control;
	}

	public void setFocus() {
		searchWordCombo.getControl().setFocus();
	}

	public IAction getGlobalAction(String id) {
		if (id.equals(ActionFactory.COPY.getId()))
			return parent.getCopyAction();
		return null;
	}

	public void stop() {
		SearchResultsPart results = (SearchResultsPart) parent
				.findPart(IHelpUIConstants.HV_FSEARCH_RESULT);
		results.canceling();
		Platform.getJobManager().cancel(FederatedSearchJob.FAMILY);
	}

	public void toggleRoleFilter() {
	}

	public void refilter() {
	}

	public void saveState(IMemento memento) {
		ScopeSet sset = scopeSetManager.getActiveSet();
		if (sset != null)
			memento.putString("activeSet", sset.getName()); //$NON-NLS-1$
		memento.putString("expression", searchWordCombo.getText()); //$NON-NLS-1$
	}
}
