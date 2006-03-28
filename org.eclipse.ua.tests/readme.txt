
About:
------

This plugin contains automated tests for the User Assistance component of eclipse,
except help, which is in org.eclipse.help.tests. This includes cheatsheets and
intro (welcome).

Running the tests:
------------------

- Right click on org.eclipse.ua.tests.AllTests in the base source folder
- Select Run As -> JUnit Plugin Test

Updating model tests:
---------------------

There are several sets of parser tests which load a model in memory, then serialize
to text. Then it compares with a pre-generated serialization that is known to be
correct (the _expected.txt) files. If changes are made to the model, both the
serializer and the saved results must be updated. There are special JUnit tests that
are not part of the suites that are used to regenerate the result files to test on,
e.g:

- org.eclipse.ua.tests.intro.util.IntroModelSerializerTest
- org.eclipse.ua.tests.cheatsheet.util.CheatSheetModelSerializerTest

To regenerate the results, simply run the serializer tests individually, then 
refresh your workspace (select folder, hit F5 - it changes the files in the
filesystem).

Note: It is done as a test because we want to ensure that the environment is
identical for the serializer and the test.
