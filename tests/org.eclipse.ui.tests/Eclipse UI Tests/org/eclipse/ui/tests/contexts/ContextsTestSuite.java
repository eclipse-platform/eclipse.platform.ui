package org.eclipse.ui.tests.contexts;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ContextsTestSuite extends TestSuite {

    public static Test suite() {
        return new ContextsTestSuite();
    }

    public ContextsTestSuite() {
        addTestSuite(ContextsTestCase1.class);
        addTestSuite(ContextsTestCase2.class);
    }
}