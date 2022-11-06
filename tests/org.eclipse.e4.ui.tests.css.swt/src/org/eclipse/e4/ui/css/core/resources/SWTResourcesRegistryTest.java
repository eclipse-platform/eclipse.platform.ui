/*******************************************************************************
 * Copyright (c) 2013, 2018 IBM Corporation and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.css.swt.resources.ResourceByDefinitionKey;
import org.eclipse.e4.ui.css.swt.resources.SWTResourcesRegistry;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Resource;
import org.junit.jupiter.api.Test;

public class SWTResourcesRegistryTest {
	@Test
	void testRemoveResourcesByKeyTypeAndType() {
		//given
		final Resource resource1 = mock(Resource.class);
		final Resource resource2 = mock(Resource.class);
		final Object resource3 = new Object();
		final Resource resource4 = mock(Resource.class);

		Map<Object, Object> resources = new LinkedHashMap<>();
		resources.put("key1", resource1);
		resources.put(new ResourceByDefinitionKey("key2"), resource2);
		resources.put(new ResourceByDefinitionKey("key3"), resource3);
		resources.put(new ResourceByDefinitionKey("key4"), resource4);

		SWTResourcesRegistryTestable registry = spy(new SWTResourcesRegistryTestable());
		doReturn(resources).when(registry).getCacheByType(Font.class);

		// when
		List<?> result = registry.removeResourcesByKeyTypeAndType(
				ResourceByDefinitionKey.class, Font.class);

		// then
		assertEquals(3, result.size());
		assertEquals(resource2, result.get(0));
		assertEquals(resource3, result.get(1));
		assertEquals(resource4, result.get(2));

		assertEquals(1, resources.size());
		assertTrue(resources.containsKey("key1"));
	}

	public static class SWTResourcesRegistryTestable extends SWTResourcesRegistry {
		public SWTResourcesRegistryTestable() {
			super(null);
		}

		@Override
		public Map<Object, Object> getCacheByType(Object type) {
			return super.getCacheByType(type);
		}
	}
}
