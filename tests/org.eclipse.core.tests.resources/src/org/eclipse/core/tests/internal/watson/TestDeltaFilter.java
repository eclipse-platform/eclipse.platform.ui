package org.eclipse.core.tests.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.watson.*;
public class TestDeltaFilter implements IDeltaFilter {
/**
 * TestDeltaFilter constructor comment.
 */
public TestDeltaFilter() {
	super();
}
/**
 * Accepts all flags
 */
public boolean includeElement(int flag) {
	return true;
}
}
