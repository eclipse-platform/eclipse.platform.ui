/*
 * Created on May 6, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.search.tests.filesearch;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.search2.internal.ui.text.EditorAnnotationManager;

public class AnnotationManagerSetup extends TestSetup {
	private int fHighlighterType;

	public AnnotationManagerSetup(Test test, int hightlighterType) {
		super(test);
		fHighlighterType= hightlighterType;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		EditorAnnotationManager.debugSetHighlighterType(fHighlighterType);
	}

	protected void tearDown() throws Exception {
		EditorAnnotationManager.debugSetHighlighterType(EditorAnnotationManager.HIGHLLIGHTER_ANY);
		super.tearDown();
	}
}
