@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Maven Wrapper Startup Script for Windows
@setlocal
set "MAVEN_PROJECTBASEDIR=%~dp0.."
set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "MVNW_VERBOSE=false"

if not defined JAVA_HOME (
  for %%i in (java) do set "JAVACMD=%%~$PATH:i"
) else (
  set "JAVACMD=%JAVA_HOME%\bin\java.exe"
)

set "MVNW_REPOURL=https://repo.maven.apache.org/maven2"
set "MVNW_VER=3.3.2"

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

set "MVN_CMD=mvnw"
set "MAVEN_OPTS=-Xmx1024m"
java -cp "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %MAVEN_OPTS% %* 2>&1
