package org.eclipse.ui.tests.api;

import java.util.Random;



class Man {
	
/*	public static int getInt( int max )
//max is INCLUSIVE
	{
		Random box = new Random();
		return box.nextInt( max + 1 );
	}*/
	
	public static boolean Debug = true;
	
	public static Random randomBox = new Random();
	public static String 
		FakeID = "Bwahahahahahahahahahahahahahahahahahahahahahahahahahahahahahaha",
		FakeLabel = "This is a fake, but hard-working label.";
	
	public static Object pick( Object[] array )
	{
		int num = randomBox.nextInt( array.length );
		
		if( Debug )
			System.out.println( "total: " + array.length );	
		
		return array[ num ];
	}
}

