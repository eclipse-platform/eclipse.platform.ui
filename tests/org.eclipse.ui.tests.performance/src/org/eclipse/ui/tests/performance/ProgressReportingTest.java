/*******************************************************************************
 * Copyright (c) 2016, 2019 Stefan Xenos and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Xenos - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.harness.util.PreferenceMementoRule;
import org.junit.Rule;

/**
 * Verifies the performance of progress reporting APIs in various contexts which
 * offer progress monitoring.
 */
public class ProgressReportingTest extends BasicPerformanceTest {

	/**
	 * Number of iterations to run for the inner loop in these tests. This
	 * should be chosen such that all the well-behaved tests produce a result of
	 * around 500ms to 1s on average. Making it too small reduces the accuracy
	 * of the measurements since the test framework can't measure times smaller
	 * than 1ms. Making it too big reduces the number of times we can rerun the
	 * tests in the 4s limit, preventing us from computing the standard
	 * deviation.
	 */
	public static final int ITERATIONS = 10000000;

	/**
	 * Number of iterations for the run-in-foreground tests, since some of them
	 * are known to be extremely slow. Please delete this constant and replace
	 * with "ITERATIONS" once we've fixed the performance problems in these
	 * seriously-bad use-cases.
	 */
	public static final int VERY_SLOW_OPERATION_ITERATIONS = 100000;

	/**
	 * Maximum time to run each test. Increase to get better results during
	 * profiling.
	 */
	public static final int MAX_RUNTIME = 4000;

	/**
	 * Maximum number of iterations for each test. Increase to get better
	 * results during profiling.
	 */
	public static final int MAX_ITERATIONS = 100;

	@Rule
	public final PreferenceMementoRule preferenceMemento = new PreferenceMementoRule();

	private volatile boolean isDone;
	private Display display;

	/**
	 * Create a new instance of the receiver.
	 */
	public ProgressReportingTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		this.display = Display.getCurrent();
		super.doSetUp();
	}

	private void setRunInBackground(boolean newRunInBackgroundSetting) {
		preferenceMemento.setPreference(WorkbenchPlugin.getDefault().getPreferenceStore(),
				IPreferenceConstants.RUN_IN_BACKGROUND,
				newRunInBackgroundSetting);
	}

	/**
	 * Starts an asynchronous performance test. The test ends whenever the
	 * runnable invokes endAsyncTest
	 */
	public void runAsyncTest(Runnable testContent) throws Exception {
		final Display display = Display.getCurrent();
		tagIfNecessary(getName(), Dimension.ELAPSED_PROCESS);
		exercise(() -> {
			startMeasuring();

			isDone = false;
			testContent.run();

			for (; !isDone;) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			stopMeasuring();
		}, 1, MAX_ITERATIONS, MAX_RUNTIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Ends an asynchronous test
	 */
	public void endAsyncTest() {
		isDone = true;
		// Trigger an empty asyncExec to ensure the event loop wakes up
		display.asyncExec(() -> {
		});
	}

	/**
	 * Test the overhead of the test framework itself
	 */
	public void testJobNoMonitorUsage() throws Exception {
		openTestWindow();
		setRunInBackground(true);
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				int i = 0;
				while (i < ITERATIONS) {
					i++;
				}

				endAsyncTest();
			}).schedule();
		});
	}

	/**
	 * Test the cost of setTaskName
	 */
	public void testJobSetTaskName() throws Exception {
		openTestWindow();
		setRunInBackground(true);
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				while(i < ITERATIONS) {
					monitor.setTaskName(Integer.toString(i));
					i++;
				}

				endAsyncTest();
			}).schedule();
		});
	}

	/**
	 * Test the cost of subTask
	 */
	public void testJobSubTask() throws Exception {
		openTestWindow();
		setRunInBackground(true);
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				while (i < ITERATIONS) {
					monitor.subTask(Integer.toString(i));
					i++;
				}

				endAsyncTest();
			}).schedule();
		});
	}

	/**
	 * Test the cost of isCanceled
	 */
	public void testJobIsCanceled() throws Exception {
		openTestWindow();
		setRunInBackground(true);
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				while (i < ITERATIONS) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					i++;
				}

				endAsyncTest();
			}).schedule();
		});
	}

	/**
	 * Test the cost of monitor.worked in jobs
	 */
	public void testJobWorked() throws Exception {
		openTestWindow();
		setRunInBackground(true);
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				while (i < ITERATIONS) {
					monitor.worked(1);
					i++;
				}

				endAsyncTest();
			}).schedule();
		});
	}

	/**
	 * Test the cost of subMonitor.split(). Note that if
	 * {@link SubMonitor#split} is performing cancellation checks at the correct
	 * rate, this test should be no more than 15% slower than
	 * {@link #testJobSubMonitorNewChild}.
	 */
	public void testJobSubMonitorSplit() throws Exception {
		openTestWindow();
		setRunInBackground(true);
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				SubMonitor subMonitor = SubMonitor.convert(monitor, ITERATIONS);
				int i = 0;
				while (i < ITERATIONS) {
					subMonitor.split(1);
					i++;
				}

				endAsyncTest();
			}).schedule();
		});
	}

	/**
	 * Test the cost of subMonitor.newChild()
	 */
	public void testJobSubMonitorNewChild() throws Exception {
		openTestWindow();
		setRunInBackground(true);
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				SubMonitor subMonitor = SubMonitor.convert(monitor, ITERATIONS);
				int i = 0;
				while (i < ITERATIONS) {
					subMonitor.newChild(1);
					i++;
				}

				endAsyncTest();
			}).schedule();
		});
	}

	/**
	 * Test the cost of subMonitor.worked()
	 */
	public void testJobSubMonitorWorked() throws Exception {
		openTestWindow();
		setRunInBackground(true);
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				SubMonitor subMonitor = SubMonitor.convert(monitor, ITERATIONS);
				int i = 0;
				while (i < ITERATIONS) {
					subMonitor.worked(1);
					i++;
				}

				endAsyncTest();
			}).schedule();
		});
	}

	/**
	 * Test the cost of monitor.subTask in the progress service
	 */
	public void testRunInForegroundNoMonitorUsage() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		setRunInBackground(false);
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", VERY_SLOW_OPERATION_ITERATIONS);
				int i = 0;
				while (i < VERY_SLOW_OPERATION_ITERATIONS) {
					i++;
				}

				endAsyncTest();
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of monitor.worked in the progress service
	 */
	public void testRunInForegroundWorked() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		setRunInBackground(false);
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", VERY_SLOW_OPERATION_ITERATIONS);
				int i = 0;
				while (i < VERY_SLOW_OPERATION_ITERATIONS) {
					monitor.worked(1);
					i++;
				}

				endAsyncTest();
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of monitor.setTaskName in the progress service
	 */
	public void testRunInForegroundSetTaskName() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		setRunInBackground(false);
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", VERY_SLOW_OPERATION_ITERATIONS);
				int i = 0;
				while (i < VERY_SLOW_OPERATION_ITERATIONS) {
					monitor.setTaskName(Integer.toString(i));
					i++;
				}

				endAsyncTest();
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of monitor.subTask in the progress service
	 */
	public void testRunInForegroundSubTask() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		setRunInBackground(false);
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", VERY_SLOW_OPERATION_ITERATIONS);
				int i = 0;
				while (i < VERY_SLOW_OPERATION_ITERATIONS) {
					monitor.subTask(Integer.toString(i));
					i++;
				}

				endAsyncTest();
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of monitor.subTask in the progress service
	 */
	public void testRunInForegroundIsCanceled() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		setRunInBackground(false);
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", VERY_SLOW_OPERATION_ITERATIONS);
				int i = 0;
				while (i < VERY_SLOW_OPERATION_ITERATIONS) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					i++;
				}

				endAsyncTest();
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of opening a progress monitor dialog without reporting any
	 * progress
	 */
	public void testProgressMonitorDialogNoMonitorUsage() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
					monitor.beginTask("Test Job", ITERATIONS);
					int i = 0;
					while (i < ITERATIONS) {
						i++;
					}

					endAsyncTest();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Test the cost of calling worked() in a progress monitor dialog
	 */
	public void testProgressMonitorDialogWorked() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
					monitor.beginTask("Test Job", ITERATIONS);
					int i = 0;
					while (i < ITERATIONS) {
						monitor.worked(1);
						i++;
					}

					endAsyncTest();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Test the cost of calling worked() in a progress monitor dialog
	 */
	public void testProgressMonitorDialogIsCanceled() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
					monitor.beginTask("Test Job", ITERATIONS);
					int i = 0;
					while (i < ITERATIONS) {
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						i++;
					}

					endAsyncTest();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Test the cost of calling setTaskName in a progress monitor dialog.
	 */
	public void testProgressMonitorDialogSetTaskName() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
					monitor.beginTask("Test Job", ITERATIONS);
					int i = 0;
					while (i < ITERATIONS) {
						monitor.setTaskName(Integer.toString(i));
						i++;
					}

					endAsyncTest();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Test the cost of calling subTask in a progress monitor dialog.
	 */
	public void testProgressMonitorDialogSubTask() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
					monitor.beginTask("Test Job", ITERATIONS);
					int i = 0;
					while (i < ITERATIONS) {
						monitor.subTask(Integer.toString(i));
						i++;
					}

					endAsyncTest();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
