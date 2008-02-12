package org.eclipse.ui.internal.views.markers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.markers.FilterConfigurationArea;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * The PriorityConfigurationArea is the configuration area for the task
 * priority.
 * 
 * @since 3.4
 * 
 */
public class PriorityConfigurationArea extends FilterConfigurationArea {

	private int priorities;
	private Button highButton;
	private Button normalButton;
	private Button lowButton;

	/**
	 * Create a new instance of the receiver.
	 */
	public PriorityConfigurationArea() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#apply(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void apply(MarkerFieldFilter filter) {
		((PriorityMarkerFieldFilter) filter).selectedPriorities = priorities;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {
		
		parent.setLayout(new GridLayout(3,false));

		highButton = new Button(parent, SWT.CHECK);
		highButton.setText(MarkerMessages.filtersDialog_priorityHigh);
		highButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updatePriorities(PriorityMarkerFieldFilter.PRIORITY_HIGH,
						highButton.getSelection());
			}

		});

		normalButton = new Button(parent, SWT.CHECK);
		normalButton.setText(MarkerMessages.filtersDialog_priorityNormal);
		normalButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updatePriorities(PriorityMarkerFieldFilter.PRIORITY_NORMAL,
						normalButton.getSelection());
			}
		});

		lowButton = new Button(parent, SWT.CHECK);
		lowButton.setText(MarkerMessages.filtersDialog_priorityLow);
		lowButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updatePriorities(PriorityMarkerFieldFilter.PRIORITY_LOW,
						normalButton.getSelection());
			}
		});
	}

	/**
	 * Update he priorities set based on the constant and the selection value.
	 * 
	 * @param constant
	 * @param enabled
	 */
	void updatePriorities(int constant, boolean enabled) {

		if (enabled)
			priorities = constant | priorities;
		else
			priorities = constant ^ priorities;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#initialize(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void initialize(MarkerFieldFilter filter) {
		priorities = ((PriorityMarkerFieldFilter) filter).selectedPriorities;

		lowButton
				.setSelection((PriorityMarkerFieldFilter.PRIORITY_LOW & priorities) > 0);
		normalButton
				.setSelection((PriorityMarkerFieldFilter.PRIORITY_NORMAL & priorities) > 0);
		highButton
				.setSelection((PriorityMarkerFieldFilter.PRIORITY_HIGH & priorities) > 0);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#getTitle()
	 */
	public String getTitle() {
		return MarkerMessages.filtersDialog_priorityTitle;
	}

}
