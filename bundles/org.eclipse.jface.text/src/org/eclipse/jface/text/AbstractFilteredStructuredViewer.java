/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Ziegler - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.viewers.StructuredViewer;

/**
 * The abstract base class for a composite containing both a column viewer and a text widget. The
 * text drives the filter of the viewer.
 *
 * @since 3.27
 */
public abstract class AbstractFilteredStructuredViewer extends Composite {

	/**
	 * The filter text widget to be used by this viewer. This value may be {@code null} if there is
	 * no filter widget, or if the controls have not yet been created.
	 */
	protected Text filterText;

	/**
	 * The Composite on which the filter controls are created. This is used to set the background
	 * color of the filter controls to match the surrounding controls.
	 */
	protected Composite filterComposite;

	/**
	 * The text to initially show in the filter text control.
	 */
	protected String initialText= ""; //$NON-NLS-1$

	/**
	 * The job used to refresh the viewer.
	 */
	private Job refreshJob;

	/**
	 * The parent composite of the filtered viewer.
	 */
	protected Composite parent;

	/**
	 * Whether or not to show the filter controls (text and clear button).
	 */
	protected boolean showFilterControls;

	/**
	 * Tells whether this filtered viewer is used to make quick selections. In this mode the first
	 * match in the viewer is automatically selected while filtering and the 'Enter' key is not used
	 * to move the focus to the viewer.
	 */
	private boolean quickSelectionMode= false;

	/**
	 * Time for refresh job delay in terms of expansion in long value
	 */
	private final long refreshJobDelayInMillis;

	protected AbstractFilteredStructuredViewer(Composite parent, int style, long refreshJobDelayInMillis) {
		super(parent, style);
		this.parent= parent;
		this.refreshJobDelayInMillis= refreshJobDelayInMillis;
	}

	/**
	 * Create the filtered viewer.
	 *
	 * @param style the style bits for the {@link StructuredViewer}.
	 */
	protected void init(int style) {
		showFilterControls= isShowFilterControls();
		createControl(parent, style);
		createRefreshJob();
		setFont(parent.getFont());
		getViewer().getControl().addDisposeListener(e -> refreshJob.cancel());
	}

	/**
	 * Indicates whether the filter controls (text and clear button) should be shown.
	 *
	 * @return {@code true}, if the filter controls should be shown, otherwise {@code false}.
	 */
	protected abstract boolean isShowFilterControls();

	/**
	 * Get the quick selection mode.
	 *
	 * @return {@code true}, if enabled otherwise {@code false}.
	 */
	protected boolean isQuickSelectionMode() {
		return quickSelectionMode;
	}

	/**
	 * Create the filtered viewer's controls. Subclasses should override.
	 *
	 * @param parentComposite the parent
	 * @param style SWT style bits used to create the viewer
	 */
	protected void createControl(Composite parentComposite, int style) {
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		setLayout(layout);

		if (parentComposite.getLayout() instanceof GridLayout) {
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}

		if (showFilterControls) {
			filterComposite= new Composite(this, SWT.NONE);
			GridLayout filterLayout= new GridLayout();
			filterLayout.marginHeight= 0;
			filterLayout.marginWidth= 0;
			filterComposite.setLayout(filterLayout);
			filterComposite.setFont(parentComposite.getFont());

			createFilterControls(filterComposite);
			filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	}
	}

	/**
	 * Create the filter controls. By default, a text and corresponding tool bar button that clears
	 * the contents of the text is created. Subclasses may override.
	 *
	 * @param parentComposite parent {@link Composite} of the filter controls
	 * @return the {@link Composite} that contains the filter controls
	 */
	protected Composite createFilterControls(Composite parentComposite) {
		createFilterText(parentComposite);
		return parentComposite;
	}

	/**
	 * Create the refresh job for the receiver.
	 */
	private void createRefreshJob() {
		refreshJob= doCreateRefreshJob();
		refreshJob.setSystem(true);
	}

	/**
	 * Creates a workbench job that will refresh the viewer based on the current filter text.
	 * Subclasses may override.
	 *
	 * @return a workbench job that can be scheduled to refresh the viewer
	 */
	protected abstract Job doCreateRefreshJob();

	/**
	 * Creates the filter text and adds listeners. This method calls
	 * {@link #doCreateFilterText(Composite)} to create the text control. Subclasses should override
	 * {@link #doCreateFilterText(Composite)} instead of overriding this method.
	 *
	 * @param parentComposite {@link Composite} of the filter text
	 */
	protected void createFilterText(Composite parentComposite) {
		filterText= doCreateFilterText(parentComposite);

		filterText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				/*
				 * Running in an asyncExec because the selectAll() does not appear to work when
				 * using mouse to give focus to text.
				 */
				Display display= filterText.getDisplay();
				display.asyncExec(() -> {
					if (!filterText.isDisposed()) {
						if (getInitialText().equals(filterText.getText().trim())) {
							filterText.selectAll();
					}
					}
				});
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (filterText.getText().equals(initialText)) {
					//We cannot call clearText() due to
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=260664
					doClearText();
				}
			}
		});

		filterText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (filterText.getText().equals(initialText)) {
					//We cannot call clearText() due to
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=260664
					doClearText();
				}
			}
		});

		filterText.addModifyListener(e -> textChanged());

		GridData gridData= new GridData(SWT.FILL, SWT.CENTER, true, false);
		filterText.setLayoutData(gridData);
	}

	/**
	 * Creates the text control for entering the filter text. Subclasses may override.
	 *
	 * @param parentComposite the parent composite
	 * @return the text widget
	 */
	protected Text doCreateFilterText(Composite parentComposite) {
		return new Text(parentComposite, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
	}

	/**
	 * Update the receiver after the text has changed.
	 */
	protected void textChanged() {
		// cancel currently running job first, to prevent unnecessary redraw
		refreshJob.cancel();
		refreshJob.schedule(getRefreshJobDelay());
	}

	/**
	 * Return the time delay that should be used when scheduling the filter refresh job. Subclasses
	 * may override.
	 *
	 * @return a time delay in milliseconds before the job should run
	 */
	protected long getRefreshJobDelay() {
		return refreshJobDelayInMillis;
	}

	/**
	 * Clears the text in the filter text widget.
	 */
	protected void clearText() {
		doClearText();
	}

	private void doClearText() {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=260664
		setFilterText(""); //$NON-NLS-1$
		textChanged();
	}

	/**
	 * Set the text in the filter control.
	 *
	 * @param filterText the text to set.
	 */
	protected void setFilterText(String filterText) {
		if (this.filterText != null) {
			this.filterText.setText(filterText);
			selectAll();
		}
	}

	/**
	 * Get the structured viewer of the receiver.
	 *
	 * @return the structured viewer
	 */
	public abstract StructuredViewer getViewer();

	/**
	 * Get the filter text for the receiver, if it was created. Otherwise return {@code null}.
	 *
	 * @return the filter Text, or null if it was not created
	 */
	public Text getFilterControl() {
		return filterText;
	}

	/**
	 * Convenience method to return the text of the filter control. If the text widget is not
	 * created, then null is returned.
	 *
	 * @return String in the text, or null if the text does not exist
	 */
	protected String getFilterString() {
		return filterText != null ? filterText.getText() : null;
	}

	/**
	 * Set the text that will be shown until the first focus. A default value is provided, so this
	 * method only need be called if overriding the default initial text is desired.
	 *
	 * @param text initial text to appear in text field
	 */
	public void setInitialText(String text) {
		initialText= text;
		if (filterText != null) {
			filterText.setMessage(text);
			if (filterText.isFocusControl()) {
				setFilterText(initialText);
				textChanged();
			} else {
				getDisplay().asyncExec(() -> {
					if (!filterText.isDisposed() && filterText.isFocusControl()) {
						setFilterText(initialText);
						textChanged();
					}
				});
			}
		} else {
			setFilterText(initialText);
			textChanged();
		}
	}

	/**
	 * Sets whether this filtered viewer is used to make quick selections. In this mode the first
	 * match in the viewer is automatically selected while filtering and the 'Enter' key is not used
	 * to move the focus to the viewer.
	 * <p>
	 * By default, this is set to {@code false}.
	 * </p>
	 *
	 * @param enabled {@code true} if this filtered viewer is used to make quick selections,
	 *            {@code false} otherwise
	 */
	public void setQuickSelectionMode(boolean enabled) {
		this.quickSelectionMode= enabled;
	}

	/**
	 * Select all text in the filter text field.
	 */
	protected void selectAll() {
		if (filterText != null) {
			filterText.selectAll();
		}
	}

	/**
	 * Get the initial text for the receiver.
	 *
	 * @return String
	 */
	protected String getInitialText() {
		return initialText;
	}
}