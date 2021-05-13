/*******************************************************************************
 * Copyright (c) 2020, 2021 Sebastian Zarnekow and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sebastian Zarnekow - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;

public class Bug401391Test {

	private Document fDocument;
	private AnnotationModel fAnnotationModel;
	private AnnotationModel fFirstInnerModel;
	private AnnotationModel fSecondInnerModel;

	@Before
	public void setUp() throws Exception {
		fDocument = new Document("123456789");

		fAnnotationModel = new AnnotationModel();
		fAnnotationModel.addAnnotation(new Annotation(false), new Position(0, 1));

		fFirstInnerModel = new AnnotationModel();
		fFirstInnerModel.addAnnotation(new Annotation(false), new Position(1, 2));
		fAnnotationModel.addAnnotationModel("first", fFirstInnerModel);

		fAnnotationModel.connect(fDocument);
	}

	/*
	 * The test simulates a concurrent modification of the attachments by installing
	 * a trap into the annotation model that will modify the available attachments
	 * when the attachments are iterated.
	 */
	private void installAttachmentTrap() throws Exception {
		installTrap(fAnnotationModel, "fAttachments", () -> {
			if (fSecondInnerModel == null) {
				fSecondInnerModel = new AnnotationModel();
				fSecondInnerModel.addAnnotation(new Annotation(false), new Position(3, 2));
				fAnnotationModel.addAnnotationModel("second", fSecondInnerModel);
			} else {
				fAnnotationModel.removeAnnotationModel("second");
			}
		});
	}

	private static void installTrap(Object target, String fieldName, Runnable onKeySetIteration) throws Exception {
		Field fld = target.getClass().getDeclaredField(fieldName);
		fld.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<Object, Object> original = (Map<Object, Object>) fld.get(target);
		class TrapMap extends AbstractMap<Object, Object> {
			@Override
			public Set<Object> keySet() {
				Set<Object> delegate = original.keySet();

				return new AbstractSet<>() {

					@Override
					public Iterator<Object> iterator() {
						Iterator<Object> result = delegate.iterator();
						onKeySetIteration.run();
						return result;
					}

					@Override
					public int size() {
						return delegate.size();
					}
				};
			}

			@Override
			public Set<Entry<Object, Object>> entrySet() {
				return original.entrySet();
			}

			@Override
			public Object put(Object key, Object value) {
				return original.put(key, value);
			}
		}
		fld.set(target, new TrapMap());
	}

	@After
	public void tearDown() {
		fAnnotationModel.disconnect(fDocument);
	}

	@Test
	public void testNoConcurrentModificationOnAddAttachment() throws Exception {
		installAttachmentTrap();
		assertNull(fAnnotationModel.getAnnotationModel("second"));
		Iterator<Annotation> iter = fAnnotationModel.getAnnotationIterator();
		assertNotNull(fAnnotationModel.getAnnotationModel("second"));
		assertEquals(3, count(iter));
	}

	@Test
	public void testNoConcurrentModificationOnRemoveAttachment() throws Exception {
		installAttachmentTrap();
		assertNull(fAnnotationModel.getAnnotationModel("second"));
		fAnnotationModel.getAnnotationIterator();
		assertNotNull(fAnnotationModel.getAnnotationModel("second"));
		Iterator<Annotation> iter = fAnnotationModel.getAnnotationIterator();
		assertNull(fAnnotationModel.getAnnotationModel("second"));
		assertEquals(2, count(iter));
	}

	@Test
	public void testRemoveAnnotationWhileIterating() throws Exception {
		Annotation removeWhileIterating = new Annotation(false);
		fFirstInnerModel.addAnnotation(removeWhileIterating, new Position(5, 2));
		Iterator<Annotation> iter = fAnnotationModel.getAnnotationIterator();
		assertTrue(iter.hasNext());
		iter.next();
		assertTrue(iter.hasNext());
		iter.next();
		fFirstInnerModel.removeAnnotation(removeWhileIterating);
		assertEquals(2, count(fAnnotationModel.getAnnotationIterator()));
		assertTrue(iter.hasNext());
		// Traverses a copy of the model at the time the iterator was created
		assertSame(removeWhileIterating, iter.next());
	}

	private int count(Iterator<?> iter) {
		int result = 0;
		while (iter.hasNext()) {
			result++;
			iter.next();
		}
		return result;
	}

}
