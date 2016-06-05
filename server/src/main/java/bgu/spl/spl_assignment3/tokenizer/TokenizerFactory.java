package bgu.spl.spl_assignment3.tokenizer;

public interface TokenizerFactory<T> {
   MessageTokenizer<T> create();
}
