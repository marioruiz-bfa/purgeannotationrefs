HOW TO USE
----------

<!-- load the task -->

<taskdef classpath="path/to/par_ant-@version@-nodeps.jar" resource="org/dyndns/fichtner/purgeannotationrefs/tasks/antlib.xml" />


<!-- remove all references to com.example.AnnoName -->

	<purgeannotationrefs>
		<src>
			<fileset dir="../path/to/classes">
				<filename name="**/*.class" />
			</fileset>
		</src>
		<remove annotation="com.example.AnnoName" />
	</purgeannotationrefs>


<!-- remove all references to com.example.AnnoName from methods and constructors only -->

	<purgeannotationrefs>
		<src>
			<fileset dir="../path/to/classes">
				<filename name="**/*.class" />
			</fileset>
		</src>
		<remove annotation="com.example.AnnoName" from="METHOD" />
		<remove annotation="com.example.AnnoName" from="CONSTRUCTOR" />
	</purgeannotationrefs>
