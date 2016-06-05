#include <stdlib.h>
#include <boost/locale.hpp>
#include "connectionHandler.h"
#include "../encoder/utf8.h"
#include "../encoder/encoder.h"
#include <iostream>
#include <boost/thread.hpp>
#include <boost/ref.hpp>


void getAndSendFromKeyboard(ConnectionHandler& connectionHandler, bool* bShouldStop, bool* bQuitSent){

	while(!(*bShouldStop)){
		const short bufsize = 1024;
		char buf[bufsize];
		if(!(*bQuitSent)){
			std::cin.getline(buf, bufsize);
			std::string line(buf);
			int len=line.length();

			if(line == "QUIT"){
				*bQuitSent = true;
			}
			if (!connectionHandler.sendLine(line)) {
				std::cout << "Disconnected. Exiting...\n" << std::endl;
				break;
			}

			std::cout << "Sent " << len+1 << " bytes to server" << std::endl;
		}
	}

	std::cout << "send stopped" << std::endl;
}

void receive(ConnectionHandler& connectionHandler, bool* bShouldStop, bool* bQuitSent){
	while(!(*bShouldStop)){
		int len = 0;
		std::string answer;
		if (!connectionHandler.getLine(answer)) {
			std::cout << "Disconnected. Exiting...\n" << std::endl;
			break;
		}

		len=answer.length();
		answer.resize(len-1);
		std::cout << answer << std::endl;

		if(answer == "SYSMSG QUIT ACCEPTED"){
			std::cout << "accepted" << std::endl;
			//connectionHandler.close();
			*bShouldStop = true;
			break;
		}else if(answer == "SYSMSG QUIT REJECTED"){
			*bQuitSent = false;
		}
	}


}

int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    bool bShouldStop = false;
    bool bQuitSent = false;

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return -1;
    }

    boost::thread keyboardThread(getAndSendFromKeyboard, boost::ref(connectionHandler), &bShouldStop, &bQuitSent);
    
    boost::thread receiverThread(receive, boost::ref(connectionHandler), &bShouldStop, &bQuitSent);

    keyboardThread.join();
    receiverThread.join();
    
    connectionHandler.close();

    return 0;
}
