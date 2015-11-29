package audb.util;

public class ComparablePair<F extends Comparable<F>, S extends Comparable<S>> 
	extends Pair<F, S> implements Comparable<ComparablePair<F, S>> {
	ComparablePair(F f, S s) {
		super(f, s);
	}

	public int compareTo(ComparablePair<F, S> other) {
		int result = first.compareTo(other.first);
		if (0 != result) {
			return result;
		}
		
		return second.compareTo(other.second);
	}
	
	public static <F extends Comparable<F>, S extends Comparable<S>> 
	ComparablePair <F,S> newPair(F f, S s) {
		return new ComparablePair<F,S>(f, s);
	}
}