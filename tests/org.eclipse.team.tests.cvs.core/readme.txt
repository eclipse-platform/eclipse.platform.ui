README for org.eclipse.core.team.tests.cvs.core

This plug-in provides the CVS tests. It also contains the script, test.xml, 
which launches the CVS automated tests after the a build.
In order to run the tests, perform the following steps:

1. Load the eclipse test harness plug-ins and fragments

2. Load the following plug-ins:
	org.eclipse.team.tests.cvs.core
	
3. Modify the repository.properties file in plug-in 
org.eclipse.team.tests.cvs.core to contain the 
information required to connect to your repository.
Important fields in the repository properties file
are:

 repository - the location string that identifies your test repository
 initrepo - true if you want to initialize the repository before beginning
 rsh - the rsh (or ssh) client used to initialize the repository
 
The rsh field is similar to the CVS_RSH environment variable

Your repository must allow rsh connections from your userid and machine in order for
the tests to run.

4. Run the test.xml Ant script in Eclipse