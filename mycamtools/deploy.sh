#!/bin/sh
dest=$1
if [[ a$1 == a ]]
then
  dest=../../../tomcat/webapps
fi

rsync -r --exclude .svn files/ $dest


