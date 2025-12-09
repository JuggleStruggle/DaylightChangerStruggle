package jugglestruggle.timechangerstruggle.client.gui.util;

/**
 * A navigation enumerator by the likes inspired in Vanilla 1.20+ for the mod to
 * use in a way exclusive for {@link TimeChangerScreen} due to its sub-screen
 * system. 
 * 
 * <p><b> Note: This enumerator is exclusive to older versions of Minecraft 
 * that don't have the navigation style introduced in Vanilla. </b>
 *
 * @author JuggleStruggle
 * @since 0.0.3
 */
public enum ScreenNavigationType
{
	NONE,
	MOUSE,
	KEYBOARD_TAB,
	KEYBOARD_ARROW;
	

	public boolean isMouse() {
		return this == MOUSE;
	}

	public boolean isKeyboard() {
		return this == KEYBOARD_ARROW || this == KEYBOARD_TAB;
	}
}

