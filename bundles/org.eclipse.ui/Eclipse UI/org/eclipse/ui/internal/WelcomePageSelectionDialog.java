package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.SimpleListContentProvider;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Dialog to allow the user to select a feature from a list.
 */
public class WelcomePageSelectionDialog extends SelectionDialog {
	/**
	 * List width in characters.
	 */
	private final static int LIST_WIDTH = 60;
	/**
	 * List height in characters.
	 */
	private final static int LIST_HEIGHT = 10;
	/**
	 * The feature about infos.
	 */
	private AboutInfo[] features;
	/**
	 * List to display the resolutions.
	 */
	private ListViewer listViewer;

	/**
	 * Creates an instance of this dialog to display
	 * the given features.
	 * <p>
	 * There must be at least one feature.
	 * </p>
	 * 
	 * @param shell the parent shell
	 * @param markerResolutions the resolutions to display
	 */
	public WelcomePageSelectionDialog(Shell shell, AboutInfo[] features) {
		super(shell);
		Assert.isTrue(features != null && features.length > 0);
		this.features = features;
		setTitle(WorkbenchMessages.getString("WelcomePageSelectionDialog.title"));	//$NON-NLS-1$
		setMessage(WorkbenchMessages.getString("WelcomePageSelectionDialog.message")); //$NON-NLS-1$
		setInitialSelections(new Object[]{features[0]});
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		WorkbenchHelp.setHelp(newShell, IHelpContextIds.WELCOME_PAGE_SELECTION_DIALOG);
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		
		// Create label
		createMessageArea(composite);
		// Create list viewer	
		listViewer = new ListViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = convertHeightInCharsToPixels(LIST_HEIGHT);
		data.widthHint = convertWidthInCharsToPixels(LIST_WIDTH);
		listViewer.getList().setLayoutData(data);
		// Set the label provider		
		listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
			 	// Return the features's label.
				return element == null ? "" : ((AboutInfo)element).getFeatureLabel(); //$NON-NLS-1$
			}
		});

		// Set the content provider
		SimpleListContentProvider cp = new SimpleListContentProvider();
		cp.setElements(features);
		listViewer.setContentProvider(cp);
		listViewer.setInput(new Object()); // it is ignored but must be non-null
		
		// Set the initial selection
		listViewer.setSelection(new StructuredSelection(getInitialSelections()), true);	
		
		// Add a selection change listener
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// Update OK button enablement
				getOkButton().setEnabled(!event.getSelection().isEmpty());
			}
		});
		
		// Add double-click listener
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		return composite;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection)listViewer.getSelection(); 
		setResult(selection.toList());
		super.okPressed();
	}
}
