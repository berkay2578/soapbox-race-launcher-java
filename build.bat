@echo off
del releases /Q
cmd /c mvn clean compile assembly:single -Px64windows -DskipDevelopment >nul 2>&1
echo Built for windows-x64.
cmd /c mvn clean compile assembly:single -Px64mac -DskipDevelopment >nul 2>&1
echo Built for mac-x64.
cmd /c mvn clean compile assembly:single -Px64unix -DskipDevelopment >nul 2>&1
echo Built for unix-x64.
cmd /c mvn clean compile assembly:single -Px86windows -DskipDevelopment >nul 2>&1
echo Built for windows-x86.
cmd /c mvn clean compile assembly:single -Px86unix -DskipDevelopment >nul 2>&1
echo Built for unix-x86.
cmd /c mvn clean compile assembly:single -Px86solaris -DskipDevelopment >nul 2>&1
echo Built for solaris-x86.
echp Finished.
@pause