/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.tests.viewers.TestComparator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @since 3.1
 */
public class ConcurrentTableTestView extends ViewPart {

	private TableViewer table;
	private boolean enableSlowComparisons = false;
	private final TestComparator comparator = new TestComparator() {

		@Override
		public int compare(Object arg0, Object arg1) {
			if (enableSlowComparisons) {
				int delay = 2; // Time to spin the CPU for (milliseconds)

				// Do some work to occupy time
				long timestamp = System.currentTimeMillis();
				while (System.currentTimeMillis() < timestamp + delay) {
				}
			}

			int result = super.compare(arg0, arg1);

			scheduleComparisonUpdate();

			return result;
		}
	};
	private DeferredContentProvider contentProvider;

	private final WorkbenchJob updateCountRunnable = new WorkbenchJob("") {

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			updateCount.setText("Comparison count = " + comparator.comparisons);
			return Status.OK_STATUS;
		}
	};

	private Label updateCount;
	private final SetModel model = new SetModel();
	private final Random rand = new Random();
	private Button slowComparisons;

	@Override
	public void createPartControl(Composite temp) {
		Composite parent = new Composite(temp, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		parent.setLayout(layout);

		// Create the table
		table = new TableViewer(parent, SWT.VIRTUAL);
		contentProvider = new DeferredContentProvider(comparator);
		table.setContentProvider(contentProvider);

		GridData data = new GridData(GridData.FILL_BOTH);
		table.getControl().setLayoutData(data);
		table.setInput(model);

		// Create the buttons
		Composite buttonBar = new Composite(parent, SWT.NONE);
		buttonBar.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout buttonBarLayout = new GridLayout();
		buttonBarLayout.numColumns = 1;
		buttonBar.setLayout(buttonBarLayout);
		updateCount = new Label(buttonBar, SWT.NONE);
		updateCount.setLayoutData(new GridData(GridData.FILL_BOTH));

		slowComparisons = new Button(buttonBar, SWT.CHECK);
		slowComparisons.setLayoutData(new GridData(GridData.FILL_BOTH));
		slowComparisons.setText("Slow comparisons");
		slowComparisons.setSelection(enableSlowComparisons);
		slowComparisons.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableSlowComparisons = slowComparisons.getSelection();
				super.widgetSelected(e);
			}
		});

		final Button limitSize = new Button(buttonBar, SWT.CHECK);
		limitSize.setLayoutData(new GridData(GridData.FILL_BOTH));
		limitSize.setText("Limit table size to 400");
		limitSize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (limitSize.getSelection()) {
					contentProvider.setLimit(400);
				} else {
					contentProvider.setLimit(-1);
				}
				super.widgetSelected(e);
			}
		});

		Button resetCountButton = new Button(buttonBar, SWT.PUSH);
		resetCountButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		resetCountButton.setText("Reset comparison count");
		resetCountButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.comparisons = 0;
				scheduleComparisonUpdate();
			}
		});

		Button testButton = new Button(buttonBar, SWT.PUSH);
		testButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		testButton.setText("add 100000 elements");
		testButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addRandomElements(100000);
			}
		});

		Button removeButton = new Button(buttonBar, SWT.PUSH);
		removeButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		removeButton.setText("remove all");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clear();
			}
		});
	}

	/**
	 *
	 * @since 3.1
	 */
	protected void scheduleComparisonUpdate() {
		updateCountRunnable.schedule(100);
	}

	public void addRandomElements(int amount) {

		ArrayList<String> tempList = new ArrayList<>();

		for (int counter = 0; counter < amount; counter++) {
			tempList.add("" + rand.nextLong() + " " + counter);
		}

		model.addAll(tempList);
	}

	public void clear() {
		model.clear();
	}

	@Override
	public void setFocus() {

	}
}
