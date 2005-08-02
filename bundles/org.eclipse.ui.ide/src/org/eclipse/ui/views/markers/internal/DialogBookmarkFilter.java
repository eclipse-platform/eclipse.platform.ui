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
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DialogBookmarkFilter extends
        org.eclipse.ui.views.markers.internal.DialogMarkerFilter {

    private DescriptionGroup descriptionGroup;

    private class DescriptionGroup {
        private Label descriptionLabel;

        private Combo combo;

        private Text description;

        private String contains = Messages.getString("filtersDialog.contains"); //$NON-NLS-1$

        private String doesNotContain = Messages
                .getString("filtersDialog.doesNotContain"); //$NON-NLS-1$

        /**
         * Create a description group.
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
            // Prevent Esc and Return from closing the dialog when the combo is active.
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

        public void updateEnablement() {
            descriptionLabel.setEnabled(isFilterEnabled());
            combo.setEnabled(isFilterEnabled());
            description.setEnabled(isFilterEnabled());
        }
    }

    /**
     * @param parentShell
     * @param filter
     */
    public DialogBookmarkFilter(Shell parentShell, BookmarkFilter filter) {
        super(parentShell, filter);
    }

    /* (non-Javadoc)
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
     * @see org.eclipse.ui.views.markerview.FiltersDialog#updateFilterFromUI(org.eclipse.ui.views.markerview.MarkerFilter)
     */
    protected void updateFilterFromUI() {
        BookmarkFilter filter = (BookmarkFilter) getFilter();

        filter.setContains(descriptionGroup.getContains());
        filter.setDescription(descriptionGroup.getDescription().trim());

        super.updateFilterFromUI();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.markerview.FiltersDialog#updateUIFromFilter(org.eclipse.ui.views.markerview.MarkerFilter)
     */
    protected void updateUIFromFilter() {
        BookmarkFilter filter = (BookmarkFilter) getFilter();

        descriptionGroup.setContains(filter.getContains());
        descriptionGroup.setDescription(filter.getDescription());

        super.updateUIFromFilter();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.markerview.FiltersDialog#updateEnabledState()
     */
    protected void updateEnabledState() {
        super.updateEnabledState();
        descriptionGroup.updateEnablement();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.markerview.FiltersDialog#resetPressed()
     */
    protected void resetPressed() {
        descriptionGroup.setContains(BookmarkFilter.DEFAULT_CONTAINS);
        descriptionGroup.setDescription(BookmarkFilter.DEFAULT_DESCRIPTION);

        super.resetPressed();
    }

}
