@SET SCRIPTPATH=%~dp0
@java -cp "%SCRIPTPATH%*;%SCRIPTPATH%..\lib\*;%SCRIPTPATH%..\config" -Dapp.path="%SCRIPTPATH:~0,-5%" org.portland.sqltorest.SqlToRest