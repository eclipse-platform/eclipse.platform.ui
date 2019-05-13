/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.testing.dumps;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Starts a task which will dump stack trace information and a screenshot after
 * some time.
 *
 * Necessary to know whether and where tests are hanging if a timeout occurred
 * during tests.
 * 
 * @since 0.15
 *
 */
public class TimeoutDumpTimer extends TimerTask {

	private static final String PLUGIN_ID = "org.eclipse.e4.ui.workbench3";

	/**
	 * SECONDS_BEFORE_TIMEOUT_BUFFER is the time we allow ourselves to take stack traces delay
	 * "SECONDS_BETWEEN_DUMPS", then do it again. On current build machine, it takes about 30
	 * seconds to do all that, so 2 minutes should be sufficient time allowed for most machines.
	 * Though, should increase, say, if we increase the "time between dumps" to a minute or more.
	 */
	private static final int SECONDS_BEFORE_TIMEOUT_BUFFER = 120;

	/**
	 * SECONDS_BETWEEN_DUMPS is the time we wait from first to second dump of stack trace. In most
	 * cases, this should suffice to determine if still busy doing something, or, hung, or waiting
	 * for user input.
	 */
	private static final int SECONDS_BETWEEN_DUMPS = 5;

	private volatile boolean assumeUiThreadIsResponsive;

	private final String timeoutArg;
	private final File outputDirectory;

	private TimeoutDumpTimer(String timeoutArg, File outputDirectory) {
		this.timeoutArg = timeoutArg;
		this.outputDirectory = outputDirectory;
	}

	/**
	 * Starts a timer that dumps interesting debugging information shortly before
	 * the given timeout expires.
	 *
	 * @param timeoutArg      the value of the -timeout argument from the command
	 *                        line
	 * @param outputDirectory where screenshots end up
	 */
	public static void startTimeoutDumpTimer(String timeoutArg) {
		startTimeoutDumpTimer(timeoutArg, null);
	}

	/**
	 * Starts a timer that dumps interesting debugging information shortly before
	 * the given timeout expires.
	 *
	 * @param timeoutArg      the -timeout argument from the command line
	 * @param outputDirectory where the test results end up
	 */
	public static void startTimeoutDumpTimer(String timeoutArg, File outputDirectory) {
		try {
			/*
			 * The delay (in ms) is the sum of - the expected time it took for launching the current
			 * VM and reaching this method - the time it will take to run the garbage collection and
			 * dump all the infos (twice)
			 */
			int delay = SECONDS_BEFORE_TIMEOUT_BUFFER * 1000;

			int timeout = Integer.parseInt(timeoutArg) - delay;
			String time0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(new Date());
			logInfo("starting DumpStackTracesTimer with timeout=" + timeout + " at " + time0);
			if (timeout > 0) {
				new Timer("DumpStackTracesTimer", true).schedule(new TimeoutDumpTimer(timeoutArg, outputDirectory), timeout);
			} else {
				logWarning("DumpStackTracesTimer argument error: '-timeout " + timeoutArg
						+ "' was too short to accommodate time delay required (" + delay + ").");
			}
		} catch (NumberFormatException e) {
			logError("Error parsing timeout argument: " + timeoutArg, e);
		}
	}

	@Override
	public void run() {
		dump(0);
		try {
			Thread.sleep(SECONDS_BETWEEN_DUMPS * 1000);
		} catch (InterruptedException e) {
			// continue
		}
		dump(SECONDS_BETWEEN_DUMPS);
	}

	/**
	 *
	 * @param num num is purely a lable used in naming the screen capture files. By
	 *            convention, we pass in 0 or "SECONDS_BETWEEN_DUMPS" just as a
	 *            subtle reminder of how much time as elapsed. Thus, files end up
	 *            with names similar to <classname>_screen0.png,
	 *            <classname>_screem5.png in a directory named "timeoutScreens"
	 *            under "results", such as
	 *            .../results/linux.gtk.x86_64/timeoutScreens/
	 */
	private void dump(final int num) {
		// Time elapsed time to do each dump, so we'll
		// know if/when we get too close to the 2
		// minutes we allow
		long start = System.currentTimeMillis();

		// Dump all stacks:
		dumpStackTraces(num, System.err);
		dumpStackTraces(num, System.out); // System.err could be blocked, see
		// https://bugs.eclipse.org/506304
		logStackTraces(num); // make this available in the log, see bug 533367

		if (outputDirectory != null) {
			if (!dumpSwtDisplay(num)) {
				String screenshotFile = getScreenshotFile(num);
				dumpAwtScreenshot(screenshotFile);
			}
		}

		// Elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis() - start;

		// Print in seconds
		float elapsedTimeSec = elapsedTimeMillis / 1000F;
		logInfo("Seconds to do dump " + num + ": " + elapsedTimeSec);
	}

	private static void dumpAwtScreenshot(String screenshotFile) {
		try {
			URL location = AwtScreenshot.class.getProtectionDomain().getCodeSource().getLocation();
			String cp = location.toURI().getPath();
			String javaHome = System.getProperty("java.home");
			String javaExe = javaHome + File.separatorChar + "bin" + File.separatorChar + "java";
			if (File.separatorChar == '\\') {
				javaExe += ".exe"; // assume it's Windows
			}
			String[] args = new String[] { javaExe, "-cp", cp, AwtScreenshot.class.getName(), screenshotFile };
			logInfo("Start process: " + Arrays.asList(args));
			ProcessBuilder processBuilder = new ProcessBuilder(args);
			if ("Mac OS X".equals(System.getProperty("os.name"))) {
				processBuilder.environment().put("AWT_TOOLKIT", "CToolkit");
			}
			Process process = processBuilder.start();
			new StreamForwarder(process.getErrorStream(), System.err).start();
			new StreamForwarder(process.getInputStream(), System.err).start();
			int screenshotTimeout = 15;
			long end = System.currentTimeMillis() + screenshotTimeout * 1000;
			boolean done = false;
			do {
				try {
					process.exitValue();
					done = true;
				} catch (IllegalThreadStateException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
					}
				}
			} while (!done && System.currentTimeMillis() < end);

			if (done) {
				logInfo("AwtScreenshot VM finished with exit code " + process.exitValue() + ".");
			} else {
				process.destroy();
				logWarning("Killed AwtScreenshot VM after " + screenshotTimeout + " seconds.");
			}
		} catch (URISyntaxException | IOException e) {
			logError("Failed to create AWT screenshot", e);
		}
	}

	private void logStackTraces(int num) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		dumpStackTraces(num, new PrintStream(outputStream));
		logWarning(outputStream.toString());
	}

	private void dumpStackTraces(int num, PrintStream out) {
		out.println("DumpStackTracesTimer almost reached timeout '" + timeoutArg + "'.");
		out.println("totalMemory:            " + Runtime.getRuntime().totalMemory());
		out.println("freeMemory (before GC): " + Runtime.getRuntime().freeMemory());
		out.flush(); // https://bugs.eclipse.org/bugs/show_bug.cgi?id=420258: flush aggressively, we could be low on memory
		System.gc();
		out.println("freeMemory (after GC):  " + Runtime.getRuntime().freeMemory());
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(new Date());
		out.println("Thread dump " + num + " at " + time + ":");
		out.flush();
		Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
		for (Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
			String name = entry.getKey().getName();
			StackTraceElement[] stack = entry.getValue();
			Exception exception = new Exception("ThreadDump for thread \"" + name + "\"");
			exception.setStackTrace(stack);
			exception.printStackTrace(out);
		}
		out.flush();
	}

	private boolean dumpSwtDisplay(final int num) {
		try {
			final Display display = Display.getDefault();

			if (!assumeUiThreadIsResponsive) {
				String message = "trying to make UI thread respond";
				IllegalStateException toThrow = new IllegalStateException(message);
				Thread t = display.getThread();
				// Initialize the cause. Its stack trace will be that of the current thread.
				toThrow.initCause(new RuntimeException(message));
				// Set the stack trace to that of the target thread.
				toThrow.setStackTrace(t.getStackTrace());
				// Stop the thread using the specified throwable.
				// Thread#stop(Throwable) doesn't work any more in JDK 8 and is removed in Java
				// 11 so it's not gonna be tried at all. Try stop0:
				try {
					Method stop0 = Thread.class.getDeclaredMethod("stop0", Object.class);
					stop0.setAccessible(true);
					stop0.invoke(t, toThrow);
				} catch (Exception e1) {
					logError("Exception occurred while trying to stop UI thread", e1);
				}
			}

			assumeUiThreadIsResponsive = false;

			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					assumeUiThreadIsResponsive = true;

					dumpDisplayState(System.err);
					dumpDisplayState(System.out); // System.err could be blocked, see
					// https://bugs.eclipse.org/506304

					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					dumpDisplayState(new PrintStream(outputStream));
					logWarning(outputStream.toString());

					// Take a screenshot:
					GC gc = new GC(display);
					final Image image = new Image(display, display.getBounds());
					gc.copyArea(image, 0, 0);
					gc.dispose();

					ImageLoader loader = new ImageLoader();
					loader.data = new ImageData[] { image.getImageData() };
					String filename = getScreenshotFile(num);
					loader.save(filename, SWT.IMAGE_PNG);
					logInfo("Screenshot saved to: " + filename);
					image.dispose();
				}

				private void dumpDisplayState(PrintStream out) {
					// Dump focus control, parents, and
					// shells:
					Control focusControl = display.getFocusControl();
					if (focusControl != null) {
						out.println("FocusControl: ");
						StringBuilder indent = new StringBuilder("  ");
						do {
							out.println(indent.toString() + focusControl);
							focusControl = focusControl.getParent();
							indent.append("  ");
						} while (focusControl != null);
					}
					Shell[] shells = display.getShells();
					if (shells.length > 0) {
						out.println("Shells: ");
						for (Shell shell : shells) {
							out.println((shell.isVisible() ? "  visible: " : "  invisible: ") + shell);
						}
					}
					out.flush(); // for bug 420258
				}
			});
			return true;
		} catch (SWTException e) {
			logError("Failed to create screenshot", e);
			return false;
		}
	}

	String getScreenshotFile(final int num) {
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		String filename = outputDirectory.getAbsolutePath() + "/dump_screen" + num + ".png";
		return filename;
	}

	private static void logInfo(String message) {
		IStatus warningStatus = new Status(IStatus.INFO, PLUGIN_ID, message);
		log(warningStatus);
	}

	private static void logWarning(String message) {
		IStatus warningStatus = new Status(IStatus.WARNING, PLUGIN_ID, message);
		log(warningStatus);
	}

	private static void logError(String message, Exception exception) {
		IStatus errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
		log(errorStatus);
	}

	private static void log(IStatus warningStatus) {
		ILog log = Platform.getLog(Platform.getBundle(PLUGIN_ID));
		log.log(warningStatus);
	}

	private static class StreamForwarder extends Thread {
		private InputStream fProcessOutput;

		private PrintStream fStream;

		public StreamForwarder(InputStream processOutput, PrintStream stream) {
			fProcessOutput = processOutput;
			fStream = stream;
		}

		@Override
		public void run() {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(fProcessOutput))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					fStream.println(line);
				}
			} catch (IOException e) {
				logError("Exception while reading stream", e);
			}
		}
	}
}
