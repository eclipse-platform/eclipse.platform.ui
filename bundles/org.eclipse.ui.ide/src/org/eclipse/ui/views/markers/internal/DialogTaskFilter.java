/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

		private String contains = Messages.getString("filtersDialog.contains"); //$NON-NLS-1$

		private String doesNotContain = Messages
				.getString("filtersDialog.doesNotContain"); //$NON-NLS-1$

		/**
		 * Create a new DescriptionGroup.
		 * 
		 * @param parent
		 */
		public DescriptionGroup(Composite parent) {
			descriptionLabel = new Label(parent, SWT.NONE);
			descriptionLabel.setFont(parent.getFont());
			descriptionLabel.setText(Messages
					.getString("filtersDialog.descriptionLabel")); //$NON-NLS-1$

			combo = new Combo(parent, SWT.READ_ONLY);
			combo.setFont(parent.getFont());
			combo.add(contains);
			combo.add(doesNotContain);
			combo.addSelectionListener(selectionListener);
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

		public void updateEnablement() {
			descriptionLabel.setEnabled(isFilterEnabled());
			combo.setEnabled(isFilterEnabled());
			description.setEnabled(isFilterEnabled());
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
					updateEnablement();
					DialogTaskFilter.this.markDirty();
				}
			};

			enablementButton = new Button(parent, SWT.CHECK);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			enablementButton.setLayoutData(data);
			enablementButton.setFont(parent.getFont());
			enablementButton.setText(Messages
					.getString("filtersDialog.priorityLabel")); //$NON-NLS-1$
			enablementButton.addSelectionListener(listener);

			highButton = new Button(parent, SWT.CHECK);
			highButton.setFont(parent.getFont());
			highButton
					.setText(Messages.getString("filtersDialog.priorityHigh")); //$NON-NLS-1$
			highButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			highButton.addSelectionListener(selectionListener);

			normalButton = new Button(parent, SWT.CHECK);
			normalButton.setFont(parent.getFont());
			normalButton.setText(Messages
					.getString("filtersDialog.priorityNormal")); //$NON-NLS-1$
			normalButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			normalButton.addSelectionListener(selectionListener);

			lowButton = new Button(parent, SWT.CHECK);
			lowButton.setFont(parent.getFont());
			lowButton.setText(Messages.getString("filtersDialog.priorityLow")); //$NON-NLS-1$
			lowButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			lowButton.addSelectionListener(selectionListener);
		}

		public boolean isEnabled() {
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

		public void updateEnablement() {
			enablementButton.setEnabled(isFilterEnabled());
			highButton.setEnabled(enablementButton.isEnabled() && isEnabled());
			normalButton
					.setEnabled(enablementButton.isEnabled() && isEnabled());
			lowButton.setEnabled(enablementButton.isEnabled() && isEnabled());
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
					updateEnablement();
					DialogTaskFilter.this.markDirty();
				}
			};

			enablementButton = new Button(parent, SWT.CHECK);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			enablementButton.setLayoutData(data);
			enablementButton.setFont(parent.getFont());
			enablementButton.setText(Messages
					.getString("filtersDialog.statusLabel")); //$NON-NLS-1$
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
			completeButton.setText(Messages
					.getString("filtersDialog.statusComplete")); //$NON-NLS-1$
			completeButton.addSelectionListener(listener);

			incompleteButton = new Button(composite, SWT.RADIO);
			incompleteButton.setFont(composite.getFont());
			incompleteButton.setText(Messages
					.getString("filtersDialog.statusIncomplete")); //$NON-NLS-1$
			incompleteButton.addSelectionListener(listener);
		}

		public boolean isEnabled() {
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

		public void updateEnablement() {
			enablementButton.setEnabled(isFilterEnabled());
			completeButton.setEnabled(enablementButton.isEnabled()
					&& isEnabled());
			incompleteButton.setEnabled(enablementButton.isEnabled()
					&& isEnabled());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.FiltersDialog#updateFilterFromUI(org.eclipse.ui.views.markerview.MarkerFilter)
	 */
	protected void updateFilterFromUI() {

		TaskFilter filter = (TaskFilter) getSelectedFilter();
		filter.setContains(descriptionGroup.getContains());
		filter.setDescription(descriptionGroup.getDescription().trim());

		filter.setSelectByPriority(priorityGroup.isEnabled());
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
		filter.setPriority(priority);

		filter.setSelectByDone(statusGroup.isEnabled());
		filter.setDone(statusGroup.getDone());

		super.updateFilterFromUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.FiltersDialog#updateUIFromFilter(org.eclipse.ui.views.markerview.MarkerFilter)
	 */
	protected void updateUIFromFilter() {

		TaskFilter filter = (TaskFilter) getSelectedFilter();
		descriptionGroup.setContains(filter.getContains());
		descriptionGroup.setDescription(filter.getDescription());

		priorityGroup.setEnabled(filter.getSelectByPriority());
		int priority = filter.getPriority();
		priorityGroup
				.setHighSelected((priority & TaskFilter.PRIORITY_HIGH) > 0);
		priorityGroup
				.setNormalSelected((priority & TaskFilter.PRIORITY_NORMAL) > 0);
		priorityGroup.setLowSelected((priority & TaskFilter.PRIORITY_LOW) > 0);

		statusGroup.setEnabled(filter.getSelectByDone());
		statusGroup.setDone(filter.getDone());

		super.updateUIFromFilter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.FiltersDialog#updateEnabledState()
	 */
	protected void updateEnabledState() {
		super.updateEnabledState();
		descriptionGroup.updateEnablement();
		priorityGroup.updateEnablement();
		statusGroup.updateEnablement();
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
