CFLAGS:=-c -Wall -Weffc++ -std=c++11 -g
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread

all: bin/encoder.o EchoClient
	g++ -o bin/echoExample bin/encoder.o bin/connectionHandler.o bin/echoClient.o $(LDFLAGS) 

EchoClient: bin/connectionHandler.o bin/echoClient.o
	
bin/connectionHandler.o: src/echoClient/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/echoClient/connectionHandler.cpp

bin/echoClient.o: src/echoClient/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient/echoClient.cpp
	
bin/encoder.o: src/encoder/encoder.cpp
	g++ $(CFLAGS) -o bin/encoder.o src/encoder/encoder.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
