package org.Code;

import java.util.ArrayList;
import java.util.List;

public class MediaCollection {
    private List<Media> items = new ArrayList<>();

    public void addItem(Media item) {
        items.add(item);
    }

    public Media searchItem(String name) {
        for (Media item : items) {
            if (item.getTitle().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }
}