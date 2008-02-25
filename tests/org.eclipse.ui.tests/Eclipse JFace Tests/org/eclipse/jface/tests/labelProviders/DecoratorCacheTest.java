/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.labelProviders;

import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.tests.viewers.ViewerTestCase;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.decorators.TestTreeContentProvider;

/**
 * @since 3.4
 * 
 */
public class DecoratorCacheTest extends ViewerTestCase {

	protected StructuredViewer v;
	protected DecoratingLabelProvider dlp;
	protected LabelProvider labelProvider;
	protected LabelDecorator ld;
	protected DecorationContext dc;

	protected ResourceManager rm;
	protected DecoratorDefinition dd;

	public DecoratorCacheTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.viewers.ViewerTestCase#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected StructuredViewer createViewer(Composite parent) {
		labelProvider = new LabelProvider();
		ld = new DecoratorManager();

		dlp = new DecoratingLabelProvider(labelProvider, ld);

		v = new TreeViewer(new Shell());
		v.setContentProvider(new TestTreeContentProvider());
		v.setLabelProvider(dlp);
		return v;

	}

	public void testDecoratorCacheIsDisposed() {
		
		dlp.dispose();
		dc = (DecorationContext) dlp.getDecorationContext();
		rm = (ResourceManager) dc.getProperty("RESOURCE_MANAGER");
		assertTrue("Resource Manager Not Cleared", dc
				.getProperty(DecorationContext.RESOURCE_MANAGER_KEY) == null);
	}
	
	

}
