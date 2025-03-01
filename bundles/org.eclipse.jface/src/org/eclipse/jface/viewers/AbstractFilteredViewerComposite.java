/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
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
 *******************************************************************************/
package org.eclipse.jface.viewers;

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

/**
 * @since 3.36
 */
public abstract class AbstractFilteredViewerComposite<T extends ViewerFilter> extends Composite {

	/**
	 * The pattern filter for the tree. This value must not be <code>null</code>.
	 */
	private ViewerFilter patternFilter;

	/**
	 * The filter text widget to be used by this tree. This value may be
	 * {@code null} if there is no filter widget, or if the controls have not yet
	 * been created.
	 */
	protected Text filterText;

	/**
	 * The Composite on which the filter controls are created. This is used to set
	 * the background color of the filter controls to match the surrounding
	 * controls.
	 */
	protected Composite filterComposite;

	/**
	 * Whether or not to show the filter controls (text and clear button). The
	 * default is to show these controls. This can be overridden by providing a
	 * setting in the product configuration file. For example, the setting to add to
	 * not show these controls in an 3x based application is:
	 *
	 * org.eclipse.ui/SHOW_FILTERED_TEXTS=false
	 */
	protected boolean showFilterControls;

	/**
	 * The text to initially show in the filter text control.
	 */
	protected String initialText = ""; //$NON-NLS-1$

	/**
	 * The parent composite of the filtered viewer.
	 */
	protected Composite parent;

	/**
	 * Time for refresh job delay in terms of expansion in long value
	 */
	private final long refreshJobDelayInMillis;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param parent                  a widget which will be the parent this
	 *                                composite
	 * @param style                   the style used to construct this widget
	 * @param refreshJobDelayInMillis refresh delay in ms, the time to expand the
	 *                                tree after debounce
	 */
	public AbstractFilteredViewerComposite(Composite parent, int style, long refreshJobDelayInMillis) {
		super(parent, style);
		this.refreshJobDelayInMillis = refreshJobDelayInMillis;
	}

	/**
	 * Create the filtered viewer.
	 *
	 * @param style  the style bits for the viewer's {@code Control}
	 * @param filter the filter to be used
	 */
	protected void init(int style, T filter) {
		patternFilter = filter;
		createControl(parent, style);
		setFont(parent.getFont());

	}

	/**
	 * Create the filtered viewer's controls. Subclasses should override.
	 *
	 * @param parent the parent
	 * @param style  SWT style bits used to create the control
	 */
	protected void createControl(Composite parent, int style) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		if (parent.getLayout() instanceof GridLayout) {
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}

		if (showFilterControls) {
			filterComposite = new Composite(this, SWT.NONE);
			GridLayout filterLayout = new GridLayout();
			filterLayout.marginHeight = 0;
			filterLayout.marginWidth = 0;
			filterComposite.setLayout(filterLayout);
			filterComposite.setFont(parent.getFont());

			createFilterControls(filterComposite);
			filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		}
	}

	/**
	 * Create the filter controls. By default, a text and corresponding tool bar
	 * button that clears the contents of the text is created. Subclasses may
	 * override.
	 *
	 * @param parent parent <code>Composite</code> of the filter controls
	 * @return the <code>Composite</code> that contains the filter controls
	 */
	protected Composite createFilterControls(Composite parent) {
		createFilterText(parent);
		return parent;
	}

	/**
	 * Creates the filter text and adds listeners. This method calls
	 * {@link #doCreateFilterText(Composite)} to create the text control. Subclasses
	 * should override {@link #doCreateFilterText(Composite)} instead of overriding
	 * this method.
	 *
	 * @param parent <code>Composite</code> of the filter text
	 */
	protected void createFilterText(Composite parent) {
		filterText = doCreateFilterText(parent);

		filterText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				/*
				 * Running in an asyncExec because the selectAll() does not appear to work when
				 * using mouse to give focus to text.
				 */
				Display display = filterText.getDisplay();
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
					setFilterText(""); //$NON-NLS-1$
					textChanged();
				}
			}
		});

		filterText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (filterText.getText().equals(initialText)) {
					// XXX: We cannot call clearText() due to
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=260664
					setFilterText(""); //$NON-NLS-1$
					textChanged();
				}
			}
		});

		filterText.addModifyListener(e -> textChanged());

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		filterText.setLayoutData(gridData);
	}

	/**
	 * Creates the text control for entering the filter text. Subclasses may
	 * override.
	 *
	 * @param parent the parent composite
	 * @return the text widget
	 */
	protected abstract Text doCreateFilterText(Composite parent);

	/**
	 * Update the receiver after the text has changed.
	 */
	protected abstract void textChanged();

	/**
	 * Return the time delay that should be used when scheduling the filter refresh
	 * job. Subclasses may override.
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
	 * Returns the pattern filter used by this tree.
	 *
	 * @return The pattern filter; never {@code null}.
	 */
	public ViewerFilter getPatternFilter() {
		return patternFilter;
	}

	/**
	 * Get the structured viewer of the receiver.
	 *
	 * @return the structured viewer
	 */
	public abstract StructuredViewer getViewer();

	/**
	 * Get the filter text for the receiver, if it was created. Otherwise return
	 * {@code null}.
	 *
	 * @return the filter Text, or null if it was not created
	 */
	public Text getFilterControl() {
		return filterText;
	}

	/**
	 * Convenience method to return the text of the filter control. If the text
	 * widget is not created, then null is returned.
	 *
	 * @return String in the text, or null if the text does not exist
	 */
	protected String getFilterString() {
		return filterText != null ? filterText.getText() : null;
	}

	/**
	 * Set the text that will be shown until the first focus. A default value is
	 * provided, so this method only need be called if overriding the default
	 * initial text is desired.
	 *
	 * @param text initial text to appear in text field
	 */
	public void setInitialText(String text) {
		initialText = text;
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
