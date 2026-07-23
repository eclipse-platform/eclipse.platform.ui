/*******************************************************************************
 * Copyright (c) 2026 Eclipse contributors and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.examples.jobs.views;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

/**
 * A view that demonstrates (and lets you reproduce) the documented and
 * undocumented behaviors of
 * {@link IProgressService#run(boolean, boolean, IRunnableWithProgress)}
 * across every combination of the <code>fork</code>/<code>cancelable</code>
 * parameters, the "Always run in background" preference, the calling thread
 * and whether the runnable pumps UI events while running.
 */
@SuppressWarnings("restriction")
public class ProgressServiceView extends ViewPart {

	private static final int SLEEP_STEP_MS = 100;

	private final ExecutorService nonUiExecutor = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "ProgressServiceView-nonUI"); //$NON-NLS-1$
		t.setDaemon(true);
		return t;
	});

	private Button backgroundPrefField;
	private IPropertyChangeListener prefListener;

	private Button forkField;
	private Button cancelableField;
	private Button callFromUIThreadField;
	private Button pumpEventsField;
	private Text durationField;

	@Override
	public void createPartControl(Composite parent) {
		Composite body = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		body.setLayout(layout);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));

		createPreferenceGroup(body);
		createRunGroup(body);
	}

	private void createPreferenceGroup(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		backgroundPrefField = new Button(group, SWT.CHECK);
		backgroundPrefField.setText("Always run in background"); //$NON-NLS-1$
		backgroundPrefField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		backgroundPrefField.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.RUN_IN_BACKGROUND));
		backgroundPrefField.addSelectionListener(SelectionListener.widgetSelectedAdapter(
				_ -> getPreferenceStore().setValue(IPreferenceConstants.RUN_IN_BACKGROUND,
						backgroundPrefField.getSelection())));

		Label hint = new Label(group, SWT.WRAP);
		hint.setText("This preference can also be set under Preferences > General."); //$NON-NLS-1$
		hint.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		prefListener = event -> {
			if (IPreferenceConstants.RUN_IN_BACKGROUND.equals(event.getProperty())) {
				if (backgroundPrefField.isDisposed()) {
					return;
				}
				Display display = backgroundPrefField.getDisplay();
				if (display.isDisposed()) {
					return;
				}
				display.asyncExec(() -> {
					if (!backgroundPrefField.isDisposed()) {
						backgroundPrefField
								.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.RUN_IN_BACKGROUND));
					}
				});
			}
		};
		getPreferenceStore().addPropertyChangeListener(prefListener);
	}

	private void createRunGroup(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText("Duration (ms):"); //$NON-NLS-1$
		durationField = new Text(group, SWT.BORDER);
		durationField.setText("3000"); //$NON-NLS-1$
		durationField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		forkField = new Button(group, SWT.CHECK);
		forkField.setText("fork"); //$NON-NLS-1$
		forkField.setSelection(true);
		GridData forkData = new GridData(GridData.FILL_HORIZONTAL);
		forkData.horizontalSpan = 2;
		forkField.setLayoutData(forkData);
		forkField.addSelectionListener(SelectionListener.widgetSelectedAdapter(_ -> updateEnablement()));

		cancelableField = new Button(group, SWT.CHECK);
		cancelableField.setText("cancelable"); //$NON-NLS-1$
		cancelableField.setSelection(true);
		GridData cancelableData = new GridData(GridData.FILL_HORIZONTAL);
		cancelableData.horizontalSpan = 2;
		cancelableField.setLayoutData(cancelableData);

		callFromUIThreadField = new Button(group, SWT.CHECK);
		callFromUIThreadField.setText("call from UI thread"); //$NON-NLS-1$
		callFromUIThreadField.setToolTipText(
				"Whether IProgressService.run() is called from the UI thread or from a background thread. Any exception thrown by the call is logged to the Error Log."); //$NON-NLS-1$
		callFromUIThreadField.setSelection(true);
		GridData callFromUIThreadData = new GridData(GridData.FILL_HORIZONTAL);
		callFromUIThreadData.horizontalSpan = 2;
		callFromUIThreadField.setLayoutData(callFromUIThreadData);
		callFromUIThreadField.addSelectionListener(SelectionListener.widgetSelectedAdapter(_ -> updateEnablement()));

		pumpEventsField = new Button(group, SWT.CHECK);
		pumpEventsField.setText("pump UI events while running"); //$NON-NLS-1$
		pumpEventsField.setToolTipText(
				"Whether the runnable calls Display.readAndDispatch() while running. Only applicable when the runnable runs in the UI thread (fork=false, called from UI thread)."); //$NON-NLS-1$
		GridData pumpEventsData = new GridData(GridData.FILL_HORIZONTAL);
		pumpEventsData.horizontalSpan = 2;
		pumpEventsField.setLayoutData(pumpEventsData);

		Button run = new Button(group, SWT.PUSH);
		run.setText("Run via IProgressService.run(fork, cancelable, ...)"); //$NON-NLS-1$
		GridData runData = new GridData(GridData.FILL_HORIZONTAL);
		runData.horizontalSpan = 2;
		run.setLayoutData(runData);
		run.addSelectionListener(SelectionListener.widgetSelectedAdapter(_ -> runViaProgressService()));

		updateEnablement();
	}

	private void updateEnablement() {
		pumpEventsField.setEnabled(!forkField.getSelection() && callFromUIThreadField.getSelection());
	}

	@SuppressWarnings("deprecation")
	private IPreferenceStore getPreferenceStore() {
		return PlatformUI.getWorkbench().getPreferenceStore();
	}

	private long getDuration() {
		try {
			return Long.parseLong(durationField.getText().trim());
		} catch (NumberFormatException e) {
			Platform.getLog(ProgressServiceView.class).error(e.getMessage(), e);
			return 3000;
		}
	}

	private IRunnableWithProgress createSleepRunnable(long durationMillis, boolean pumpEvents) {
		return monitor -> {
			int ticks = (int) Math.max(1, durationMillis / SLEEP_STEP_MS);
			monitor.beginTask("Simulated long-running operation", ticks); //$NON-NLS-1$
			try {
				for (int i = 0; i < ticks; i++) {
					if (monitor.isCanceled()) {
						return;
					}
					try {
						Thread.sleep(SLEEP_STEP_MS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
					if (pumpEvents) {
						Display display = Display.getCurrent();
						if (display != null) {
							while (display.readAndDispatch()) {
								// drain pending UI events so the heartbeat keeps ticking
							}
						}
					}
					monitor.worked(1);
				}
			} finally {
				monitor.done();
			}
		};
	}

	private void runViaProgressService() {
		boolean fork = forkField.getSelection();
		boolean cancelable = cancelableField.getSelection();
		boolean pumpEvents = pumpEventsField.isEnabled() && pumpEventsField.getSelection();
		IRunnableWithProgress runnable = createSleepRunnable(getDuration(), pumpEvents);
		if (callFromUIThreadField.getSelection()) {
			try {
				PlatformUI.getWorkbench().getProgressService().run(fork, cancelable, runnable);
			} catch (InvocationTargetException | InterruptedException e) {
				Platform.getLog(ProgressServiceView.class).error(e.getMessage(), e);
			}
		} else {
			nonUiExecutor.execute(() -> {
				try {
					PlatformUI.getWorkbench().getProgressService().run(fork, cancelable, runnable);
				} catch (Throwable t) {
					Platform.getLog(ProgressServiceView.class).error(t.getMessage(), t);
				}
			});
		}
	}

	@Override
	public void dispose() {
		if (prefListener != null) {
			getPreferenceStore().removePropertyChangeListener(prefListener);
		}
		nonUiExecutor.shutdownNow();
		super.dispose();
	}

	@Override
	public void setFocus() {
		// do nothing
	}

}
