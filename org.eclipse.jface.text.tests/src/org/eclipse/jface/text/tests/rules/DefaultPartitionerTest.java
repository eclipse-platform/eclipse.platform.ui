package org.eclipse.jface.text.tests.rules;

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class DefaultPartitionerTest extends FastPartitionerTest {
	protected IDocumentPartitioner createPartitioner(IPartitionTokenScanner scanner) {
		return new DefaultPartitioner(scanner, new String[] { DEFAULT, COMMENT });
	}
}
