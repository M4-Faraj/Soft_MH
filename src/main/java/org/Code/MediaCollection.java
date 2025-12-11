package org.Code;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents a collection of {@link Media} items in the library system.
 * <p>
 * This class allows adding media items (such as books or CDs) and
 * searching for an item by its title. The search is case-insensitive
 * and returns the first matching item.
 *
 * <p>Typical usage:
 * <pre>
 *     MediaCollection collection = new MediaCollection();
 *     collection.addItem(new Book("Clean Code", "Robert Martin", "111", false));
 *     Media item = collection.searchItem("clean code");
 * </pre>
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */

public class MediaCollection {
    private List<Media> items = new ArrayList<>();
    /**
     * Adds a media item (Book, CD, etc.) to the collection.
     *
     * @param item the media item to add; must not be {@code null}
     */
    public void addItem(Media item) {
        items.add(item);
    }
    /**
     * Searches the collection for a media item with a matching title.
     * <p>
     * The comparison is case-insensitive and returns the first match found.
     * If no item matches the provided name, {@code null} is returned.
     *
     * @param name the title of the media item to search for
     * @return the matching {@link Media} item, or {@code null} if none found
     */
    public Media searchItem(String name) {
        for (Media item : items) {
            if (item.getTitle().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }
}