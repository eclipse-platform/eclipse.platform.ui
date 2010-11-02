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
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class ParentContextDisposalTest extends TestCase {
	static class Target {
		int pc = 0;
		int pd = 0;

		@Inject
		@Named("o")
		Object o;

		@PostConstruct
		void pc() {
			pc++;
		}

		@PreDestroy
		void pd() {
			pd++;
		}
	}

	public void testParentContextDisposal() {
		IEclipseContext context = EclipseContextFactory.create();
		IEclipseContext child = context.createChild();
		child.set("o", new Object());

		Target target = (Target) ContextInjectionFactory.make(Target.class, child);
		assertEquals(1, target.pc);

		context.dispose();
		assertEquals(1, target.pd);
	}
}
