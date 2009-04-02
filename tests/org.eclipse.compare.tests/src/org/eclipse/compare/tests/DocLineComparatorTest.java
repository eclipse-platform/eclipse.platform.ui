/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;

public class DocLineComparatorTest extends TestCase {
	
	public DocLineComparatorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		// empty
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRangesEqual() {
		IDocument doc1= new Document();
		doc1.set("if (s.strip))"); //$NON-NLS-1$
		
		IDocument doc2= new Document();
		doc2.set("if (s.strip)"); //$NON-NLS-1$
		
		IRangeComparator comp1= new DocLineComparator(doc1, null, true);
		IRangeComparator comp2= new DocLineComparator(doc2, null, true);

		Assert.assertFalse(comp1.rangesEqual(0, comp2, 0));
	}

	public void testWhitespaceAtEnd() {
		IDocument doc1= new Document();
		doc1.set("if (s.strip))"); //$NON-NLS-1$
		
		IDocument doc2= new Document();
		doc2.set("if (s.strip))   "); //$NON-NLS-1$
		
		IRangeComparator comp1= new DocLineComparator(doc1, null, true);
		IRangeComparator comp2= new DocLineComparator(doc2, null, true);

		Assert.assertTrue(comp1.rangesEqual(0, comp2, 0));
	}

	public void testEmpty() {
		IDocument doc1= new Document();
		doc1.set(""); //$NON-NLS-1$
		
		IDocument doc2= new Document();
		doc2.set("    "); //$NON-NLS-1$
		
		IRangeComparator comp1= new DocLineComparator(doc1, null, true);
		IRangeComparator comp2= new DocLineComparator(doc2, null, true);

		Assert.assertTrue(comp1.rangesEqual(0, comp2, 0));
	}

	public void testNoContent() {
		IDocument doc= new Document();
		
		IRangeComparator comp1= new DocLineComparator(doc, null, true);
		IRangeComparator comp2= new DocLineComparator(doc, new Region(0, doc.getLength()), true);

		Assert.assertTrue(comp1.rangesEqual(0, comp2, 0));
		Assert.assertEquals(comp1.getRangeCount(), comp2.getRangeCount());
		Assert.assertEquals(1, comp2.getRangeCount());
	}
	
	public void testOneLine() {
		IDocument doc = new Document();
		doc.set("line1"); //$NON-NLS-1$
		
		IRangeComparator comp1= new DocLineComparator(doc, null, true);
		IRangeComparator comp2= new DocLineComparator(doc, new Region(0, doc.getLength()), true);

		Assert.assertEquals(comp1.getRangeCount(), comp2.getRangeCount());
		Assert.assertEquals(1, comp2.getRangeCount());
	}
	
	public void testTwoLines() {
		IDocument doc = new Document();
		doc.set("line1\nline2"); //$NON-NLS-1$
		
		IRangeComparator comp1= new DocLineComparator(doc, null, true);
		IRangeComparator comp2= new DocLineComparator(doc, new Region(0, doc.getLength()), true);

		Assert.assertEquals(comp1.getRangeCount(), comp2.getRangeCount());
		Assert.assertEquals(2, comp2.getRangeCount());
		
		IRangeComparator comp3= new DocLineComparator(doc, new Region(0, "line1".length()), true);
		Assert.assertEquals(1, comp3.getRangeCount());
		
		comp3= new DocLineComparator(doc, new Region(0, "line1".length()+1), true);
		Assert.assertEquals(2, comp3.getRangeCount()); // two lines
	}
	
	public void testBug259422() {
		IDocument doc = new Document();
		doc.set(""); //$NON-NLS-1$
		
		IRangeComparator comp1= new DocLineComparator(doc, null, true);
		IRangeComparator comp2= new DocLineComparator(doc, new Region(0, doc.getLength()), true);

		Assert.assertEquals(comp1.getRangeCount(), comp2.getRangeCount());
	}
	
}
