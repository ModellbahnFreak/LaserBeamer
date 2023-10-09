@echo off

echo Starting LaserBeamer on loop
:start

echo (Re-)Starting now

CALL LaserBeamer.bat -f %*

echo LaserBeamer terminated.
echo ----------------------------------------
echo Do you want to terminate the loop? Press CRTL+C or Y within the next two seconds.
echo Otherwise LaserBeamer will be relaunched

CHOICE /T 2 /C YN /D N
set _e=%ERRORLEVEL%
if %_e%==1 echo Y&goto :end
if %_e%==2 echo N&goto :start
echo Error while waiting for input.
echo %_e%

:end
echo Terminating.