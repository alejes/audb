package audb.util;

public class Pair<F, S> {
	Pair(F f, S s) {
		first = f;
		second = s;
	}
	
	public static <F,S> Pair <F,S> newPair(F f, S s) {
		return new Pair<F,S>(f, s);
	}
	
    public F first;
    public S second;
}
