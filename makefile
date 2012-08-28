# ThingML-Editor Makefile v0.1
all:
	compile make-jar run

compile:
	cd ./ThingMLDemo && ant compile

make-jar:
	cd ./ThingMLDemo && ant make-jar

run:
	java -jar ./ThingMLDemo/dist/language_support.jar

