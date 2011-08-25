package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.swt.helpers.PropertyHelper;

import junit.framework.TestCase;

public class TestPropertyHelper extends TestCase {
	public static class Base {
		private String a = "A";
		public String getA() {
			return a;
		}
		public void setA(String a) {
			this.a = a;
		}
		
		public String getC() {
			return "C";
		}
		
		public boolean isD() {
			return true;
		}
	}
	
	public static class Impl extends Base {
		private String b = "B";
		private Base nested = new Base();
		{
			nested.a = "Nested";
		}
		
		public String getB() {
			return b;
		}
		
		public void setB(String b) {
			this.b = b;
		}
		
		public Base getNested() {
			return nested;
		}
		
		public void setNested(Base nested) {
			this.nested = nested;
		}
	}
	
	public void testReadWriteProperty() {
		Impl bean = new Impl();
		try {
			assertEquals("A",PropertyHelper.getProperty(bean, "a"));
			assertEquals("B",PropertyHelper.getProperty(bean, "b"));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testReadOnlyProperty() {
		Impl bean = new Impl();
		try {
			assertEquals("C",PropertyHelper.getProperty(bean, "c"));
			assertEquals(true,PropertyHelper.getProperty(bean, "d"));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testNestedProperty() {
		Impl bean = new Impl();
		try {
			assertEquals("Nested",PropertyHelper.getProperty(bean, "nested.a"));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
