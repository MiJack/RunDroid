@echo off  
set base=%1
set output=%2
:::set baseLength=length $base

echo 'base: '%base%
echo 'output: '%output%
call:funListFile base
goto:eof  

::::::::::::::::::::::::::::::::::::::::::::::::
:funFile
  echo "find a file %1"
  string=%1
  echo %string%
  srcml.exe %1 -o %1.xml
goto:eof

:::::::param 1：source
:funListFile
  for file in %1/*
:::::::遍历输入目标文件夹下的file
  do
  if test -f $file
  then
    call :funFile $file
  fi
  if test -d $file
    then
    call :funListFile $file
  fi
  done
goto:eof