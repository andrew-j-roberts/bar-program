@echo off
REM ======================================================================
REM   Run JCSMP sample applications
REM
REM   Copyright 2004-2019 Solace Corporation. All rights reserved.
REM ======================================================================

echo Copyright 2004-2019 Solace Corporation. All rights reserved.

if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_JCSMP_HOME=%~dp0..

if "%JCSMP_HOME%"=="" set JCSMP_HOME=%DEFAULT_JCSMP_HOME%
set DEFAULT_JCSMP_HOME=

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set JCSMP_CMD_LINE_ARGS=%1
if ""%1""=="""" goto errorMsg
set FOUND=1
for %%i in (introsamples.AdPubAck introsamples.AsyncCacheRequest introsamples.BlockingSubscriber introsamples.DirectPubSub introsamples.DtoPubSub introsamples.EventMonitor introsamples.intro.HelloWorldPub introsamples.intro.HelloWorldQueuePub introsamples.intro.HelloWorldQueueSub introsamples.intro.HelloWorldSub introsamples.MessageSelectorsOnQueue introsamples.MessageTTLAndDeadMessageQueue introsamples.NoLocalPubSub introsamples.PerfSempRequest introsamples.QueueProvisionAndBrowse introsamples.QueueProvisionAndRequestActiveFlowIndication introsamples.Replication introsamples.RRDirectReplier introsamples.RRDirectRequester introsamples.RRGuaranteedReplier introsamples.RRGuaranteedRequester introsamples.SDTPubSubMsgIndep introsamples.SecureSession introsamples.SempGetOverMB introsamples.SempHttpSetRequest introsamples.SempPagingRequests introsamples.SimpleFlowToQueue introsamples.SimpleFlowToTopic introsamples.SubscribeOnBehalfOfClient introsamples.SyncCacheRequest introsamples.TopicToQueueMapping introsamples.Transactions) do (
  if ""%%i""==""%1"" set FOUND=0
)

if "%FOUND%"=="1" goto errorMsg

shift
:setupArgs
if ""%1""=="""" goto doneStart
set JCSMP_CMD_LINE_ARGS=%JCSMP_CMD_LINE_ARGS% %1
shift
goto setupArgs
rem This label provides a place for the argument list loop to break out 
rem and for NT handling to skip to.

:doneStart
set _JAVACMD=%JAVACMD%
set LOCALCLASSPATH=%JCSMP_HOME%\classes;%CLASSPATH%
for %%i in ("%JCSMP_HOME%\..\lib\*.jar") do call "%JCSMP_HOME%\bin\lcp.bat" %%i

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto run

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

:run

if "%JCSMP_OPTS%" == "" set JCSMP_OPTS=-Xmx128M

REM Uncomment to enable remote debugging
REM SET JCSMP_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005

REM Uncomment to enable logging:
REM set LOCALCLASSPATH=%JCSMP_HOME%\config;%LOCALCLASSPATH%
REM for %%i in ("%JCSMP_HOME%\..\lib\optional\*.jar") do call "%JCSMP_HOME%\bin\lcp.bat" %%i

"%_JAVACMD%" %JCSMP_DEBUG_OPTS% %JCSMP_OPTS% -classpath "%LOCALCLASSPATH%" com.solacesystems.jcsmp.samples.%JCSMP_CMD_LINE_ARGS%
goto end

:errorMsg

echo Expecting one of the following as the first argument:
echo introsamples.AdPubAck
echo introsamples.AsyncCacheRequest
echo introsamples.BlockingSubscriber
echo introsamples.DirectPubSub
echo introsamples.DtoPubSub
echo introsamples.EventMonitor
echo introsamples.intro.HelloWorldPub 
echo introsamples.intro.HelloWorldQueuePub 
echo introsamples.intro.HelloWorldQueueSub 
echo introsamples.intro.HelloWorldSub
echo introsamples.MessageReplay
echo introsamples.MessageSelectorsOnQueue
echo introsamples.MessageTTLAndDeadMessageQueue
echo introsamples.NoLocalPubSub
echo introsamples.PerfSempRequest
echo introsamples.QueueProvisionAndBrowse
echo introsamples.QueueProvisionAndRequestActiveFlowIndication
echo introsamples.Replication
echo introsamples.RRDirectReplier
echo introsamples.RRDirectRequester
echo introsamples.RRGuaranteedReplier
echo introsamples.RRGuaranteedRequester
echo introsamples.SDTPubSubMsgIndep
echo introsamples.SecureSession
echo introsamples.SempGetOverMB
echo introsamples.SempHttpSetRequest
echo introsamples.SempPagingRequests
echo introsamples.SimpleFlowToQueue
echo introsamples.SimpleFlowToTopic
echo introsamples.SubscribeOnBehalfOfClient
echo introsamples.SyncCacheRequest
echo introsamples.TopicToQueueMapping
echo introsamples.Transactions
goto end

:end
set LOCALCLASSPATH=
set _JAVACMD=
set JCSMP_CMD_LINE_ARGS=

if "%OS%"=="Windows_NT" @endlocal

:mainEnd

