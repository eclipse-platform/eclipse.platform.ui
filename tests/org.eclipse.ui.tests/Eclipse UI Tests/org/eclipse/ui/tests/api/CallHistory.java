package org.eclipse.ui.tests.api;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;

public class CallHistory {	
	private ArrayList methodList;	
	
	private static CallHistory callHistoryArgument;
		
	public CallHistory()
	{		
		methodList = new ArrayList();			
	}
	
	/* 
		returns true if array contains element, false other wise
	*/
	private static boolean classHasMethod( Class c, String methodName )
	{
		Method[] methods = c.getMethods();
				
		for( int i = 0; i < methods.length; i ++ )
			if( methods[ i ].getName().equals( methodName ) )
				return true;
		return false;
	}
	
	/**
	 * @param methodName method name that is being invoked
	 */
	
	public void add( Object o, String methodName )
	{		
		if( classHasMethod( o.getClass(), methodName  ) )
			methodList.add( methodName );	
		else
			reportMethodNotFound( o, methodName );
	}
	
	private void reportMethodNotFound( Object o, String methodName )
	{
		System.out.println( "[Call History]: There is no such method named " + methodName + " in class " + o.getClass().getName() );		
	}
	
	/**
	 * clears the call history
	 */
	public void clear()
	{
		methodList.clear();
	}
	
	/**
	 * @return an array of the names of the methods invoked, in chronological order
	 */
	public String[] getHistory()
	{
		Object[] src = methodList.toArray();	
		String[] dest = new String[ src.length ];
		System.arraycopy( src, 0, dest, 0, src.length);
		return dest;
	}
	
	/**
	 * convience method that checks the methods were invoked in the order of the given array of the method names.
	 * This method does not 
	 * @methodNames an array of the names of methods in the order in which the methods are expected to be invoked
	 */
	public boolean verifyOrder( Object o, String[] methodNames ) throws IllegalArgumentException
	{
		if( methodNames.length == methodList.size() ){
			Class c = o.getClass();
			for( int i = 0; i < methodNames.length; i ++ )
				if( classHasMethod( c, methodNames[ i ] ) ){
					if( methodNames[ i ].equals( methodList.get( i ) ) == false )
						return false;
				}
				else
					return false;
			
			return true;
		}
		else 
			return false;
	}
	
	public boolean contains( Object o, String methodName )
	{
		if( classHasMethod( o.getClass(), methodName ) )
			return methodList.contains( methodName );
		else
			return false;
	}
	
	public boolean contains( Object o, String[] methodNames )
	{	
		Class c = o.getClass();	
		for( int i = 0; i < methodNames.length; i ++ ){
			if( classHasMethod( c, methodNames[ i ] ) ){
				if( methodList.contains( methodNames[ i ] ) == false )
					return false;
			}
			else
				return false;
		}
		return true;
	}
	
	public void dump()
	{
		for( int i = 0; i < methodList.size(); i ++ )
			System.out.println( methodList.get( i ) );
	}
}