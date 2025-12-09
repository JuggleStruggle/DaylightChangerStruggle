package jugglestruggle.timechangerstruggle.util;

import java.util.List;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

/**
 * A character visitor whose goal is to extract characters 
 * from {@link OrderedText} and provide it to those needing it.
 * 
 * <p> Please note: The characters being extracted have an
 * upper-bound limit based from {@link Short#MAX_VALUE}.
 *
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.2
 */
public class SimpleCharacterVisitor implements CharacterVisitor
{
	char[] chars;
	int size;

	public SimpleCharacterVisitor(int startingCharsSize) {
		this.chars = new char[startingCharsSize]; this.size = 0;
	}
	
	@Override
	public boolean accept(int index, Style style, int codePoint)
	{
		final char[] chars = Character.toChars(codePoint);
		int charsToAdd = chars.length;
		int newSizeWithChars = this.size + charsToAdd;
		
		if (newSizeWithChars >= this.chars.length)
		{
			// cap the size to avoid certain issues from popping up
			if (this.chars.length >= Short.MAX_VALUE)
				return false;
				
			int newSize = this.chars.length + 20;
			if (newSize > Short.MAX_VALUE)
				newSize = Short.MAX_VALUE;
				
			char[] copy = new char[newSize];
			System.arraycopy(this.chars, 0, copy, 0, this.chars.length);
			
			this.chars = copy;
		}
		
		if (newSizeWithChars > Short.MAX_VALUE)
			charsToAdd = Short.MAX_VALUE - this.size;
		
		for (int i = charsToAdd - 1; i >= 0; --i)
			this.chars[this.size + i] = chars[i];
		
		this.size += charsToAdd;
		
		return true;
	}
	
	/**
	 * Gets the finalized text and then 'clear' it after such operation
	 * is done. Primarily used as a helper method to avoid needing to do
	 * both things separately.
	 * 
	 * @return a String
	 * @see #getAsString()
	 * @see #clear()
	 */
	public String getAndClear() 
	{
		String v = this.getAsString();
		this.clear(); return v;
	}
	
	/**
	 * Compile the string based on the {@linkplain #chars characters provided in the array}
	 * and the {@link #size} to know exactly how many characters were actually added.
	 * 
	 * @return a complete string representing the extracted data from 
	 * 		   {@link #accept(int, Style, int)}
	 */
	public String getAsString() {
		return String.valueOf(this.chars, 0, this.size);
	}
	
	/**
	 * Simple method whose goal isn't really to clear the array, but sets the {@link #size}
	 * to 0 instead.
	 */
	public void clear() {
		this.size = 0;
	}
	
	
	/**
	 * Creates a mutable text based on the list of ordered texts.
	 * 
	 * @param linesToSkip the amount of lines to skip from {@code orderedTexts}, 
	 *        leave it 0 to start from the beginning; must not be below 0
	 * @param linesToUse the maximum amount of lines to use, regardless of {@code orderedTexts}'s size;
	 *        leave -1 to use all possible lines the parameter comes with
	 * @param orderedTexts list of ordered texts that can't be used for anything else other than to consume the provided
	 *        characters
	 *        
	 * @return a mutable text which may contain the contents of {@link OrderedText}; otherwise empty
	 */
	public static MutableText asMutableText(int linesToSkip, int linesToUse, List<OrderedText> orderedTexts)
	{
		MutableText mt = Text.empty();
		int size = orderedTexts.size();
		
		if (linesToUse > 0)
			size = linesToUse < size ? linesToUse : size;
		
		if (size > linesToSkip)
		{
			SimpleCharacterVisitor scv = new SimpleCharacterVisitor(100);
		
			for (int i = linesToSkip; i < size; ++i) 
			{
				OrderedText ot = orderedTexts.get(i);
				ot.accept(scv);
				
				mt.append(scv.getAndClear());
			}
		}
		
		return mt;
	}
}
