/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Base class for preference dialogs that will show two tabs of preferences -
 * filtered and unfiltered.
 * 
 * @since 3.0
 */
public abstract class FilteredPreferenceDialog extends PreferenceDialog {

	//The id of the last page that was selected
	private static String lastGroupId = null;

	private static String SEARCH_ICON = "org.eclipse.ui.internal.dialogs.SEARCH_ICON";//$NON-NLS-1$

	private static String LOOK_ICON = "org.eclipse.ui.internal.dialogs.LOOK_ICON";//$NON-NLS-1$

	private Point minimumSize = new Point(400, 400);

	/**
	 * The preference page history.
	 * 
	 * @since 3.1
	 */
	private PreferencePageHistory history;

	ToolBar toolbar;

	GroupedPreferenceLabelProvider groupedLabelProvider;

	private WorkbenchPreferenceGroup currentGroup;
	
	private CTabItem tab;

	static {
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				PlatformUI.PLUGIN_ID, "icons/full/obj16/search.gif"); //$NON-NLS-1$
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(SEARCH_ICON, descriptor);
		}
	}

	/**
	 * Creates a new preference dialog under the control of the given preference
	 * manager.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param manager
	 *            the preference manager
	 */
	public FilteredPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
		history = createHistory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#close()
	 */
	public boolean close() {
		// clear the search results do they don't appear next time we open the dialog
		clearSearchResults();
		return super.close();
	}

	private void clearSearchResults() {
		WorkbenchPreferenceGroup group = (WorkbenchPreferenceGroup) getTreeViewer().getInput();
		group.highlightHits(""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		if (getGroups().length > 0)
			return createCategorizedDialogArea(parent);
		return super.createDialogArea(parent);
	}

	/**
	 * Create the dialog area with a spot for the categories
	 * @param parent
	 * @return Control
	 */
	private Control createCategorizedDialogArea(Composite parent) {
		//		 create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		ToolBar toolbar = createToolBar(composite);

		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		data.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		data.verticalIndent = IDialogConstants.VERTICAL_MARGIN;
		toolbar.setLayoutData(data);

		createDialogContents(composite);

		applyDialogFont(composite);

		return composite;
	}

	/**
	 * Create the contents area of the dialog
	 * @param composite
	 */
	private void createDialogContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData compositeData = new GridData(GridData.FILL_BOTH);
		compositeData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		composite.setLayoutData(compositeData);
		applyDialogFont(composite);
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		Control treeControl = createTreeAreaContents(composite);
		createSash(composite, treeControl);

		Composite pageAreaComposite = new Composite(composite, SWT.NONE);
		pageAreaComposite.setBackground(composite.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));

		pageAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout pageAreaLayout = new GridLayout();
		pageAreaLayout.marginHeight = 0;
		pageAreaLayout.marginWidth = 0;
		pageAreaLayout.horizontalSpacing = 0;

		pageAreaComposite.setLayout(pageAreaLayout);

		// Build the Page container
		setPageContainer(createPageContainer(pageAreaComposite));
		getPageContainer().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	/**
	 * Create the search area for the receiver.
	 * @param composite
	 * @return Control The control that contains the area.
	 */
	private Control createSearchArea(Composite composite) {

		Composite searchArea = new Composite(composite, SWT.NONE);
		searchArea.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		GridLayout searchLayout = new GridLayout();
		searchLayout.marginWidth = 0;
		searchLayout.marginHeight = 0;
		searchArea.setLayout(searchLayout);

		final Text searchText = new Text(searchArea, SWT.BORDER | SWT.SINGLE);

		GridData textData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL);
		textData.verticalIndent = IDialogConstants.VERTICAL_MARGIN / 2;
		searchText.setLayoutData(textData);
		searchText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				highlightHits(searchText.getText());
			}
		});

		return searchArea;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeAreaContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createTreeAreaContents(Composite parent) {

		if (hasGroups()) {
			Composite leftArea = new Composite(parent, SWT.NONE);
			leftArea.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

			GridLayout leftLayout = new GridLayout();
			leftLayout.numColumns = 1;
			leftLayout.marginWidth = 0;
			leftLayout.marginHeight = 0;
			leftLayout.horizontalSpacing = 0;
			leftLayout.verticalSpacing = 0;

			leftArea.setLayout(leftLayout);

			Control searchArea = createSearchArea(leftArea);
			GridData searchData = new GridData(GridData.FILL_HORIZONTAL);
			searchData.grabExcessHorizontalSpace = true;
			searchData.verticalAlignment = SWT.BOTTOM;
			searchArea.setLayoutData(searchData);

			//Build the tree an put it into the composite.
			TreeViewer viewer = createTreeViewer(leftArea);
			setTreeViewer(viewer);

			viewer.setInput(getPreferenceManager());
			updateTreeFont(JFaceResources.getDialogFont());
			GridData viewerData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
			viewer.getControl().setLayoutData(viewerData);

			layoutTreeAreaControl(leftArea);

			return leftArea;
		}
		return super.createTreeAreaContents(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createSash(org.eclipse.swt.widgets.Composite, org.eclipse.swt.widgets.Control)
	 */
	protected Sash createSash(Composite composite, Control rightControl) {
		if (hasGroups()) {
			Sash sash = super.createSash(composite, rightControl);
			sash.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

			return sash;
		}
		return super.createSash(composite, rightControl);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPageContainer(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createPageContainer(Composite parent) {

		if (!hasGroups())
			return super.createPageContainer(parent);

		Color background = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);

		CTabFolder parentFolder = new CTabFolder(parent, SWT.BORDER);

		tab = new CTabItem(parentFolder, SWT.BORDER);

		parentFolder.setSelectionForeground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		parentFolder.setSelectionBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		parentFolder.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		tab.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));

		parentFolder.setTopRight(getContainerToolBar(parentFolder), SWT.RIGHT);
		parentFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));
		parentFolder.setSimple(false);
	
		Control historyBar = history.createHistoryControls(parentFolder);
		parentFolder.setTopRight(historyBar, SWT.RIGHT);
		historyBar.setBackground(background);

		Composite result = new Composite(parentFolder, SWT.NULL);
		result.setLayout(getPageLayout());
		tab.setControl(result);
		parentFolder.setSelection(0);
		return result;
	}

	/**
	 * Return the layout for the page container.
	 * @return
	 */
	private Layout getPageLayout() {
		return new Layout() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
			 */
			protected Point computeSize(Composite composite, int wHint, int hHint,
					boolean flushCache) {

				if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
					return new Point(wHint, hHint);
				int x = minimumSize.x;
				int y = minimumSize.y;
				Control[] children = composite.getChildren();
				for (int i = 0; i < children.length; i++) {
					Point size = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					x = Math.max(x, size.x);
					y = Math.max(y, size.y);
				}
				
				x += IDialogConstants.HORIZONTAL_MARGIN * 2;
				y += IDialogConstants.VERTICAL_MARGIN * 2;
				
				if (wHint != SWT.DEFAULT)
					x = wHint;
				if (hHint != SWT.DEFAULT)
					y = hHint;
				return new Point(x, y);

			}

			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
			 */
			protected void layout(Composite composite, boolean flushCache) {
				Rectangle rect = composite.getClientArea();
				Control[] children = composite.getChildren();
				for (int i = 0; i < children.length; i++) {
					children[i].setBounds(IDialogConstants.HORIZONTAL_MARGIN,
							IDialogConstants.VERTICAL_MARGIN,
							rect.width - (2 * IDialogConstants.HORIZONTAL_MARGIN),
							rect.height - (2 * IDialogConstants.VERTICAL_MARGIN)
							);
				}
			}

		};
	}

	/**
	 * Get the toolbar for the container
	 * @return Control
	 */
	private Control getContainerToolBar(Composite parentFolder) {
		return history.createHistoryControls(parentFolder);
	}

	/**
	 * Highlight all nodes that match text;
	 * @param text
	 */
	protected void highlightHits(String text) {
		WorkbenchPreferenceGroup group = (WorkbenchPreferenceGroup) getTreeViewer().getInput();

		group.highlightHits(text);
		getTreeViewer().refresh();
	}

	/**
	 * Create a generic list viewer for entry selection.
	 * 
	 * @param composite
	 * @return GenericListViewer
	 */
	private ToolBar createToolBar(Composite composite) {

		toolbar = new ToolBar(composite, SWT.HORIZONTAL | SWT.CENTER | SWT.FLAT);
		toolbar.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		WorkbenchPreferenceGroup[] groups = getGroups();

		for (int i = 0; i < groups.length; i++) {
			final WorkbenchPreferenceGroup group = groups[i];
			ToolItem newItem = new ToolItem(toolbar, SWT.RADIO);
			newItem.setText(group.getName());
			newItem.setImage(group.getImage());
			newItem.setData(group);
			newItem.addSelectionListener(new SelectionAdapter() {
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					groupSelected(group);
				}
			});

		}

		return toolbar;
	}

	/**
	 * Return the label provider for the categories.
	 * @return ILabelProvider
	 */
	private ILabelProvider getCategoryLabelProvider() {
		return new LabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
			 */
			public Image getImage(Object element) {
				return ((WorkbenchPreferenceGroup) element).getImage();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((WorkbenchPreferenceGroup) element).getName();
			}
		};
	}

	/**
	 * Return a content provider for the categories.
	 * 
	 * @return IContentProvider
	 */
	private IContentProvider getCategoryContentProvider() {
		return new ListContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.dialogs.ListContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object input) {
				return getGroups();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		TreeViewer tree = super.createTreeViewer(parent);

		if (hasGroups()) {
			groupedLabelProvider = new GroupedPreferenceLabelProvider();
			tree.setContentProvider(new GroupedPreferenceContentProvider());
			tree.setLabelProvider(groupedLabelProvider);
			tree.addSelectionChangedListener(new ISelectionChangedListener() {
				/* (non-Javadoc)
				 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
				 */
				public void selectionChanged(SelectionChangedEvent event) {
					if (event.getSelection() instanceof IStructuredSelection) {
						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();
						if (selection.isEmpty())
							return;
						Object item = selection.getFirstElement();
						currentGroup.setLastSelection(item);
					}
				}
			});

		} else {
			tree.setContentProvider(new FilteredPreferenceContentProvider());
			tree.setLabelProvider(new PreferenceLabelProvider());
		}
		return tree;
	}

	/**
	 * Differs from super implementation in that if the node is found but should
	 * be filtered based on a call to
	 * <code>WorkbenchActivityHelper.filterItem()</code> then
	 * <code>null</code> is returned.
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#findNodeMatching(java.lang.String)
	 */
	protected IPreferenceNode findNodeMatching(String nodeId) {
		IPreferenceNode node = super.findNodeMatching(nodeId);
		if (WorkbenchActivityHelper.filterItem(node))
			return null;
		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#selectSavedItem()
	 */
	protected void selectSavedItem() {
		if (hasGroups()) {

			WorkbenchPreferenceGroup startGroup = null;

			if (lastGroupId != null) {
				ToolItem[] items = toolbar.getItems();
				for (int i = 0; i < items.length; i++) {
					ToolItem item = items[i];
					WorkbenchPreferenceGroup group = (WorkbenchPreferenceGroup) items[i].getData();
					if (lastGroupId.equals(group.getId())) {
						item.setSelection(true);
						startGroup = group;
						break;
					}
				}
			}
			if (startGroup == null) {
				toolbar.getItem(0).setSelection(true);
				startGroup = getGroups()[0];
			}
			groupSelected(startGroup);
		} else
			super.selectSavedItem();
	}

	/**
	 * Return the categories in the receiver.
	 * @return WorkbenchPreferenceGroup[]
	 */
	protected WorkbenchPreferenceGroup[] getGroups() {
		return ((WorkbenchPreferenceManager) WorkbenchPlugin.getDefault().getPreferenceManager())
				.getGroups();
	}

	/**
	 * Return whether or not there are categories.
	 * @return boolean
	 */
	protected boolean hasGroups() {
		return getGroups().length > 0;
	}

	/**
	 * A group has been selected. Update the tree viewer.
	 * @param group
	 */
	private void groupSelected(final WorkbenchPreferenceGroup group) {

		lastGroupId = group.getId();
		currentGroup = group;

		getTreeViewer().setInput(group);
		Object selection = group.getLastSelection();
		if (selection == null)
			selection = group.getGroupsAndNodes()[0];
		getTreeViewer().setSelection(new StructuredSelection(selection), true);
	}

	/**
	 * Set the search results of the receiver to be filteredIds.
	 * @param filteredIds
	 */
	protected void setSearchResults(String[] filteredIds) {

		WorkbenchPreferenceGroup[] groups = getGroups();
		for (int i = 0; i < groups.length; i++) {
			WorkbenchPreferenceGroup group = groups[i];
			group.highlightIds(filteredIds);
		}

	}

	/*
	 * @see org.eclipse.jface.preference.PreferenceDialog#showPage(org.eclipse.jface.preference.IPreferenceNode)
	 * @since 3.1
	 */
	protected boolean showPage(IPreferenceNode node) {
		final boolean success = super.showPage(node);
		if (success) {
			history.addHistoryEntry(new PreferenceHistoryEntry(node.getId(), node.getLabelText(),
					null));
		}
		return success;
	}

	//Create a preference page history
	protected PreferencePageHistory createHistory() {
		return new PreferencePageHistory(this);
	}

	/**
	 * Selects the current page based on the given preference page identifier.
	 * If no node can be found, then nothing will change.
	 * 
	 * @param preferencePageId
	 *            The preference page identifier to select; should not be
	 *            <code>null</code>.
	 */
	public final void setCurrentPageId(final String preferencePageId) {
		final IPreferenceNode node = findNodeMatching(preferencePageId);
		if (node != null) {
			getTreeViewer().setSelection(new StructuredSelection(node));
			showPage(node);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateMessage()
	 */
	public void updateMessage() {
		//Do nothing as this is done by the page now
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateTitle()
	 */
	public void updateTitle() {
		tab.setText(getCurrentPage().getTitle());
		tab.setImage(getCurrentPage().getImage());
	}

	/**
	 * Return the history for the receiver.
	 * @return Returns the history.
	 */
	protected PreferencePageHistory getHistory() {
		return this.history;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPageControl(org.eclipse.jface.preference.IPreferencePage, org.eclipse.swt.widgets.Composite)
	 */
	protected void createPageControl(IPreferencePage page, Composite parent) {
		if(page instanceof PreferencePage)
			((PreferencePage) page).createControl(parent,true);
		else
			super.createPageControl(page, parent);
	}
}