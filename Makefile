# Makefile

TARGET = tiko/TikoMain.class

SOURCES = tiko/TikoMain.java

all : $(TARGET)

.PHONY: clean

clean:
	rm tiko/*.class

CMD = javac

$(TARGET):$(SOURCES)
	${CMD} $(SOURCES)

