package jugglestruggle.timechangerstruggle.util;

/**
 * An interface which applies the functions interchangeably without
 * needing to use two functions for one thing.
 *
 * @author JuggleStruggle
 * @implNote Created on 08-Feb-2022, Tuesday
 * 
 * @see java.util.function.Function
 */
public interface InterchangeableFunction<L, R>
{
	L applyLeft(R value);
	R applyRight(L value);
	
//	default Function<R, L> getLeftFunction() {
//		return this::getLeftFunction;
//	}
}
