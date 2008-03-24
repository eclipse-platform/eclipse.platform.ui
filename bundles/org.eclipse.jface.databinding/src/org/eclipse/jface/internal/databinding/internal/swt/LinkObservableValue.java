/*******************************************************************************
 * Copyright (c) 2008 Michael Krauter, Catuno GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Krauter, Catuno GmbH - initial API and implementation (bug 180223)
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.swt.widgets.Link;

/**
 * @since 1.2
 * 
 */
public class LinkObservableValue extends AbstractSWTObservableValue {

	private final Link link;

	/**
	 * @param link
	 */
	public LinkObservableValue(Link link) {
		super(link);
		this.link = link;
	}

	public void doSetValue(final Object value) {
		String oldValue = link.getText();
		link.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
		fireValueChange(Diffs.createValueDiff(oldValue, link.getText()));
	}

	public Object doGetValue() {
		return link.getText();
	}

	public Object getValueType() {
		return String.class;
	}

}
