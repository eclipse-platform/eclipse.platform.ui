/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2005-2006. All Rights Reserved. 
 * 
 * Note to U.S. Government Users Restricted Rights:  Use, 
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class SWTUtil {
    /**
     * Stores a work queue for each display
     */
    private static Map mapDisplayOntoWorkQueue = new HashMap();

    private SWTUtil() {
    }

    /**
     * Runs the given runnable on the given display as soon as possible. If
     * possible, the runnable will be executed before the next widget is
     * repainted, but this behavior is not guaranteed. Use this method to
     * schedule work will affect the way one or more widgets are drawn.
     * 
     * <p>
     * This is threadsafe.
     * </p>
     * 
     * @param d
     *            display
     * @param r
     *            runnable to execute in the UI thread.
     */
    public static void greedyExec(Display d, Runnable r) {
        if (d.isDisposed()) {
            return;
        }

        // if (Display.getCurrent() == d) {
        // r.run();
        // } else {
        WorkQueue queue = getQueueFor(d);
        queue.asyncExec(r);
        // }
    }

    /**
     * Runs the given runnable on the given display as soon as possible. Unlike
     * greedyExec, this has no effect if the given runnable has already been
     * scheduled for execution. Use this method to schedule work that will
     * affect the way one or more wigdets are drawn, but that should only happen
     * once.
     * 
     * <p>
     * This is threadsafe.
     * </p>
     * 
     * @param d
     *            display
     * @param r
     *            runnable to execute in the UI thread. Has no effect if the
     *            given runnable has already been scheduled but has not yet run.
     */
    public static void runOnce(Display d, Runnable r) {
        if (d.isDisposed()) {
            return;
        }
        WorkQueue queue = getQueueFor(d);
        queue.runOnce(r);
    }

    /**
     * Cancels a greedyExec or runOnce that was previously scheduled on the
     * given display. Has no effect if the given runnable is not in the queue
     * for the given display
     * 
     * @param d
     *            target display
     * @param r
     *            runnable to execute
     */
    public static void cancelExec(Display d, Runnable r) {
        if (d.isDisposed()) {
            return;
        }
        WorkQueue queue = getQueueFor(d);
        queue.cancelExec(r);
    }

    /**
     * Returns the work queue for the given display. Creates a work queue if
     * none exists yet.
     * 
     * @param d
     *            display to return queue for
     * @return a work queue (never null)
     */
    private static WorkQueue getQueueFor(final Display d) {
        WorkQueue result;
        synchronized (mapDisplayOntoWorkQueue) {
            // Look for existing queue
            result = (WorkQueue) mapDisplayOntoWorkQueue.get(d);

            if (result == null) {
                // If none, create new queue
                result = new WorkQueue(d);
                final WorkQueue q = result;
                mapDisplayOntoWorkQueue.put(d, result);
                d.asyncExec(new Runnable() {
                    public void run() {
                        d.disposeExec(new Runnable() {
                            public void run() {
                                synchronized (mapDisplayOntoWorkQueue) {
                                    q.cancelAll();
                                    mapDisplayOntoWorkQueue.remove(d);
                                }
                            }
                        });
                    }
                });
            }
            return result;
        }
    }
    
    public static RGB mix(RGB rgb1, RGB rgb2, double ratio) {
        return new RGB(interp(rgb1.red, rgb2.red, ratio), 
                interp(rgb1.green, rgb2.green, ratio),
                interp(rgb1.blue, rgb2.blue, ratio));
    }
    
    private static int interp(int i1, int i2, double ratio) {
        int result = (int)(i1 * ratio + i2 * (1.0d - ratio));
        if (result < 0) result = 0;
        if (result > 255) result = 255;
        return result;
    }

	/**
	 * Logs an exception as though it was thrown by a SafeRunnable
	 * being run with the default ISafeRunnableRunner. Will not
	 * open modal dialogs or spin the event loop.
	 * 
	 * @param t throwable to log
	 */
	public static void logException(final Exception t) {
		SafeRunnable.run(new SafeRunnable() {
			public void run() throws Exception {
				throw t;
			};
			public void handleException(Throwable e) {
				// IMPORTANT: Do not call the super implementation, since
				// it opens a modal dialog, and may cause *syncExecs to run
				// too early.
			};
		});
	}
}
