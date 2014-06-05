/*******************************************************************************
 * Copyright (c) 2015 Google Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 ******************************************************************************/
package org.eclipse.ui.tests.navigator;

import java.util.ArrayList;

import org.eclipse.ui.internal.navigator.VisibilityAssistant;
import org.eclipse.ui.internal.navigator.extensions.EvaluationCache;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.tests.navigator.util.TestNavigatorActivationService;
import org.eclipse.ui.tests.navigator.util.TestNavigatorViewerDescriptor;
import org.junit.Assert;

/**
 * Tests the {@link EvaluationCache} to ensure it can find various key types as
 * well as maintain the cache properly.
 *
 * @since 3.3
 */
public class EvaluationCacheTest extends NavigatorTestBase {
	EvaluationCache cache;

	public EvaluationCacheTest() {
		_navigatorInstanceId = TEST_VIEWER_PROGRAMMATIC;
	}

	@Override
	public void setUp() {
		super.setUp();
		INavigatorViewerDescriptor mockViewerDescript = new TestNavigatorViewerDescriptor();
		INavigatorActivationService mockActivationService = new TestNavigatorActivationService();
		VisibilityAssistant mockAssistant = new VisibilityAssistant(mockViewerDescript, mockActivationService);

		cache = new EvaluationCache(mockAssistant);
	}

	private void doSimpleAddGet(boolean toComputeOverrides) {
		Object key = new Object();
		NavigatorContentDescriptor[] value = new NavigatorContentDescriptor[0];
		cache.setDescriptors(key, value, toComputeOverrides);
		Assert.assertSame(value, cache.getDescriptors(key, toComputeOverrides));
		// The other "half" of the cache should not have this.
		Assert.assertNull(cache.getDescriptors(key, !toComputeOverrides));
	}

	public void testSimpleAddGetNotOverrides() {
		doSimpleAddGet(false);
	}

	public void testSimpleAddGetOverrides() {
		doSimpleAddGet(true);
	}

	private void doNotSameInstEqual(boolean toComputeOverrides) {
		java.util.List<String> key = new ArrayList<String>(2);
		key.add("Hi");
		key.add("There");
		NavigatorContentDescriptor[] value = new NavigatorContentDescriptor[0];
		// Should find it under the original key.
		cache.setDescriptors(key, value, toComputeOverrides);
		// Equal thing but different instance should still be equal.
		java.util.List key2 = new ArrayList<String>(key);
		// Should also find it under this new, equal key.
		Assert.assertSame(value, cache.getDescriptors(key2, toComputeOverrides));
		// The other "half" of the cache should not have this for either key.
		Assert.assertNull(cache.getDescriptors(key, !toComputeOverrides));
		Assert.assertNull(cache.getDescriptors(key2, !toComputeOverrides));
	}

	public void testNotSameInstEqualNotOverrides() {
		doNotSameInstEqual(false);
	}

	public void testNotSameInstEqualOverrides() {
		doNotSameInstEqual(true);
	}

	private void doTestReplace(boolean toComputeOverrides) {
		Object key = new Object();
		NavigatorContentDescriptor[] value1 = new NavigatorContentDescriptor[0];
		cache.setDescriptors(key, value1, toComputeOverrides);
		Assert.assertSame(value1, cache.getDescriptors(key, toComputeOverrides));
		NavigatorContentDescriptor[] value2 = new NavigatorContentDescriptor[0];
		cache.setDescriptors(key, value2, toComputeOverrides);
		Assert.assertSame(value2, cache.getDescriptors(key, toComputeOverrides));
	}

	public void testReplaceNotOverrides() {
		doTestReplace(false);
	}

	public void testReplaceOverrides() {
		doTestReplace(true);
	}

	public void testOnVisibilityOrActivationChangeClearsCaches() {
		Object key = new Object();
		NavigatorContentDescriptor[] value1 = new NavigatorContentDescriptor[0];
		cache.setDescriptors(key, value1, false);
		// Make sure they actually got inserted.
		Assert.assertSame(value1, cache.getDescriptors(key, false));
		NavigatorContentDescriptor[] value2 = new NavigatorContentDescriptor[0];
		cache.setDescriptors(key, value2, true);
		Assert.assertSame(value2, cache.getDescriptors(key, true));
		cache.onVisibilityOrActivationChange();
		// Now trying to find them should give null (non present).
		Assert.assertNull(cache.getDescriptors(key, false));
		Assert.assertNull(cache.getDescriptors(key, true));
	}

	// TODO Some way to reliably test the clearing of entries. Possibly using
	// java.lang.ref.Reference#enqueue().
}
