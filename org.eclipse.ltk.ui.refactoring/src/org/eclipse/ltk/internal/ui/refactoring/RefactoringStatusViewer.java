/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.compare.CompareUI;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.PageBook;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.internal.ui.refactoring.util.PixelConverter;
import org.eclipse.ltk.internal.ui.refactoring.util.ViewerPane;
import org.eclipse.ltk.ui.refactoring.IStatusContextViewer;

public class RefactoringStatusViewer extends SashForm {

	private static class NullContextViewer implements IStatusContextViewer {
		private Label fLabel;
		public NullContextViewer() {
		}
		public void createControl(Composite parent) {
			fLabel= new Label(parent, SWT.CENTER | SWT.FLAT);
			fLabel.setText(RefactoringUIMessages.getString("ErrorWizardPage.no_context_information_available")); //$NON-NLS-1$
		}
		public void setInput(RefactoringStatusContext input) {
			// do nothing
		}
		public Control getControl() {
			return fLabel;
		}
	}
	
	private class NextProblem extends Action {
		public NextProblem() {
			setImageDescriptor(CompareUI.DESC_ETOOL_NEXT);
			setDisabledImageDescriptor(CompareUI.DESC_DTOOL_NEXT);
			setHoverImageDescriptor(CompareUI.DESC_CTOOL_NEXT);
			setToolTipText(RefactoringUIMessages.getString("ErrorWizardPage.next_Change")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, IRefactoringHelpContextIds.NEXT_PROBLEM_ACTION);
		}
		public void run() {
			revealElement(true);
		}
		public void update() {
			boolean enabled= false;
			if (fStatus != null && fStatus.hasEntries()) {
				RefactoringStatusEntry[] entries= fStatus.getEntries();
				int index= fTableViewer.getTable().getSelectionIndex();
				enabled= index == -1 || index < entries.length - 1;
			}
			setEnabled(enabled);
		}
	}
	
	private class PreviousProblem extends Action {
		public PreviousProblem() {
			setImageDescriptor(CompareUI.DESC_ETOOL_PREV);
			setDisabledImageDescriptor(CompareUI.DESC_DTOOL_PREV);
			setHoverImageDescriptor(CompareUI.DESC_CTOOL_PREV);
			setToolTipText(RefactoringUIMessages.getString("ErrorWizardPage.previous_Change")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, IRefactoringHelpContextIds.PREVIOUS_PROBLEM_ACTION);
		}	
		public void run() {
			revealElement(false);
		}
		public void update() {
			boolean enabled= false;
			if (fStatus != null && fStatus.hasEntries()) {
				int index= fTableViewer.getTable().getSelectionIndex();
				enabled= index == -1 || index > 0;
			}
			setEnabled(enabled);
		}
	}
	
	private static class RefactoringStatusSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			int r1= ((RefactoringStatusEntry)e1).getSeverity();
			int r2= ((RefactoringStatusEntry)e2).getSeverity();
			if (r1 < r2)
				return 1;
			if (r2 < r1)
				return -1;
			return 0;
		}

	}
	
	private RefactoringStatus fStatus;
	private TableViewer fTableViewer;
	private PageBook fContextViewerContainer;
	private StatusContextViewerDescriptor fCurrentDescriptor;
	private IStatusContextViewer fCurrentContextViewer;
	private NullContextViewer fNullContextViewer;
	
	private NextProblem fNextProblem;
	private PreviousProblem fPreviousProblem;
	
	public RefactoringStatusViewer(Composite parent, int style) {
		super(parent, style | SWT.VERTICAL);
		createContents();
	}

	/**
	 * Sets the refactoring status.
	 * 
	 * @param status the refactoring status.
	 */
	public void setStatus(RefactoringStatus status){
		fStatus= status;
		if (fTableViewer.getInput() != fStatus) {
			fTableViewer.setInput(fStatus);
			fTableViewer.getTable().getColumn(0).pack();
			ISelection selection= fTableViewer.getSelection();
			if (selection.isEmpty()) {
				RefactoringStatusEntry entry= getFirstEntry();
				if (entry != null) {
					fTableViewer.setSelection(new StructuredSelection(entry));
					showContextViewer(entry);
					fTableViewer.getControl().setFocus();
				}
			}
			fNextProblem.update();
			fPreviousProblem.update();
		}
	}
	
	/**
	 * Returns the currently used <tt>RefactoringStatus</tt>.
	 * @return the <tt>RefactoringStatus</tt>
	 */
	public RefactoringStatus getStatus() {
		return fStatus;
	}
	
	//---- UI creation ----------------------------------------------------------------------
	
	public Point computeSize (int wHint, int hHint, boolean changed) {
		PixelConverter converter= new PixelConverter(this);
		return new Point(converter.convertWidthInCharsToPixels(90), converter.convertHeightInCharsToPixels(25));
	}
	
	private void createContents() {
		GridLayout layout= new GridLayout();
		layout.numColumns= 1; layout.marginWidth= 0; layout.marginHeight= 0;
		setLayout(layout);
		
		ViewerPane contextPane= new ViewerPane(this, SWT.BORDER | SWT.FLAT);
		contextPane.setText(RefactoringUIMessages.getString("RefactoringStatusViewer.Found_problems")); //$NON-NLS-1$
		ToolBarManager tbm= contextPane.getToolBarManager();
		tbm.add(fNextProblem= new NextProblem());
		tbm.add(fPreviousProblem= new PreviousProblem());
		tbm.update(true);
		createTableViewer(contextPane);
		contextPane.setContent(fTableViewer.getControl());
		
		fContextViewerContainer= new PageBook(this, SWT.NONE);
		fNullContextViewer= new NullContextViewer();
		fNullContextViewer.createControl(fContextViewerContainer);
		fContextViewerContainer.showPage(fNullContextViewer.getControl());
		fCurrentContextViewer= fNullContextViewer;
		fCurrentContextViewer= fNullContextViewer;
		fCurrentDescriptor= null;	
		
		setWeights(new int[]{35, 65});		
	}
	
	private  void createTableViewer(Composite parent) {
		fTableViewer= new TableViewer(new Table(parent, SWT.SINGLE | SWT.H_SCROLL));
		fTableViewer.setLabelProvider(new RefactoringStatusEntryLabelProvider());
		fTableViewer.setContentProvider(new RefactoringStatusContentProvider());
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				entrySelected(event.getSelection());
				fNextProblem.update();
				fPreviousProblem.update();
			}
		});
		fTableViewer.setSorter(new RefactoringStatusSorter());	
		Table tableControl= fTableViewer.getTable();
		// must set the dialog font here since we pack the table and this
		// might otherwise happen with the wrong font resulting in clipped
		// messages.
		tableControl.setFont(JFaceResources.getDialogFont());
		GridData gd= new GridData(GridData.FILL_BOTH);
		tableControl.setLayoutData(gd);
		// Add a column so that we can pack it in setVisible.
		TableColumn tc= new TableColumn(tableControl, SWT.NONE);
		tc.setResizable(false);
	}

	//---- Feed status entry into context viewer ---------------------------------------------------------

	private void entrySelected(ISelection s) {
		if (!(s instanceof IStructuredSelection))
			return;
		Object first= ((IStructuredSelection) s).getFirstElement();
		if (! (first instanceof RefactoringStatusEntry))
			return;
		
		RefactoringStatusEntry entry= (RefactoringStatusEntry)first;
		showContextViewer(entry);
	}

	private void showContextViewer(RefactoringStatusEntry entry) {
		RefactoringStatusContext context= entry.getContext();
		if (context == null) {
			showNullContextViewer();
		} else {
			try {
				StatusContextViewerDescriptor descriptor= StatusContextViewerDescriptor.get(context);
				if (fCurrentDescriptor != descriptor) {
					IStatusContextViewer newViewer;
					if (descriptor != null) {
						newViewer= descriptor.createViewer();
						newViewer.createControl(fContextViewerContainer);
					} else {
						newViewer= fNullContextViewer;
					}
					fCurrentDescriptor= descriptor;
					newViewer.setInput(context);
					if (fCurrentContextViewer != null && fCurrentContextViewer != fNullContextViewer)
						fCurrentContextViewer.getControl().dispose();
					fCurrentContextViewer= newViewer;				
					fContextViewerContainer.showPage(fCurrentContextViewer.getControl());
				} else {
					fCurrentContextViewer.setInput(context);
				}
			} catch (CoreException e) {
				showNullContextViewer();
				ExceptionHandler.handle(e, getShell(),
					RefactoringUIMessages.getString("RefactoringStatusViewer.error.title"), //$NON-NLS-1$
					RefactoringUIMessages.getString("RefactoringStatusViewer.error.message")); //$NON-NLS-1$
			}
		}
	}
	
	private void showNullContextViewer() {
		fCurrentContextViewer= fNullContextViewer;
		fCurrentDescriptor= null;
		fContextViewerContainer.showPage(fCurrentContextViewer.getControl());
	}
	
	//---- Helpers ----------------------------------------------------------------------------------------
	
	private RefactoringStatusEntry getFirstEntry() {
		if (fStatus == null || !fStatus.hasEntries())
			return null;
		return fStatus.getEntryAt(0);
	}
		
	private void revealElement(boolean next) {
		RefactoringStatusEntry[] entries= fStatus.getEntries();
		if (entries.length == 0) {
			return;
		}
		int index= fTableViewer.getTable().getSelectionIndex();
		int last= entries.length - 1;
		boolean doIt= true;
		if (index == -1) {
			index= 0;
		} else if (next && index < last) {
			index++;
		} else if (!next && index > 0) {
			index--;
		} else {
			doIt= false;
		}
		if (doIt)	
			fTableViewer.setSelection(new StructuredSelection(entries[index]));
	}

}
