#!/bin/sh
base=$1
output=$2
baseLength=length $base

funFile(){
  echo "find a file $1"
  string=$1
  echo ${string}
#  expr substr "$string" 1 3  # 从第一个位置开始取3个字符， abc
 # expr substr "$string" $baseLength
# fileName=${string##*/}
srcml $1 -o $1.xml
}

# param 1：source
funListFile(){
  for file in $1/*
  # 遍历输入目标文件夹下的file
  do
  if test -f $file
  then
    funFile $file
  fi
  if test -d $file
    then
    funListFile $file
  fi
  done
}

echo 'base: '$base
echo 'output: '$output
funListFile $base