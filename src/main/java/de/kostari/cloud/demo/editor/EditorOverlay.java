package de.kostari.cloud.demo.editor;

import java.util.ArrayList;

import de.kostari.cloud.core.ui.elements.ElementList;
import de.kostari.cloud.core.window.Window;
import de.kostari.cloud.demo.files.FileReader;
import de.kostari.cloud.demo.files.FileWriter;
import de.kostari.cloud.demo.utils.PathPoint;

public class EditorOverlay {

    private ArrayList<PathPoint> points = new ArrayList<>();
    public static PathPoint lastSelectedPoint;

    private ElementList elementList;

    private ImageButton addPathPointButton;
    private ImageButton delPathPointButton;

    public EditorOverlay() {
        this.elementList = new ElementList();
        this.elementList.setGap(20);
        this.elementList.setHorizontal(true);
        this.addPathPointButton = new ImageButton("addPathPoint.png", "AddPathPoint", 100, 100);
        this.delPathPointButton = new ImageButton("removePathPoint.png", "RemovePathPoint", 100, 100);
        this.elementList.addElements(addPathPointButton, delPathPointButton);
    }

    public void init() {
        readPointsFromFile();
    }

    public void draw() {
        elementList.draw();
        elementList.drawBounds();
        // if (GameController.gameState == GameStates.EDITOR) {
        // points.forEach(PathPoint::draw);

        // if (lastSelectedPoint != null) {
        // Render.drawText(lastSelectedPoint.toString(),
        // lastSelectedPoint.getX() - Render.getTextWidth(lastSelectedPoint.toString()),
        // lastSelectedPoint.getY());
        // }

        // for (int i = 0; i < points.size(); i++) {
        // PathPoint currentPoint = points.get(i);
        // PathPoint prevPoint = i > 0 ? points.get(i - 1) : points.get(0);
        // PathPoint nextPoint = i < points.size() - 1 ? points.get(i + 1) :
        // points.get(points.size() - 1);
        // Render.color(Colors.WHITE);
        // Render.drawLine(prevPoint.getX(), prevPoint.getY(), currentPoint.getX(),
        // currentPoint.getY());
        // Render.drawLine(currentPoint.getX(), currentPoint.getY(), nextPoint.getX(),
        // nextPoint.getY());
        // }
        // }
    }

    public void update() {
        this.elementList.setPosition(Window.instance.getWindowWidth() / 2,
                Window.instance.getWindowHeight() - elementList.getElementsHeight() / 2);
        elementList.update();
        // points.forEach(PathPoint::update);
        // if (Input.mouseButtonPressed(0) && !isHoveringPoint()) {
        // points.add(new PathPoint(Input.getMouseX(), Input.getMouseY()));
        // }

        // if (Input.keyPressed((Keys.KEY_P))) {
        // savePointsToFile();
        // }
    }

    public void savePointsToFile() {
        ArrayList<String> pointsAsStrings = new ArrayList<>();
        points.forEach(point -> {
            pointsAsStrings.add(pointsAsStrings.size() + ":" + point.getX() + ":" + point.getY());
        });
        FileWriter.writeStringList("map.txt", pointsAsStrings);
    }

    public void readPointsFromFile() {
        ArrayList<String> pointsAsStrings = (ArrayList<String>) FileReader.readAsStringList("map.txt");
        pointsAsStrings.forEach(point -> {
            String[] props = point.split(":");
            float x = Float.valueOf(props[1]);
            float y = Float.valueOf(props[2]);
            points.add(new PathPoint(x, y));
        });
    }
}
