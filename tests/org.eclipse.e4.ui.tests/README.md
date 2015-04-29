org.eclipse.e4.ui.tests
=======================

This plug-in contains the test for the e4.ui related API.

Setup
-----

This plug-in has a dependency to Mockito and Hamcrest from the Orbit repository.
See [1] for a detailed description on how to setup a workspace from scratch. 
If you want to install the necessary plug-ins manually, use one of the available update sites listed in [2]

[1] https://wiki.eclipse.org/Platform_UI/How_to_Contribute#2._Install_the_development_tools
[2] http://download.eclipse.org/tools/orbit/downloads/

Running the tests
-----------------

Use the following command to run the tests via Maven:
mvn clean verify -Pbuild-individual-bundles -DskipTests=false

See [https://wiki.eclipse.org/Platform_UI/How_to_Contribute#Unit_Testing][2] for more information.

Contributions
-------------

For information how to contribute to the Platform UI project see [Platform UI - How to contribute wiki page] [3].

License
-------

[Eclipse Public License (EPL) v1.0][4]
