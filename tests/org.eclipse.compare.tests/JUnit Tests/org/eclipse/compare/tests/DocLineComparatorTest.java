/*
 * Created on May 22, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.compare.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * @author weinand
 */
public class DocLineComparatorTest extends TestCase {
	
	public DocLineComparatorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
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

}
