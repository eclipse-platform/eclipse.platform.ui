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
package org.eclipse.search2.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.search.internal.ui.util.ListContentProvider;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Dialog that shows a list of items with icon and label.
 */
public class SearchesDialog extends SelectionDialog {
	
	private static final int REMOVE_ID= IDialogConstants.CLIENT_ID+1;
	private static final int WIDTH_IN_CHARACTERS= 55;
	
	private List fInput;
	private TableViewer fViewer;
	
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

	public SearchesDialog(Shell parent, List input) {
		super(parent);
		setTitle(SearchMessages.getString("SearchesDialog.title"));  //$NON-NLS-1$
		setMessage(SearchMessages.getString("SearchesDialog.message")); //$NON-NLS-1$
		fInput= input;
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
	
	/*
	 * Overrides method from Dialog
	 */
	protected Control createDialogArea(Composite container) {
		Composite ancestor= (Composite) super.createDialogArea(container);
		
		createMessageArea(ancestor);
		
		Composite parent= new Composite(ancestor, SWT.NONE);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		parent.setLayout(layout);
		

		fViewer= new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fViewer.setContentProvider(new ListContentProvider());
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getButton(REMOVE_ID).setEnabled(!event.getSelection().isEmpty());
			}
		});

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
		
		Button button= createButton(parent, REMOVE_ID, SearchMessages.getString("SearchesDialog.remove.label"), false); //$NON-NLS-1$
		((GridData)button.getLayoutData()).verticalAlignment= GridData.BEGINNING;
		
		applyDialogFont(ancestor);

		// set input & selections last, so all the widgets are created.
		fViewer.setInput(fInput);
		List initialSelection= getInitialElementSelections();
		if (initialSelection != null)
			fViewer.setSelection(new StructuredSelection(initialSelection));

		return table;
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == REMOVE_ID) {
			IStructuredSelection selection= (IStructuredSelection) fViewer.getSelection();
			Iterator searchResults= selection.iterator();
			while (searchResults.hasNext()) {
				ISearchResult result= (ISearchResult)searchResults.next();
				InternalSearchUI.getInstance().removeQuery(result.getQuery());
				fInput.remove(result);
			}
			fViewer.refresh();
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


