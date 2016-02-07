/*
 * Copyright 2015-2016 Docktitude
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.docktitude.cli;

import java.util.Arrays;
import java.util.List;

interface Command {

	String[] FIND = new String[]{"sudo", "bash", "-c", "find . -type f -name \"Dockerfile\" 2>/dev/null"};

	String[] IMAGES = new String[]{"sudo", "bash", "-c", "docker images | awk -F ' ' '{print $1\":\"$2}' | awk 'NR != 1' | sort | uniq -u | grep -v \"<none>:<none>\""};
	String[] IMAGES_IDS = new String[]{"sudo", "bash", "-c", "docker images | awk -F ' ' '{print $1\":\"$2\"+\"$3}' | awk 'NR != 1' | sort | uniq -u | grep -v \"<none>:<none>\""};

	String[] RM0 = new String[]{"sudo", "bash", "-c", "echo $(sudo docker ps -a | grep 'Exited' | awk -F ' ' '{print $1}') | awk /./"};
	String[] RM = new String[]{"sudo", "bash", "-c", "docker rm $(sudo docker ps -a | grep 'Exited' | awk -F ' ' '{print $1}')"};
	String[] RMV = new String[]{"sudo", "bash", "-c", "docker rm -v $(sudo docker ps -a | grep 'Exited' | awk -F ' ' '{print $1}')"};
	String[] RMI0 = new String[]{"sudo", "bash", "-c", "echo $(sudo docker images | grep '<none>' | awk -F ' ' '{print $3}') | awk /./"};
	String[] RMI = new String[]{"sudo", "bash", "-c", "docker rmi $(sudo docker images | grep '<none>' | awk -F ' ' '{print $3}')"};

	String[] EXPORT = new String[]{"bash", "-c", "find . -size -3000k -exec file {} \\; | grep text | cut -d: -f1 | tar -cJ -f export.tar.xz -T -"};

	List<String> SUDO = Arrays.asList("sudo", "bash", "-c");

	String PULL = "docker pull ?";
	String HISTORY = "docker history -q ?";
	String BUILD = "docker build -t ?";
	String SAVE = "docker save ? > ?.tar";

	String PREFIX = "#>";
}
