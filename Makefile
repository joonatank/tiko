# Makefile

TARGET = tiko/TikoMain.class

SOURCES = tiko/TikoMain.java tiko/User.java tiko/BookInfo.java

all : $(TARGET)

.PHONY: clean

clean:
	rm tiko/*.class

CMD = javac

$(TARGET):$(SOURCES)
	${CMD} $(SOURCES)

