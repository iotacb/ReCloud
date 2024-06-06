package de.kostari.cloud.core.ui.elements;

import java.util.ArrayList;

import de.kostari.cloud.core.ui.UIElement;

public class ElementList extends UIElement {

    private ArrayList<UIElement> elements = new ArrayList<>();

    private boolean horizontal;

    private float gap;

    @Override
    public void draw() {
        for (int i = 0; i < elements.size(); i++) {
            UIElement currentElement = elements.get(i);
            currentElement.draw();
        }
    }

    @Override
    public void update() {
        float offsetX = horizontal ? -gap / 2 : 0;
        float offsetY = horizontal ? 0 : -gap / 2;
        for (int i = 0; i < elements.size(); i++) {
            UIElement currentElement = elements.get(i);
            currentElement.setX(getX() - currentElement.getWidth() / 2 + offsetX);
            currentElement.setY(getY() + offsetY);
            currentElement.update();

            if (horizontal) {
                offsetX += currentElement.getWidth() + gap;
            } else {
                offsetY += currentElement.getHeight() + gap;
            }
        }
        setWidth(getElementsWidth());
        setHeight(getElementsHeight());
    }

    public ArrayList<UIElement> getElements() {
        return elements;
    }

    public void addElements(UIElement... elements) {
        for (UIElement element : elements) {
            this.elements.add(element);
        }
    }

    public float getGap() {
        return gap;
    }

    public void setGap(float gap) {
        this.gap = gap;
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public float getElementsWidth() {
        float width = 0;
        float widestWidth = 0;
        for (int i = 0; i < elements.size(); i++) {
            UIElement element = elements.get(i);
            width += element.getWidth() + (i == 0 ? 0 : gap);
            if (element.getWidth() > widestWidth)
                widestWidth = element.getWidth();
        }
        return horizontal ? width : widestWidth;
    }

    public float getElementsHeight() {
        float height = 0;
        float heighestHeight = 0;
        for (int i = 0; i < elements.size(); i++) {
            UIElement element = elements.get(i);
            height += element.getHeight() + (i == 0 ? 0 : gap);
            if (element.getHeight() > heighestHeight)
                heighestHeight = element.getHeight();
        }
        return horizontal ? heighestHeight : height;
    }

}
