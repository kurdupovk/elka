# elka

Run with debug
mvn clean install exec:exec -Dexec.args="-classpath %classpath -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n com.elka.Main"
