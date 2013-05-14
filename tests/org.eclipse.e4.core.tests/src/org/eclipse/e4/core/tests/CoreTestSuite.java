/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.e4.core.internal.tests.contexts.ActivationTest;
import org.eclipse.e4.core.internal.tests.contexts.ContextDynamicTest;
import org.eclipse.e4.core.internal.tests.contexts.DependenciesLeakTest;
import org.eclipse.e4.core.internal.tests.contexts.EclipseContextTest;
import org.eclipse.e4.core.internal.tests.contexts.ReparentingTest;
import org.eclipse.e4.core.internal.tests.contexts.RunAndTrackTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ActivationInjectionTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.AnnotationsInjectionTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.Bug317183Test;
import org.eclipse.e4.core.internal.tests.contexts.inject.ComplexDisposalTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ContextFunctionDynamicsTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ContextInjectionDisposeTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ContextInjectionFactoryTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ContextInjectionTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.DisposingReferencedContextTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ExtraDependenciesTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.GenericsInjectionTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.GroupedUpdatesTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.InjectStaticContextTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.InjectionUpdateTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.InvokeInRATTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ParentContextDisposalTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ProviderInjectionTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.ServiceContextTest;
import org.eclipse.e4.core.internal.tests.contexts.inject.TestConstructorInjection;
import org.eclipse.e4.core.internal.tests.di.AtInjectTest;
import org.eclipse.e4.core.internal.tests.di.AutoConstructTest;
import org.eclipse.e4.core.internal.tests.di.DisposeClassLinkTest;
import org.eclipse.e4.core.internal.tests.di.InjectArraysTest;
import org.eclipse.e4.core.internal.tests.di.InjectBaseTypeTest;
import org.eclipse.e4.core.internal.tests.di.InjectionOrderTest;
import org.eclipse.e4.core.internal.tests.di.InjectionResultLeakTest;
import org.eclipse.e4.core.internal.tests.di.InvokeTest;
import org.eclipse.e4.core.internal.tests.di.RecursiveObjectCreationTest;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionEventTest;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionMixedSuppliersTest;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionPreferencesTest;

public class CoreTestSuite extends TestSuite {
	public static Test suite() {
		return new CoreTestSuite();
	}

	public CoreTestSuite() {
		addTestSuite(InjectionPreferencesTest.class);
		addTestSuite(InjectionMixedSuppliersTest.class);
		addTestSuite(InjectionEventTest.class);

		// DI
		addTestSuite(InjectionOrderTest.class);
		addTestSuite(InvokeTest.class);
		addTestSuite(InjectBaseTypeTest.class);
		addTestSuite(InjectionResultLeakTest.class);
		addTest(AtInjectTest.suite());
		addTestSuite(AutoConstructTest.class);

		// Contexts
		addTestSuite(EclipseContextTest.class);
		addTestSuite(ContextInjectionTest.class);
		addTestSuite(ContextInjectionDisposeTest.class);
		addTestSuite(ContextInjectionFactoryTest.class);
		addTestSuite(ContextDynamicTest.class);
		addTestSuite(ReparentingTest.class);
		addTestSuite(RunAndTrackTest.class);
		addTestSuite(ParentContextDisposalTest.class);
		addTestSuite(ComplexDisposalTest.class);
		addTestSuite(DisposeClassLinkTest.class);
		addTestSuite(InjectStaticContextTest.class);
		addTestSuite(ActivationTest.class);

		// Contexts injection
		addTestSuite(AnnotationsInjectionTest.class);
		addTestSuite(TestConstructorInjection.class);
		addTestSuite(ServiceContextTest.class);
		addTestSuite(ProviderInjectionTest.class);
		addTestSuite(InjectionUpdateTest.class);
		addTestSuite(DisposingReferencedContextTest.class);
		addTestSuite(InjectionOrderTest.class);
		addTestSuite(GroupedUpdatesTest.class);
		addTestSuite(ExtraDependenciesTest.class);
		addTestSuite(ContextFunctionDynamicsTest.class);
		addTestSuite(InjectArraysTest.class);
		addTestSuite(InvokeInRATTest.class);
		addTestSuite(Bug317183Test.class);
		addTestSuite(DependenciesLeakTest.class);
		addTestSuite(ActivationInjectionTest.class);
		addTestSuite(GenericsInjectionTest.class);
		addTestSuite(RecursiveObjectCreationTest.class);
	}
}
