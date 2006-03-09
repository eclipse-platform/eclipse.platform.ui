package org.eclipse.jface.text.tests.rules;

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class DefaultPartitionerTest extends FastPartitionerTest {
	protected IDocumentPartitioner createPartitioner(IPartitionTokenScanner scanner) {
		return new DefaultPartitioner(scanner, new String[] { DEFAULT, COMMENT });
	}
	
	/*
	 * @see org.eclipse.jface.text.tests.rules.FastPartitionerTest#testPR130900()
	 */
	public void testPR130900() throws Exception {
		System.out.println("Bug130900 not fixed in DefaultPartitioner");
	}
}
