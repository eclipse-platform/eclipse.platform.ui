/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A property tab is composed by one or more property sections and is used to
 * categorize sections.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public final class TabContents {

    private ISection[] sections;

    private boolean controlsCreated;

    /**
     *
     */
    public TabContents() {
        controlsCreated = false;
    }

    /**
     * Retrieve a numbered index for the section.
     * @param section the section.
     * @return the section index.
     */
    public int getSectionIndex(ISection section) {
        for (int i = 0; i < sections.length; i++) {
			if (section == sections[i]) {
				return i;
			}
		}
        return -1;
    }

    /**
     * Retrieve the section at a numbered index.
     * @param i a numbered index.
     * @return the section.
     */
    public ISection getSectionAtIndex(int i) {
        if (i >= 0 && i < sections.length) {
			return sections[i];
		}
        return null;
    }

    /**
     * Retrieve the sections on the tab.
     *
     * @return the sections on the tab.
     */
    public ISection[] getSections() {
        return sections;
    }

    /**
     * Creates page's sections controls.
     *
     * @param parent
     * @param page
     */
    public void createControls(Composite parent,
            final TabbedPropertySheetPage page) {
        Composite pageComposite = page.getWidgetFactory().createComposite(
            parent, SWT.NO_FOCUS);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        pageComposite.setLayout(layout);

        for (final ISection section : sections) {
            final Composite sectionComposite = page.getWidgetFactory()
                .createComposite(pageComposite, SWT.NO_FOCUS);
            sectionComposite.setLayout(new FillLayout());
            int style = (section.shouldUseExtraSpace()) ? GridData.FILL_BOTH
                : GridData.FILL_HORIZONTAL;
            GridData data = new GridData(style);
            data.heightHint = section.getMinimumHeight();
            sectionComposite.setLayoutData(data);

            ISafeRunnable runnable = new SafeRunnable() {

                @Override
				public void run()
                    throws Exception {
                    section.createControls(sectionComposite, page);
                }
            };
            SafeRunnable.run(runnable);
        }
        controlsCreated = true;
    }

    /**
     * Dispose of page's sections controls.
     */
    public void dispose() {
        for (final ISection section : sections) {
            ISafeRunnable runnable = new SafeRunnable() {

                @Override
				public void run()
                    throws Exception {
                    section.dispose();
                }
            };
            SafeRunnable.run(runnable);
        }
    }

    /**
     * Sends the lifecycle event to the page's sections.
     */
    public void aboutToBeShown() {
        for (final ISection section : sections) {
            ISafeRunnable runnable = new SafeRunnable() {

                @Override
				public void run()
                    throws Exception {
                    section.aboutToBeShown();
                }
            };
            SafeRunnable.run(runnable);
        }
    }

    /**
     * Sends the lifecycle event to the page's sections.
     */
    public void aboutToBeHidden() {
        for (final ISection section : sections) {
            ISafeRunnable runnable = new SafeRunnable() {

                @Override
				public void run()
                    throws Exception {
                    section.aboutToBeHidden();
                }
            };
            SafeRunnable.run(runnable);
        }
    }

    /**
     * Sets page's sections input objects.
     *
     * @param part
     * @param selection
     */
    public void setInput(final IWorkbenchPart part, final ISelection selection) {
        for (final ISection section : sections) {
            ISafeRunnable runnable = new SafeRunnable() {

                @Override
				public void run()
                    throws Exception {
                    section.setInput(part, selection);
                }
            };
            SafeRunnable.run(runnable);
        }
    }

    /**
     * Set the sections for the tab.
     *
     * @param sections the sections for the tab.
     */
    public void setSections(ISection[] sections) {
        this.sections = sections;
    }

    /**
     * Determine if the controls have been created.
     *
     * @return <code>true</code> if controls have been created.
     */
    public boolean controlsHaveBeenCreated() {
        return controlsCreated;
    }

    /**
     * If controls have been created, refresh all sections on the page.
     */
    public void refresh() {
        if (controlsCreated) {
            for (final ISection section : sections) {
                ISafeRunnable runnable = new SafeRunnable() {

                    @Override
					public void run()
                        throws Exception {
                        section.refresh();
                    }
                };
                SafeRunnable.run(runnable);
            }
        }
    }
}
