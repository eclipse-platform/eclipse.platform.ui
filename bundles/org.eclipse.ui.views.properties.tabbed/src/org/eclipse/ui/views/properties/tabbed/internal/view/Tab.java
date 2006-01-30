/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed.internal.view;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;


/**
 * A property tab is composed by one or more property sections and is used to
 * categorize sections.
 * 
 * @author Anthony Hunter
 */
public class Tab {

	private ISection[] sections;

	private boolean controlsCreated;

	Tab() {
		controlsCreated = false;
	}

	public int getSectionIndex(ISection section) {
		for (int i = 0; i < sections.length; i++)
			if (section == sections[i])
				return i;
		return -1;
	}

	public ISection getSectionAtIndex(int i) {
		if (i >= 0 && i < sections.length)
			return sections[i];
		return null;
	}

	public ISection[] getSections() {
		return sections;
	}

	/**
	 * Creates page's sections controls.
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

		for (int i = 0; i < sections.length; i++) {
			final ISection section = sections[i];
			final Composite sectionComposite = page.getWidgetFactory()
				.createComposite(pageComposite, SWT.NO_FOCUS);
			sectionComposite.setLayout(new FillLayout());
			int style = (section.shouldUseExtraSpace()) ? GridData.FILL_BOTH
				: GridData.FILL_HORIZONTAL;
			GridData data = new GridData(style);
			data.heightHint = section.getMinimumHeight();
			sectionComposite.setLayoutData(data);

			ISafeRunnable runnable = new ISafeRunnable() {

				public void run()
					throws Exception {
					section.createControls(sectionComposite, page);
				}

				public void handleException(Throwable exception) {
					/* not used */
				}
			};
			Platform.run(runnable);
		}
		controlsCreated = true;
	}

	/**
	 * Dispose of page's sections controls.
	 */
	public void dispose() {
		for (int i = 0; i < sections.length; i++) {
			final ISection section = sections[i];
			ISafeRunnable runnable = new ISafeRunnable() {

				public void run()
					throws Exception {
					section.dispose();
				}

				public void handleException(Throwable exception) {
					/* not used */
				}
			};
			Platform.run(runnable);
		}
	}

	/**
	 * Sends the lifecycle event to the page's sections.
	 */
	public void aboutToBeShown() {
		for (int i = 0; i < sections.length; i++) {
			final ISection section = sections[i];
			ISafeRunnable runnable = new ISafeRunnable() {

				public void run()
					throws Exception {
					section.aboutToBeShown();
				}

				public void handleException(Throwable exception) {
					/* not used */
				}
			};
			Platform.run(runnable);
		}
	}

	/**
	 * Sends the lifecycle event to the page's sections.
	 */
	public void aboutToBeHidden() {
		for (int i = 0; i < sections.length; i++) {
			final ISection section = sections[i];
			ISafeRunnable runnable = new ISafeRunnable() {

				public void run()
					throws Exception {
					section.aboutToBeHidden();
				}

				public void handleException(Throwable exception) {
					/* not used */
				}
			};
			Platform.run(runnable);
		}
	}

	/**
	 * Sets page's sections input objects.
	 */
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		for (int i = 0; i < sections.length; i++) {
			final ISection section = sections[i];
			ISafeRunnable runnable = new ISafeRunnable() {

				public void run()
					throws Exception {
					section.setInput(part, selection);
				}

				public void handleException(Throwable throwable) {
					throwable.printStackTrace();
				}
			};
			Platform.run(runnable);
		}
	}

	void setSections(ISection[] sections) {
		this.sections = sections;
	}

	public boolean controlsHaveBeenCreated() {
		return controlsCreated;
	}

	/**
	 * If controls have been created, refresh all sections on the page.
	 */
	public void refresh() {
		if (controlsCreated) {
			for (int i = 0; i < sections.length; i++) {
				final ISection section = sections[i];
				ISafeRunnable runnable = new ISafeRunnable() {

					public void run()
						throws Exception {
						section.refresh();
					}

					public void handleException(Throwable throwable) {
						throwable.printStackTrace();
					}
				};
				Platform.run(runnable);
			}
		}
	}
}
