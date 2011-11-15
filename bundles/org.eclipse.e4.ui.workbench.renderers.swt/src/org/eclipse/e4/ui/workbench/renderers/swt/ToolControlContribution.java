/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ToolControlContribution extends ControlContribution {

	private MToolControl model;

	@Inject
	private IContributionFactory contribFactory;

	@Inject
	private IEclipseContext parentContext;

	public ToolControlContribution() {
		super(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ControlContribution#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		IEclipseContext localContext = EclipseContextFactory.create();

		final Composite newComposite = new Composite(parent, SWT.NONE);
		newComposite.setLayout(new FillLayout());
		localContext.set(Composite.class.getName(), newComposite);
		localContext.set(MToolControl.class.getName(), model);

		if (model.getObject() == null) {
			Object tcImpl = contribFactory.create(model.getContributionURI(),
					parentContext, localContext);
			model.setObject(tcImpl);
		}

		model.setWidget(newComposite);
		newComposite.setData(AbstractPartRenderer.OWNING_ME, model);
		newComposite.setData(this);

		return newComposite;
	}

	public void setModel(MToolControl c) {
		model = c;
		setId(model.getElementId());
	}
}
