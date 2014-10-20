package org.eclipse.e4.core.internal.tests.nls;

import java.util.Locale;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.eclipse.e4.core.services.translation.TranslationService;

public class MessageRegistryTest extends TestCase {

	static class TestObject {
		@Inject
		BundleMessagesRegistry registry;
	}

	class TestLocalizableObject {
		private String localizableValue;

		public String getLocalizableValue() {
			return localizableValue;
		}

		public void setLocalizableValue(String localizableValue) {
			this.localizableValue = localizableValue;
		}
	}

	private IEclipseContext context;

	@Override
	public void setUp() {
		this.context = EclipseContextFactory.getServiceContext(CoreTestsActivator.getDefault().getBundleContext());
		ContextInjectionFactory.setDefault(context);
	}

	public void testRegisterLocalizationByProperty() {
		// ensure the en Locale is set for this test
		this.context.set(TranslationService.LOCALE, Locale.ENGLISH);
		TestObject o = ContextInjectionFactory.make(TestObject.class, this.context);

		TestLocalizableObject control = new TestLocalizableObject();
		o.registry.registerProperty(control, "localizableValue", "message");

		// test value is set
		assertNotNull(control.getLocalizableValue());

		// test the set value
		assertEquals("BundleMessage", control.getLocalizableValue());
	}

	public void testRegisterLocalizationByMethod() {
		// ensure the en Locale is set for this test
		this.context.set(TranslationService.LOCALE, Locale.ENGLISH);
		TestObject o = ContextInjectionFactory.make(TestObject.class, this.context);

		TestLocalizableObject control = new TestLocalizableObject();
		o.registry.register(control, "setLocalizableValue", "message");

		// test value is set
		assertNotNull(control.getLocalizableValue());

		// test the set value
		assertEquals("BundleMessage", control.getLocalizableValue());
	}

	public void testRegisterLocalizationByPropertyAndChangeLocale() {
		// ensure the en Locale is set for this test
		this.context.set(TranslationService.LOCALE, Locale.ENGLISH);
		TestObject o = ContextInjectionFactory.make(TestObject.class, this.context);

		TestLocalizableObject control = new TestLocalizableObject();
		o.registry.registerProperty(control, "localizableValue", "message");

		// test value is set
		assertNotNull(control.getLocalizableValue());

		// test the set value
		assertEquals("BundleMessage", control.getLocalizableValue());

		// change the locale to GERMAN
		this.context.set(TranslationService.LOCALE, Locale.GERMAN);

		assertEquals("BundleNachricht", control.getLocalizableValue());
	}

	public void testRegisterLocalizationByMethodAndChangeLocale() {
		// ensure the en Locale is set for this test
		this.context.set(TranslationService.LOCALE, Locale.ENGLISH);
		TestObject o = ContextInjectionFactory.make(TestObject.class, this.context);

		TestLocalizableObject control = new TestLocalizableObject();
		o.registry.register(control, "setLocalizableValue", "message");

		// test value is set
		assertNotNull(control.getLocalizableValue());

		// test the set value
		assertEquals("BundleMessage", control.getLocalizableValue());

		// change the locale to GERMAN
		this.context.set(TranslationService.LOCALE, Locale.GERMAN);

		assertEquals("BundleNachricht", control.getLocalizableValue());
	}

	// TODO add testcases for Java 8 method references
}
