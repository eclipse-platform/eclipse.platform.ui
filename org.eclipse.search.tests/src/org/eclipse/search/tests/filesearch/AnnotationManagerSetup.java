/*
 * Created on May 6, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.search.tests.filesearch;

import org.eclipse.search2.internal.ui.text.AnnotationManager;

import junit.extensions.TestSetup;
import junit.framework.Test;

public class AnnotationManagerSetup extends TestSetup {
	private int fHighlighterType;

	public AnnotationManagerSetup(Test test, int hightlighterType) {
		super(test);
		fHighlighterType= hightlighterType;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		AnnotationManager.debugSetHighlighterType(fHighlighterType);
	}

	protected void tearDown() throws Exception {
		AnnotationManager.debugSetHighlighterType(AnnotationManager.HIGHLLIGHTER_ANY);
		super.tearDown();
	}
}
