/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.text;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptorProvider;

/**
 * A tab descriptor for the text test view.
 *
 * @author Anthony Hunter
 */
public class TextTestsTabDescriptorProvider implements ITabDescriptorProvider {

	@Override
	public ITabDescriptor[] getTabDescriptors(IWorkbenchPart part,
			ISelection selection) {
		if (selection instanceof ITextSelection textSelection) {
			if (textSelection.getLength() != 0) {
				List<ITabDescriptor> result = new ArrayList<>();
				StringTokenizer tokenizer = new StringTokenizer(textSelection
						.getText());
				while (tokenizer.hasMoreTokens()) {
					result
							.add(new TextTestsTabDescriptor(tokenizer
									.nextToken()));
				}
				return result.toArray(new ITabDescriptor[result.size()]);
			}
		}
		return new ITabDescriptor[0];
	}

}
