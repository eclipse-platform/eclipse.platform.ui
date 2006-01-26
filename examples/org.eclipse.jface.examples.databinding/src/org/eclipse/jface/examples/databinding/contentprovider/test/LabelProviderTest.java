/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.Collections;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IReadableValue;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.updatables.WritableSet;
import org.eclipse.jface.databinding.viewers.ListeningLabelProvider;
import org.eclipse.jface.databinding.viewers.UpdatableSetContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersProperties;
import org.eclipse.jface.examples.databinding.ExampleBinding;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Tests UpdatableTreeContentProvider and DirtyIndicationLabelProvider.
 * Creates a tree containing three randomly-generated sets of integers,
 * and one node that contains the union of the other sets.
 * 
 * @since 3.2
 */
public class LabelProviderTest {
	
	private Shell shell;
	private ListViewer list;
	private Label exploredNodesLabel;
	private WritableSet setOfRenamables;
	private Button addButton;
	private Button removeButton;
	private Button renameButton;
	private SelectionListener buttonSelectionListener = new SelectionAdapter() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			Button pressed = (Button)e.widget;
			if (pressed == addButton) {
				setOfRenamables.add(new RenamableItem());
			} else if (pressed == removeButton) {
				setOfRenamables.remove(getCurrentSelection());
			} else if (pressed == renameButton) {
				rename(getCurrentSelection());
			}
			
			super.widgetSelected(e);
		}
	};
	private IReadableValue selectedRenamable;
	
	public LabelProviderTest() {
				
		// Create shell
		shell = new Shell(Display.getCurrent());
		{ // Initialize shell
			setOfRenamables = new WritableSet();
			
			list = new ListViewer(shell);
			UpdatableSetContentProvider contentProvider = new UpdatableSetContentProvider();
			list.setContentProvider(contentProvider);
			list.setLabelProvider(new ListeningLabelProvider(contentProvider.getKnownElements()) {
				IChangeListener listener = new IChangeListener() {
					/* (non-Javadoc)
					 * @see org.eclipse.jface.databinding.IChangeListener#handleChange(org.eclipse.jface.databinding.ChangeEvent)
					 */
					public void handleChange(ChangeEvent changeEvent) {
						fireChangeEvent(Collections.singleton(changeEvent.getSource()));
					}
				};
				
				/* (non-Javadoc)
				 * @see org.eclipse.jface.databinding.viewers.ViewerLabelProvider#updateLabel(org.eclipse.jface.viewers.ViewerLabel, java.lang.Object)
				 */
				public void updateLabel(ViewerLabel label, Object element) {
					if (element instanceof RenamableItem) {
						RenamableItem item = (RenamableItem) element;
						
						label.setText(item.getName());
					}
				}
				
				protected void addListenerTo(Object next) {
					RenamableItem item = (RenamableItem)next;
					
					item.addListener(listener);
				}
				
				protected void removeListenerFrom(Object next) {
					RenamableItem item = (RenamableItem)next;
					
					item.removeListener(listener);					
				}
			});
			list.setInput(setOfRenamables);
	
			IDataBindingContext viewerContext = ExampleBinding.createContext(list.getControl());
			selectedRenamable = (IReadableValue)viewerContext.createUpdatable(new Property(list, ViewersProperties.SINGLE_SELECTION));
			
			Composite buttonBar = new Composite(shell, SWT.NONE);
			{   // Initialize buttonBar
				addButton = new Button(buttonBar, SWT.PUSH);
				addButton.setText("Add");
				addButton.addSelectionListener(buttonSelectionListener);
				removeButton = new Button(buttonBar, SWT.PUSH);
				removeButton.addSelectionListener(buttonSelectionListener);
				removeButton.setText("Remove");
				renameButton = new Button(buttonBar, SWT.PUSH);
				renameButton.addSelectionListener(buttonSelectionListener);
				renameButton.setText("Rename");

				selectedRenamable.addChangeListener(new IChangeListener() {
					public void handleChange(ChangeEvent changeEvent) {
						boolean shouldEnable = selectedRenamable.getValue() != null;
						
						removeButton.setEnabled(shouldEnable);
						renameButton.setEnabled(shouldEnable);
					}
				});
				removeButton.setEnabled(false);
				renameButton.setEnabled(false);
				
				GridLayoutFactory.fillDefaults().generateLayout(buttonBar);
			}
			
		}
		GridLayoutFactory.fillDefaults().numColumns(2).margins(LayoutConstants.getMargins()).generateLayout(shell);
	}

	/**
	 * @param currentSelection
	 */
	protected void rename(final RenamableItem currentSelection) {
		final Shell promptShell = new Shell(shell, SWT.DIALOG_TRIM);
		{
			Label prompt = new Label(promptShell, SWT.WRAP);
			
			prompt.setText("Enter a the new item name");
			final Text promptText = new Text(promptShell, SWT.BORDER);
			promptText.setText(currentSelection.getName());
			
			Composite buttonBar = new Composite(promptShell, SWT.NONE);
			{
				Button okay = new Button(buttonBar, SWT.PUSH);
				okay.addSelectionListener(new SelectionAdapter() {
					/* (non-Javadoc)
					 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
					 */
					public void widgetSelected(SelectionEvent e) {
						currentSelection.setName(promptText.getText());

						promptShell.close();
					}
				});
				
				okay.setText("Okay");
				GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(buttonBar);
			}
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(buttonBar);

		}
		
		GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins()).generateLayout(promptShell);
		
		promptShell.pack();
		
		promptShell.setVisible(true);
	}

	/**
	 * @return
	 */
	protected RenamableItem getCurrentSelection() {
		return (RenamableItem)selectedRenamable.getValue();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		LabelProviderTest test = new LabelProviderTest();
		Shell s = test.getShell();
		s.pack();
		s.setVisible(true);

		while (!s.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private Shell getShell() {
		return shell;
	}
}
