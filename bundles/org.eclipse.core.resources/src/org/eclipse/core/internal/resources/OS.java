package org.eclipse.core.internal.resources;
import java.util.Arrays;
import org.eclipse.core.boot.BootLoader;

/**
 * Captures platform specific attributes relevant to the core resources plugin.  This
 * class is not intended to be instantiated.
 */
public abstract class OS {
	private static final String INSTALLED_PLATFORM;
	
	public static final char[] INVALID_RESOURCE_CHARACTERS;
	public static final String[] INVALID_RESOURCE_NAMES;	
	
static {
	//find out the OS being used
	//setup the invalid names
	char[] chars = null;
	String[] names = null;
	INSTALLED_PLATFORM = BootLoader.getOS();
	if (INSTALLED_PLATFORM.equals(BootLoader.OS_WIN32)) {
			//list taken from http://support.microsoft.com/support/kb/articles/q177/5/06.asp
			chars = new char[]{'\\', '/', ':', '*', '?', '"', '<', '>', '|'};
			
			//list taken from http://support.microsoft.com/support/kb/articles/Q216/6/54.ASP
			names = new String[] {"aux", "clock$", "com1", "com2", "com3", "com4", 
				"com5", "com6", "com7", "com8", "com9", "con", "lpt1", "lpt2", 
				"lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "nul", "prn"};
	} else {
			//only front slash and null char are invalid on UNIXes
			//taken from http://www.faqs.org/faqs/unix-faq/faq/part2/section-2.html
			//backslash and colon are illegal path segments regardless of filesystem.
			chars = new char[] {'\\', '/', ':', '\0',};
	}
	INVALID_RESOURCE_CHARACTERS = chars == null ? new char[0] : chars;
	INVALID_RESOURCE_NAMES = names == null ? new String[0] : names;
}
/**
 * Returns true if the given name is a valid resource name on this operating system,
 * and false otherwise.
 */
public static boolean isNameValid(String name) {
	if (INSTALLED_PLATFORM.equals(BootLoader.OS_WIN32)) {
		//on windows, filename suffixes are not relevant to name validity
		int dot = name.indexOf('.');
		name = dot == -1 ? name : name.substring(0, dot);
		return Arrays.binarySearch(INVALID_RESOURCE_NAMES, name.toLowerCase()) < 0;
	}
	return true;
}
}

