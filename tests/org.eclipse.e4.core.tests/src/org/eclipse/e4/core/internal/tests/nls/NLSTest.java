package org.eclipse.e4.core.internal.tests.nls;

import java.util.Locale;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.core.services.translation.TranslationService;

public class NLSTest extends TestCase {
	static class TestSimpleObject {
		@Inject
		@Translation
		SimpleMessages simpleMessages;
	}
	
	static class TestMessagesObject {
		@Inject
		@Translation
		Messages Messages;
	}
	
	static class TestBundleObject {
		@Inject
		@Translation
		BundleMessages bundleMessages;
	}
	
	static class TestResourceBundleClassObject {
		@Inject
		@Translation
		ResourceBundleClassMessages bundleClassMessages;
	}
	
	static class TestResourcesBundleObject {
		@Inject
		@Translation
		ResourcesMessages resourcesMessages;
	}
	
	private IEclipseContext context;
	
	private IEclipseContext getOrCreateContext() {
		if( context != null ) {
			return context;
		}
		context = EclipseContextFactory.getServiceContext(CoreTestsActivator.getDefault().getBundleContext());
		ContextInjectionFactory.setDefault(context);
		return context;
	}
	
	public void testSimpleMessages() {
		//ensure the en Locale is set for this test
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestSimpleObject o = ContextInjectionFactory.make(TestSimpleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.simpleMessages);
		assertNotNull(o.simpleMessages.message);
		assertNotNull(o.simpleMessages.message_one);
		assertNotNull(o.simpleMessages.messageOne);
		assertNotNull(o.simpleMessages.message_two);
		assertNotNull(o.simpleMessages.messageThree);
		assertNotNull(o.simpleMessages.messageFour);

		//test the set values
		assertEquals("SimpleMessage", o.simpleMessages.message);
		assertEquals("SimpleMessageUnderscore", o.simpleMessages.message_one);
		assertEquals("SimpleMessageCamelCase", o.simpleMessages.messageOne);
		assertEquals("SimpleMessageUnderscoreDot", o.simpleMessages.message_two);
		assertEquals("SimpleMessageCamelCaseDot", o.simpleMessages.messageThree);
		assertEquals("The idea is from Tom", o.simpleMessages.messageFour);
	}
	
	public void testSimpleMessagesDifferentLocale() {
		//set Locale to de
		getOrCreateContext().set(TranslationService.LOCALE, "de");
		TestSimpleObject o = ContextInjectionFactory.make(TestSimpleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.simpleMessages);
		assertNotNull(o.simpleMessages.message);
		assertNotNull(o.simpleMessages.message_one);
		assertNotNull(o.simpleMessages.messageOne);
		assertNotNull(o.simpleMessages.message_two);
		assertNotNull(o.simpleMessages.messageThree);
		assertNotNull(o.simpleMessages.messageFour);

		//test the set values
		assertEquals("SimpleNachricht", o.simpleMessages.message);
		assertEquals("SimpleNachrichtUnderscore", o.simpleMessages.message_one);
		assertEquals("SimpleNachrichtCamelCase", o.simpleMessages.messageOne);
		assertEquals("SimpleNachrichtUnderscoreDot", o.simpleMessages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("SimpleMessageCamelCaseDot", o.simpleMessages.messageThree);
		assertEquals("Die Idee ist von Tom", o.simpleMessages.messageFour);
	}
	
	public void testSimpleMessagesSkipDefaultLocaleForEquinoxRoot() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));

		//set the locale to en
		//as there is no _en properties file, by default the _de properties file would be loaded and we would
		//get german translations as the default locale is set to "de_DE"
		//with checking the equinox.root.locale in the system properties the fallback is skipped as it tells
		//that the root properties file is for locale en.
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestSimpleObject o = ContextInjectionFactory.make(TestSimpleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.simpleMessages);
		assertNotNull(o.simpleMessages.message);
		assertNotNull(o.simpleMessages.message_one);
		assertNotNull(o.simpleMessages.messageOne);
		assertNotNull(o.simpleMessages.message_two);
		assertNotNull(o.simpleMessages.messageThree);
		assertNotNull(o.simpleMessages.messageFour);

		//not the default resource bundle should be used (e.g. de when running on a machine with german OS) 
		//but the default resource bundle
		assertEquals("SimpleMessage", o.simpleMessages.message);
		assertEquals("SimpleMessageUnderscore", o.simpleMessages.message_one);
		assertEquals("SimpleMessageCamelCase", o.simpleMessages.messageOne);
		assertEquals("SimpleMessageUnderscoreDot", o.simpleMessages.message_two);
		assertEquals("SimpleMessageCamelCaseDot", o.simpleMessages.messageThree);
		assertEquals("The idea is from Tom", o.simpleMessages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}
	
	public void testSimpleMessagesUseDefaultLocaleForInvalidLocale() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));
		
		//set a locale for which no resource bundle is set
		getOrCreateContext().set(TranslationService.LOCALE, "fr");
		TestSimpleObject o = ContextInjectionFactory.make(TestSimpleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.simpleMessages);
		assertNotNull(o.simpleMessages.message);
		assertNotNull(o.simpleMessages.message_one);
		assertNotNull(o.simpleMessages.messageOne);
		assertNotNull(o.simpleMessages.message_two);
		assertNotNull(o.simpleMessages.messageThree);
		assertNotNull(o.simpleMessages.messageFour);

		//the default resource bundle should be used
		assertEquals("SimpleNachricht", o.simpleMessages.message);
		assertEquals("SimpleNachrichtUnderscore", o.simpleMessages.message_one);
		assertEquals("SimpleNachrichtCamelCase", o.simpleMessages.messageOne);
		assertEquals("SimpleNachrichtUnderscoreDot", o.simpleMessages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("SimpleMessageCamelCaseDot", o.simpleMessages.messageThree);
		assertEquals("Die Idee ist von Tom", o.simpleMessages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}
	
	public void testMessages() {
		//ensure the en Locale is set for this test
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestMessagesObject o = ContextInjectionFactory.make(TestMessagesObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.Messages);
		assertNotNull(o.Messages.message);
		assertNotNull(o.Messages.message_one);
		assertNotNull(o.Messages.messageOne);
		assertNotNull(o.Messages.message_two);
		assertNotNull(o.Messages.messageThree);
		assertNotNull(o.Messages.messageFour);

		//test the set values
		assertEquals("Message", o.Messages.message);
		assertEquals("MessageUnderscore", o.Messages.message_one);
		assertEquals("MessageCamelCase", o.Messages.messageOne);
		assertEquals("MessageUnderscoreDot", o.Messages.message_two);
		assertEquals("MessageCamelCaseDot", o.Messages.messageThree);
		assertEquals("The idea is from Tom", o.Messages.messageFour);
	}
	
	public void testMessagesDifferentLocale() {
		//set Locale to de
		getOrCreateContext().set(TranslationService.LOCALE, "de");
		TestMessagesObject o = ContextInjectionFactory.make(TestMessagesObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.Messages);
		assertNotNull(o.Messages.message);
		assertNotNull(o.Messages.message_one);
		assertNotNull(o.Messages.messageOne);
		assertNotNull(o.Messages.message_two);
		assertNotNull(o.Messages.messageThree);
		assertNotNull(o.Messages.messageFour);

		//test the set values
		assertEquals("Nachricht", o.Messages.message);
		assertEquals("NachrichtUnderscore", o.Messages.message_one);
		assertEquals("NachrichtCamelCase", o.Messages.messageOne);
		assertEquals("NachrichtUnderscoreDot", o.Messages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("MessageCamelCaseDot", o.Messages.messageThree);
		assertEquals("Die Idee ist von Tom", o.Messages.messageFour);
	}
	
	public void testMessagesSkipDefaultLocaleForEquinoxRoot() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));

		//set the locale to en
		//as there is no _en properties file, by default the _de properties file would be loaded and we would
		//get german translations as the default locale is set to "de_DE"
		//with checking the equinox.root.locale in the system properties the fallback is skipped as it tells
		//that the root properties file is for locale en.
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestMessagesObject o = ContextInjectionFactory.make(TestMessagesObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.Messages);
		assertNotNull(o.Messages.message);
		assertNotNull(o.Messages.message_one);
		assertNotNull(o.Messages.messageOne);
		assertNotNull(o.Messages.message_two);
		assertNotNull(o.Messages.messageThree);
		assertNotNull(o.Messages.messageFour);

		//not the default resource bundle should be used (e.g. de when running on a machine with german OS) 
		//but the default resource bundle
		assertEquals("Message", o.Messages.message);
		assertEquals("MessageUnderscore", o.Messages.message_one);
		assertEquals("MessageCamelCase", o.Messages.messageOne);
		assertEquals("MessageUnderscoreDot", o.Messages.message_two);
		assertEquals("MessageCamelCaseDot", o.Messages.messageThree);
		assertEquals("The idea is from Tom", o.Messages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}
	
	public void testMessagesUseDefaultLocaleForInvalidLocale() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));
		
		//set a locale for which no resource bundle is set
		getOrCreateContext().set(TranslationService.LOCALE, "fr");
		TestMessagesObject o = ContextInjectionFactory.make(TestMessagesObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.Messages);
		assertNotNull(o.Messages.message);
		assertNotNull(o.Messages.message_one);
		assertNotNull(o.Messages.messageOne);
		assertNotNull(o.Messages.message_two);
		assertNotNull(o.Messages.messageThree);
		assertNotNull(o.Messages.messageFour);

		//the default resource bundle should be used
		assertEquals("Nachricht", o.Messages.message);
		assertEquals("NachrichtUnderscore", o.Messages.message_one);
		assertEquals("NachrichtCamelCase", o.Messages.messageOne);
		assertEquals("NachrichtUnderscoreDot", o.Messages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("MessageCamelCaseDot", o.Messages.messageThree);
		assertEquals("Die Idee ist von Tom", o.Messages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}

	public void testBundleMessages() {
		//ensure the en Locale is set for this test
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestBundleObject o = ContextInjectionFactory.make(TestBundleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.bundleMessages);
		assertNotNull(o.bundleMessages.message);
		assertNotNull(o.bundleMessages.message_one);
		assertNotNull(o.bundleMessages.messageOne);
		assertNotNull(o.bundleMessages.message_two);
		assertNotNull(o.bundleMessages.messageThree);
		assertNotNull(o.bundleMessages.messageFour);

		//test the set values
		assertEquals("BundleMessage", o.bundleMessages.message);
		assertEquals("BundleMessageUnderscore", o.bundleMessages.message_one);
		assertEquals("BundleMessageCamelCase", o.bundleMessages.messageOne);
		assertEquals("BundleMessageUnderscoreDot", o.bundleMessages.message_two);
		assertEquals("BundleMessageCamelCaseDot", o.bundleMessages.messageThree);
		assertEquals("The idea is from Tom", o.bundleMessages.messageFour);
	}
	
	public void testBundleMessagesDifferentLocale() {
		//set Locale to de
		getOrCreateContext().set(TranslationService.LOCALE, "de");
		TestBundleObject o = ContextInjectionFactory.make(TestBundleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.bundleMessages);
		assertNotNull(o.bundleMessages.message);
		assertNotNull(o.bundleMessages.message_one);
		assertNotNull(o.bundleMessages.messageOne);
		assertNotNull(o.bundleMessages.message_two);
		assertNotNull(o.bundleMessages.messageThree);
		assertNotNull(o.bundleMessages.messageFour);

		//test the set values
		assertEquals("BundleNachricht", o.bundleMessages.message);
		assertEquals("BundleNachrichtUnderscore", o.bundleMessages.message_one);
		assertEquals("BundleNachrichtCamelCase", o.bundleMessages.messageOne);
		assertEquals("BundleNachrichtUnderscoreDot", o.bundleMessages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("BundleMessageCamelCaseDot", o.bundleMessages.messageThree);
		assertEquals("Die Idee ist von Tom", o.bundleMessages.messageFour);
	}
	
	public void testBundleMessagesSkipDefaultLocaleForEquinoxRoot() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));

		//set the locale to en
		//as there is no _en properties file, by default the _de properties file would be loaded and we would
		//get german translations as the default locale is set to "de_DE"
		//with checking the equinox.root.locale in the system properties the fallback is skipped as it tells
		//that the root properties file is for locale en.
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestBundleObject o = ContextInjectionFactory.make(TestBundleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.bundleMessages);
		assertNotNull(o.bundleMessages.message);
		assertNotNull(o.bundleMessages.message_one);
		assertNotNull(o.bundleMessages.messageOne);
		assertNotNull(o.bundleMessages.message_two);
		assertNotNull(o.bundleMessages.messageThree);
		assertNotNull(o.bundleMessages.messageFour);

		//not the default resource bundle should be used (e.g. de when running on a machine with german OS) 
		//but the default resource bundle
		assertEquals("BundleMessage", o.bundleMessages.message);
		assertEquals("BundleMessageUnderscore", o.bundleMessages.message_one);
		assertEquals("BundleMessageCamelCase", o.bundleMessages.messageOne);
		assertEquals("BundleMessageUnderscoreDot", o.bundleMessages.message_two);
		assertEquals("BundleMessageCamelCaseDot", o.bundleMessages.messageThree);
		assertEquals("The idea is from Tom", o.bundleMessages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}
	
	public void testBundleMessagesUseDefaultLocaleForInvalidLocale() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));
		
		//set a locale for which no resource bundle is set
		getOrCreateContext().set(TranslationService.LOCALE, "fr");
		TestBundleObject o = ContextInjectionFactory.make(TestBundleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.bundleMessages);
		assertNotNull(o.bundleMessages.message);
		assertNotNull(o.bundleMessages.message_one);
		assertNotNull(o.bundleMessages.messageOne);
		assertNotNull(o.bundleMessages.message_two);
		assertNotNull(o.bundleMessages.messageThree);
		assertNotNull(o.bundleMessages.messageFour);

		//the default resource bundle should be used
		assertEquals("BundleNachricht", o.bundleMessages.message);
		assertEquals("BundleNachrichtUnderscore", o.bundleMessages.message_one);
		assertEquals("BundleNachrichtCamelCase", o.bundleMessages.messageOne);
		assertEquals("BundleNachrichtUnderscoreDot", o.bundleMessages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("BundleMessageCamelCaseDot", o.bundleMessages.messageThree);
		assertEquals("Die Idee ist von Tom", o.bundleMessages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}

	public void testClassBasedResourceBundle() {
		//ensure the en Locale is set for this test
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestResourceBundleClassObject o = ContextInjectionFactory.make(TestResourceBundleClassObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.bundleClassMessages);
		assertNotNull(o.bundleClassMessages.message);
		assertNotNull(o.bundleClassMessages.message_one);
		assertNotNull(o.bundleClassMessages.messageOne);
		assertNotNull(o.bundleClassMessages.message_two);
		assertNotNull(o.bundleClassMessages.messageThree);
		assertNotNull(o.bundleClassMessages.messageFour);

		//test the set values
		assertEquals("ResourceBundleClassMessage", o.bundleClassMessages.message);
		assertEquals("ResourceBundleClassMessageUnderscore", o.bundleClassMessages.message_one);
		assertEquals("ResourceBundleClassMessageCamelCase", o.bundleClassMessages.messageOne);
		assertEquals("ResourceBundleClassMessageUnderscoreDot", o.bundleClassMessages.message_two);
		assertEquals("ResourceBundleClassCamelCaseDot", o.bundleClassMessages.messageThree);
		assertEquals("The idea is from Tom", o.bundleClassMessages.messageFour);
	}
	
	public void testClassBasedResourceBundleDifferentLocale() {
		//set Locale to de
		getOrCreateContext().set(TranslationService.LOCALE, "de");
		TestResourceBundleClassObject o = ContextInjectionFactory.make(TestResourceBundleClassObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.bundleClassMessages);
		assertNotNull(o.bundleClassMessages.message);
		assertNotNull(o.bundleClassMessages.message_one);
		assertNotNull(o.bundleClassMessages.messageOne);
		assertNotNull(o.bundleClassMessages.message_two);
		assertNotNull(o.bundleClassMessages.messageThree);
		assertNotNull(o.bundleClassMessages.messageFour);

		//test the set values
		assertEquals("ResourceBundleClassNachricht", o.bundleClassMessages.message);
		assertEquals("ResourceBundleClassNachrichtUnderscore", o.bundleClassMessages.message_one);
		assertEquals("ResourceBundleClassNachrichtCamelCase", o.bundleClassMessages.messageOne);
		assertEquals("ResourceBundleNachrichtMessageUnderscoreDot", o.bundleClassMessages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("ResourceBundleClassCamelCaseDot", o.bundleClassMessages.messageThree);
		assertEquals("Die Idee ist von Tom", o.bundleClassMessages.messageFour);
	}
	
	public void testClassBasedResourceBundleSkipDefaultLocaleForEquinoxRoot() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));

		//set the locale to en
		//as there is no _en properties file, by default the _de properties file would be loaded and we would
		//get german translations as the default locale is set to "de_DE"
		//with checking the equinox.root.locale in the system properties the fallback is skipped as it tells
		//that the root properties file is for locale en.
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestResourceBundleClassObject o = ContextInjectionFactory.make(TestResourceBundleClassObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.bundleClassMessages);
		assertNotNull(o.bundleClassMessages.message);
		assertNotNull(o.bundleClassMessages.message_one);
		assertNotNull(o.bundleClassMessages.messageOne);
		assertNotNull(o.bundleClassMessages.message_two);
		assertNotNull(o.bundleClassMessages.messageThree);
		assertNotNull(o.bundleClassMessages.messageFour);

		//not the default resource bundle should be used (e.g. de when running on a machine with german OS) 
		//but the default resource bundle
		assertEquals("ResourceBundleClassMessage", o.bundleClassMessages.message);
		assertEquals("ResourceBundleClassMessageUnderscore", o.bundleClassMessages.message_one);
		assertEquals("ResourceBundleClassMessageCamelCase", o.bundleClassMessages.messageOne);
		assertEquals("ResourceBundleClassMessageUnderscoreDot", o.bundleClassMessages.message_two);
		assertEquals("ResourceBundleClassCamelCaseDot", o.bundleClassMessages.messageThree);
		assertEquals("The idea is from Tom", o.bundleClassMessages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}
	
	public void testClassBasedResourceBundleUseDefaultLocaleForInvalidLocale() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));
		
		//set a locale for which no resource bundle is set
		getOrCreateContext().set(TranslationService.LOCALE, "fr");
		TestResourceBundleClassObject o = ContextInjectionFactory.make(TestResourceBundleClassObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.bundleClassMessages);
		assertNotNull(o.bundleClassMessages.message);
		assertNotNull(o.bundleClassMessages.message_one);
		assertNotNull(o.bundleClassMessages.messageOne);
		assertNotNull(o.bundleClassMessages.message_two);
		assertNotNull(o.bundleClassMessages.messageThree);
		assertNotNull(o.bundleClassMessages.messageFour);

		//the default resource bundle should be used
		assertEquals("ResourceBundleClassNachricht", o.bundleClassMessages.message);
		assertEquals("ResourceBundleClassNachrichtUnderscore", o.bundleClassMessages.message_one);
		assertEquals("ResourceBundleClassNachrichtCamelCase", o.bundleClassMessages.messageOne);
		assertEquals("ResourceBundleNachrichtMessageUnderscoreDot", o.bundleClassMessages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("ResourceBundleClassCamelCaseDot", o.bundleClassMessages.messageThree);
		assertEquals("Die Idee ist von Tom", o.bundleClassMessages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}

//	//TODO test location (within plugin)

	public void testResourcesBundle() {
		//ensure the en Locale is set for this test
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestResourcesBundleObject o = ContextInjectionFactory.make(TestResourcesBundleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.resourcesMessages);
		assertNotNull(o.resourcesMessages.message);
		assertNotNull(o.resourcesMessages.message_one);
		assertNotNull(o.resourcesMessages.messageOne);
		assertNotNull(o.resourcesMessages.message_two);
		assertNotNull(o.resourcesMessages.messageThree);
		assertNotNull(o.resourcesMessages.messageFour);

		//test the set values
		assertEquals("ResourcesMessage", o.resourcesMessages.message);
		assertEquals("ResourcesMessageUnderscore", o.resourcesMessages.message_one);
		assertEquals("ResourcesMessageCamelCase", o.resourcesMessages.messageOne);
		assertEquals("ResourcesMessageUnderscoreDot", o.resourcesMessages.message_two);
		assertEquals("ResourcesMessageCamelCaseDot", o.resourcesMessages.messageThree);
		assertEquals("The idea is from Tom", o.resourcesMessages.messageFour);
	}
	
	public void testResourcesBundleDifferentLocale() {
		//set Locale to de
		getOrCreateContext().set(TranslationService.LOCALE, "de");
		TestResourcesBundleObject o = ContextInjectionFactory.make(TestResourcesBundleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.resourcesMessages);
		assertNotNull(o.resourcesMessages.message);
		assertNotNull(o.resourcesMessages.message_one);
		assertNotNull(o.resourcesMessages.messageOne);
		assertNotNull(o.resourcesMessages.message_two);
		assertNotNull(o.resourcesMessages.messageThree);
		assertNotNull(o.resourcesMessages.messageFour);

		//test the set values
		assertEquals("ResourceNachricht", o.resourcesMessages.message);
		assertEquals("ResourceNachrichtUnderscore", o.resourcesMessages.message_one);
		assertEquals("ResourceNachrichtCamelCase", o.resourcesMessages.messageOne);
		assertEquals("ResourceNachrichtUnderscoreDot", o.resourcesMessages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("ResourcesMessageCamelCaseDot", o.resourcesMessages.messageThree);
		assertEquals("Die Idee ist von Tom", o.resourcesMessages.messageFour);
	}
	
	public void testResourcesBundleSkipDefaultLocaleForEquinoxRoot() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));

		//set the locale to en
		//as there is no _en properties file, by default the _de properties file would be loaded and we would
		//get german translations as the default locale is set to "de_DE"
		//with checking the equinox.root.locale in the system properties the fallback is skipped as it tells
		//that the root properties file is for locale en.
		getOrCreateContext().set(TranslationService.LOCALE, "en");
		TestResourcesBundleObject o = ContextInjectionFactory.make(TestResourcesBundleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.resourcesMessages);
		assertNotNull(o.resourcesMessages.message);
		assertNotNull(o.resourcesMessages.message_one);
		assertNotNull(o.resourcesMessages.messageOne);
		assertNotNull(o.resourcesMessages.message_two);
		assertNotNull(o.resourcesMessages.messageThree);
		assertNotNull(o.resourcesMessages.messageFour);

		//not the default resource bundle should be used (e.g. de when running on a machine with german OS) 
		//but the default resource bundle
		assertEquals("ResourcesMessage", o.resourcesMessages.message);
		assertEquals("ResourcesMessageUnderscore", o.resourcesMessages.message_one);
		assertEquals("ResourcesMessageCamelCase", o.resourcesMessages.messageOne);
		assertEquals("ResourcesMessageUnderscoreDot", o.resourcesMessages.message_two);
		assertEquals("ResourcesMessageCamelCaseDot", o.resourcesMessages.messageThree);
		assertEquals("The idea is from Tom", o.resourcesMessages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}
	
	public void testResourcesBundleUseDefaultLocaleForInvalidLocale() {
		Locale defaultLocaleBefore = Locale.getDefault();
		//change the default Locale for this testcase
		Locale.setDefault(new Locale("de"));
		
		//set a locale for which no resource bundle is set
		getOrCreateContext().set(TranslationService.LOCALE, "fr");
		TestResourcesBundleObject o = ContextInjectionFactory.make(TestResourcesBundleObject.class, getOrCreateContext());
		
		//test all values are set
		assertNotNull(o.resourcesMessages);
		assertNotNull(o.resourcesMessages.message);
		assertNotNull(o.resourcesMessages.message_one);
		assertNotNull(o.resourcesMessages.messageOne);
		assertNotNull(o.resourcesMessages.message_two);
		assertNotNull(o.resourcesMessages.messageThree);
		assertNotNull(o.resourcesMessages.messageFour);

		//the default resource bundle should be used
		assertEquals("ResourceNachricht", o.resourcesMessages.message);
		assertEquals("ResourceNachrichtUnderscore", o.resourcesMessages.message_one);
		assertEquals("ResourceNachrichtCamelCase", o.resourcesMessages.messageOne);
		assertEquals("ResourceNachrichtUnderscoreDot", o.resourcesMessages.message_two);
		//for messageThree there is no key specified in de properties file, so there should be the fallback
		assertEquals("ResourcesMessageCamelCaseDot", o.resourcesMessages.messageThree);
		assertEquals("Die Idee ist von Tom", o.resourcesMessages.messageFour);
		
		//reset the default locale
		Locale.setDefault(defaultLocaleBefore);
	}

}