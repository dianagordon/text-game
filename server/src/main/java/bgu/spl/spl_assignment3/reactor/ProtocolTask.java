package bgu.spl.spl_assignment3.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;

import bgu.spl.spl_assignment3.ProtocolCallback;
import bgu.spl.spl_assignment3.protocol.AsyncServerProtocol;
import bgu.spl.spl_assignment3.tokenizer.MessageTokenizer;

/**
 * This class supplies some data to the protocol, which then processes the data,
 * possibly returning a reply. This class is implemented as an executor task.
 * 
 */
public class ProtocolTask<T> implements Runnable {

	private final AsyncServerProtocol<T> _protocol;
	private final MessageTokenizer<T> _tokenizer;
	private final ConnectionHandler<T> _handler;
	private final Callback<T> _callback;

	public ProtocolTask(final AsyncServerProtocol<T> protocol, final MessageTokenizer<T> tokenizer, final ConnectionHandler<T> h) {
		this._protocol = protocol;
		this._tokenizer = tokenizer;
		this._handler = h;
		this._callback = new Callback<T>(tokenizer);
	}

	public class Callback<T> implements ProtocolCallback<T> {
		MessageTokenizer<T> _tokenizer;
		public Callback(MessageTokenizer<T> tokenizer) {
			this._tokenizer = tokenizer;
		}
		
		@Override
		public void sendMessage(T msg) throws IOException {
			_handler.addOutData(_tokenizer.getBytesForMessage(msg));
		}
		
	}
	// we synchronize on ourselves, in case we are executed by several threads
	// from the thread pool.
	public synchronized void run() {
      // go over all complete messages and process them.
      while (_tokenizer.hasMessage()) {
         T msg = _tokenizer.nextMessage();
         this._protocol.processMessage(msg, _callback);
      }
	}

	public void addBytes(ByteBuffer b) {
		_tokenizer.addBytes(b);
	}
}
