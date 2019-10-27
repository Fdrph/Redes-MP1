JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	client.java \
	server.java \
	mtServer.java \
	myRequest.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class