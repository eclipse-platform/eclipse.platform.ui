/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.SimpleListContentProvider;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Dialog to allow the user to select a feature from a list.
 */
public class FeatureSelectionDialog extends SelectionDialog {
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
	 * The help context id
	 */
	private String helpContextId;

	/**
	 * Creates an instance of this dialog to display
	 * the given features.
	 * <p>
	 * There must be at least one feature.
	 * </p>
	 * 
	 * @param the parent shell
	 * @param the features to display
	 * @param the primary feature
	 * @param shell title
	 * @param shell message
	 * @param help context id
	 */
	public FeatureSelectionDialog(
		Shell shell,
		AboutInfo[] features,
		AboutInfo primaryFeature,
		String shellTitle,
		String shellMessage,
		String helpContextId) {
			
		super(shell);
		Assert.isTrue(features != null && features.length > 0);
		this.features = features;
		this.helpContextId = helpContextId;
		setTitle(WorkbenchMessages.getString(shellTitle));
		setMessage(WorkbenchMessages.getString(shellMessage));
			
		// Sort ascending
		Arrays.sort(features, new Comparator() {
			Collator coll = Collator.getInstance(Locale.getDefault());
				public int compare(Object a, Object b) {
					AboutInfo i1, i2;
					String name1, name2;
					i1 = (AboutInfo)a;
					name1 = i1.getFeatureLabel();
					i2 = (AboutInfo)b;
					name2 = i2.getFeatureLabel();
					if (name1 == null)
						name1 = ""; //$NON-NLS-1$
					if (name2 == null)
						name2 = ""; //$NON-NLS-1$
					return coll.compare(name1, name2);
				}
			});

		// Find primary feature
		int index = -1;
		if (primaryFeature != null) {
			for (int i = 0; i < features.length; i++) {
				if (features[i].getFeatureId().equals(primaryFeature.getFeatureId())) {
					index = i;
					break;
				}
			}
		}	
		
		// Set the intitial selection		
		if (index >= 0 && index < features.length)
			setInitialSelections(new Object[] { features[index] });
		else
			setInitialSelections(new Object[0]);
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		WorkbenchHelp.setHelp(newShell, helpContextId);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		// Create label
		createMessageArea(composite);
		// Create list viewer	
		listViewer =
			new ListViewer(
				composite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = convertHeightInCharsToPixels(LIST_HEIGHT);
		data.widthHint = convertWidthInCharsToPixels(LIST_WIDTH);
		listViewer.getList().setLayoutData(data);
		listViewer.getList().setFont(parent.getFont());
		// Set the label provider		
		listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				// Return the features's label.
				return element == null ? "" : ((AboutInfo) element).getFeatureLabel(); //$NON-NLS-1$
			}
		});

		// Set the content provider
		SimpleListContentProvider cp = new SimpleListContentProvider();
		cp.setElements(features);
		listViewer.setContentProvider(cp);
		listViewer.setInput(new Object());
		// it is ignored but must be non-null

		// Set the initial selection
		listViewer.setSelection(
			new StructuredSelection(getInitialElementSelections()),
			true);

		// Add a selection change listener
		listViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
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
		IStructuredSelection selection =
			(IStructuredSelection) listViewer.getSelection();
		setResult(selection.toList());
		super.okPressed();
	}
}