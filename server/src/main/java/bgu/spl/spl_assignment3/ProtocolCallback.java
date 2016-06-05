package bgu.spl.spl_assignment3;

public interface ProtocolCallback<T> {
	void sendMessage(T msg) throws java.io.IOException;
}
