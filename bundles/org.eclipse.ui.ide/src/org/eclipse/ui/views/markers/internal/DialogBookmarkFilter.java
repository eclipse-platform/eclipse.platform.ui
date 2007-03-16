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
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * DialogBookmarkFilter is the filter dialog for bookmarks
 * 
 */
public class DialogBookmarkFilter extends DialogMarkerFilter {

	private DescriptionGroup descriptionGroup;

	private class DescriptionGroup {
		private Label descriptionLabel;

		private Combo combo;

		private Text description;

		private String contains = MarkerMessages.filtersDialog_contains;

		private String doesNotContain = 
			MarkerMessages.filtersDialog_doesNotContain;

		/**
		 * Create a description group.
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
					DialogBookmarkFilter.this.markDirty();
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
		 * Update the enablement based on enabled.
		 * @param enabled
		 */
		public void updateEnablement(boolean enabled) {
			descriptionLabel.setEnabled(enabled);
			combo.setEnabled(enabled);
			description.setEnabled(enabled);
		}
	}

	/**
	 * @param parentShell
	 * @param filters
	 */
	public DialogBookmarkFilter(Shell parentShell, BookmarkFilter[] filters) {
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateFilterFromUI(org.eclipse.ui.views.markers.internal.MarkerFilter)
	 */
	protected void updateFilterFromUI(MarkerFilter filter) {
		super.updateFilterFromUI(filter);

		BookmarkFilter bookmark = (BookmarkFilter) filter;
		bookmark.setContains(descriptionGroup.getContains());
		bookmark.setDescription(descriptionGroup.getDescription().trim());
	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateUIWithFilter(org.eclipse.ui.views.markers.internal.MarkerFilter)
	 */
	protected void updateUIWithFilter(MarkerFilter filter) {
		super.updateUIWithFilter(filter);
		BookmarkFilter bookmark = (BookmarkFilter) filter;
		descriptionGroup.setContains(bookmark.getContains());
		descriptionGroup.setDescription(bookmark.getDescription());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateEnabledState(boolean)
	 */
	protected void updateEnabledState(boolean enabled) {
		super.updateEnabledState(enabled);
		descriptionGroup.updateEnablement(enabled);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.FiltersDialog#resetPressed()
	 */
	protected void resetPressed() {
		descriptionGroup.setContains(BookmarkFilter.DEFAULT_CONTAINS);
		descriptionGroup.setDescription(BookmarkFilter.DEFAULT_DESCRIPTION);

		super.resetPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#newFilter(java.lang.String)
	 */
	protected MarkerFilter newFilter(String newName) {

		return new BookmarkFilter(newName);
	}

}
