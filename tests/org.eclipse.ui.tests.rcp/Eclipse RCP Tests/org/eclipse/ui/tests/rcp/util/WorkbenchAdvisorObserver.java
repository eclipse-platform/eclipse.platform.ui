/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;
import org.junit.Assert;

/**
 * This utility class is used to record the order in which the hooks are called.
 * It should be subclassed to provide the behaviour needed for a particular test
 * case. After the case has run, its list can be examined to check for the
 * expected order.
 * <p>
 * NOTE: This differs from <code>org.eclipse.ui.tests.CallHistory</code> since
 * this class allows assertions on individual method names. Which means that the
 * junit error will identify the exact error, not just the existence of an
 * error.
 */
public class WorkbenchAdvisorObserver extends RCPTestWorkbenchAdvisor {

	private List<String> operations = new LinkedList<>();

	private Iterator<String> iterator;

    public final static String INITIALIZE = "initialize"; //$NON-NLS-1$

    public final static String PRE_STARTUP = "preStartup"; //$NON-NLS-1$

    public final static String POST_STARTUP = "postStartup"; //$NON-NLS-1$

    public final static String PRE_WINDOW_OPEN = "preWindowOpen"; //$NON-NLS-1$

    public final static String FILL_ACTION_BARS = "fillActionBars"; //$NON-NLS-1$

    public final static String POST_WINDOW_RESTORE = "postWindowRestore"; //$NON-NLS-1$

    public final static String POST_WINDOW_OPEN = "postWindowOpen"; //$NON-NLS-1$

    public final static String PRE_WINDOW_SHELL_CLOSE = "preWindowShellClose"; //$NON-NLS-1$

    public final static String EVENT_LOOP_EXCEPTION = "eventLoopException"; //$NON-NLS-1$

    public final static String PRE_SHUTDOWN = "preShutdown"; //$NON-NLS-1$

    public final static String POST_SHUTDOWN = "postShutdown"; //$NON-NLS-1$

    public IWorkbenchConfigurer workbenchConfig;

    public WorkbenchAdvisorObserver() {
        super();
    }

    public WorkbenchAdvisorObserver(int idleBeforeExit) {
        super(idleBeforeExit);
    }

    public void resetOperationIterator() {
        iterator = operations.iterator();
    }

    public void assertNextOperation(String expected) {
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(expected, iterator.next());
    }

    public void assertAllOperationsExamined() {
        Assert.assertNotNull(iterator);
        Assert.assertFalse(iterator.hasNext());
    }

    private void addOperation(String operation) {
        operations.add(operation);
    }

    @Override
	public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        workbenchConfig = configurer;
        addOperation(INITIALIZE);
    }

    @Override
	public void preStartup() {
        super.preStartup();
        addOperation(PRE_STARTUP);
    }

    @Override
	public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
        super.preWindowOpen(configurer);
        addOperation(PRE_WINDOW_OPEN);
    }

    @Override
	public void fillActionBars(IWorkbenchWindow window,
            IActionBarConfigurer configurer, int flags) {
        super.fillActionBars(window, configurer, flags);
        addOperation(FILL_ACTION_BARS);
    }

    @Override
	public void postWindowRestore(IWorkbenchWindowConfigurer configurer)
            throws WorkbenchException {
        super.postWindowRestore(configurer);
        addOperation(POST_WINDOW_RESTORE);
    }

    @Override
	public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
        super.postWindowOpen(configurer);
        addOperation(POST_WINDOW_OPEN);
    }

    @Override
	public void postStartup() {
        super.postStartup();
        addOperation(POST_STARTUP);
    }

    @Override
	public boolean preWindowShellClose(IWorkbenchWindowConfigurer configurer) {
        if (!super.preWindowShellClose(configurer)) {
			return false;
		}
        addOperation(PRE_WINDOW_SHELL_CLOSE);
        return true;
    }

    @Override
	public boolean preShutdown() {
        boolean result = super.preShutdown();
        addOperation(PRE_SHUTDOWN);
        return result;
    }

    @Override
	public void postShutdown() {
        super.postShutdown();
        addOperation(POST_SHUTDOWN);
    }

    @Override
	public void eventLoopException(Throwable exception) {
        super.eventLoopException(exception);
        addOperation(EVENT_LOOP_EXCEPTION);
    }
}
