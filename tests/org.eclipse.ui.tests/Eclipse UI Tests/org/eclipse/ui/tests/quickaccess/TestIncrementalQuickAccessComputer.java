/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.quickaccess;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.IQuickAccessComputerExtension;
import org.eclipse.ui.quickaccess.QuickAccessElement;

public class TestIncrementalQuickAccessComputer implements IQuickAccessComputerExtension {

	public static String ENABLEMENT_QUERY = TestIncrementalQuickAccessComputer.class.getSimpleName()
			+ ".enablementQuery";

	public static final class EchoQuickAccessElement extends QuickAccessElement {

		private final String query;

		public EchoQuickAccessElement(String query) {
			this.query = query;
		}

		@Override
		public String getLabel() {
			return "Echo " + query;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getId() {
			return getLabel().toLowerCase();
		}

		@Override
		public void execute() {
		}

	}

	@Override
	public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor) {
		if (ENABLEMENT_QUERY.toLowerCase().equals(query.toLowerCase())) {
			return new QuickAccessElement[] { new EchoQuickAccessElement(query) };
		}
		return null;
	}

	@Override
	public QuickAccessElement[] computeElements() {
		return null;
	}

	@Override
	public void resetState() {
	}

	@Override
	public boolean needsRefresh() {
		return false;
	}

	public static boolean isContributedItem(String text) {
		return text != null && text.toLowerCase().contains(ENABLEMENT_QUERY.toLowerCase());
	}

}
