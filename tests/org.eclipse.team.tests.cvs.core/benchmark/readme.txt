README Benchmark Tests
======================

This plugin provides automated benchmark tests and related support
programs to locate regressions or improvements between different versions
of the CVS Team Provider.

For now, all of the tests are targeted towards typical UI workflows.
This need not be the case.  Should it be necessary, benchmark tests can
easily be written for lower level core components as well.



Deploying the Tests
===================

1. If you have a pre-built plugin available, then you do not need to rebuild
   the org.eclipse.team.* plugins.  Otherwise you must build these as usual,
   but you do not need to create a JAR file.  Having all of the compiled
   classes in the 'bin' directory of each plugin is sufficient.

2. Checkout the matching version of the org.eclipse.team.tests.cvs.core source
   for your org.eclipse.team.* plugins.  Ensure all dependencies have been
   satisfied.  Compile as usual.
   
3. Checkout and compile org.eclipse.core.tests.harness.
   
4. On the target machine, install a matching version of Eclipse in a dedicated
   test directory.  Also install a suitable JRE for the platform into that
   directory (should always choose the same one).  

   If you are using Windows, then copy the "teamui_benchmark.bat" script to the
   directory.  For other platforms, you will need to cook up your own script.
   You will probably want to change the definition of the "ROOT" variable and
   other options before running the tests.
   
   Copy the template "repository.properties" file to the test directory.  Fill it in.
   
   Replace the existing org.eclipse.team.* plugins in the eclipse/plugins
   directory with the new ones.  Also install org.eclipse.core.tests.harness
   there.  In the typical case, this just means copying or exporting the projects
   directly from the workspace where you compiled them to the new location.
   
   The test directory structure should look like this:
     + mytestdirectory/
       - teamui_benchmark.bat
       - repository.properties
         + jre/
           ... a suitable java runtime ...
         + eclipse/
           - install/
           - readme/
           - splash/
           - buildmanifest.properties
           - startup.jar
           + plugins/
             - org.eclipse.core.tests.harness/
             - org.eclipse.team.core/
             - org.eclipse.team.ui/
             - org.eclipse.team.cvs.core/
             - org.eclipse.team.cvs.ui/
             - org.eclipse.team.tests.core/
             - org.eclipse.team.tests.cvs.core/
             - org.junit/
             ... and all of the other required Eclipse plugins ...
            
5. From a command shell, run the script.  Grab a coffee.



Running or Debugging the Tests from within Eclipse
==================================================

1. Checkout and compile the necessary projects (see above).

2. Using the PDE launcher, run the "org.eclipse.team.tests.cvs.core.harness"
   application with the following arguments:
   
   VM Arguments:
     -Declipse.cvs.properties=<location of your repository.properties file>
     
   Program arguments:
     -test <suite>   : id of suite to run (must be plugged into extension point)
                       [see plugin.xml file for the list of available tests]
     -log <file>     : specify a file for logging
     -nolog          : do not write a log file
     -repeat <n>     : number of iterations to run
     -ignorefirst    : ignore (do not record) results from first iteration
     -purge          : purge all projects from the workspace before each iteration
     <anything else> : passed verbatim to the org.eclipse.ui.workbench application



Inspecting the Output
=====================

1. Checkout and compile the org.eclipse.team.tests.cvs.core project.

2. Note that the log formatting tools require org.apache.xerces to be on the
   classpath when they are run.  They do not require any other Eclipse
   components, however.
   
3. Run any of the following Java programs:

   org.eclipse.team.tests.ccvs.ui.logformatter.PrintAverageMain
   ------------------------------------------------------------
   
   Synopsis:
     Prints the average of the output of all runs contained in a particular
     XML log file.  It is not possible to average runs in multiple log files
     at once without merging the files together on disk.  [Strip the closing
     tag of the first file, and the opening tag of the second file, then
     append the second file to the first]
   
   Program arguments:
     <log> : the path of the log file to print


   org.eclipse.team.tests.ccvs.ui.logformatter.PrintRawMain
   --------------------------------------------------------
   
   Synopsis:
     Prints the raw output of each individual run contained in a particular
     XML log file without summarizing the data in any way.

   Program arguments:
     <log> : the path of the log file to print


   org.eclipse.team.tests.ccvs.ui.logformatter.PrintDiffMain
   ---------------------------------------------------------

   Synopsis:
     Prints the difference between the average of all runs contained
     in one XML log file (the newer one) and the average of all runs
     contained in another XML log file (the older one).  This makes it
     possible to locate regressions or improvements between versions.
     
   Program arguments:
     <newer log> : the path of the "newer" log file
     <older log> : the path of the "older" log file
     -t <thresh> : specify the minimum non-negligible absolute difference in ms
     -i          : ignore negligible changes in results [filter them out]



What is Being Logged
====================

At the present date the following information is logged for each test run:
  - current time
  - current SDK build number
  - for JUnit test cases:
    - test name
    - fully qualified class name
  - for groups of benchmark tasks:
    - name
  - for benchmark tasks:
    - name
    - elapsed time
  - for exceptions and errors:
    - type of error (warning, error, failure)
    - error message
    - stack trace, if applicable
    - printout of IStatus contents, if applicable



NOTES
=====

Exceptions and errors are not reported through the log formatting tools yet
since it is difficult to determine automatically which benchmark tasks are
affected (directly or indirectly) by the error.  For this reason, you should
MANUALLY INSPECT the generated XML log files and search for elements with
the name "abort".  Since running the test cases is time consuming, it
may be better to trim out any affected cases from the log rather than to run
the whole suite over once again.
