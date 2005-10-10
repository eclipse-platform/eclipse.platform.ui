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
package org.eclipse.search2.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.ui.dialogs.SelectionDialog;

import org.eclipse.search.ui.ISearchResult;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ListContentProvider;
import org.eclipse.search.internal.ui.util.SWTUtil;

/**
 * Dialog that shows a list of items with icon and label.
 */
public class SearchHistorySelectionDialog extends SelectionDialog {
	
	private static final int REMOVE_ID= IDialogConstants.CLIENT_ID+1;
	private static final int WIDTH_IN_CHARACTERS= 55;
	
	private List fInput;
	private TableViewer fViewer;
	private Button fRemoveButton;
	
	private static final class SearchesLabelProvider extends LabelProvider {
		
		private ArrayList fImages= new ArrayList();
		
		public String getText(Object element) {
			return ((ISearchResult)element).getLabel();
		}
		
		public Image getImage(Object element) {

			ImageDescriptor imageDescriptor= ((ISearchResult)element).getImageDescriptor(); 
			if (imageDescriptor == null)
				return null;
			
			Image image= imageDescriptor.createImage();
			fImages.add(image);

			return image;
		}
		
		public void dispose() {
			Iterator iter= fImages.iterator();
			while (iter.hasNext())
				((Image)iter.next()).dispose();
			
			fImages= null;
		}
	}

	public SearchHistorySelectionDialog(Shell parent, List input) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setTitle(SearchMessages.SearchesDialog_title);  
		setMessage(SearchMessages.SearchesDialog_message); 
		fInput= input;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		return SearchPlugin.getDefault().getDialogSettingsSection("DialogBounds_SearchHistorySelectionDialog"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
	 */
	protected int getDialogBoundsStrategy() {
		return 0x0002; // TODO: change to DIALOG_PERSISTSIZE;
	}
	
	
	/*
	 * Overrides method from Dialog
	 */
	protected Label createMessageArea(Composite composite) {
		Label label = new Label(composite,SWT.WRAP);
		label.setText(getMessage()); 
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(WIDTH_IN_CHARACTERS);
		label.setLayoutData(gd);
		applyDialogFont(label);
		return label;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		
		List initialSelection= getInitialElementSelections();
		if (initialSelection != null)
			fViewer.setSelection(new StructuredSelection(initialSelection));

		validateDialogState();
	}
	
	/*
	 * Overrides method from Dialog
	 */
	protected Control createDialogArea(Composite container) {
		Composite ancestor= (Composite) super.createDialogArea(container);
		
		createMessageArea(ancestor);
		
		Composite parent= new Composite(ancestor, SWT.NONE);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		parent.setLayout(layout);
		

		fViewer= new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fViewer.setContentProvider(new ListContentProvider());
		
		final Table table= fViewer.getTable();
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				okPressed();
			}
		});
		fViewer.setLabelProvider(new SearchesLabelProvider());
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(15);
		gd.widthHint= convertWidthInCharsToPixels(WIDTH_IN_CHARACTERS);
		table.setLayoutData(gd);
		
		
        fRemoveButton= new Button(parent, SWT.PUSH);
        fRemoveButton.setText(SearchMessages.SearchesDialog_remove_label); 
        fRemoveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                buttonPressed(REMOVE_ID);
            }
        });
		fRemoveButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		SWTUtil.setButtonDimensionHint(fRemoveButton);
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validateDialogState();
			}
		});
		
		applyDialogFont(ancestor);

		// set input & selections last, so all the widgets are created.
		fViewer.setInput(fInput);
		return table;
	}
	
	protected final void validateDialogState() {
		IStructuredSelection sel= (IStructuredSelection) fViewer.getSelection();
		int elementsSelected= sel.toList().size();
		
		fRemoveButton.setEnabled(elementsSelected > 0);
		Button okButton= getOkButton();
		if (okButton != null) {
			okButton.setEnabled(elementsSelected == 1);
		}
	}
	
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == REMOVE_ID) {
			IStructuredSelection selection= (IStructuredSelection) fViewer.getSelection();
			Iterator searchResults= selection.iterator();
			while (searchResults.hasNext()) {
				ISearchResult result= (ISearchResult)searchResults.next();
				InternalSearchUI.getInstance().removeQuery(result.getQuery());
				fInput.remove(result);
				fViewer.remove(result);
			}
			if (fViewer.getSelection().isEmpty() && !fInput.isEmpty()) {
				fViewer.setSelection(new StructuredSelection(fInput.get(0)));
			}
			return;
		}
		super.buttonPressed(buttonId);
	}
		
	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed() {
		// Build a list of selected children.
		ISelection selection= fViewer.getSelection();
		if (selection instanceof IStructuredSelection)
			setResult(((IStructuredSelection)fViewer.getSelection()).toList());
		super.okPressed();
	}
}


