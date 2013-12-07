/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.e4.ui.css.swt.resources.ResourceByDefinitionKey;
import org.eclipse.e4.ui.css.swt.resources.SWTResourcesRegistry;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Resource;

@SuppressWarnings("restriction")
public class SWTResourcesRegistryTest extends TestCase {
	public void testRemoveResourcesByKeyTypeAndType() throws Exception {
		//given
		final Resource resource1 = mock(Resource.class);
		final Resource resource2 = mock(Resource.class);
		final Object resource3 = new Object();
		final Resource resource4 = mock(Resource.class);

		Map<Object, Object> resources = new LinkedHashMap<Object, Object>();
		resources.put("key1", resource1);
		resources.put(new ResourceByDefinitionKey("key2"), resource2);
		resources.put(new ResourceByDefinitionKey("key3"), resource3);
		resources.put(new ResourceByDefinitionKey("key4"), resource4);

		SWTResourcesRegistryTestable registry = spy(new SWTResourcesRegistryTestable());
		doReturn(resources).when(registry).getCacheByType(any(Font.class));

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

	public static class SWTResourcesRegistryTestable extends
	SWTResourcesRegistry {
		public SWTResourcesRegistryTestable() {
			super(null);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Map getCacheByType(Object type) {
			return super.getCacheByType(type);
		}
	}
}
