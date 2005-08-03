/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.branding.IProductConstants;

/* package */class StartupProgressMonitor implements IProgressMonitor {

	// this class prints to the output stream and calls flush()
	// on it in a separate thread so that the main thread will
	// not block if the other end of the stream (the executable
	// that displays the splash screen) misbehaves.
	private class AsynchronousPrinter extends Thread {
		private PrintStream printStream;

		// this list holds strings to print. The empty string is
		// added to the list if the output stream should be flushed.
		// If the output stream should be closed, null is added
		// to this list.
		private LinkedList tasks = new LinkedList();

		private AsynchronousPrinter(OutputStream stream) {
			this.printStream = new PrintStream(stream, false);
			setName("Startup Progress Printer"); //$NON-NLS-1$
		}

		public void run() {
			while (true) {
				Object task;
				synchronized (tasks) {
					while (tasks.isEmpty()) {
						try {
							tasks.wait();
						} catch (InterruptedException e) {
							return;
						}
					}
					task = tasks.removeFirst();
				}
				if (task == null) {
					printStream.close();
					return;
				} else if ("".equals(task)) { //$NON-NLS-1$
					printStream.flush();
				} else {
					printStream.print(task.toString() + "\n"); //$NON-NLS-1$
				}
			}
		}

		private void addTask(Object o) {
			synchronized (tasks) {
				tasks.addLast(o);
				tasks.notifyAll();
			}
		}

		void println(String string) {
			addTask(string);
		}

		void flush() {
			addTask(""); //$NON-NLS-1$
		}

		void close() {
			addTask(null);
		}
	}

	private static boolean progressMonitorReturned = false;

	/**
	 * Returns a progress monitor to report startup progress, or
	 * <code>null</code> if progress cannot be reported. This method will
	 * return <code>null</code> if called more than once.
	 * 
	 * @return a progress monitor, or null
	 */
	/* package */static IProgressMonitor getInstance() {
		if (!progressMonitorReturned) {
			OutputStream outputStream = WorkbenchPlugin.getDefault()
					.getSplashStream();
			if (outputStream != null) {
				progressMonitorReturned = true;
				return new StartupProgressMonitor(outputStream);
			}
		}
		return null;
	}

	private double sumWorked = 0;

	private int totalWork;

	private int lastReportedWork = -1;

	private AsynchronousPrinter printer;

	private StartupProgressMonitor(OutputStream os) {
		printer = new AsynchronousPrinter(os);
	}

	private void reportWork(int value) {
		if (lastReportedWork != value) {
			printer.println("value=" + value); //$NON-NLS-1$
			printer.flush();
			lastReportedWork = value;
		}
	}

	public void beginTask(String name, int total) {
		this.totalWork = total;
		printer.start();
		printInitializationData();
	}

	private void printInitializationData() {
		String progressRect = null;
		String messageRect = null;
		String foregroundColor = null;
		IProduct product = Platform.getProduct();
		if (product != null) {
			progressRect = product
					.getProperty(IProductConstants.STARTUP_PROGRESS_RECT);
			messageRect = product
					.getProperty(IProductConstants.STARTUP_MESSAGE_RECT);
			foregroundColor = product
					.getProperty(IProductConstants.STARTUP_FOREGROUND_COLOR);
		}
		if (progressRect == null)
			progressRect = "10,10,300,15"; //$NON-NLS-1$
		if (messageRect == null)
			messageRect = "10,35,300,15"; //$NON-NLS-1$
		int foregroundColorInteger;
		try {
			foregroundColorInteger = Integer.parseInt(foregroundColor, 16);
		} catch (Exception ex) {
			foregroundColorInteger = 13817855; // D2D7FF=white
		}
		printer.println("foreground=" + foregroundColorInteger); //$NON-NLS-1$
		printer.println("messageRect=" + messageRect); //$NON-NLS-1$
		printer.println("progressRect=" + progressRect); //$NON-NLS-1$
		printer.println("maximum=" + totalWork); //$NON-NLS-1$
		printer.flush();
	}

	public void done() {
		if (lastReportedWork < totalWork) {
			reportWork(totalWork);
		}
		printer.close();
	}

	public void internalWorked(double work) {
		if (work == 0) {
			return;
		}
		sumWorked += work;
		if (sumWorked > totalWork) {
			sumWorked = totalWork;
		}
		if (sumWorked < 0) {
			sumWorked = 0;
		}
		reportWork((int) sumWorked);
	}

	public boolean isCanceled() {
		return false;
	}

	public void setCanceled(boolean value) {
		// cannot cancel
	}

	public void setTaskName(String name) {
		// ignore, this does not change anything in the splash screen
	}

	public void subTask(String name) {
		printer.println("message=" + name.replace('\n', ' ')); //$NON-NLS-1$
		printer.flush();
	}

	public void worked(int work) {
		internalWorked(work);
	}
}
