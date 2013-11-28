package org.eclipse.e4.core.internal.tests.nls;

import java.text.MessageFormat;

import javax.annotation.PostConstruct;

import org.eclipse.e4.core.services.nls.Message;

/**
 * Load messages from a resource folder in the plugin
 */
@Message(contributorURI="platform:/plugin/org.eclipse.e4.core.tests/resources/another")
public class ResourcesMessages {
	
	//message as is
	public String message;
	
	//message as is with underscore
	public String message_one;
	
	//message as is camel cased
	public String messageOne;
	
	//message with underscore transformed to . separated properties key
	public String message_two;
	
	//camel cased message transformed to . separated properties key 
	public String messageThree;

	//message with placeholder
	public String messageFour;
	
	@PostConstruct
	public void format() {
		messageFour = MessageFormat.format(messageFour, "Tom"); //$NON-NLS-1$
	}
}
