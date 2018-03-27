#!/bin/sh



success=0
skip=true

#compile core

cd ../blerter-core
./script/compile.sh

cd ../blerter-vaadin-service

case "$1" in
		test)
		cp resources/test/application.yml src/main/resources/application.yml
		skip=false
		;;
		*)
		cp resources/application.yml src/main/resources/application.yml
		;;
esac

#checking code standard
echo "validating codebase {checkstyle}..."
mvn  checkstyle:check
status=$?
if [ $status -eq 1 ]; then
	success=1
	break
fi

echo "validating codebase {pmd}..."
mvn pmd:check 
status=$?
if [ $status -eq 1 ]; then
	success=1
	break
fi
    	
mvn clean
status=$?
if [ $status -eq 0 ]; then
  	mvn package install -Dmaven.test.skip=$skip
  	status=$?
	if [ $status -eq 1 ]; then
		success=1
	fi
else
	success=1
	break
fi
echo "		code base:$source done ..  "
if [ $success -eq 0 ]; then

 	echo "---------------------------------------------------"
	echo "Vaadin Service compiled successful ..."
	echo "---------------------------------------------------"
else
	echo "---------------------------------------------------"
	echo "Vaadin Service compiled error ..."
	echo "---------------------------------------------------"
fi
