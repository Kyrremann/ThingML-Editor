# ThingML-Editor Makefile v0.1
all:
	make jar run

compile:
	cd ./ThingMLDemo && ant compile

jar:
	cd ./ThingMLDemo && ant make-jar

run:
	java -jar ./ThingMLDemo/dist/language_support.jar

