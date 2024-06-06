package de.kostari.cloud.core.window;

import de.kostari.cloud.core.events.Event;

public class WindowEvents {

    /**
     * Called when the window is resized, passes the new width and height as int
     * 
     * @param width  The new width of the window
     * @param height The new height of the window
     */
    public static Event onWindowResize = new Event();

    /**
     * Called when the window is dragged, passes the new x and y position as int
     * 
     * @param x The new x position of the window
     * @param y The new y position of the window
     */
    public static Event onWindowDrag = new Event();

    /**
     * Called when the window is focused or unfocused, passes the new focus state as
     * boolean
     * 
     * @param focus The new focus state of the window
     */
    public static Event onWindowFocus = new Event();

    /**
     * Called when the mouse is moved, passes the new x and y position as int
     * 
     * @param x The new x position of the mouse
     * @param y The new y position of the mouse
     */
    public static Event onMouseMove = new Event();

    /**
     * Called when the mouse is scrolled, passes the scroll x and y as float
     * 
     * @param scrollX The scroll x of the mouse
     * @param scrollY The scroll y of the mouse
     */
    public static Event onMouseScroll = new Event();

    public static Event onMouseClick = new Event();

    /**
     * Called when a key is pressed, passes the key code and action as int
     * 
     * @param key    The key code of the key that was pressed
     * @param action The action of the key press
     */
    public static Event onKeyType = new Event();

    /**
     * Called when one or more files are dropped into the window, passes the paths
     * as String[]
     */
    public static Event onFileDrop = new Event();

}
