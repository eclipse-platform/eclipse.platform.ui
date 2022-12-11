/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Teddy Walker <teddy.walker@googlemail.com>
 *     	- Bug 188056 [Preferences] PreferencePages have to less indent in PreferenceDialog
 *     Stefan Xenos <sxenos@google.com> - Bug 466793
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 475879
 *******************************************************************************/
package org.eclipse.jface.preference;

import static org.eclipse.swt.events.SelectionListener.widgetDefaultSelectedAdapter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.dialogs.DialogMessageArea;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/**
 * A preference dialog is a hierarchical presentation of preference pages. Each
 * page is represented by a node in the tree shown on the left hand side of the
 * dialog; when a node is selected, the corresponding page is shown on the right
 * hand side.
 */
public class PreferenceDialog extends TrayDialog implements IPreferencePageContainer, IPageChangeProvider {
	/**
	 * Layout for the page container.
	 *
	 */
	private class PageLayout extends Layout {
		@Override
		public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
			int x = minimumPageSize.x;
			int y = minimumPageSize.y;
			Control[] children = composite.getChildren();
			for (Control element : children) {
				Point size = element.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}

			//As pages can implement thier own computeSize
			//take it into account
			if(currentPage != null){
				Point size = currentPage.computeSize();
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}

			if (wHint != SWT.DEFAULT) {
				x = wHint;
			}
			if (hHint != SWT.DEFAULT) {
				y = hHint;
			}
			return new Point(x, y);
		}

		@Override
		public void layout(Composite composite, boolean force) {
			Rectangle rect = composite.getClientArea();
			Control[] children = composite.getChildren();
			for (Control element : children) {
				element.setSize(rect.width, rect.height);
			}
		}
	}

	//The id of the last page that was selected
	private static String lastPreferenceId = null;

	//The last known tree width
	private static int lastTreeWidth = 180;

	/**
	 * Indentifier for the error image
	 */
	public static final String PREF_DLG_IMG_TITLE_ERROR = DLG_IMG_MESSAGE_ERROR;

	/**
	 * Title area fields
	 */
	public static final String PREF_DLG_TITLE_IMG = "preference_dialog_title_image"; //$NON-NLS-1$

	/**
	 * Return code used when dialog failed
	 */
	protected static final int FAILED = 2;

	/**
	 * The current preference page, or <code>null</code> if there is none.
	 */
	private IPreferencePage currentPage;

	private DialogMessageArea messageArea;

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

	/**
	 * The tree viewer.
	 */
	private TreeViewer treeViewer;

	private ListenerList<IPageChangedListener> pageChangedListeners = new ListenerList<>();

	/**
	 *  Composite with a FormLayout to contain the title area
	 */
	Composite formTitleComposite;

	private ScrolledComposite scrolled;

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
		preferenceManager = manager;
	}

	@Override
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

	@Override
	protected void cancelPressed() {
		// Inform all pages that we are cancelling
		Iterator<IPreferenceNode> nodes = preferenceManager.getElements(PreferenceManager.PRE_ORDER).iterator();
		final boolean[] cancelOK = new boolean[] { true };
		while (nodes.hasNext()) {
			final IPreferenceNode node = nodes.next();
			if (getPage(node) != null) {
				SafeRunnable.run(new SafeRunnable() {
					@Override
					public void run() {
						if (!getPage(node).performCancel()) {
							cancelOK[0] = false;
						}
					}
				});
				if (!cancelOK[0])
					return;
			}
		}

		// Give subclasses the choice to save the state of the preference pages if needed
		handleSave();

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

	@Override
	public boolean close() {

		//Do this is in a SafeRunnable as it may run client code
		SafeRunnable runnable = new SafeRunnable(){
			@Override
			public void run() throws Exception {
				for (IPreferenceNode node : preferenceManager.getElements(PreferenceManager.PRE_ORDER)) {
					node.disposeResources();
				}

			}

			@Override
			public void handleException(Throwable e) {
				super.handleException(e);
				clearSelectedNode();//Do not cache a node with problems
			}
		};

		SafeRunner.run(runnable);

		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(JFaceResources.getString("PreferenceDialog.title")); //$NON-NLS-1$
		newShell.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				if (lastShellSize == null) {
					lastShellSize = getShell().getSize();
				}
			}

		});

	}

	@Override
	protected void constrainShellSize() {
		super.constrainShellSize();
		// record opening shell size
		if (lastShellSize == null) {
			lastShellSize = getShell().getSize();
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				JFaceResources.getString("PreferencesDialog.okButtonLabel"), true); //$NON-NLS-1$
		getShell().setDefaultButton(okButton);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Control[] control = new Control[1];
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			control[0] = PreferenceDialog.super.createContents(parent);
			// Add the first page
			selectSavedItem();
		});

		return control[0];
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout parentLayout = ((GridLayout) composite.getLayout());
		parentLayout.numColumns = 4;
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parentLayout.verticalSpacing = 0;
		parentLayout.horizontalSpacing = 0;

		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		Control treeControl = createTreeAreaContents(composite);
		createSash(composite,treeControl);

		Label versep = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(versep);

		Composite pageAreaComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(pageAreaComposite);
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		pageAreaComposite.setLayout(layout);

		formTitleComposite = new Composite(pageAreaComposite, SWT.NONE);
		FormLayout titleLayout = new FormLayout();
		titleLayout.marginWidth = 0;
		titleLayout.marginHeight = 0;
		formTitleComposite.setLayout(titleLayout);

		GridData titleGridData = new GridData(GridData.FILL_HORIZONTAL);
		titleGridData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		formTitleComposite.setLayoutData(titleGridData);

		// Build the title area and separator line
		Composite titleComposite = new Composite(formTitleComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginBottom = 5;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		titleComposite.setLayout(layout);

		FormData titleFormData = new FormData();
		titleFormData.top = new FormAttachment(0,0);
		titleFormData.left = new FormAttachment(0,0);
		titleFormData.right = new FormAttachment(100,0);
		titleFormData.bottom = new FormAttachment(100,0);

		titleComposite.setLayoutData(titleFormData);
		createTitleArea(titleComposite);

		Label separator = new Label(pageAreaComposite, SWT.HORIZONTAL | SWT.SEPARATOR);

		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));


		// Build the Page container
		pageContainer = createPageContainer(pageAreaComposite);
		GridData pageContainerData = new GridData(GridData.FILL_BOTH);
		pageContainerData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		pageContainer.setLayoutData(pageContainerData);
		// Build the separator line
		Label bottomSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		bottomSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		return composite;
	}

	/**
	 * Create the sash with right control on the right. Note that this method
	 * assumes GridData for the layout data of the rightControl.
	 *
	 * @param composite    the parent composite
	 * @param rightControl control for the right part of the created sash
	 * @return Sash
	 *
	 * @since 3.1
	 */
	protected Sash createSash(final Composite composite, final Control rightControl) {
		final Sash sash = new Sash(composite, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		sash.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		// the following listener resizes the tree control based on sash deltas.
		// If necessary, it will also grow/shrink the dialog.
		sash.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.DRAG) {
				return;
			}
			int shift = event.x - sash.getBounds().x;
			GridData data = (GridData) rightControl.getLayoutData();
			int newWidthHint = data.widthHint + shift;
			if (newWidthHint < 20) {
				return;
			}
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
			if (customSize) {
				computedSize.x = Math.max(computedSize.x, currentSize.x);
			}
			computedSize.y = Math.max(computedSize.y, currentSize.y);
			if (computedSize.equals(currentSize)) {
				return;
			}
			setShellSize(computedSize.x, computedSize.y);
			lastShellSize = getShell().getSize();
		});
		return sash;
	}

	/**
	 * Creates the inner page container.
	 *
	 * @param parent the parent composite
	 * @return Composite
	 */
	protected Composite createPageContainer(Composite parent) {

		Composite outer = new Composite(parent, SWT.NONE);

		GridData outerData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
		outerData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;

		outer.setLayout(new GridLayout());
		outer.setLayoutData(outerData);

		//Create an outer composite for spacing
		scrolled = new ScrolledComposite(outer, SWT.V_SCROLL | SWT.H_SCROLL);

		// always show the focus control
		scrolled.setShowFocusedControl(true);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);

		GridData scrolledData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);

		scrolled.setLayoutData(scrolledData);

		Composite result = new Composite(scrolled, SWT.NONE);

		GridData resultData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);

		result.setLayout(getPageLayout());
		result.setLayoutData(resultData);

		scrolled.setContent(result);

		return result;
	}

	/**
	 * Return the layout for the composite that contains
	 * the pages.
	 * @return PageLayout
	 *
	 * @since 3.1
	 */
	protected Layout getPageLayout() {
		return new PageLayout();
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
		layout.marginHeight = 0;
		layout.marginWidth = margins;
		titleArea.setLayout(layout);


		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.verticalAlignment = SWT.TOP;
		titleArea.setLayoutData(layoutData);

		// Message label
		messageArea = new DialogMessageArea();
		messageArea.createContents(titleArea);

		titleArea.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateMessage();
			}
		});

		final IPropertyChangeListener fontListener = event -> {
			if (JFaceResources.BANNER_FONT.equals(event.getProperty())) {
				updateMessage();
			}
			if (JFaceResources.DIALOG_FONT.equals(event.getProperty())) {
				updateMessage();
				Font dialogFont = JFaceResources.getDialogFont();
				updateTreeFont(dialogFont);
				Control[] children = ((Composite) buttonBar).getChildren();
				for (Control element : children) {
					element.setFont(dialogFont);
				}
			}
		};

		titleArea.addDisposeListener(event -> JFaceResources.getFontRegistry().removeListener(fontListener));
		JFaceResources.getFontRegistry().addListener(fontListener);
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
		messageData.bottom = new FormAttachment(100);
		messageData.right = new FormAttachment(100);
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
		TreeViewer viewer = new TreeViewer(parent, SWT.NONE);
		viewer.setUseHashlookup(true);
		addListeners(viewer);
		viewer.setLabelProvider(new PreferenceLabelProvider());
		viewer.setContentProvider(new PreferenceContentProvider());
		return viewer;
	}

	/**
	 * Add the listeners to the tree viewer.
	 *
	 * @param viewer viewer to add listeners to
	 *
	 * @since 3.1
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

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final IPreferenceNode node = getSingleSelection(event.getSelection());
				if (node != null) {
					BusyIndicator.showWhile(getShell().getDisplay(), () -> {
						if (!isCurrentPageValid()) {
							handleError();
						} else if (!showPage(node)) {
							// Page flipping wasn't successful
							handleError();
						} else {
							// Everything went well
							lastSuccessfulNode = node;
						}
					});
				}
			}
		});
		((Tree) viewer.getControl()).addSelectionListener(widgetDefaultSelectedAdapter(event -> {
			ISelection selection = viewer.getSelection();
			if (selection.isEmpty()) {
				return;
			}
			IPreferenceNode singleSelection = getSingleSelection(selection);
			boolean expanded = viewer.getExpandedState(singleSelection);
			viewer.setExpandedState(singleSelection, !expanded);
		}));
		//Register help listener on the tree to use context sensitive help
		viewer.getControl().addHelpListener(new HelpListener() {
			@Override
			public void helpRequested(HelpEvent event) {
				if (currentPage == null) { // no current page? open dialog's help
					openDialogHelp();
					return;
				}
				// A) A typical path: the current page has registered its own help link
				// via WorkbenchHelpSystem#setHelp(). When just call it and let
				// it handle the help request.
				Control pageControl = currentPage.getControl();
				if (pageControl != null && pageControl.isListening(SWT.Help)) {
					currentPage.performHelp();
					return;
				}

				// B) Less typical path: no standard listener has been created for the page.
				// In this case we may or may not have an override of page's #performHelp().
				// 1) Try to get default help opened for the dialog;
				openDialogHelp();
				// 2) Next call currentPage's #performHelp(). If it was overridden, it might switch help
				// to something else.
				currentPage.performHelp();
			}

			private void openDialogHelp() {
				if (pageContainer == null)
					return;
				for(Control currentControl = pageContainer; currentControl != null; currentControl = currentControl.getParent()) {
					if (currentControl.isListening(SWT.Help)) {
						currentControl.notifyListeners(SWT.Help, new Event());
						break;
					}
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
		List<IPreferenceNode> nodes = preferenceManager.getElements(PreferenceManager.POST_ORDER);
		for (IPreferenceNode node : nodes) {
			if (node.getId().equals(nodeId)) {
				return node;
			}
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

	@Override
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
			if (structured.getFirstElement() instanceof IPreferenceNode) {
				return (IPreferenceNode) structured.getFirstElement();
			}
		}
		return null;
	}

	/**
	 * @return the <code>TreeViewer</code> for this dialog.
	 * @since 3.3
	 */
	public TreeViewer getTreeViewer() {
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
		Iterator<IPreferenceNode> nodes = preferenceManager.getElements(PreferenceManager.PRE_ORDER).iterator();
		while (nodes.hasNext()) {
			IPreferenceNode node = nodes.next();
			IPreferencePage page = node.getPage();
			if (page instanceof PreferencePage) {
				// Save now in case tbe workbench does not shutdown cleanly
				IPreferenceStore store = ((PreferencePage) page).getPreferenceStore();
				if (store != null && store.needsSaving()
						&& store instanceof IPersistentPreferenceStore) {
					try {
						((IPersistentPreferenceStore) store).save();
					} catch (IOException e) {
						String message = JFaceResources.format("PreferenceDialog.saveErrorMessage", page.getTitle(), //$NON-NLS-1$
								e.getMessage());
						Policy.getStatusHandler().show(
								new Status(IStatus.ERROR, Policy.JFACE, message, e),
								JFaceResources.getString("PreferenceDialog.saveErrorTitle")); //$NON-NLS-1$

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
	@Override
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
		if (currentPage == null) {
			return true;
		}
		return currentPage.isValid();
	}

	/**
	 * @param control
	 *            the <code>Control</code> to lay out.
	 * @since 3.0
	 */
	protected void layoutTreeAreaControl(Control control) {
		GridDataFactory.fillDefaults().hint(getLastRightWidth(), convertVerticalDLUsToPixels(120)).grab(false, true)
				.applyTo(control);
	}

	/**
	 * The preference dialog implementation of this <code>Dialog</code>
	 * framework method sends <code>performOk</code> to all pages of the
	 * preference dialog, then calls <code>handleSave</code> on this dialog to
	 * save any state, and then calls <code>close</code> to close this dialog.
	 */
	@Override
	protected void okPressed() {
		SafeRunnable.run(new SafeRunnable() {
			private boolean errorOccurred;

			@Override
			public void run() {
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				errorOccurred = false;
				boolean hasFailedOK = false;
				try {
					// Notify all the pages and give them a chance to abort
					Iterator<IPreferenceNode> nodes = preferenceManager.getElements(PreferenceManager.PRE_ORDER)
							.iterator();
					while (nodes.hasNext()) {
						IPreferenceNode node = nodes.next();
						IPreferencePage page = node.getPage();
						if (page != null) {
							if (!page.performOk()){
								hasFailedOK = true;
								return;
							}
						}
					}
				} catch (Exception e) {
					handleException(e);
				} finally {
					//Don't bother closing if the OK failed
					if(hasFailedOK){
						setReturnCode(FAILED);
						getButton(IDialogConstants.OK_ID).setEnabled(true);
						return;
					}

					if (!errorOccurred) {
						//Give subclasses the choice to save the state of the
						//preference pages.
						handleSave();
					}
					setReturnCode(OK);
					close();
				}
			}

			@Override
			public void handleException(Throwable e) {
				errorOccurred = true;

				Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, 0, e.toString(), e));

				clearSelectedNode();
				String message = JFaceResources.getString("SafeRunnable.errorMessage"); //$NON-NLS-1$

				Policy.getStatusHandler().show(
						new Status(IStatus.ERROR, Policy.JFACE, message, e),
						JFaceResources.getString("Error")); //$NON-NLS-1$

			}
		});
	}

	/**
	 * Selects the page determined by <code>lastSuccessfulNode</code> in the
	 * page hierarchy.
	 */
	void selectCurrentPageAgain() {
		if (lastSuccessfulNode == null) {
			return;
		}
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
			IPreferenceNode[] nodes = preferenceManager.getRootSubNodes();
			ViewerComparator comparator = getTreeViewer().getComparator();
			if (comparator != null)	{
				comparator.sort(null, nodes);
			}
			ViewerFilter[] filters = getTreeViewer().getFilters();
			for (IPreferenceNode preferenceNode : nodes) {
				IPreferenceNode selectedNode = preferenceNode;
				// See if it passes all filters
				for (ViewerFilter filter : filters) {
					if (!filter.select(this.treeViewer, preferenceManager.getRoot(), selectedNode)) {
						selectedNode = null;
						break;
					}
				}
				// if it passes all filters select it
				if (selectedNode != null) {
					node = selectedNode;
					break;
				}
			}
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
		if (newErrorMessage == null) {
			messageArea.clearErrorMessage();
		} else {
			messageArea.updateText(newErrorMessage, IMessageProvider.ERROR);
		}
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
		IStructuredSelection selection = getTreeViewer().getStructuredSelection();
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
		if (node == null) {
			return false;
		}
		// Create the page if nessessary
		if (node.getPage() == null) {
			createPage(node);
		}
		if (node.getPage() == null) {
			return false;
		}
		IPreferencePage newPage = getPage(node);
		if (newPage == currentPage) {
			return true;
		}
		if (currentPage != null) {
			if (!currentPage.okToLeave()) {
				return false;
			}
		}
		IPreferencePage oldPage = currentPage;
		currentPage = newPage;
		// Set the new page's container
		currentPage.setContainer(this);
		// Ensure that the page control has been created
		// (this allows lazy page control creation)
		if (currentPage.getControl() == null) {
			final boolean[] failed = { false };
			SafeRunnable.run(new ISafeRunnable() {
				@Override
				public void handleException(Throwable e) {
					failed[0] = true;
				}

				@Override
				public void run() {
					createPageControl(currentPage, pageContainer);
				}
			});
			if (failed[0]) {
				return false;
			}
			// the page is responsible for ensuring the created control is
			// accessable
			// via getControl.
			Assert.isNotNull(currentPage.getControl());
		}
		// Force calculation of the page's description label because
		// label can be wrapped.
		final Point[] size = new Point[1];
		final Point failed = new Point(-1, -1);
		SafeRunnable.run(new ISafeRunnable() {
			@Override
			public void handleException(Throwable e) {
				size[0] = failed;
			}

			@Override
			public void run() {
				size[0] = currentPage.computeSize();
			}
		});
		if (size[0].equals(failed)) {
			return false;
		}
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
			if ((hdiff > 0 || vdiff > 0) && shellSize.equals(lastShellSize)) {
					hdiff = Math.max(0, hdiff);
					vdiff = Math.max(0, vdiff);
					setShellSize(shellSize.x + hdiff, shellSize.y + vdiff);
					lastShellSize = shell.getSize();
					if (currentPage.getControl().getSize().x == 0) {
						currentPage.getControl().setSize(containerSize);
					}

			} else {
				currentPage.getControl().setSize(containerSize);
			}
		}

		scrolled.setMinSize(contentSize);
		// Ensure that all other pages are invisible
		// (including ones that triggered an exception during
		// their creation).
		Control[] children = pageContainer.getChildren();
		Control currentControl = currentPage.getControl();
		for (Control element : children) {
			if (element != currentControl) {
				element.setVisible(false);
			}
		}
		// Make the new page visible
		currentPage.setVisible(true);
		if (oldPage != null) {
			oldPage.setVisible(false);
		}
		// update the dialog controls
		update();
		return true;
	}

	/**
	 * Create the page for the node.
	 *
	 * @param node the node to create page for
	 *
	 * @since 3.1
	 */
	protected void createPage(IPreferenceNode node) {
		node.createPage();
	}

	/**
	 * Get the page for the node.
	 *
	 * @param node the node to get page for
	 * @return IPreferencePage
	 *
	 * @since 3.1
	 */
	protected IPreferencePage getPage(IPreferenceNode node) {
		return node.getPage();
	}

	/**
	 * Shows the "Page Flipping abort" dialog.
	 */
	void showPageFlippingAbortDialog() {
		MessageDialog.open(MessageDialog.ERROR, getShell(), JFaceResources
				.getString("AbortPageFlippingDialog.title"), //$NON-NLS-1$
				JFaceResources.getString("AbortPageFlippingDialog.message"), SWT.SHEET); //$NON-NLS-1$
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
		firePageChanged(new PageChangedEvent(this, getCurrentPage()));
	}

	@Override
	public void updateButtons() {
		okButton.setEnabled(isCurrentPageValid());
	}

	@Override
	public void updateMessage() {
		String message = null;
		String errorMessage = null;
		if(currentPage != null){
			message = currentPage.getMessage();
			errorMessage = currentPage.getErrorMessage();
		}
		int messageType = IMessageProvider.NONE;
		if (message != null && currentPage instanceof IMessageProvider) {
			messageType = ((IMessageProvider) currentPage).getMessageType();
		}

		if (errorMessage == null){
			if (showingError) {
				// we were previously showing an error
				showingError = false;
			}
		}
		else {
			message = errorMessage;
			messageType = IMessageProvider.ERROR;
			if (!showingError) {
				// we were not previously showing an error
				showingError = true;
			}
		}
		messageArea.updateText(message,messageType);
	}

	@Override
	public void updateTitle() {
		if(currentPage == null) {
			return;
		}
		messageArea.showTitle(LegacyActionTools.escapeMnemonics(currentPage.getTitle()), currentPage.getImage());
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
	 * @since 3.1
	 */
	protected IPreferencePage getCurrentPage() {
		return currentPage;
	}

	/**
	 * Sets the current page.
	 *
	 * @param currentPage page to set
	 *
	 * @since 3.1
	 */
	protected void setCurrentPage(IPreferencePage currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * Set the treeViewer.
	 *
	 * @param treeViewer viewer to set
	 *
	 * @since 3.1
	 */
	protected void setTreeViewer(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	/**
	 * Get the composite that is showing the page.
	 *
	 * @return Composite.
	 *
	 * @since 3.1
	 */
	protected Composite getPageContainer() {
		return this.pageContainer;
	}

	/**
	 * Set the composite that is showing the page.
	 * @param pageContainer Composite
	 *
	 * @since 3.1
	 */
	protected void setPageContainer(Composite pageContainer) {
		this.pageContainer = pageContainer;
	}
	/**
	 * Create the page control for the supplied page.
	 *
	 * @param page - the preference page to be shown
	 * @param parent - the composite to parent the page
	 *
	 * @since 3.1
	 */
	protected void createPageControl(IPreferencePage page, Composite parent) {
		page.createControl(parent);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#getSelectedPage()
	 *
	 * @since 3.1
	 */
	@Override
	public Object getSelectedPage() {
			return getCurrentPage();
		}

	/**
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)
	 * @since 3.1
	 */
	@Override
	public void addPageChangedListener(IPageChangedListener listener) {
		pageChangedListeners.add(listener);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#removePageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)
	 * @since 3.1
	 */
	@Override
	public void removePageChangedListener(IPageChangedListener listener) {
		pageChangedListeners.remove(listener);

	}

	/**
	 * Notifies any selection changed listeners that the selected page
	 * has changed.
	 * Only listeners registered at the time this method is called are notified.
	 *
	 * @param event a selection changed event
	 *
	 * @see IPageChangedListener#pageChanged
	 *
	 * @since 3.1
	 */
	protected void firePageChanged(final PageChangedEvent event) {
		for (IPageChangedListener l : pageChangedListeners) {
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					l.pageChanged(event);
				}
			});
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
