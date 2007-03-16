/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DialogTaskFilter extends
		org.eclipse.ui.views.markers.internal.DialogMarkerFilter {

	private DescriptionGroup descriptionGroup;

	private PriorityGroup priorityGroup;

	private StatusGroup statusGroup;

	private class DescriptionGroup {
		private Label descriptionLabel;

		private Combo combo;

		private Text description;

		private String contains = MarkerMessages.filtersDialog_contains;

		private String doesNotContain = MarkerMessages.filtersDialog_doesNotContain;

		/**
		 * Create a new DescriptionGroup.
		 * 
		 * @param parent
		 */
		public DescriptionGroup(Composite parent) {
			descriptionLabel = new Label(parent, SWT.NONE);
			descriptionLabel.setFont(parent.getFont());
			descriptionLabel.setText(
				MarkerMessages.filtersDialog_descriptionLabel);

			combo = new Combo(parent, SWT.READ_ONLY);
			combo.setFont(parent.getFont());
			combo.add(contains);
			combo.add(doesNotContain);
			combo.addSelectionListener(new SelectionAdapter(){
	        	/* (non-Javadoc)
	        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	        	 */
	        	public void widgetSelected(SelectionEvent e) {
	        		  updateForSelection();
	        	}
	          });
			// Prevent Esc and Return from closing the dialog when the combo is
			// active.
			combo.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_ESCAPE
							|| e.detail == SWT.TRAVERSE_RETURN) {
						e.doit = false;
					}
				}
			});

			description = new Text(parent, SWT.SINGLE | SWT.BORDER);
			description.setFont(parent.getFont());
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 3;
			description.setLayoutData(data);
			description.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					DialogTaskFilter.this.markDirty();
				}
			});
		}

		public boolean getContains() {
			return combo.getSelectionIndex() == combo.indexOf(contains);
		}

		public void setContains(boolean value) {
			if (value) {
				combo.select(combo.indexOf(contains));
			} else {
				combo.select(combo.indexOf(doesNotContain));
			}
		}

		public void setDescription(String text) {
			if (text == null) {
				description.setText(""); //$NON-NLS-1$ 
			} else {
				description.setText(text);
			}
		}

		public String getDescription() {
			return description.getText();
		}

		/**
		 * Update the enabled state.
		 * @param enabled
		 */
		public void updateEnablement(boolean enabled) {
			descriptionLabel.setEnabled(enabled);
			combo.setEnabled(enabled);
			description.setEnabled(enabled);
		}
	}

	private class PriorityGroup {
		private Button enablementButton;

		private Button highButton;

		private Button normalButton;

		private Button lowButton;

		/**
		 * Create a new priority group.
		 * 
		 * @param parent
		 */
		public PriorityGroup(Composite parent) {
			SelectionListener listener = new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					updateEnablement(true);
					DialogTaskFilter.this.markDirty();
				}
			};

			enablementButton = new Button(parent, SWT.CHECK);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			enablementButton.setLayoutData(data);
			enablementButton.setFont(parent.getFont());
			enablementButton.setText(
					MarkerMessages.filtersDialog_priorityLabel);
			enablementButton.addSelectionListener(listener);

			highButton = new Button(parent, SWT.CHECK);
			highButton.setFont(parent.getFont());
			highButton
					.setText(MarkerMessages.filtersDialog_priorityHigh);
			highButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			highButton.addSelectionListener(new SelectionAdapter(){
	        	/* (non-Javadoc)
	        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	        	 */
	        	public void widgetSelected(SelectionEvent e) {
	        		  updateForSelection();
	        	}
	          });

			normalButton = new Button(parent, SWT.CHECK);
			normalButton.setFont(parent.getFont());
			normalButton.setText(MarkerMessages.filtersDialog_priorityNormal);
			normalButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			normalButton.addSelectionListener(new SelectionAdapter(){
	        	/* (non-Javadoc)
	        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	        	 */
	        	public void widgetSelected(SelectionEvent e) {
	        		  updateForSelection();
	        	}
	          });

			lowButton = new Button(parent, SWT.CHECK);
			lowButton.setFont(parent.getFont());
			lowButton.setText(MarkerMessages.filtersDialog_priorityLow);
			lowButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			lowButton.addSelectionListener(new SelectionAdapter(){
	        	/* (non-Javadoc)
	        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	        	 */
	        	public void widgetSelected(SelectionEvent e) {
	        		  updateForSelection();
	        	}
	          });
		}

		public boolean isPriorityEnabled() {
			return enablementButton.getSelection();
		}

		public void setEnabled(boolean enabled) {
			enablementButton.setSelection(enabled);
		}

		public boolean isHighSelected() {
			return highButton.getSelection();
		}

		public void setHighSelected(boolean selected) {
			highButton.setSelection(selected);
		}

		public boolean isNormalSelected() {
			return normalButton.getSelection();
		}

		public void setNormalSelected(boolean selected) {
			normalButton.setSelection(selected);
		}

		public boolean isLowSelected() {
			return lowButton.getSelection();
		}

		public void setLowSelected(boolean selected) {
			lowButton.setSelection(selected);
		}

		/**
		 * Update enablement based on enabled.
		 * @param enabled
		 */
		public void updateEnablement(boolean enabled) {
			enablementButton.setEnabled(enabled);
			highButton.setEnabled(enabled && isPriorityEnabled());
			normalButton
					.setEnabled(enabled && isPriorityEnabled());
			lowButton.setEnabled(enabled && isPriorityEnabled());
		}
	}

	private class StatusGroup {
		private Button enablementButton;

		private Button completeButton;

		private Button incompleteButton;

		/**
		 * Create a new StatusGroup.
		 * 
		 * @param parent
		 */
		public StatusGroup(Composite parent) {
			SelectionListener enablementListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateEnablement(true);
					DialogTaskFilter.this.markDirty();
				}
			};

			enablementButton = new Button(parent, SWT.CHECK);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			enablementButton.setLayoutData(data);
			enablementButton.setFont(parent.getFont());
			enablementButton.setText(MarkerMessages.filtersDialog_statusLabel);
			enablementButton.addSelectionListener(enablementListener);

			Composite composite = new Composite(parent, SWT.NONE);
			composite.setFont(parent.getFont());
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			composite.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 3;
			composite.setLayoutData(data);

			SelectionListener listener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					completeButton.setSelection(!incompleteButton
							.getSelection());
					incompleteButton.setSelection(!completeButton
							.getSelection());
					DialogTaskFilter.this.markDirty();
				}
			};

			completeButton = new Button(composite, SWT.RADIO);
			completeButton.setFont(composite.getFont());
			completeButton.setText(MarkerMessages.filtersDialog_statusComplete);
			completeButton.addSelectionListener(listener);

			incompleteButton = new Button(composite, SWT.RADIO);
			incompleteButton.setFont(composite.getFont());
			incompleteButton.setText(MarkerMessages.filtersDialog_statusIncomplete);
			incompleteButton.addSelectionListener(listener);
		}

		public boolean isStatusEnabled() {
			return enablementButton.getSelection();
		}

		public void setEnabled(boolean enabled) {
			enablementButton.setSelection(enabled);
		}

		public boolean getDone() {
			return completeButton.getSelection();
		}

		public void setDone(boolean done) {
			completeButton.setSelection(done);
			incompleteButton.setSelection(!done);
		}

		/**
		 * Update the enablement state of the group.
		 * @param enabled
		 */
		public void updateEnablement(boolean enabled) {
			enablementButton.setEnabled(enabled);
			completeButton.setEnabled(isStatusEnabled()
					&& enabled);
			incompleteButton.setEnabled(isStatusEnabled()
					&& enabled);
		}
	}

	/**
	 * Create a new instance of the receiver
	 * 
	 * @param parentShell
	 * @param filters
	 */
	public DialogTaskFilter(Shell parentShell, TaskFilter[] filters) {
		super(parentShell, filters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#createAttributesArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createAttributesArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(5, false);
		layout.verticalSpacing = 7;
		composite.setLayout(layout);

		descriptionGroup = new DescriptionGroup(composite);
		priorityGroup = new PriorityGroup(composite);
		statusGroup = new StatusGroup(composite);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateFilterFromUI(org.eclipse.ui.views.markers.internal.MarkerFilter)
	 */
	protected void updateFilterFromUI(MarkerFilter filter) {
		super.updateFilterFromUI(filter);

		TaskFilter taskFilter = (TaskFilter)filter;
		taskFilter.setContains(descriptionGroup.getContains());
		taskFilter.setDescription(descriptionGroup.getDescription().trim());

		taskFilter.setSelectByPriority(priorityGroup.isPriorityEnabled());
		int priority = 0;
		if (priorityGroup.isHighSelected()) {
			priority = priority | TaskFilter.PRIORITY_HIGH;
		}
		if (priorityGroup.isNormalSelected()) {
			priority = priority | TaskFilter.PRIORITY_NORMAL;
		}
		if (priorityGroup.isLowSelected()) {
			priority = priority | TaskFilter.PRIORITY_LOW;
		}
		taskFilter.setPriority(priority);

		taskFilter.setSelectByDone(statusGroup.isStatusEnabled());
		taskFilter.setDone(statusGroup.getDone());
	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateUIWithFilter(org.eclipse.ui.views.markers.internal.MarkerFilter)
	 */
	protected void updateUIWithFilter(MarkerFilter filter) {
		TaskFilter taskFilter = (TaskFilter)filter;
		descriptionGroup.setContains(taskFilter.getContains());
		descriptionGroup.setDescription(taskFilter.getDescription());

		priorityGroup.setEnabled(taskFilter.getSelectByPriority());
		int priority = taskFilter.getPriority();
		priorityGroup
				.setHighSelected((priority & TaskFilter.PRIORITY_HIGH) > 0);
		priorityGroup
				.setNormalSelected((priority & TaskFilter.PRIORITY_NORMAL) > 0);
		priorityGroup.setLowSelected((priority & TaskFilter.PRIORITY_LOW) > 0);

		statusGroup.setEnabled(taskFilter.getSelectByDone());
		statusGroup.setDone(taskFilter.getDone());
		
		super.updateUIWithFilter(filter);

	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateEnabledState(boolean)
	 */
	protected void updateEnabledState(boolean enabled) {
		super.updateEnabledState(enabled);
		descriptionGroup.updateEnablement(enabled);
		priorityGroup.updateEnablement(enabled);
		statusGroup.updateEnablement(enabled);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.FiltersDialog#resetPressed()
	 */
	protected void resetPressed() {
		descriptionGroup.setContains(TaskFilter.DEFAULT_CONTAINS);
		descriptionGroup.setDescription(TaskFilter.DEFAULT_DESCRIPTION);

		priorityGroup.setEnabled(TaskFilter.DEFAULT_SELECT_BY_PRIORITY);
		priorityGroup
				.setHighSelected((TaskFilter.DEFAULT_PRIORITY & TaskFilter.PRIORITY_HIGH) > 0);
		priorityGroup
				.setNormalSelected((TaskFilter.DEFAULT_PRIORITY & TaskFilter.PRIORITY_NORMAL) > 0);
		priorityGroup
				.setLowSelected((TaskFilter.DEFAULT_PRIORITY & TaskFilter.PRIORITY_NORMAL) > 0);

		statusGroup.setEnabled(TaskFilter.DEFAULT_SELECT_BY_DONE);
		statusGroup.setDone(TaskFilter.DEFAULT_DONE);

		super.resetPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#newFilter(java.lang.String)
	 */
	protected MarkerFilter newFilter(String newName) {
		return new TaskFilter(newName);
	}

}
