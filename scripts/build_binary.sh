#!/bin/sh

#
# Copyright 2015-2016 Docktitude
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

VERSION=$1
ARCHIVE_NAME=docktitude-${VERSION}-bin.tar

clean_build () {
  if [ -f /tmp/docktitude.txt ]; then
    rm /tmp/docktitude.txt
  fi
}

if [ -f build/libs/docktitude-*-all.jar ]; then
  clean_build
  sed 's|$JAVA_PATH -jar /usr/lib/docktitude/docktitude.jar $@|exec "$JAVA_PATH" -jar $0 "$@"|' scripts/docktitude > /tmp/docktitude.txt
  cat /tmp/docktitude.txt build/libs/docktitude-*-all.jar > build/distributions/docktitude
  tar cfC build/distributions/${ARCHIVE_NAME} build/distributions docktitude && gzip -f build/distributions/${ARCHIVE_NAME}
  md5sum build/distributions/${ARCHIVE_NAME}.gz | awk '{ print $1 }' > build/distributions/${ARCHIVE_NAME}.gz.MD5
  rm build/distributions/docktitude
  clean_build
fi
