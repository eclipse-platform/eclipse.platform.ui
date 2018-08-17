/*******************************************************************************
 * Copyright (c) 2017 Andrey Loskutov.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.SaveablesLifecycleEvent.POST_CLOSE;
import static org.eclipse.ui.SaveablesLifecycleEvent.POST_OPEN;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.5
 */
public class SaveablesListTest extends UITestCase {

	static class GoodSaveable extends Saveable {

		Object source;

		public GoodSaveable(Object source) {
			this.source = source;
		}

		@Override
		public boolean isDirty() {
			return false;
		}

		@Override
		public int hashCode() {
			return 42;
		}

		@Override
		public String getToolTipText() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public boolean equals(Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof GoodSaveable)) {
				return false;
			}
			GoodSaveable other = (GoodSaveable) object;
			return Objects.equals(source, other.source);
		}

		@Override
		public void doSave(IProgressMonitor monitor) {
		}
	}

	class BadSaveable extends GoodSaveable {
		public BadSaveable(Object source) {
			super(source);
		}

		void dispose() {
			source = null;
		}
	}

	static class DummyPart implements IWorkbenchPart {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public void addPropertyListener(IPropertyListener listener) {
		}

		@Override
		public void createPartControl(Composite parent) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public IWorkbenchPartSite getSite() {
			return null;
		}

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public Image getTitleImage() {
			return null;
		}

		@Override
		public String getTitleToolTip() {
			return null;
		}

		@Override
		public void removePropertyListener(IPropertyListener listener) {
		}

		@Override
		public void setFocus() {
		}

	}

	static class SaveablesListForTest extends SaveablesList {
		@Override
		protected Map<Saveable, List<Saveable>> getEqualKeys() {
			return super.getEqualKeys();
		}

		@Override
		protected Map<Object, Set<Saveable>> getModelMap() {
			return super.getModelMap();
		}

		@Override
		protected Map<Saveable, Integer> getModelRefCounts() {
			return super.getModelRefCounts();
		}
	}

	private SaveablesListForTest slist;
	private BadSaveable badSaveable;
	private GoodSaveable goodSaveable;
	private Object source;
	private DummyPart part1;
	private DummyPart part2;
	private DummyPart part3;

	public SaveablesListTest(String testName) {
		super(testName);
	}
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		slist = new SaveablesListForTest();
		source = new Object();
		part1 = new DummyPart();
		part2 = new DummyPart();
		part3 = new DummyPart();
		badSaveable = new BadSaveable(source);
		goodSaveable = new GoodSaveable(source);
	}

	public void testNotBrokenSaveables() throws Exception {
		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(goodSaveable, part2);
		emulateOpenPart(badSaveable, part3);

		assertPartsForSaveable(badSaveable, part1, part2, part3);
		assertPartsForSaveable(goodSaveable, part1, part2, part3);

		Map<Saveable, Integer> modelRefCounts = slist.getModelRefCounts();

		assertOpenModelCount(1);

		assertEquals(Integer.valueOf(3), modelRefCounts.get(goodSaveable));

		assertHasBothSaveables(3);

		emulateClosePart(badSaveable, part3);
		assertPartsForSaveable(badSaveable, part1, part2);
		assertPartsForSaveable(goodSaveable, part1, part2);

		assertHasBothSaveables(2);

		emulateClosePart(badSaveable, part1);

		assertOnlyOne(goodSaveable);

		assertPartsForSaveable(badSaveable, part2);
		assertPartsForSaveable(goodSaveable, part2);

		emulateClosePart(goodSaveable, part2);

		assertAllEmpty();
	}

	public void testBrokenSaveablesBadClosedFirst() throws Exception {
		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(goodSaveable, part2);
		doTestBrokenSaveablesBadClosedFirst();

		// restore bad saveable and test different opening order
		badSaveable = new BadSaveable(source);
		emulateOpenPart(goodSaveable, part2);
		emulateOpenPart(badSaveable, part1);
		doTestBrokenSaveablesBadClosedFirst();
	}

	private void doTestBrokenSaveablesBadClosedFirst() {
		assertPartsForSaveable(badSaveable, part1, part2);
		assertPartsForSaveable(goodSaveable, part1, part2);

		assertOpenModelCount(1);

		badSaveable.dispose();

		emulateClosePart(badSaveable, part1);

		assertOnlyOne(goodSaveable);

		emulateClosePart(goodSaveable, part2);

		assertAllEmpty();
	}

	public void testBrokenSaveablesGoodClosedFirst() throws Exception {
		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(goodSaveable, part2);

		doTestBrokenSaveablesGoodClosedFirst();

		// restore bad saveable and test different opening order
		badSaveable = new BadSaveable(source);
		emulateOpenPart(goodSaveable, part2);
		emulateOpenPart(badSaveable, part1);

		doTestBrokenSaveablesGoodClosedFirst();
	}

	private void doTestBrokenSaveablesGoodClosedFirst() {
		assertPartsForSaveable(badSaveable, part1, part2);
		assertPartsForSaveable(goodSaveable, part1, part2);

		assertOpenModelCount(1);

		badSaveable.dispose();

		emulateClosePart(goodSaveable, part2);

		assertOnlyOne(badSaveable);

		emulateClosePart(badSaveable, part1);

		assertAllEmpty();
	}

	public void testBrokenSaveablesBadContainedMultipleTimes() throws Exception {
		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(goodSaveable, part2);
		emulateOpenPart(badSaveable, part3);

		doTestBrokenSaveablesContainedMultipleTimes();

		// restore bad saveable and test different opening order
		badSaveable = new BadSaveable(source);

		emulateOpenPart(goodSaveable, part2);
		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(badSaveable, part3);

		doTestBrokenSaveablesContainedMultipleTimes();

		// restore bad saveable and test different opening order
		badSaveable = new BadSaveable(source);

		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(badSaveable, part3);
		emulateOpenPart(goodSaveable, part2);

		doTestBrokenSaveablesContainedMultipleTimes();
	}

	private void doTestBrokenSaveablesContainedMultipleTimes() {
		assertPartsForSaveable(badSaveable, part1, part2, part3);
		assertPartsForSaveable(goodSaveable, part1, part2, part3);

		assertOpenModelCount(1);

		badSaveable.dispose();

		// XXX check if this is expected
		assertOpenModelCount(2);

		emulateClosePart(badSaveable, part1);

		// XXX check if this is expected
		assertOpenModelCount(2);

		assertHasBothSaveables(2);

		emulateClosePart(badSaveable, part3);

		assertOnlyOne(goodSaveable);

		emulateClosePart(goodSaveable, part2);

		assertAllEmpty();
	}

	public void testGoodSaveablesBadContainedMultipleTimes() throws Exception {
		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(goodSaveable, part2);
		emulateOpenPart(goodSaveable, part3);

		doTestGoodSaveablesContainedMultipleTimes();

		// restore bad saveable and test different opening order
		badSaveable = new BadSaveable(source);

		emulateOpenPart(goodSaveable, part2);
		emulateOpenPart(badSaveable, part1);
		emulateOpenPart(goodSaveable, part3);

		doTestGoodSaveablesContainedMultipleTimes();

		// restore bad saveable and test different opening order
		badSaveable = new BadSaveable(source);

		emulateOpenPart(goodSaveable, part3);
		emulateOpenPart(goodSaveable, part2);
		emulateOpenPart(badSaveable, part1);

		doTestGoodSaveablesContainedMultipleTimes();
	}

	private void doTestGoodSaveablesContainedMultipleTimes() {
		assertPartsForSaveable(badSaveable, part1, part2, part3);
		assertPartsForSaveable(goodSaveable, part1, part2, part3);

		assertOpenModelCount(1);

		badSaveable.dispose();

		// XXX check if this is expected
		assertOpenModelCount(2);

		emulateClosePart(badSaveable, part1);

		assertOpenModelCount(1);

		assertHasTwoSaveables(goodSaveable, 2);

		emulateClosePart(goodSaveable, part3);

		assertOnlyOne(goodSaveable);

		emulateClosePart(goodSaveable, part2);

		assertAllEmpty();
	}

	void emulateOpenPart(Saveable saveable, IWorkbenchPart part) {
		Saveable[] saveables = new Saveable[] { saveable };
		SaveablesLifecycleEvent event = new SaveablesLifecycleEvent(part, POST_OPEN, saveables, false);
		slist.handleLifecycleEvent(event);
	}

	void emulateClosePart(Saveable saveable, IWorkbenchPart part) {
		Saveable[] saveables = new Saveable[] { saveable };
		SaveablesLifecycleEvent event = new SaveablesLifecycleEvent(part, POST_CLOSE, saveables, false);
		slist.handleLifecycleEvent(event);
	}

	private void assertHasBothSaveables(int partCount) {
		Map<Object, Set<Saveable>> modelMap = slist.getModelMap();
		Map<Saveable, Integer> modelRefCounts = slist.getModelRefCounts();
		Map<Saveable, List<Saveable>> equalKeys = slist.getEqualKeys();
		Collection<Saveable> equalSaveables = equalKeys.get(badSaveable);

		if (modelRefCounts.get(badSaveable) != null) {
			assertEquals(Integer.valueOf(partCount), modelRefCounts.get(badSaveable));
		} else {
			assertEquals(Integer.valueOf(partCount), modelRefCounts.get(goodSaveable));
		}

		assertEquals(partCount, modelMap.size());
		assertEquals(partCount, modelMap.values().size());

		assertEquals(1, modelRefCounts.keySet().size());
		assertEquals(2, equalKeys.keySet().size());
		assertTrue(equalKeys.containsKey(badSaveable));
		assertTrue(equalKeys.containsKey(goodSaveable));
		assertSame(equalKeys.get(goodSaveable), equalKeys.get(badSaveable));

		assertEquals(partCount, equalSaveables.size());
		assertTrue(equalSaveables.stream().anyMatch(x -> x == badSaveable));
		assertTrue(equalSaveables.stream().anyMatch(x -> x == goodSaveable));
	}

	private void assertHasTwoSaveables(Saveable two, int partCount) {
		Map<Object, Set<Saveable>> modelMap = slist.getModelMap();
		Map<Saveable, Integer> modelRefCounts = slist.getModelRefCounts();
		Map<Saveable, List<Saveable>> equalKeys = slist.getEqualKeys();
		Collection<Saveable> equalSaveables = equalKeys.get(two);

		assertEquals(Integer.valueOf(partCount), modelRefCounts.get(two));

		assertEquals(partCount, modelMap.size());
		assertEquals(partCount, modelMap.values().size());

		assertEquals(1, modelRefCounts.keySet().size());
		assertEquals(1, equalKeys.keySet().size());
		assertTrue(equalKeys.containsKey(two));

		assertEquals(partCount, equalSaveables.size());
		assertTrue(equalSaveables.stream().anyMatch(x -> x == two));
	}

	private void assertAllEmpty() {
		Map<Object, Set<Saveable>> modelMap = slist.getModelMap();
		Map<Saveable, Integer> modelRefCounts = slist.getModelRefCounts();
		Map<Saveable, List<Saveable>> equalKeys = slist.getEqualKeys();
		Collection<Saveable> equalSaveables = equalKeys.get(badSaveable);

		assertOpenModelCount(0);
		assertEquals(0, modelMap.size());
		assertEquals(0, modelMap.values().size());

		assertEquals(0, modelRefCounts.size());
		assertEquals(0, equalKeys.size());
		assertNull(equalSaveables);

		assertPartsForSaveable(badSaveable);
		assertPartsForSaveable(goodSaveable);
	}

	private void assertOnlyOne(Saveable one) {
		Map<Object, Set<Saveable>> modelMap = slist.getModelMap();
		Map<Saveable, Integer> modelRefCounts = slist.getModelRefCounts();
		Map<Saveable, List<Saveable>> equalKeys = slist.getEqualKeys();
		Collection<Saveable> equalSaveables = equalKeys.get(one);

		assertOpenModelCount(1);

		assertEquals(1, modelMap.size());
		assertEquals(1, modelMap.values().size());

		assertEquals(1, modelRefCounts.keySet().size());
		assertSame(one, modelRefCounts.keySet().iterator().next());
		assertEquals(Integer.valueOf(1), modelRefCounts.get(one));

		assertEquals(1, equalKeys.keySet().size());
		assertTrue(equalKeys.containsKey(one));

		assertEquals(1, equalSaveables.size());
		assertTrue(equalSaveables.stream().anyMatch(x -> x == one));
	}

	private void assertPartsForSaveable(Saveable saveable, DummyPart... parts) {
		IWorkbenchPart[] seen = slist.getPartsForSaveable(saveable);
		assertEquals(new HashSet<>(Arrays.asList(parts)), new HashSet<>(Arrays.asList(seen)));
	}

	private void assertOpenModelCount(int count) {
		IdentitySet<Saveable> identSet = new IdentitySet<>(Arrays.asList(slist.getOpenModels()));
		assertEquals(count, identSet.size());
	}

}
