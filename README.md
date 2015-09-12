# purgeannotationrefs

Remove references to annotations out of the java bytecode/classfiles (remove the @Anno tag from annotated elements). 
Now you can use annotations to check constellations in bytecode after compilation but purge the used annotations before releasing the jars.

There are three modules: 
- The library doing the bytecode transformation (could be included in Java projects)
- An Ant Task to include the bytecode transformation in your Ant build
- A Maven Module to include the bytecode transformation in your Maven build
