package org.eclipse.help.internal.ui.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import org.xml.sax.Attributes;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributions.xml.*;
import org.eclipse.help.internal.util.Resources;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.ui.*;
import org.eclipse.help.internal.server.PluginURL;

/**
 * SearchPage
 */
public class SearchPage extends NavigationPage {

	// Maximum number of displayed hits.
	// This is different than maximum number of hits returned by GTR.
	// We allow GTR to search for more hits,
	// and than display only few top ranked ones.
	private final static int RESULT_GROUP_SIZE = 15;
	// Maximum number of search results.
	// This is how many sets of RESULT_GROUP_SIZE we provide
	private final static int RESULTS_GROUPS = 6;

	private static final int ENTRY_FIELD_LENGTH = 256;
	private static final int ENTRY_FIELD_ROW_COUNT = 1;
	public static final int BASE_HEIGHT = 60;

	private Composite control;
	private Text searchTextField = null;
	private Button searchButton = null;
	private Button advancedButton = null;
	private Composite searchControl;

	// This is a cheat to keep the last query string around.
	// This won't work if we need to support multithreaded SearchURLs.
	private static String lastQuery = null;
	private TreeViewer resultsViewer = null;
	private static final String IMAGE_GO = "go_icon";
	private static ImageRegistry imgRegistry = null;

	// Listeners to register later, because we use lazy control creation
	private Collection selectionChangedListeners = new ArrayList();

	// Search query based on the data entered in the UI
	HelpSearchQuery searchQuery;

	// nodes to show in the results tree
	class SearchElement extends HelpTopic {
		public SearchElement(String label, String url) {
			super(null);
			id = url;
			this.label = label;
			if (url != null)
				href = PluginURL.getPrefix() + "/" + url;
		}
		public SearchElement(Attributes atts) {
			super(atts);
		}
	}

	/**
	 * Search Page
	 * @parameter workbook workbook that this page is part of
	 */
	public SearchPage(NavigationWorkbook workbook) {
		super(workbook, WorkbenchResources.getString("Search"));
		if (imgRegistry == null) {
			imgRegistry = WorkbenchHelpPlugin.getDefault().getImageRegistry();
			imgRegistry.put(
				IMAGE_GO,
				ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("go_icon")));
		}

		// Create the query to perform search for the current info set
		String infoSet =
			HelpSystem
				.getNavigationManager()
				.getCurrentNavigationModel()
				.getRootElement()
				.getID();

		searchQuery = new HelpSearchQuery("");
		searchQuery.setInfoset(infoSet);
		searchQuery.setLocale(Locale.getDefault().toString());
	}
	/**
	 * Adds a listener for selection changes in this selection provider.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener a selection changed listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (resultsViewer != null)
			resultsViewer.addSelectionChangedListener(listener);
		selectionChangedListeners.add(listener);
	}
	protected Contribution buildSearchTree(String resultsAsXMLString) {
		XMLSearchContributor searchContributor =
			new XMLSearchContributor(resultsAsXMLString);
		HelpContribution root = (HelpContribution) searchContributor.getContribution();

		//SearchElement root =
		//  new SearchElement(WorkbenchResources.getString("Search_results"), null);

		if (root == null || !root.getChildren().hasNext()) {
			root.addChild(
				new SearchElement(WorkbenchResources.getString("No_results_found"), "org.eclipse.help/" + Resources.getString("noresults.html")));
			return root;
		}

		List documents = root.getChildrenList();
		if (documents.size() > RESULT_GROUP_SIZE) {
			int maxResultsSets =
				(documents.size() + RESULT_GROUP_SIZE - 1) / RESULT_GROUP_SIZE;
			int resultSetCount = Math.min(maxResultsSets, RESULTS_GROUPS);

			// Create a duplicate list of all the children
			List resultsBackup = new ArrayList(documents.size());
			for (Iterator it = documents.iterator(); it.hasNext();)
				resultsBackup.add(it.next());

			// Remove all topics from the root, and create groups of GROUP_SIZE topics
			root.getChildrenList().removeAll(documents);
			for (int r = 0; r < resultSetCount; r++) {
				// the range of results displayed
				int resultsBegin = 1 + r * RESULT_GROUP_SIZE;
				int resultsEnd = Math.min((r + 1) * RESULT_GROUP_SIZE, resultsBackup.size());

				String label =
					WorkbenchResources.getString("Results")
						+ String.valueOf(resultsBegin)
						+ ".."
						+ String.valueOf(resultsEnd);
				HelpTopic resultsGroup = new HelpTopic(null);
				resultsGroup.setRawLabel(label);
				root.addChild(resultsGroup);

				//Inserting results rows here
				int maxRange = Math.min(resultsBackup.size(), resultsEnd);
				for (int i = r * RESULT_GROUP_SIZE; i < maxRange; i++) {
					//SearchElement doc = (SearchElement)documents.get(i);
					//SearchElement child = new SearchElement(doc.getLabel(), doc.getHref());
					Contribution child = (Contribution) resultsBackup.get(i);
					resultsGroup.addChild(child);
				}
			}
		}

		return root;
	}
	protected Control createControl(Composite parent) {
		control = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		//layout.verticalSpacing = 5;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		control.setLayout(layout);
		control.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the search entry/options part
		createSearchControl(control);

		// Create the search results tree
		createResultsControl(control);

		// add all listeners registered before actual control was created
		for (Iterator it = selectionChangedListeners.iterator(); it.hasNext();) {
			resultsViewer.addSelectionChangedListener(
				(ISelectionChangedListener) it.next());
		}

		WorkbenchHelp.setHelp(
			control,
			new String[] {
				IHelpUIConstants.SEARCH_PAGE,
				IHelpUIConstants.NAVIGATION_VIEWER,
				IHelpUIConstants.EMBEDDED_HELP_VIEW});
		return control;
	}
	/**
	* Returns SWT control for SearchViewer viewer.
	*/
	protected Control createResultsControl(Composite parent) {

		resultsViewer =
			new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		resultsViewer.setContentProvider(TreeContentProvider.getDefault());
		resultsViewer.setLabelProvider(ElementLabelProvider.getDefault());
		resultsViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		resultsViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});

		WorkbenchHelp.setHelp(
			resultsViewer.getControl(),
			new String[] {
				IHelpUIConstants.RESULTS_VIEWER,
				IHelpUIConstants.SEARCH_PAGE,
				IHelpUIConstants.NAVIGATION_VIEWER,
				IHelpUIConstants.EMBEDDED_HELP_VIEW});
		return resultsViewer.getControl();
	}
	/**
	 * Returns SWT control for SearchViewer viewer.
	 */
	protected Control createSearchControl(Composite parent) {
		GridData gd = new GridData();

		// Create the search entry/options part
		Composite searchPart = new Composite(parent, SWT.FLAT);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		gl.marginHeight = 2;
		gl.marginWidth = 2;
		searchPart.setLayout(gl);
		gd.horizontalAlignment = gd.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = gd.BEGINNING;
		gd.grabExcessVerticalSpace = false;
		searchPart.setLayoutData(gd);

		// Create the entry field.    
		searchTextField = new Text(searchPart, SWT.BORDER);
		searchTextField.setTextLimit(ENTRY_FIELD_LENGTH);
		//searchTextField.getVerticalBar().setVisible(false);
		gd = new GridData();
		gd.heightHint = searchTextField.getLineHeight() * ENTRY_FIELD_ROW_COUNT;
		gd.horizontalAlignment = gd.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = gd.FILL;
		gd.grabExcessVerticalSpace = true;
		searchTextField.setLayoutData(gd);
		/*
		searchTextField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				// set the focus to the field.
				// this should not be necessary but it looks likes
				// the advanced button sometimes gets focus after
				// showing the results.
				//if (!searchTextField.isFocusControl())
				//	searchTextField.setFocus();
				if (e.character == '\n' || e.character == '\r')
					doSearch();
			}
			public void keyReleased(KeyEvent e) {
			}
		});
		*/

		// Create the button that launchs the search
		searchButton = new Button(searchPart, SWT.PUSH);
		//searchButton.setText("Go");
		searchButton.setImage(imgRegistry.get(IMAGE_GO));
		//searchButton.setFocus();

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSearch();
				searchTextField.setFocus();
			}
		};

		searchButton.addSelectionListener(listener);

		gd = new GridData();
		gd.verticalAlignment = gd.BEGINNING;
		searchButton.setLayoutData(gd);
		searchPart.getShell().setDefaultButton(searchButton);

		// Create the "Advanced Search Options" button
		// opens Options window
		advancedButton = new Button(searchPart, SWT.FLAT);
		advancedButton.setText(WorkbenchResources.getString("Advanced"));

		SelectionListener listener2 = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AdvancedSearchDialog dialog =
					new AdvancedSearchDialog(Display.getCurrent().getActiveShell(), searchQuery);
				dialog.open();
				//searchButton.setFocus();
			}
		};
		advancedButton.addSelectionListener(listener2);
		gd = new GridData();
		gd.verticalAlignment = gd.BEGINNING;
		gd.horizontalSpan = 2;
		advancedButton.setLayoutData(gd);

		// If I try to select all the text, the cursor gets
		// scrolled to the middle of the text box, and you
		// lose the first 4 characters of the message...
		searchTextField.setText(WorkbenchResources.getString("Enter_search_string"));
		//searchTextField.setFocus();
		searchControl = searchPart;
		return searchPart;
	}
	protected void doSearch() {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {

				// May need to index first
				if (!ensureIndexIsUpdated())
					return;

				// Create the query and convert it to a URL format
				searchQuery.setKey(searchTextField.getText());
				String queryURL = searchQuery.toURLQuery();

				String results = null;
				if (HelpSystem.isClient())
					// help server is remote
					results =
						HelpSystem.getSearchManager().getRemoteSearchResults(
							searchQuery.getInfoset(),
							queryURL);
				else
					results =
						HelpSystem.getSearchManager().getSearchResults(
							searchQuery.getInfoset(),
							queryURL);

				Contribution searchRoot = buildSearchTree(results);

				if (resultsViewer != null && searchRoot != null) {
					resultsViewer.setInput(searchRoot);

					// Expand to and select first result
					if (searchRoot.getChildren().hasNext()) {
						HelpTopic firstFolder = (HelpTopic) searchRoot.getChildren().next();
						if (firstFolder.getChildren().hasNext()) {
							HelpTopic firstTopic = (HelpTopic) firstFolder.getChildren().next();
							resultsViewer.expandToLevel(firstTopic, 0);
							resultsViewer.setSelection(new StructuredSelection(firstTopic), true);
						} else {
							//firstFolder is not folder but actually a topic
							resultsViewer.expandToLevel(firstFolder, 0);
							resultsViewer.setSelection(new StructuredSelection(firstFolder), true);
						}
					}

				}
			}
		});
	}
	/*
	 * @return true if has been updated, or does not need update,
	 *  false if update did not succeed (failed or canceled)
	 */
	protected boolean ensureIndexIsUpdated() {
		// Only verify the index (or do the actual indexing)
		// when the install is local (same process for both help client/server

		if (HelpSystem.isClient()
			|| !HelpSystem.getSearchManager().isIndexingNeeded(
				searchQuery.getInfoset(),
				searchQuery.getLocale()))
			return true;

		ProgressMonitorDialog mon =
			new ProgressMonitorDialog(Display.getDefault().getActiveShell());
		mon.setCancelable(true);

		IRunnableWithProgress indexingRunnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
				String infoSet =
					HelpSystem
						.getNavigationManager()
						.getCurrentNavigationModel()
						.getRootElement()
						.getID();
				try {
					HelpSystem.getSearchManager().updateIndex(
						searchQuery.getInfoset(),
						monitor,
						searchQuery.getLocale());
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		try {
			mon.run(true, true, indexingRunnable); // throws InvocationTargetException
		} catch (InterruptedException ie) {
			return false;
		} catch (InvocationTargetException e) {
			// wraps OperationCanceledException or Exception in case of error
			return false;
		}
		return true;
	}
	public Control getControl() {
		return control;
	}
	/**
	 * Returns the current selection for this provider.
	 * 
	 * @return the current selection
	 */
	public ISelection getSelection() {
		if (resultsViewer != null)
			return resultsViewer.getSelection();
		return null;
	}
	/**
	 * Handles double clicks in viewer.
	 * Opens editor if file double-clicked.
	 */
	void handleDoubleClick(DoubleClickEvent event) {

		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		// Double-clicking in navigator should expand/collapse containers
		if (resultsViewer != null && resultsViewer.isExpandable(element)) {
			resultsViewer.setExpandedState(
				element,
				!resultsViewer.getExpandedState(element));
		}
	}
	/**
	 * Removes the given selection change listener from this selection provider.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener a selection changed listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (resultsViewer != null)
			resultsViewer.removeSelectionChangedListener(listener);
		selectionChangedListeners.remove(listener);
	}
	/**
	 * Sets the selection current selection for this selection provider.
	 *
	 * @param selection the new selection
	 */
	public void setSelection(ISelection selection) {
		if (resultsViewer != null)
			resultsViewer.setSelection((selection), true);
	}
}
