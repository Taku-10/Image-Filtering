JAVAC=/usr/bin/javac
.SUFFEXES: /java .class
SRCDIR=src
BINDIR=bin

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<

CLASSES=MeanFilterSerial.class MedianFilterSerial.class MeanFilterParallel.class MedianFilterParallel.class
CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)

default: $(CLASS_FILES)
clean:
	rm $(BINDIR)/*.class
run1:	$(CLASS_FILES)
	java -cp bin MeanFilterSerial
run2:	 $(CLASS_FILES)
	java -cp bin MedianFilterSerial
run3:	 $(CLASS_FILES)
	java -cp bin MedianFilterParallel
run4:	 $(CLASS_FILES)
	java -cp bin MeanFilterParallel


