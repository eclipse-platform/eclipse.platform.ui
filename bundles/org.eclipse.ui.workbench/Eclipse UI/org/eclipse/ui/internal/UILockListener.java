/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Jeremiah Lott (jeremiah.lott@timesys.com) - fix for deadlock bug 76378
 *
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.LockListener;
import org.eclipse.swt.widgets.Display;

/**
 * The UI lock listener is used to prevent the UI thread from deadlocking on
 * a lock when the thread owning the lock is attempting to syncExec.
 */
public class UILockListener extends LockListener {

    /**
     * The Queue is the construct that keeps track of Semaphores.
     */
    public class Queue {
        private static final int BASE_SIZE = 8;

        protected PendingSyncExec[] elements = new PendingSyncExec[BASE_SIZE];

        protected int head = 0;

        protected int tail = 0;

        /**
         * Add the semaphore to the queue.
         * @param element
         */
        public synchronized void add(PendingSyncExec element) {
            int newTail = increment(tail);
            if (newTail == head) {
                grow();
                newTail = tail + 1;
            }
            elements[tail] = element;
            tail = newTail;
        }

        private void grow() {
            int newSize = elements.length * 2;
            PendingSyncExec[] newElements = new PendingSyncExec[newSize];
            if (tail >= head) {
				System.arraycopy(elements, head, newElements, head, size());
			} else {
                int newHead = newSize - (elements.length - head);
                System.arraycopy(elements, 0, newElements, 0, tail + 1);
                System.arraycopy(elements, head, newElements, newHead,
                        (newSize - newHead));
                head = newHead;
            }
            elements = newElements;
        }

        private int increment(int index) {
            return (index == (elements.length - 1)) ? 0 : index + 1;
        }

        /**
         * Remove the next semaphore to be woken up.
         * @return
         */
        public synchronized PendingSyncExec remove() {
            if (tail == head) {
				return null;
			}
            PendingSyncExec result = elements[head];
            elements[head] = null;
            head = increment(head);
            //reset the queue if it is empty and it has grown
            if (tail == head && elements.length > BASE_SIZE) {
                elements = new PendingSyncExec[BASE_SIZE];
                tail = head = 0;
            }
            return result;
        }

        private int size() {
            return tail > head ? (tail - head)
                    : ((elements.length - head) + tail);
        }
    }

    protected Display display;

    protected final Queue pendingWork = new Queue();

    protected PendingSyncExec currentWork = null;

	/**
	 * Points to the UI thread if it is currently waiting on a lock or null
	 */
	protected volatile Thread ui;

    /**
     * Create a new instance of the receiver.
     * @param display
     */
    public UILockListener(Display display) {
        this.display = display;
    }

    @Override
	public void aboutToRelease() {
        if (isUI()) {
			ui = null;
		}
    }

    @Override
	public boolean aboutToWait(Thread lockOwner) {
        if (isUI()) {
            // If a syncExec was executed from the current operation, it
            // has already acquired the lock. So, just return true.
            if (currentWork != null
                    && currentWork.getOperationThread() == lockOwner) {
				return true;
			}
            ui = Thread.currentThread();
            try {
                doPendingWork();
            } finally {
                //UI field may be nulled if there is a nested wait during execution
                //of pending work, so make sure it is assigned before we start waiting
                ui = Thread.currentThread();
            }
        }
        return false;
    }

    void addPendingWork(PendingSyncExec work) {
        pendingWork.add(work);
    }

	@Override
	public boolean canBlock() {
		return !isUI();
	}

	/**
	 * Should always be called from the UI thread.
	 */
	void doPendingWork() {
		// Clear the interrupt flag that we may have set in interruptUI()
		Thread.interrupted();
		PendingSyncExec work;
		while ((work = pendingWork.remove()) != null) {
			// Remember the old current work before replacing, to handle
			// the nested waiting case (bug 76378)
			PendingSyncExec oldWork = currentWork;
			try {
				currentWork = work;
				work.run();
			} finally {
				currentWork = oldWork;
			}
		}
	}

	void interruptUI(Runnable runnable) {
		reportInterruption(runnable);
        display.getThread().interrupt();
    }

    boolean isLockOwner() {
        return isLockOwnerThread();
    }

    boolean isUI() {
        return (!display.isDisposed())
                && (display.getThread() == Thread.currentThread());
    }

	boolean isUIWaiting() {
		Thread localUi = ui;
		return (localUi != null) && (Thread.currentThread() != localUi);
	}

	/**
	 * Adds a 'UI thread interrupted' message to the log with extra lock state
	 * and thread stack information.
	 */
	private void reportInterruption(Runnable runnable) {
		Thread nonUiThread = Thread.currentThread();

		String msg = "To avoid deadlock while executing Display.syncExec() with argument: " //$NON-NLS-1$
				+ runnable + ", thread " + nonUiThread.getName() //$NON-NLS-1$
				+ " will interrupt UI thread."; //$NON-NLS-1$
		MultiStatus main = new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, msg, null);

		ThreadInfo[] threads = ManagementFactory.getThreadMXBean().getThreadInfo(new long[] { nonUiThread.getId(), display.getThread().getId() }, true, true);

		for (ThreadInfo info : threads) {
			String childMsg;
			if (info.getThreadId() == nonUiThread.getId()) {
				// see org.eclipse.core.internal.jobs.LockManager.isLockOwner()
				childMsg = nonUiThread.getName() + " thread is an instance of Worker or owns an ILock"; //$NON-NLS-1$
			} else {
				childMsg = "UI thread waiting on a job or lock."; //$NON-NLS-1$
			}
			Exception childEx = new IllegalStateException("Call stack for thread " + info.getThreadName()); //$NON-NLS-1$
			childEx.setStackTrace(info.getStackTrace());
			Status child = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, childMsg, childEx);
			main.add(child);
		}

		WorkbenchPlugin.log(main);
	}
}
