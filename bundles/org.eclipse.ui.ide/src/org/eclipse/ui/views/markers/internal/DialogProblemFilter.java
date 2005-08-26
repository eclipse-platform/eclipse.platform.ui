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

public class DialogProblemFilter extends DialogMarkerFilter {

	private DescriptionGroup descriptionGroup;

	private SeverityGroup severityGroup;

	private class DescriptionGroup {
		private Label descriptionLabel;

		private Combo combo;

		private Text description;

		private String contains = MarkerMessages.filtersDialog_contains;

		private String doesNotContain = MarkerMessages.filtersDialog_doesNotContain;

		/**
		 * Create a descriptor group.
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
					DialogProblemFilter.this.markDirty();
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
		 * Update the enablement state based on whether or not
		 * the receiver is enabled.
		 * @param enabled
		 */
		public void updateEnablement(boolean enabled) {
			descriptionLabel.setEnabled(enabled);
			combo.setEnabled(enabled);
			description.setEnabled(enabled);
		}
	}

	private class SeverityGroup {
		private Button enablementButton;

		private Button errorButton;

		private Button warningButton;

		private Button infoButton;

		/**
		 * Create a group for severity.
		 * 
		 * @param parent
		 */
		public SeverityGroup(Composite parent) {
			SelectionListener listener = new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					updateEnablement(true);
					DialogProblemFilter.this.markDirty();
				}
			};

			enablementButton = new Button(parent, SWT.CHECK);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			enablementButton.setLayoutData(data);
			enablementButton.setFont(parent.getFont());
			enablementButton.setText(MarkerMessages.filtersDialog_severityLabel);
			enablementButton.addSelectionListener(listener);

			errorButton = new Button(parent, SWT.CHECK);
			errorButton.setFont(parent.getFont());
			errorButton.setText(MarkerMessages.filtersDialog_severityError);
			errorButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			errorButton.addSelectionListener(new SelectionAdapter(){
	        	/* (non-Javadoc)
	        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	        	 */
	        	public void widgetSelected(SelectionEvent e) {
	        		  updateForSelection();
	        	}
	          });

			warningButton = new Button(parent, SWT.CHECK);
			warningButton.setFont(parent.getFont());
			warningButton.setText(MarkerMessages.filtersDialog_severityWarning);
			warningButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			warningButton.addSelectionListener(new SelectionAdapter(){
	        	/* (non-Javadoc)
	        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	        	 */
	        	public void widgetSelected(SelectionEvent e) {
	        		  updateForSelection();
	        	}
	          });

			infoButton = new Button(parent, SWT.CHECK);
			infoButton.setFont(parent.getFont());
			infoButton
					.setText(MarkerMessages.filtersDialog_severityInfo);
			infoButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			infoButton.addSelectionListener(new SelectionAdapter(){
	        	/* (non-Javadoc)
	        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	        	 */
	        	public void widgetSelected(SelectionEvent e) {
	        		  updateForSelection();
	        	}
	          });
		}

		public boolean isSeveritySelected() {
			return enablementButton.getSelection();
		}

		public void setEnabled(boolean enabled) {
			enablementButton.setSelection(enabled);
		}

		public boolean isErrorSelected() {
			return errorButton.getSelection();
		}

		public void setErrorSelected(boolean selected) {
			errorButton.setSelection(selected);
		}

		public boolean isWarningSelected() {
			return warningButton.getSelection();
		}

		public void setWarningSelected(boolean selected) {
			warningButton.setSelection(selected);
		}

		public boolean isInfoSelected() {
			return infoButton.getSelection();
		}

		public void setInfoSelected(boolean selected) {
			infoButton.setSelection(selected);
		}

		/**
		 * Update enablement based on the enabled flag.
		 * @param enabled
		 */
		public void updateEnablement(boolean enabled) {
			
			boolean showingSeverity = isSeveritySelected();
			enablementButton.setEnabled(enabled);
			errorButton.setEnabled(showingSeverity && enabled);
			warningButton.setEnabled(showingSeverity && enabled);
			infoButton.setEnabled(showingSeverity && enabled);
			
		}
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 * @param filters
	 */
	public DialogProblemFilter(Shell parentShell, ProblemFilter[] filters) {
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
		severityGroup = new SeverityGroup(composite);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateFilterFromUI(org.eclipse.ui.views.markers.internal.MarkerFilter)
	 */
	protected void updateFilterFromUI(MarkerFilter filter) {
		super.updateFilterFromUI(filter);

		ProblemFilter problemFilter = (ProblemFilter) filter;
		problemFilter.setContains(descriptionGroup.getContains());
		problemFilter.setDescription(descriptionGroup.getDescription().trim());

		problemFilter.setSelectBySeverity(severityGroup.isSeveritySelected());
		int severity = 0;
		if (severityGroup.isErrorSelected()) {
			severity = severity | ProblemFilter.SEVERITY_ERROR;
		}
		if (severityGroup.isWarningSelected()) {
			severity = severity | ProblemFilter.SEVERITY_WARNING;
		}
		if (severityGroup.isInfoSelected()) {
			severity = severity | ProblemFilter.SEVERITY_INFO;
		}
		problemFilter.setSeverity(severity);
	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateUIWithFilter(org.eclipse.ui.views.markers.internal.MarkerFilter)
	 */
	protected void updateUIWithFilter(MarkerFilter filter) {

		ProblemFilter problemFilter = (ProblemFilter) filter;
		descriptionGroup.setContains(problemFilter.getContains());
		descriptionGroup.setDescription(problemFilter.getDescription());

		severityGroup.setEnabled(problemFilter.getSelectBySeverity());
		int severity = problemFilter.getSeverity();
		
		severityGroup
				.setErrorSelected((severity & ProblemFilter.SEVERITY_ERROR) > 0);
		severityGroup
				.setWarningSelected((severity & ProblemFilter.SEVERITY_WARNING) > 0);
		severityGroup
				.setInfoSelected((severity & ProblemFilter.SEVERITY_INFO) > 0);
		
		super.updateUIWithFilter(filter);
	
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateEnabledState(boolean)
	 */
	protected void updateEnabledState(boolean enabled) {
		super.updateEnabledState(enabled);
		descriptionGroup.updateEnablement(enabled);
		severityGroup.updateEnablement(enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.FiltersDialog#resetPressed()
	 */
	protected void resetPressed() {
		descriptionGroup.setContains(ProblemFilter.DEFAULT_CONTAINS);
		descriptionGroup.setDescription(ProblemFilter.DEFAULT_DESCRIPTION);

		severityGroup.setEnabled(ProblemFilter.DEFAULT_SELECT_BY_SEVERITY);
		severityGroup
				.setErrorSelected((ProblemFilter.DEFAULT_SEVERITY & ProblemFilter.SEVERITY_ERROR) > 0);
		severityGroup
				.setWarningSelected((ProblemFilter.DEFAULT_SEVERITY & ProblemFilter.SEVERITY_WARNING) > 0);
		severityGroup
				.setInfoSelected((ProblemFilter.DEFAULT_SEVERITY & ProblemFilter.SEVERITY_INFO) > 0);

		super.resetPressed();
	}

	protected MarkerFilter newFilter(String newName) {
		return new ProblemFilter(newName);
	}
}
