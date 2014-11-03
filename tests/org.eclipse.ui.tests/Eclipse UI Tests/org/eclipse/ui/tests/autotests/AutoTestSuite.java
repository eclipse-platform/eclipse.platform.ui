/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.autotests;

import java.net.URL;

import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.tests.TestPlugin;

/**
 * @since 3.1
 */
public class AutoTestSuite extends TestSuite {
    private AutoTestLogger logger;

    public AutoTestSuite(URL expectedResults) {
        if (expectedResults == null) {
            logger = new AutoTestLogger();
        } else {
            try {
                logger = new AutoTestLogger(expectedResults);
            } catch (WorkbenchException e) {
                logger = new AutoTestLogger();
                e.printStackTrace();
            }
        }
    }

    protected AutoTestLogger getLog() {
        return logger;
    }

    public void addWrapper(AutoTest test) {
        addTest(new AutoTestWrapper(test, logger));
    }

    @Override
	public void run(TestResult result) {
        super.run(result);

        IPath statePath = Platform.getStateLocation(TestPlugin.getDefault().getBundle());

        String testName = this.getName();
        if (testName == null) {
            testName = this.getClass().getName();
        }

        if (!logger.getErrors().isEmpty()) {
            IPath errorsPath = statePath.append(testName).append("errors.xml");

            System.out.println("Errors detected. Results written to " + errorsPath.toString());

            XMLMemento output = XMLMemento.createWriteRoot("errors");
            logger.getErrors().saveState(output);
            try {
                XmlUtil.write(errorsPath.toFile(), output);
            } catch (WorkbenchException e) {
                e.printStackTrace();
            }
        }

        if (!logger.getUnknownTests().isEmpty()) {

            IPath unknownPath = statePath.append(testName).append("newtests.xml");

            System.out.println("New tests detected. Results written to " + unknownPath.toString());

            XMLMemento output = XMLMemento.createWriteRoot("unknown");
            logger.getUnknownTests().saveState(output);
            try {
                XmlUtil.write(unknownPath.toFile(), output);
            } catch (WorkbenchException e) {
                e.printStackTrace();
            }
        }
    }
}
