/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.standalone;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;


/**
 * This is the standalone help proxy class. It takes care of 
 * launching the eclipse help system under cover and communicating with it.
 * 
 * Usage: 
 * <ul>
 * <li> create an instantance of this class by passing the plugins directory, and then hold onto 
 * this instance for the duration of your application</li>
 * <li> call start() </li>
 * <li> call displayHelp(...) or displayContext(..) any number of times </li>
 * <li> at the end, call shutdown(). </li>
 * </ul>
 */
public class Help
{
	private Eclipse eclipse;
	private String pluginsDir;
	
	/**
	 * Constructor
	 */
	public Help(String pluginsDir)
	{
		this.pluginsDir = pluginsDir;
	}
	
	/**
	 * Starts the help system. To be called once only.
	 */
	public void start()
	{
		try
		{
			eclipse = new Eclipse(pluginsDir, null);
		}
		catch(Exception e)
		{
		}
	}
	
	/**
	 * Shuts-down the help system . To be called once, at the end. 
	 * Do not call other methods after shutdown is called.
	 */
	public void shutdown()
	{
		if (eclipse != null)
				eclipse.shutdown();
	}
	
	/**
	 * Displays help
	 */
	public void displayHelp()
	{
		displayHelp(null);
	}
	
	/**
	 * Displays specified help resource
	 * @param toc the href of the table of contents
	 * @param topic the topic url to display
	 */
	public void displayHelp(String href)
	{
		try
		{
			Boolean b = eclipse.displayHelp(href);
		}
		catch(Exception e)
		{
			System.out.println("Could not display help: " + href );
		}
	}
	
	/**
	 * Displays context sensitive help
	 * @param id context id
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public void displayContext(String contextId, int x, int y)
	{
		try
		{
			Boolean b = eclipse.displayContext(contextId,x, y);
		}
		catch(Exception e)
		{
			System.out.println("Could not display  context:"  +contextId);
		}
	}
	
	/**
	 * Displays context sensitive help in infopop
	 * @param id context id
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public void displayContextInfopop(String contextId, int x, int y)
	{
		try
		{
			Boolean b = eclipse.displayContextInfopop(contextId,x, y);
		}
		catch(Exception e)
		{
			System.out.println("Could not display  context:"  +contextId);
		}
	}
	
	public static void main(String[] args)
	{
		// Test
		String plugins = "d:\\eclipse\\plugins";
		if (args.length > 0)
			plugins = args[0];
		final Help help = new Help(plugins);
		help.start();
		
		final Frame f = new Frame();
		Panel p = new Panel();
		Button b1 = new Button("context help");
		Button b2 = new Button("help");
		
		ActionListener a1 = new ActionListener()
		{
			/*
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e)
			{
				help.displayContext("org.eclipse.help.ui.f1Shell", 200, 800);
			}

		};

		ActionListener a2 = new ActionListener()
		{
			/*
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e)
			{
				help.displayHelp();
			}

		};
		
		f.addWindowListener(new WindowAdapter()
		{
			/*
			 * @see WindowListener#windowClosed(WindowEvent)
			 */
			public void windowClosed(WindowEvent e)
			{
				f.dispose();
				help.shutdown();
			}
		});
		
		b1.addActionListener(a1);
		b2.addActionListener(a2);
		
		f.add(p);
		p.add(b1);
		p.add(b2);
		
		f.pack();
		
		f.show();
		
		/*
		// Test
		Help help = new Help("d:\\eclipse\\plugins");
		help.start();
		help.displayContext("org.eclipse.help.ui.f1Shell", 200, 800);
		help.displayContext("org.eclipse.help.ui.f1Shell", 600, 800);
		//help.displayHelp();
		 */
		/*
		try
		{
		Thread.sleep(20000);
		}catch(InterruptedException e) {}
		*/
		try
		{
		System.in.read();
		}catch(Exception ex){}
		help.shutdown();
	}
}
