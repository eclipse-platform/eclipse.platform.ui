/*******************************************************************************
 * Copyright (c) 2006 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.dialogs;

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class WizardDialogTest extends TestCase {
    /**
     * Asserts that the only FocusOut that is fired when saving the state of the page (thus
     * disabling) is for the control that is the focused control.
     * 
     * @throws Exception
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=155164
     */
    public void testFocusOutEventsWhenSavingState() throws Exception {
        AWizard wizard = new AWizard();
        final APage page = new APage("1"); //$NON-NLS-1$
        wizard.addPage(page);

        WizardDialog dialog = new WizardDialog(new Shell(), wizard);
        dialog.setBlockOnOpen(false);
        dialog.open();

        final EventCounter counter1 = new EventCounter();
        page.button1.addListener(SWT.FocusOut, counter1);

        final EventCounter counter2 = new EventCounter();
        page.button2.addListener(SWT.FocusOut, counter2);

        assertTrue(page.button1.isFocusControl());
        assertEquals(0, counter1.count);
        assertEquals(0, counter2.count);

        dialog.run(true, false, new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException,
                InterruptedException {
                // do stuff...
                Thread.sleep(500);

                page.button1.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        assertFalse(page.button1.isEnabled());
                        assertFalse(page.button2.isEnabled());
                    }
                });
            }
        });

        assertEquals(1, counter1.count);
        assertEquals(0, counter2.count);

        assertTrue(page.button1.isEnabled());
        assertTrue(page.button2.isEnabled());
        assertTrue(page.button1.isFocusControl());
    }

    private static class EventCounter implements Listener {
        private int count;

        public void handleEvent(Event event) {
            count++;
        }
    }

    private static class AWizard extends Wizard {
        public boolean performFinish() {
            return false;
        }
    }

    private static class APage extends WizardPage {
        Button button1;
        Button button2;

        protected APage(String pageName) {
            super(pageName);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout());
            button1 = new Button(composite, SWT.PUSH);
            button2 = new Button(composite, SWT.PUSH);

            setControl(composite);
        }
    }
}
