/*******************************************************************************
 *  Copyright (c) 2019 Julian Honnen
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Julian Honnen <julian.honnen@vector.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContainerCheckedTreeViewerTest {

	private ContainerCheckedTreeViewer fViewer;
	private TreeNode fRoot;

	@Before
	public void setup() {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		fViewer = new ContainerCheckedTreeViewer(shell);
		fViewer.setContentProvider(new TreeNodeContentProvider());
		fViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((TreeNode) element).getValue().toString();
			}
		});

		fRoot = new TreeNode("0");
		generateChildren(fRoot, 3);
		fViewer.setInput(fRoot.getChildren());
		fViewer.expandAll();
	}

	private void generateChildren(TreeNode parent, int levels) {
		if (levels <= 0) {
			return;
		}

		int count = 3;
		TreeNode[] children = IntStream.range(1, count + 1).mapToObj(i -> new TreeNode(parent.getValue() + "." + i))
				.peek(child -> child.setParent(parent)).peek(child -> generateChildren(child, levels - 1))
				.toArray(TreeNode[]::new);

		parent.setChildren(children);
	}

	@After
	public void teardown() {
		Shell shell = fViewer.getControl().getShell();

		for (TreeNode node : fRoot.getChildren()) {
			verifyGrayedState(node);
		}

		shell.dispose();
	}

	@Test
	public void setChecked() {
		TreeNode node = fRoot.getChildren()[0];
		fViewer.setChecked(node, true);
		assertEquals(SelectionState.CHECKED, stateOf(node));
	}

	@Test
	public void subtreeChecked() {
		TreeNode node = fRoot.getChildren()[0].getChildren()[0];
		fViewer.setSubtreeChecked(node, true);
		assertEquals(SelectionState.CHECKED, stateOf(node));
	}

	private void verifyGrayedState(TreeNode node) {
		TreeNode[] children = node.getChildren();
		if (children == null) {
			return;
		}

		List<SelectionState> childStates = Arrays.stream(children).map(this::stateOf).toList();

		SelectionState expectedState = aggregate(childStates);
		SelectionState actualState = stateOf(node);
		if (expectedState != actualState) {
			fail(MessageFormat.format("Expected state of {0} to be {1}, but was {2}. Children states: {3}",
					node.getValue(), expectedState, actualState, childStates));
		}

		for (TreeNode child : children) {
			verifyGrayedState(child);
		}
	}

	private SelectionState aggregate(Collection<SelectionState> states) {
		HashSet<SelectionState> set = new HashSet<>(states);
		if (set.equals(singleton(SelectionState.CHECKED))) {
			return SelectionState.CHECKED;
		}
		if (set.equals(singleton(SelectionState.UNCHECKED))) {
			return SelectionState.UNCHECKED;
		}

		return SelectionState.GRAYED;
	}

	private SelectionState stateOf(TreeNode node) {
		if (!fViewer.getChecked(node)) {
			return SelectionState.UNCHECKED;
		}

		return fViewer.getGrayed(node) ? SelectionState.GRAYED : SelectionState.CHECKED;
	}

	private enum SelectionState {
		CHECKED("\u2611"), GRAYED("\u2588"), UNCHECKED("\u2610");

		private final String fLabel;

		SelectionState(String label) {
			fLabel = label;
		}

		@Override
		public String toString() {
			return fLabel;
		}
	}

}
