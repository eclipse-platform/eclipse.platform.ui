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
package org.eclipse.jface.preference;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogMessageArea;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/**
 * A preference dialog is a hierarchical presentation of preference pages. Each
 * page is represented by a node in the tree shown on the left hand side of the
 * dialog; when a node is selected, the corresponding page is shown on the right
 * hand side.
 */
public class PreferenceDialog extends Dialog implements IPreferencePageContainer {
	/**
	 * Layout for the page container.
	 *  
	 */
	private class PageLayout extends Layout {
		public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
			int x = minimumPageSize.x;
			int y = minimumPageSize.y;
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				Point size = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}
			if (wHint != SWT.DEFAULT)
				x = wHint;
			if (hHint != SWT.DEFAULT)
				y = hHint;
			return new Point(x, y);
		}

		public void layout(Composite composite, boolean force) {
			Rectangle rect = composite.getClientArea();
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				children[i].setSize(rect.width, rect.height);
			}
		}
	}

	//The id of the last page that was selected
	private static String lastPreferenceId = null;

	//The last known tree width
	private static int lastTreeWidth = 150;

	/**
	 * Indentifier for the error image
	 */
	public static final String PREF_DLG_IMG_TITLE_ERROR = DLG_IMG_MESSAGE_ERROR; //$NON-NLS-1$

	/**
	 * Title area fields
	 */
	public static final String PREF_DLG_TITLE_IMG = "preference_dialog_title_image"; //$NON-NLS-1$
	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(PREF_DLG_TITLE_IMG, ImageDescriptor.createFromFile(PreferenceDialog.class,
				"images/pref_dialog_title.gif")); //$NON-NLS-1$
	}

	/**
	 * The current preference page, or <code>null</code> if there is none.
	 */
	private IPreferencePage currentPage;

	private DialogMessageArea messageArea;

	/**
	 * Indicates whether help is available; <code>false</code> by default.'
	 * 
	 * @see #setHelpAvailable
	 */
	private boolean isHelpAvailable = false;

	private Point lastShellSize;

	private IPreferenceNode lastSuccessfulNode;

	/**
	 * The minimum page size; 400 by 400 by default.
	 * 
	 * @see #setMinimumPageSize(Point)
	 */
	private Point minimumPageSize = new Point(400, 400);

	/**
	 * The OK button.
	 */
	private Button okButton;

	/**
	 * The Composite in which a page is shown.
	 */
	private Composite pageContainer;

	/**
	 * The preference manager.
	 */
	private PreferenceManager preferenceManager;

	/**
	 * Flag for the presence of the error message.
	 */
	private boolean showingError = false;

	/**
	 * Preference store, initially <code>null</code> meaning none.
	 * 
	 * @see #setPreferenceStore
	 */
	private IPreferenceStore preferenceStore;

	private Composite titleArea;

	private Label titleImage;

	/**
	 * The tree viewer.
	 */
	private TreeViewer treeViewer;

	/**
	 * Creates a new preference dialog under the control of the given preference
	 * manager.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param manager
	 *            the preference manager
	 */
	public PreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		preferenceManager = manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case IDialogConstants.OK_ID: {
			okPressed();
			return;
		}
		case IDialogConstants.CANCEL_ID: {
			cancelPressed();
			return;
		}
		case IDialogConstants.HELP_ID: {
			helpPressed();
			return;
		}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		// Inform all pages that we are cancelling
		Iterator nodes = preferenceManager.getElements(PreferenceManager.PRE_ORDER).iterator();
		while (nodes.hasNext()) {
			final IPreferenceNode node = (IPreferenceNode) nodes.next();
			if (getPage(node) != null) {
				Platform.run(new SafeRunnable() {
					public void run() {
						if (!getPage(node).performCancel())
							return;
					}
				});
			}
		}
		setReturnCode(CANCEL);
		close();
	}

	/**
	 * Clear the last selected node. This is so that we not chache the last
	 * selection in case of an error.
	 */
	void clearSelectedNode() {
		setSelectedNodePreference(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		List nodes = preferenceManager.getElements(PreferenceManager.PRE_ORDER);
		for (int i = 0; i < nodes.size(); i++) {
			IPreferenceNode node = (IPreferenceNode) nodes.get(i);
			node.disposeResources();
		}
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(JFaceResources.getString("PreferenceDialog.title")); //$NON-NLS-1$
		newShell.addShellListener(new ShellAdapter() {
			public void shellActivated(ShellEvent e) {
				if (lastShellSize == null)
					lastShellSize = getShell().getSize();
			}

		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#constrainShellSize()
	 */
	protected void constrainShellSize() {
		super.constrainShellSize();
		// record opening shell size
		if (lastShellSize == null)
			lastShellSize = getShell().getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		getShell().setDefaultButton(okButton);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		if (isHelpAvailable) {
			createButton(parent, IDialogConstants.HELP_ID, IDialogConstants.HELP_LABEL, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(final Composite parent) {
		final Control[] control = new Control[1];
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				control[0] = PreferenceDialog.super.createContents(parent);
				// Add the first page
				selectSavedItem();
			}
		});

		return control[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 3;
		Control treeControl = createTreeAreaContents(composite);
		createSash(composite,treeControl);
		Composite pageAreaComposite = new Composite(composite, SWT.NONE);
		pageAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 10;
		pageAreaComposite.setLayout(layout);
		// Build the title area and separator line
		Composite titleComposite = new Composite(pageAreaComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		titleComposite.setLayout(layout);
		titleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createTitleArea(titleComposite);
		// Build the Page container
		pageContainer = createPageContainer(pageAreaComposite);
		pageContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Build the separator line
		Label separator = new Label(pageAreaComposite, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(gd);
		return composite;
	}

	/**
	 * Create the pageArea in the composite.
	 * @param composite
	 * @param layout
	 */
	private void createPageArea(Composite composite, GridLayout layout) {
		Composite pageAreaComposite = new Composite(composite, SWT.NONE);
		pageAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout pageAreaLayout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 10;
		pageAreaComposite.setLayout(pageAreaLayout);
		
		// Build the Page container
		pageContainer = createPageContainer(pageAreaComposite);
		pageContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Build the separator line
		Label separator = new Label(pageAreaComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(gd);
	}

	/**
	 * Create the sash with right control on the right. Note 
	 * that this method assumes GridData for the layout data
	 * of the rightControl.
	 * @param composite
	 * @param rightControl
	 * @return Sash
	 */
	protected Sash createSash(final Composite composite, final Control rightControl) {
		final Sash sash = new Sash(composite, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		// the following listener resizes the tree control based on sash deltas.
		// If necessary, it will also grow/shrink the dialog.
		sash.addListener(SWT.Selection, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				if (event.detail == SWT.DRAG)
					return;
				int shift = event.x - sash.getBounds().x;
				GridData data = (GridData) rightControl.getLayoutData();
				int newWidthHint = data.widthHint + shift;
				if (newWidthHint < 20)
					return;
				Point computedSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point currentSize = getShell().getSize();
				// if the dialog wasn't of a custom size we know we can shrink
				// it if necessary based on sash movement.
				boolean customSize = !computedSize.equals(currentSize);
				data.widthHint = newWidthHint;
				setLastTreeWidth(newWidthHint);
				composite.layout(true);
				// recompute based on new widget size
				computedSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				// if the dialog was of a custom size then increase it only if
				// necessary.
				if (customSize)
					computedSize.x = Math.max(computedSize.x, currentSize.x);
				computedSize.y = Math.max(computedSize.y, currentSize.y);
				if (computedSize.equals(currentSize))
					return;
				setShellSize(computedSize.x, computedSize.y);
				lastShellSize = getShell().getSize();
			}
		});
		return sash;
	}

	/**
	 * Creates the inner page container.
	 * 
	 * @param parent
	 * @return Composite
	 */
	protected Composite createPageContainer(Composite parent) {
		Composite result = new Composite(parent, SWT.NULL);
		result.setLayout(new PageLayout());
		return result;
	}

	/**
	 * Creates the wizard's title area.
	 * 
	 * @param parent
	 *            the SWT parent for the title area composite.
	 * @return the created title area composite.
	 */
	protected Composite createTitleArea(Composite parent) {
		// Create the title area which will contain
		// a title, message, and image.
		int margins = 2;
		titleArea = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginHeight = margins;
		layout.marginWidth = margins;
		titleArea.setLayout(layout);
		// Get the background color for the title area
		Display display = parent.getDisplay();
		Color background = JFaceColors.getBannerBackground(display);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = JFaceResources.getImage(PREF_DLG_TITLE_IMG).getBounds().height
				+ (margins * 3);
		titleArea.setLayoutData(layoutData);
		titleArea.setBackground(background);

		titleArea.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(titleArea.getDisplay().getSystemColor(
						SWT.COLOR_WIDGET_NORMAL_SHADOW));
				Rectangle bounds = titleArea.getClientArea();
				bounds.height = bounds.height - 2;
				bounds.width = bounds.width - 1;
				e.gc.drawRectangle(bounds);
			}
		});

		// Message label
		messageArea = new DialogMessageArea();
		messageArea.createContents(titleArea);

		titleArea.addControlListener(new ControlAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
			 */
			public void controlResized(ControlEvent e) {
				updateMessage();
			}
		});

		final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JFaceResources.BANNER_FONT.equals(event.getProperty()))
					updateMessage();
				if (JFaceResources.DIALOG_FONT.equals(event.getProperty())) {
					updateMessage();
					Font dialogFont = JFaceResources.getDialogFont();
					updateTreeFont(dialogFont);
					Control[] children = ((Composite) buttonBar).getChildren();
					for (int i = 0; i < children.length; i++)
						children[i].setFont(dialogFont);
				}
			}
		};

		titleArea.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});
		JFaceResources.getFontRegistry().addListener(fontListener);
		// Title image
		titleImage = new Label(titleArea, SWT.LEFT);
		titleImage.setBackground(background);
		titleImage.setImage(JFaceResources.getImage(PREF_DLG_TITLE_IMG));
		FormData imageData = new FormData();
		imageData.right = new FormAttachment(100);
		imageData.top = new FormAttachment(0);
		imageData.bottom = new FormAttachment(100);
		titleImage.setLayoutData(imageData);
		messageArea.setTitleLayoutData(createMessageAreaData());
		messageArea.setMessageLayoutData(createMessageAreaData());

		return titleArea;
	}

	/**
	 * Create the layout data for the message area.
	 * 
	 * @return FormData for the message area.
	 */
	private FormData createMessageAreaData() {
		FormData messageData = new FormData();
		messageData.top = new FormAttachment(0);
		messageData.bottom = new FormAttachment(titleImage, 0, SWT.BOTTOM);
		messageData.right = new FormAttachment(titleImage, 0);
		messageData.left = new FormAttachment(0);
		return messageData;
	}

	/**
	 * @param parent
	 *            the SWT parent for the tree area controls.
	 * @return the new <code>Control</code>.
	 * @since 3.0
	 */
	protected Control createTreeAreaContents(Composite parent) {
		// Build the tree an put it into the composite.
		treeViewer = createTreeViewer(parent);
		treeViewer.setInput(getPreferenceManager());
		updateTreeFont(JFaceResources.getDialogFont());
		layoutTreeAreaControl(treeViewer.getControl());
		return treeViewer.getControl();
	}

	/**
	 * Create a new <code>TreeViewer</code>.
	 * 
	 * @param parent
	 *            the parent <code>Composite</code>.
	 * @return the <code>TreeViewer</code>.
	 * @since 3.0
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		final TreeViewer viewer = new TreeViewer(parent, SWT.NONE);
		addListeners(viewer);
		viewer.setLabelProvider(new PreferenceLabelProvider());
		viewer.setContentProvider(new PreferenceContentProvider());
		return viewer;
	}

	/**
	 * Add the listeners to the tree viewer.
	 * @param viewer
	 */
	protected void addListeners(final TreeViewer viewer) {
		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			private void handleError() {
				try {
					// remove the listener temporarily so that the events caused
					// by the error handling dont further cause error handling
					// to occur.
					viewer.removePostSelectionChangedListener(this);
					showPageFlippingAbortDialog();
					selectCurrentPageAgain();
					clearSelectedNode();
				} finally {
					viewer.addPostSelectionChangedListener(this);
				}
			}

			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = getSingleSelection(event.getSelection());
				if (selection instanceof IPreferenceNode) {
					if (!isCurrentPageValid()) {
						handleError();
					} else if (!showPage((IPreferenceNode) selection)) {
						// Page flipping wasn't successful
						handleError();
					} else {
						// Everything went well
						lastSuccessfulNode = (IPreferenceNode) selection;
					}
					viewer.getControl().setFocus();
				}
			}
		});
		((Tree) viewer.getControl()).addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(final SelectionEvent event) {
				ISelection selection = viewer.getSelection();
				if (selection.isEmpty())
					return;
				IPreferenceNode singleSelection = getSingleSelection(selection);
				boolean expanded = viewer.getExpandedState(singleSelection);
				viewer.setExpandedState(singleSelection, !expanded);
			}
		});
		//Register help listener on the tree to use context sensitive help
		viewer.getControl().addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent event) {
				// call perform help on the current page
				if (currentPage != null) {
					currentPage.performHelp();
				}
			}
		});
	}

	/**
	 * Find the <code>IPreferenceNode</code> that has data the same id as the
	 * supplied value.
	 * 
	 * @param nodeId
	 *            the id to search for.
	 * @return <code>IPreferenceNode</code> or <code>null</code> if not
	 *         found.
	 */
	protected IPreferenceNode findNodeMatching(String nodeId) {
		List nodes = preferenceManager.getElements(PreferenceManager.POST_ORDER);
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			IPreferenceNode node = (IPreferenceNode) i.next();
			if (node.getId().equals(nodeId))
				return node;
		}
		return null;
	}

	/**
	 * Get the last known right side width.
	 * 
	 * @return the width.
	 */
	protected int getLastRightWidth() {
		return lastTreeWidth;
	}

	/**
	 * Returns the preference mananger used by this preference dialog.
	 * 
	 * @return the preference mananger
	 */
	public PreferenceManager getPreferenceManager() {
		return preferenceManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}

	/**
	 * Get the name of the selected item preference
	 * 
	 * @return String
	 */
	protected String getSelectedNodePreference() {
		return lastPreferenceId;
	}

	/**
	 * @param selection
	 *            the <code>ISelection</code> to examine.
	 * @return the first element, or null if empty.
	 */
	protected IPreferenceNode getSingleSelection(ISelection selection) {
		if (!selection.isEmpty()) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			if (structured.getFirstElement() instanceof IPreferenceNode)
				return (IPreferenceNode) structured.getFirstElement();
		}
		return null;
	}

	/**
	 * @return the <code>TreeViewer</code> for this dialog.
	 * @since 3.0
	 */
	protected TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * Save the values specified in the pages.
	 * <p>
	 * The default implementation of this framework method saves all pages of
	 * type <code>PreferencePage</code> (if their store needs saving and is a
	 * <code>PreferenceStore</code>).
	 * </p>
	 * <p>
	 * Subclasses may override.
	 * </p>
	 */
	protected void handleSave() {
		Iterator nodes = preferenceManager.getElements(PreferenceManager.PRE_ORDER).iterator();
		while (nodes.hasNext()) {
			IPreferenceNode node = (IPreferenceNode) nodes.next();
			IPreferencePage page = node.getPage();
			if (page instanceof PreferencePage) {
				// Save now in case tbe workbench does not shutdown cleanly
				IPreferenceStore store = ((PreferencePage) page).getPreferenceStore();
				if (store != null && store.needsSaving()
						&& store instanceof IPersistentPreferenceStore) {
					try {
						((IPersistentPreferenceStore) store).save();
					} catch (IOException e) {
						MessageDialog
								.openError(
										getShell(),
										JFaceResources.getString("PreferenceDialog.saveErrorTitle"), //$NON-NLS-1$
										JFaceResources
												.format(
														"PreferenceDialog.saveErrorMessage", new Object[] { page.getTitle(), e.getMessage() })); //$NON-NLS-1$
					}
				}
			}
		}
	}

	/**
	 * Notifies that the window's close button was pressed, the close menu was
	 * selected, or the ESCAPE key pressed.
	 * <p>
	 * The default implementation of this framework method sets the window's
	 * return code to <code>CANCEL</code> and closes the window using
	 * <code>close</code>. Subclasses may extend or reimplement.
	 * </p>
	 */
	protected void handleShellCloseEvent() {
		// handle the same as pressing cancel
		cancelPressed();
	}

	/**
	 * Notifies of the pressing of the Help button.
	 * <p>
	 * The default implementation of this framework method calls
	 * <code>performHelp</code> on the currently active page.
	 * </p>
	 */
	protected void helpPressed() {
		if (currentPage != null) {
			currentPage.performHelp();
		}
	}

	/**
	 * Returns whether the current page is valid.
	 * 
	 * @return <code>false</code> if the current page is not valid, or or
	 *         <code>true</code> if the current page is valid or there is no
	 *         current page
	 */
	protected boolean isCurrentPageValid() {
		if (currentPage == null)
			return true;
		return currentPage.isValid();
	}

	/**
	 * @param control
	 *            the <code>Control</code> to lay out.
	 * @since 3.0
	 */
	protected void layoutTreeAreaControl(Control control) {
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = getLastRightWidth();
		gd.verticalSpan = 1;
		control.setLayoutData(gd);
	}

	/**
	 * The preference dialog implementation of this <code>Dialog</code>
	 * framework method sends <code>performOk</code> to all pages of the
	 * preference dialog, then calls <code>handleSave</code> on this dialog to
	 * save any state, and then calls <code>close</code> to close this dialog.
	 */
	protected void okPressed() {
		Platform.run(new SafeRunnable() {
			private boolean errorOccurred;

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			public void run() {
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				errorOccurred = false;
				try {
					// Notify all the pages and give them a chance to abort
					Iterator nodes = preferenceManager.getElements(PreferenceManager.PRE_ORDER)
							.iterator();
					while (nodes.hasNext()) {
						IPreferenceNode node = (IPreferenceNode) nodes.next();
						IPreferencePage page = node.getPage();
						if (page != null) {
							if (!page.performOk())
								return;
						}
					}
				} catch (Exception e) {
					handleException(e);
				} finally {
					// Give subclasses the choice to save the state of the
					// preference pages.
					if (!errorOccurred)
						handleSave();
					// Need to restore state
					close();
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
			 */
			public void handleException(Throwable e) {
				errorOccurred = true;
				if (Platform.isRunning()) {
					String bundle = Platform.PI_RUNTIME;
					Platform.getLog(Platform.getBundle(bundle)).log(
							new Status(IStatus.ERROR, bundle, 0, e.toString(), e));
				} else
					e.printStackTrace();
				clearSelectedNode();
				String message = JFaceResources.getString("SafeRunnable.errorMessage"); //$NON-NLS-1$
				MessageDialog.openError(getShell(), JFaceResources.getString("Error"), message); //$NON-NLS-1$

			}
		});
	}

	/**
	 * Selects the page determined by <code>lastSuccessfulNode</code> in the
	 * page hierarchy.
	 */
	void selectCurrentPageAgain() {
		if (lastSuccessfulNode == null)
			return;
		getTreeViewer().setSelection(new StructuredSelection(lastSuccessfulNode));
		currentPage.setVisible(true);
	}

	/**
	 * Selects the saved item in the tree of preference pages. If it cannot do
	 * this it saves the first one.
	 */
	protected void selectSavedItem() {
		IPreferenceNode node = findNodeMatching(getSelectedNodePreference());
		if (node == null) {
			IPreferenceNode[] nodes = preferenceManager.getRoot().getSubNodes();
			if (nodes.length > 0)
				node = nodes[0];
		}
		if (node != null) {
			getTreeViewer().setSelection(new StructuredSelection(node), true);
			// Keep focus in tree. See bugs 2692, 2621, and 6775.
			getTreeViewer().getControl().setFocus();
		}
	}

	/**
	 * Display the given error message. The currently displayed message is saved
	 * and will be redisplayed when the error message is set to
	 * <code>null</code>.
	 * 
	 * @param newErrorMessage
	 *            the errorMessage to display or <code>null</code>
	 */
	public void setErrorMessage(String newErrorMessage) {
		if (newErrorMessage == null)
			messageArea.clearErrorMessage();
		else
			messageArea.updateText(newErrorMessage, IMessageProvider.ERROR);
	}

	/**
	 * Save the last known tree width.
	 * 
	 * @param width
	 *            the width.
	 */
	private void setLastTreeWidth(int width) {
		lastTreeWidth = width;
	}

	/**
	 * Sets whether a Help button is available for this dialog.
	 * <p>
	 * Clients must call this framework method before the dialog's control has
	 * been created.
	 * <p>
	 * 
	 * @param b
	 *            <code>true</code> to include a Help button, and
	 *            <code>false</code> to not include one (the default)
	 */
	public void setHelpAvailable(boolean b) {
		isHelpAvailable = b;
	}

	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 * <p>
	 * Shortcut for <code>setMessage(newMessage, NONE)</code>
	 * </p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 */
	public void setMessage(String newMessage) {
		setMessage(newMessage, IMessageProvider.NONE);
	}

	/**
	 * Sets the message for this dialog with an indication of what type of
	 * message it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>,
	 * <code>INFORMATION</code>,<code>WARNING</code>, or
	 * <code>ERROR</code>.
	 * </p>
	 * <p>
	 * Note that for backward compatibility, a message of type
	 * <code>ERROR</code> is different than an error message (set using
	 * <code>setErrorMessage</code>). An error message overrides the current
	 * message until the error message is cleared. This method replaces the
	 * current message and does not affect the error message.
	 * </p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 * @since 2.0
	 */
	public void setMessage(String newMessage, int newType) {
		messageArea.updateText(newMessage, newType);
	}

	/**
	 * Sets the minimum page size.
	 * 
	 * @param minWidth
	 *            the minimum page width
	 * @param minHeight
	 *            the minimum page height
	 * @see #setMinimumPageSize(Point)
	 */
	public void setMinimumPageSize(int minWidth, int minHeight) {
		minimumPageSize.x = minWidth;
		minimumPageSize.y = minHeight;
	}

	/**
	 * Sets the minimum page size.
	 * 
	 * @param size
	 *            the page size encoded as <code>new Point(width,height)</code>
	 * @see #setMinimumPageSize(int,int)
	 */
	public void setMinimumPageSize(Point size) {
		minimumPageSize.x = size.x;
		minimumPageSize.y = size.y;
	}

	/**
	 * Sets the preference store for this preference dialog.
	 * 
	 * @param store
	 *            the preference store
	 * @see #getPreferenceStore
	 */
	public void setPreferenceStore(IPreferenceStore store) {
		Assert.isNotNull(store);
		preferenceStore = store;
	}

	/**
	 * Save the currently selected node.
	 */
	private void setSelectedNode() {
		String storeValue = null;
		IStructuredSelection selection = (IStructuredSelection) getTreeViewer().getSelection();
		if (selection.size() == 1) {
			IPreferenceNode node = (IPreferenceNode) selection.getFirstElement();
			storeValue = node.getId();
		}
		setSelectedNodePreference(storeValue);
	}

	/**
	 * Sets the name of the selected item preference. Public equivalent to
	 * <code>setSelectedNodePreference</code>.
	 * 
	 * @param pageId
	 *            The identifier for the page
	 * @since 3.0
	 */
	public void setSelectedNode(String pageId) {
		setSelectedNodePreference(pageId);
	}

	/**
	 * Sets the name of the selected item preference.
	 * 
	 * @param pageId
	 *            The identifier for the page
	 */
	protected void setSelectedNodePreference(String pageId) {
		lastPreferenceId = pageId;
	}

	/**
	 * Changes the shell size to the given size, ensuring that it is no larger
	 * than the display bounds.
	 * 
	 * @param width
	 *            the shell width
	 * @param height
	 *            the shell height
	 */
	private void setShellSize(int width, int height) {
		Rectangle preferred = getShell().getBounds();
		preferred.width = width;
		preferred.height = height;
		getShell().setBounds(getConstrainedShellBounds(preferred));
	}

	/**
	 * Shows the preference page corresponding to the given preference node.
	 * Does nothing if that page is already current.
	 * 
	 * @param node
	 *            the preference node, or <code>null</code> if none
	 * @return <code>true</code> if the page flip was successful, and
	 *         <code>false</code> is unsuccessful
	 */
	protected boolean showPage(IPreferenceNode node) {
		if (node == null)
			return false;
		// Create the page if nessessary
		if (node.getPage() == null)
			node.createPage();
		if (node.getPage() == null)
			return false;
		IPreferencePage newPage = getPage(node);
		if (newPage == currentPage)
			return true;
		if (currentPage != null) {
			if (!currentPage.okToLeave())
				return false;
		}
		IPreferencePage oldPage = currentPage;
		currentPage = newPage;
		// Set the new page's container
		currentPage.setContainer(this);
		// Ensure that the page control has been created
		// (this allows lazy page control creation)
		if (currentPage.getControl() == null) {
			final boolean[] failed = { false };
			Platform.run(new ISafeRunnable() {
				public void handleException(Throwable e) {
					failed[0] = true;
				}

				public void run() {
					createPageControl(currentPage, pageContainer);
				}
			});
			if (failed[0])
				return false;
			// the page is responsible for ensuring the created control is
			// accessable
			// via getControl.
			Assert.isNotNull(currentPage.getControl());
		}
		// Force calculation of the page's description label because
		// label can be wrapped.
		final Point[] size = new Point[1];
		final Point failed = new Point(-1, -1);
		Platform.run(new ISafeRunnable() {
			public void handleException(Throwable e) {
				size[0] = failed;
			}

			public void run() {
				size[0] = currentPage.computeSize();
			}
		});
		if (size[0].equals(failed))
			return false;
		Point contentSize = size[0];
		// Do we need resizing. Computation not needed if the
		// first page is inserted since computing the dialog's
		// size is done by calling dialog.open().
		// Also prevent auto resize if the user has manually resized
		Shell shell = getShell();
		Point shellSize = shell.getSize();
		if (oldPage != null) {
			Rectangle rect = pageContainer.getClientArea();
			Point containerSize = new Point(rect.width, rect.height);
			int hdiff = contentSize.x - containerSize.x;
			int vdiff = contentSize.y - containerSize.y;
			if (hdiff > 0 || vdiff > 0) {
				if (shellSize.equals(lastShellSize)) {
					hdiff = Math.max(0, hdiff);
					vdiff = Math.max(0, vdiff);
					setShellSize(shellSize.x + hdiff, shellSize.y + vdiff);
					lastShellSize = shell.getSize();
					if (currentPage.getControl().getSize().x == 0)
						currentPage.getControl().setSize(containerSize);
				} else {
					currentPage.setSize(containerSize);
				}
			} else if (hdiff < 0 || vdiff < 0) {
				currentPage.setSize(containerSize);
			}
		}
		// Ensure that all other pages are invisible
		// (including ones that triggered an exception during
		// their creation).
		Control[] children = pageContainer.getChildren();
		Control currentControl = currentPage.getControl();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != currentControl)
				children[i].setVisible(false);
		}
		// Make the new page visible
		currentPage.setVisible(true);
		if (oldPage != null)
			oldPage.setVisible(false);
		// update the dialog controls
		update();
		return true;
	}

	/**
	 * Get the page for the node.
	 * @param node
	 * @return IPreferencePage
	 */
	protected IPreferencePage getPage(IPreferenceNode node) {
		return node.getPage();
	}

	/**
	 * Shows the "Page Flipping abort" dialog.
	 */
	void showPageFlippingAbortDialog() {
		MessageDialog.openError(getShell(), JFaceResources
				.getString("AbortPageFlippingDialog.title"), //$NON-NLS-1$
				JFaceResources.getString("AbortPageFlippingDialog.message")); //$NON-NLS-1$
	}

	/**
	 * Updates this dialog's controls to reflect the current page.
	 */
	protected void update() {
		// Update the title bar
		updateTitle();
		// Update the message line
		updateMessage();
		// Update the buttons
		updateButtons();
		//Saved the selected node in the preferences
		setSelectedNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
		okButton.setEnabled(isCurrentPageValid());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
		String message = currentPage.getMessage();
		int messageType = IMessageProvider.NONE;
		if (message != null && currentPage instanceof IMessageProvider)
			messageType = ((IMessageProvider) currentPage).getMessageType();
		String errorMessage = currentPage.getErrorMessage();
		if (errorMessage != null) {
			message = errorMessage;
			messageType = IMessageProvider.ERROR;
			if (!showingError) {
				// we were not previously showing an error
				showingError = true;
				titleImage.setImage(null);
				titleImage.setBackground(JFaceColors.getErrorBackground(titleImage.getDisplay()));
				titleImage.setSize(0, 0);
				titleImage.getParent().layout();
			}
		} else {
			if (showingError) {
				// we were previously showing an error
				showingError = false;
				titleImage.setImage(JFaceResources.getImage(PREF_DLG_TITLE_IMG));
				titleImage.computeSize(SWT.NULL, SWT.NULL);
				titleImage.getParent().layout();
			}
		}
		messageArea.updateText(getShortenedString(message), messageType);
	}

	private final String ellipsis = "..."; //$NON-NLS-1$

	/**
	 * Shortened the message if too long.
	 * 
	 * @param textValue
	 *            The messgae value.
	 * @return The shortened string.
	 */
	private String getShortenedString(String textValue) {
		if (textValue == null)
			return null;
		Display display = titleArea.getDisplay();
		GC gc = new GC(display);
		int maxWidth = titleArea.getBounds().width - 28;
		if (gc.textExtent(textValue).x < maxWidth) {
			gc.dispose();
			return textValue;
		}
		int length = textValue.length();
		int ellipsisWidth = gc.textExtent(ellipsis).x;
		int pivot = length / 2;
		int start = pivot;
		int end = pivot + 1;
		while (start >= 0 && end < length) {
			String s1 = textValue.substring(0, start);
			String s2 = textValue.substring(end, length);
			int l1 = gc.textExtent(s1).x;
			int l2 = gc.textExtent(s2).x;
			if (l1 + ellipsisWidth + l2 < maxWidth) {
				gc.dispose();
				return s1 + ellipsis + s2;
			}
			start--;
			end++;
		}
		gc.dispose();
		return textValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
		messageArea.showTitle(currentPage.getTitle(), currentPage.getImage());
	}

	/**
	 * Update the tree to use the specified <code>Font</code>.
	 * 
	 * @param dialogFont
	 *            the <code>Font</code> to use.
	 * @since 3.0
	 */
	protected void updateTreeFont(Font dialogFont) {
		getTreeViewer().getControl().setFont(dialogFont);
	}

	/**
	 * Returns the currentPage.
	 * @return IPreferencePage
	 */
	protected IPreferencePage getCurrentPage() {
		return currentPage;
	}

	/**
	 * Sets the current page.
	 * @param currentPage
	 */
	protected void setCurrentPage(IPreferencePage currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * Set the treeViewer.
	 * <strong>This API is experimental and may be deleted in
	 * the 3.1 timeframe</strong>.
	 * @param treeViewer
	 */
	protected void setTreeViewer(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	/**
	 * Get the composite that is showing the page.
	 * <strong>This API is experimental and may be deleted in
	 * the 3.1 timeframe</strong>.
	 * @return Composite.
	 */
	protected Composite getPageContainer() {
		return this.pageContainer;
	}

	/**
	 * Set the composite that is showing the page.
	 * <strong>This API is experimental and may be deleted in
	 * the 3.1 timeframe</strong>.
	 * @param pageContainer Composite
	 */
	protected void setPageContainer(Composite pageContainer) {
		this.pageContainer = pageContainer;
	}
	/**
	 * Create the page control for the supplied page.
	 */
	protected void createPageControl(IPreferencePage page, Composite parent) {
		page.createControl(parent);
	}
}