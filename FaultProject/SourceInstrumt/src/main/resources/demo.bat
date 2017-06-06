@echo off  
set base=%1
set output=%2
:::set baseLength=length $base

echo 'base: '%base%
echo 'output: '%output%
for /r  %base%  %%i IN (*) do (
srcml.exe %%i -o %%i.xml  --src-encoding utf-8
)
goto:eof  
