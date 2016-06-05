package bgu.spl.spl_assignment3.protocol;

import java.util.Map;

import bgu.spl.spl_assignment3.ProtocolCallback;
import bgu.spl.spl_assignment3.Room;
import bgu.spl.spl_assignment3.tokenizer.TBGPMessage;
import bgu.spl.spl_assignment3.ClientDetails;
public interface ServerProtocolFactory<T> {
   AsyncServerProtocol<T> create(Map<ProtocolCallback<TBGPMessage>, ClientDetails> clients, Map<String, Room> rooms);
}
