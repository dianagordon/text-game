package bgu.spl.spl_assignment3;

import java.util.Map;

import bgu.spl.spl_assignment3.protocol.ServerProtocol;
import bgu.spl.spl_assignment3.tokenizer.TBGPMessage;
import bgu.spl.spl_assignment3.ClientDetails;
/**
 * A protocol that describes the behabiour of the server.
 */
public interface ServerProtocolFactory<T> {

   ServerProtocol<T> create(Map<ProtocolCallback<TBGPMessage>, ClientDetails> clients, Map<String, Room> rooms);
}
