/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton) <francisu@ieee.org> - bug 219273
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceContentProvider;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.model.IContributionService;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.service.prefs.BackingStoreException;


/**
 * Baseclass for preference dialogs that will show two tabs of preferences -
 * filtered and unfiltered.
 * 
 * @since 3.0
 */
public abstract class FilteredPreferenceDialog extends PreferenceDialog
		implements IWorkbenchPreferenceContainer {

	/**
	 * 
	 */
	private static final int PAGE_MULTIPLIER = 9;

	/**
	 * 
	 */
	private static final int INCREMENT = 10;

	protected class PreferenceFilteredTree extends FilteredTree {
		/**
		 * An (optional) additional filter on the TreeViewer.
		 */
		private ViewerFilter viewerFilter;

		/**
		 * Initial title of dialog. This is only used if the additional filter
		 * provided by the addFilter(ViewerFilter) method is utilized.
		 */
		private String cachedTitle;

		/**
		 * Constructor.
		 * 
		 * @param parent
		 *            parent Composite
		 * @param treeStyle
		 *            SWT style bits for Tree
		 * @param filter
		 *            the PatternFilter to use for the TreeViewer
		 */
		PreferenceFilteredTree(Composite parent, int treeStyle,
				PatternFilter filter) {
			super(parent, treeStyle, filter, true);
		}

		/**
		 * Add an additional, optional filter to the viewer. If the filter text
		 * is cleared, this filter will be removed from the TreeViewer.
		 * 
		 * @param filter
		 */
		protected void addFilter(ViewerFilter filter) {
			viewerFilter = filter;
			getViewer().addFilter(filter);

			if (filterText != null) {
				setFilterText(WorkbenchMessages.FilteredTree_FilterMessage);
				textChanged();
			}

			cachedTitle = getShell().getText();
			getShell().setText(
					NLS.bind(
							WorkbenchMessages.FilteredTree_FilteredDialogTitle,
							cachedTitle));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.FilteredTree#updateToolbar(boolean)
		 */
		protected void updateToolbar(boolean visible) {
			super.updateToolbar(viewerFilter != null || visible);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.FilteredTree#clearText()
		 */
		protected void clearText() {
			setFilterText(""); //$NON-NLS-1$
			// remove the filter if text is cleared
			if (!locked && viewerFilter != null) {
				getViewer().removeFilter(viewerFilter);
				viewerFilter = null;
				getShell().setText(cachedTitle);
			}
			textChanged();
		}
	}

	protected PreferenceFilteredTree filteredTree;

	private Object pageData;

	IWorkingCopyManager workingCopyManager;

	private Collection updateJobs = new ArrayList();

	/**
	 * The preference page history.
	 * 
	 * @since 3.1
	 */
	PreferencePageHistory history;

	private Sash sash;

	private IHandlerActivation showViewHandler;

	private boolean locked;

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
		history = new PreferencePageHistory(this);
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
		if (WorkbenchActivityHelper.filterItem(node)) {
			return null;
		}
		return node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		int styleBits = SWT.SINGLE;
		TreeViewer tree;
		if (!hasAtMostOnePage()) {
			filteredTree= new PreferenceFilteredTree(parent, styleBits, new PreferencePatternFilter());
			GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.horizontalIndent= IDialogConstants.HORIZONTAL_MARGIN;
			filteredTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

			tree= filteredTree.getViewer();
		} else
			tree= new TreeViewer(parent, styleBits);

		setContentAndLabelProviders(tree);
		tree.setInput(getPreferenceManager());


		tree.addFilter(new CapabilityFilter());

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				handleTreeSelectionChanged(event);
			}
		});

		super.addListeners(tree);
		return tree;
	}

	/**
	 * Return whether or not there are less than two pages.
	 * 
	 * @return <code>true</code> if there are less than two pages.
	 */
	private boolean hasAtMostOnePage() {
		ITreeContentProvider contentProvider= new PreferenceContentProvider();
		try {
			Object[] children= contentProvider.getElements(getPreferenceManager());
			return children.length == 0 || children.length == 1 && !contentProvider.hasChildren(children[0]);
		} finally {
			contentProvider.dispose();
		}
	}

	/**
	 * Set the content and label providers for the treeViewer
	 * 
	 * @param treeViewer
	 */
	protected void setContentAndLabelProviders(TreeViewer treeViewer) {
		if (hasAtMostOnePage()) {
			treeViewer.setLabelProvider(new PreferenceLabelProvider());
		} else {
			treeViewer.setLabelProvider(new PreferenceBoldLabelProvider(filteredTree));
		}
		IContributionService cs = (IContributionService) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getService(
						IContributionService.class);
		treeViewer.setComparator(cs.getComparatorFor(getContributionType()));
		treeViewer.setContentProvider(new PreferenceContentProvider());
	}

	/**
	 * Return the contributionType (used by the IContributionService).
	 * 
	 * Override this with a more specific contribution type as required.
	 * 
	 * @return a string, the contributionType
	 */
	protected String getContributionType() {
		return IContributionService.TYPE_PREFERENCE;
	}

	/**
	 * A selection has been made in the tree.
	 * 
	 * @param event
	 *            SelectionChangedEvent
	 */
	protected void handleTreeSelectionChanged(SelectionChangedEvent event) {
		// Do nothing by default
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeAreaContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createTreeAreaContents(Composite parent) {
		Composite leftArea = new Composite(parent, SWT.NONE);
		leftArea.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		leftArea.setFont(parent.getFont());
		GridLayout leftLayout = new GridLayout();
		leftLayout.numColumns = 1;
		leftLayout.marginHeight = 0;
		leftLayout.marginTop = IDialogConstants.VERTICAL_MARGIN;
		leftLayout.marginWidth = IDialogConstants.HORIZONTAL_MARGIN;
		leftLayout.horizontalSpacing = 0;
		leftLayout.verticalSpacing = 0;
		leftArea.setLayout(leftLayout);

		// Build the tree an put it into the composite.
		TreeViewer viewer = createTreeViewer(leftArea);
		setTreeViewer(viewer);

		updateTreeFont(JFaceResources.getDialogFont());
		GridData viewerData = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_VERTICAL);
		viewer.getControl().getParent().setLayoutData(viewerData);

		layoutTreeAreaControl(leftArea);

		return leftArea;
	}

	/**
	 * Show only the supplied ids.
	 * 
	 * @param filteredIds
	 */
	public void showOnly(String[] filteredIds) {
		if (!hasAtMostOnePage()) {
			filteredTree.addFilter(new PreferenceNodeFilter(filteredIds));
		}
	}

	/**
	 * Set the data to be applied to a page after it is created.
	 * 
	 * @param pageData
	 *            Object
	 */
	public void setPageData(Object pageData) {
		this.pageData = pageData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPage(org.eclipse.jface.preference.IPreferenceNode)
	 */
	protected void createPage(IPreferenceNode node) {

		super.createPage(node);
		if (this.pageData == null) {
			return;
		}
		// Apply the data if it has been set.
		IPreferencePage page = node.getPage();
		if (page instanceof PreferencePage) {
			((PreferencePage) page).applyData(this.pageData);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#getCurrentPage()
	 */
	public IPreferencePage getCurrentPage() {
		return super.getCurrentPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.IWorkbenchPreferenceContainer#openPage(java.lang.String,
	 *      java.lang.Object)
	 */
	public boolean openPage(String pageId, Object data) {
		setPageData(data);
		setCurrentPageId(pageId);
		IPreferencePage page = getCurrentPage();
		if (page instanceof PreferencePage) {
			((PreferencePage) page).applyData(data);
		}
		return true;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.IWorkbenchPreferenceContainer#getWorkingCopyManager()
	 */
	public IWorkingCopyManager getWorkingCopyManager() {
		if (workingCopyManager == null) {
			workingCopyManager = new WorkingCopyManager();
		}
		return workingCopyManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		super.okPressed();

		if (getReturnCode() == FAILED) {
			return;
		}

		if (workingCopyManager != null) {
			try {
				workingCopyManager.applyChanges();
			} catch (BackingStoreException e) {
				String msg = e.getMessage();
				if (msg == null) {
					msg = WorkbenchMessages.FilteredPreferenceDialog_PreferenceSaveFailed;
				}
				StatusUtil
						.handleStatus(
								WorkbenchMessages.PreferencesExportDialog_ErrorDialogTitle
										+ ": " + msg, e, StatusManager.SHOW, //$NON-NLS-1$
								getShell());
			}
		}

		// Run the update jobs
		Iterator updateIterator = updateJobs.iterator();
		while (updateIterator.hasNext()) {
			((Job) updateIterator.next()).schedule();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.IWorkbenchPreferenceContainer#registerUpdateJob(org.eclipse.core.runtime.jobs.Job)
	 */
	public void registerUpdateJob(Job job) {
		updateJobs.add(job);
	}

	/**
	 * Get the toolbar for the container
	 * 
	 * @return Control
	 */
	Control getContainerToolBar(Composite composite) {

		final ToolBarManager historyManager = new ToolBarManager(SWT.HORIZONTAL
				| SWT.FLAT);
		historyManager.createControl(composite);

		history.createHistoryControls(historyManager.getControl(),
				historyManager);

		Action popupMenuAction = new Action() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#getImageDescriptor()
			 */
			public ImageDescriptor getImageDescriptor() {
				return WorkbenchImages
						.getImageDescriptor(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				MenuManager manager = new MenuManager();
				manager.add(new Action() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.action.Action#run()
					 */
					public void run() {
						
						sash.addFocusListener(new FocusAdapter() {
							public void focusGained(FocusEvent e) {
								sash.setBackground(sash.getDisplay()
										.getSystemColor(
												SWT.COLOR_LIST_SELECTION));
							}

							public void focusLost(FocusEvent e) {
								sash.setBackground(sash.getDisplay()
										.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
							}
						});
						sash.setFocus();
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.action.Action#getText()
					 */
					public String getText() {
						return WorkbenchMessages.FilteredPreferenceDialog_Resize;
					}
				});
				manager.add(new Action() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.action.Action#run()
					 */
					public void run() {
						activeKeyScrolling();
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.action.Action#getText()
					 */
					public String getText() {
						return WorkbenchMessages.FilteredPreferenceDialog_Key_Scrolling;
					}
				});
				Menu menu = manager.createContextMenu(getShell());
				Rectangle bounds = historyManager.getControl().getBounds();
				Point topLeft = new Point(bounds.x + bounds.width, bounds.y + bounds.height);
				topLeft = historyManager.getControl().toDisplay(topLeft);
				menu.setLocation(topLeft.x, topLeft.y);
				menu.setVisible(true);
			}
		};
		popupMenuAction.setToolTipText(WorkbenchMessages.FilteredPreferenceDialog_FilterToolTip);
		historyManager.add(popupMenuAction);
		IHandlerService service = (IHandlerService) PlatformUI.getWorkbench()
				.getService(IHandlerService.class);
		showViewHandler = service
				.activateHandler(
						IWorkbenchCommandConstants.WINDOW_SHOW_VIEW_MENU,
						new ActionHandler(popupMenuAction),
						new ActiveShellExpression(getShell()));

		historyManager.update(false);

		return historyManager.getControl();
	}
	
	private boolean keyScrollingEnabled = false;
	private Listener keyScrollingFilter = null;

	void activeKeyScrolling() {
		if (keyScrollingFilter == null) {
			Composite pageParent = getPageContainer().getParent();
			if (!(pageParent instanceof ScrolledComposite)) {
				return;
			}
			final ScrolledComposite sc = (ScrolledComposite) pageParent;
			keyScrollingFilter = new Listener() {
				public void handleEvent(Event event) {
					if (!keyScrollingEnabled || sc.isDisposed()) {
						return;
					}
					switch (event.keyCode) {
					case SWT.ARROW_DOWN:
						sc.setOrigin(sc.getOrigin().x, sc.getOrigin().y
								+ INCREMENT);
						break;
					case SWT.ARROW_UP:
						sc.setOrigin(sc.getOrigin().x, sc.getOrigin().y
								- INCREMENT);
						break;
					case SWT.ARROW_LEFT:
						sc.setOrigin(sc.getOrigin().x - INCREMENT, sc
								.getOrigin().y);
						break;
					case SWT.ARROW_RIGHT:
						sc.setOrigin(sc.getOrigin().x + INCREMENT, sc
								.getOrigin().y);
						break;
					case SWT.PAGE_DOWN:
						sc.setOrigin(sc.getOrigin().x, sc.getOrigin().y
								+ PAGE_MULTIPLIER * INCREMENT);
						break;
					case SWT.PAGE_UP:
						sc.setOrigin(sc.getOrigin().x, sc.getOrigin().y
								- PAGE_MULTIPLIER * INCREMENT);
						break;
					case SWT.HOME:
						sc.setOrigin(0, 0);
						break;
					case SWT.END:
						sc.setOrigin(0, sc.getSize().y);
						break;
					default:
						keyScrollingEnabled = false;
					}
					event.type = SWT.None;
					event.doit = false;
				}
			};
			Display display = PlatformUI.getWorkbench().getDisplay();
			display.addFilter(SWT.KeyDown, keyScrollingFilter);
			display.addFilter(SWT.Traverse, keyScrollingFilter);
			sc.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					removeKeyScrolling();
				}
			});
		}
		keyScrollingEnabled = true;
	}
	
	void removeKeyScrolling() {
		if (keyScrollingFilter != null) {
			keyScrollingEnabled = false;
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (display != null) {
				display.removeFilter(SWT.KeyDown, keyScrollingFilter);
				display.removeFilter(SWT.Traverse, keyScrollingFilter);
			}
			keyScrollingFilter = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#showPage(org.eclipse.jface.preference.IPreferenceNode)
	 */
	protected boolean showPage(IPreferenceNode node) {
		final boolean success = super.showPage(node);
		if (success) {
			history.addHistoryEntry(new PreferenceHistoryEntry(node.getId(),
					node.getLabelText(), null));
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (showViewHandler != null) {
			IHandlerService service = (IHandlerService) PlatformUI
					.getWorkbench().getService(IHandlerService.class);
			service.deactivateHandler(showViewHandler);
			showViewHandler.getHandler().dispose();
			showViewHandler = null;
		}
		removeKeyScrolling();
		history.dispose();
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTitleArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Composite createTitleArea(Composite parent) {

		GridLayout parentLayout = (GridLayout) parent.getLayout();
		parentLayout.numColumns = 2;
		parentLayout.marginHeight = 0;
		parentLayout.marginTop = IDialogConstants.VERTICAL_MARGIN;
		parent.setLayout(parentLayout);

		Composite titleComposite = super.createTitleArea(parent);

		Composite toolbarArea = new Composite(parent, SWT.NONE);
		GridLayout toolbarLayout = new GridLayout();
		toolbarLayout.marginHeight = 0;
		toolbarLayout.verticalSpacing = 0;
		toolbarArea.setLayout(toolbarLayout);
		toolbarArea.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		Control topBar = getContainerToolBar(toolbarArea);
		topBar.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));

		return titleComposite;
	}

	protected void selectSavedItem() {
		getTreeViewer().setInput(getPreferenceManager());
		super.selectSavedItem();
		if (getTreeViewer().getTree().getItemCount() > 1) {
			// unfortunately super will force focus to the list but we want the
			// type ahead combo to get it.
			Text filterText = filteredTree.getFilterControl();
			if (filterText != null) {
				filterText.setFocus();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateTreeFont(org.eclipse.swt.graphics.Font)
	 */
	protected void updateTreeFont(Font dialogFont) {
		if (hasAtMostOnePage()) {
			Composite composite= getTreeViewer().getTree();
			applyDialogFont(composite, dialogFont);
			composite.layout(true);
		} else {
			applyDialogFont(filteredTree, dialogFont);
			filteredTree.layout(true);
		}
	}

	/**
	 * Apply the dialog font to the given control and it's children.
	 * 
	 * @param control the control
	 * @param dialogFont the dialog font
	 */
	private void applyDialogFont(Control control, Font dialogFont) {
		control.setFont(dialogFont);
		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			for (int i = 0; i < children.length; i++) {
				applyDialogFont(children[i], dialogFont);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createSash(org.eclipse.swt.widgets.Composite, org.eclipse.swt.widgets.Control)
	 */
	protected Sash createSash(Composite composite, Control rightControl) {
		sash = super.createSash(composite, rightControl);
		return sash;
	}

	/**
	 * <code>true</code> if upon clearing the filter field, the list of pages
	 * should not be reset to all property or preference pages.
	 * 
	 * @param b
	 */
	public void setLocked(boolean b) {
		this.locked = b;
	}
}
