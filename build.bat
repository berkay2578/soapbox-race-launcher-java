del releases /Q
cmd /c mvn clean compile assembly:single -Px64windows -DskipDevelopment
cmd /c mvn clean compile assembly:single -Px64mac -DskipDevelopment
cmd /c mvn clean compile assembly:single -Px64unix -DskipDevelopment
cmd /c mvn clean compile assembly:single -Px86windows -DskipDevelopment
cmd /c mvn clean compile assembly:single -Px86unix -DskipDevelopment
cmd /c mvn clean compile assembly:single -Px86solaris -DskipDevelopment
pause