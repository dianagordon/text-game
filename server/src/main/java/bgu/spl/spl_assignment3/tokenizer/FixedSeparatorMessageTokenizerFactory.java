package bgu.spl.spl_assignment3.tokenizer;

import java.nio.charset.StandardCharsets;

public class FixedSeparatorMessageTokenizerFactory implements TokenizerFactory<TBGPMessage>{

	@Override
	public MessageTokenizer<TBGPMessage> create() {
		return new FixedSeparatorMessageTokenizer("\n", StandardCharsets.UTF_8);
	}
	
}