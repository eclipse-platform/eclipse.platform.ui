/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.tests;

import org.eclipse.compare.internal.CompareFilter;

import junit.framework.*;
import junit.framework.TestCase;

public class FilterTest extends TestCase {
	
	CompareFilter fFilter;
	
	public FilterTest(String name) {
		super(name);
	}
		
	public void testFilterFile() {
		CompareFilter f= new CompareFilter();
		f.setFilters("*.class"); //$NON-NLS-1$
		Assert.assertTrue("file foo.class should be filtered", f.filter("foo.class", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertFalse("file foo.java shouldn't be filtered", f.filter("foo.java", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testFilterDotFile() {
		CompareFilter f= new CompareFilter();
		f.setFilters(".cvsignore"); //$NON-NLS-1$
		Assert.assertTrue("file .cvsignore should be filtered", f.filter(".cvsignore", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertFalse("file foo.cvsignore shouldn't be filtered", f.filter("foo.cvsignore", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testFilterFolder() {
		CompareFilter f= new CompareFilter();
		f.setFilters("bin/"); //$NON-NLS-1$
		Assert.assertTrue("folder bin should be filtered", f.filter("bin", true, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertFalse("file bin shouldn't be filtered", f.filter("bin", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testMultiFilter() {
		CompareFilter f= new CompareFilter();
		f.setFilters("*.class, .cvsignore, bin/, src/"); //$NON-NLS-1$
		Assert.assertTrue("file foo.class should be filtered", f.filter("foo.class", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertFalse("file foo.java shouldn't be filtered", f.filter("foo.java", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertTrue("file .cvsignore should be filtered", f.filter(".cvsignore", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertFalse("file foo.cvsignore shouldn't be filtered", f.filter("foo.cvsignore", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertTrue("folder bin should be filtered", f.filter("bin", true, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertFalse("file bin shouldn't be filtered", f.filter("bin", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertTrue("folder src should be filtered", f.filter("src", true, false)); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertFalse("file src shouldn't be filtered", f.filter("src", false, false)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testVerify() {
		//Assert.assertNull("filters don't verify", Filter.validateResourceFilters("*.class, .cvsignore, bin/"));
		//Assert.assertNotNull("filters shouldn't verify", Filter.validateResourceFilters("bin//"));
	}
}
