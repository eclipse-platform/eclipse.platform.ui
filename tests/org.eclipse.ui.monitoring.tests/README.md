org.eclipse.ui.tests
====================

Contains the tests org.eclipse.ui.monitoring plugin. 

For more information, refer to the [Platform UI wiki page] [1].


Running the tests
-----------------

Use the following command to run the tests via Maven:
mvn clean verify -Pbuild-individual-bundles -Dmaven.test.skip=false


See [https://wiki.eclipse.org/Platform_UI/How_to_Contribute#Unit_Testing][2] for more information.

License
-------

[Eclipse Public License (EPL) v1.0][3]



[1]: http://wiki.eclipse.org/Platform_UI
[2]: https://wiki.eclipse.org/Platform_UI/How_to_Contribute#Unit_Testing
[3]: http://wiki.eclipse.org/EPL
