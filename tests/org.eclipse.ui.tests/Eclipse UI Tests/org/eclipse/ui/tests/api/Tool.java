package org.eclipse.ui.tests.api;

import java.util.Random;
import org.eclipse.core.resources.*;
import java.io.*;

public class Tool {
	/**
	 * bit masks for events
	 */
	public static final int 
		NONE = 0x00,
		OPEN = 0x01,
		CLOSED = 0x02,
		ACTIVATED = 0x04,
		CHANGED = 0x08;

	public static String TextEditorID = "org.eclipse.ui.DefaultTextEditor";	
	public static String WBE = "WorkbenchException";

	public static String FakeID =
		"Is this fake ID for buying beer or getting into a club?",
		FakeLabel =
			"Hmmm. Who's to say this is a fake label? I mean, someday, this can be a real label.",
		UnknownFileName[] =
			{
				"This is not a known file name to the world.with a cool extension",
				"this is not the same as the other.anyways this is a file extension" },
		KnownFileName[] = { "plugin.xml" },
		ProjectName = "JUnitTestingProjectName";

	private static String DEL_ERROR = "couldn't delete ";
	private static Random randomBox = new Random();

	private Tool()
	{
	}
	
	public static Object pick(Object[] array) {
		int num = randomBox.nextInt(array.length);
		return array[num];
	}

	public static boolean check(Object[] array) {
		if (array == null)
			return false;
		else {
			for (int i = 0; i < array.length; i++)
				if (array[i] == null)
					return false;
			return true;
		}
	}

	public static boolean arrayHas(Object[] array, Object element) {
		if (array == null)
			return false;
		else {
			for (int i = 0; i < array.length; i++)
				if( array[ i ] == element )
					return true;
			return false;
		}
	}

	public static IFile getIFile(String name) throws Throwable {
		File localFile = new File(name);

		localFile.createNewFile();
		System.out.println(localFile.getPath());
		FileInputStream is = new FileInputStream(localFile);

		IFile coreFile =
			ResourcesPlugin.getWorkspace().getRoot().getProject(ProjectName).getFile(name);
		coreFile.create(is, false, null);

		return coreFile;
	}

	public static String pickKnownFileName() {
		return KnownFileName[randomBox.nextInt(KnownFileName.length)];
	}

	public static String pickUnknownFileName() {
		return UnknownFileName[randomBox.nextInt(UnknownFileName.length)];
	}

	public static boolean equals(Object[] one, Object[] two) {
		if (one.length != two.length)
			return false;
		else {
			for (int i = 0; i < one.length; i++)
				if (one[i] != two[i])
					return false;
			return true;
		}
	}

	public static void delete(File r) {
		if (r != null) {
			File[] children = r.listFiles();

			if (children != null) {
				int total = children.length;
				for (int i = 0; i < total; i++)
					if (children[i].isDirectory())
						delete(children[i]);
					else {
						if (children[i].delete() == false)
							System.out.println(DEL_ERROR + r.getName());
					}
			}
			if (r.delete() == false)
				System.out.println(DEL_ERROR + r.getName());
		} else
			System.out.println("can't delete a null");
	}
	
	public static String notCaught( String e )
	{
		return e + " should have been thrown";
	}
	
	public static boolean isOpen( int bits )
	{
		return ( ( bits & OPEN ) != 0 );		
	}

	public static boolean isClosed( int bits )
	{
		return ( ( bits & CLOSED ) != 0 );		
	}
	
	public static boolean isActivated( int bits )
	{
		return ( ( bits & ACTIVATED ) != 0 );		
	}
	
	public static boolean isChanged( int bits )
	{
		return( ( bits & CLOSED ) != 0 );
	}
}