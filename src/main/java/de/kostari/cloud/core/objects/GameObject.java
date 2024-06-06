package de.kostari.cloud.core.objects;

import java.util.ArrayList;

import de.kostari.cloud.core.components.Component;
import de.kostari.cloud.core.components.Transform;
import de.kostari.cloud.core.scene.SceneManager;

public class GameObject {

    // The Id of the object
    public static int OBJECT_ID = 0;

    private GameObject parent;

    private ArrayList<Component> components;
    private ArrayList<GameObject> children;

    /**
     * The transform of the gameobject.
     * 
     * @see Transform
     */
    public transient Transform transform;

    public GameObject() {
        OBJECT_ID++;
        this.components = new ArrayList<>();
        this.children = new ArrayList<>();
        this.transform = addComponent(new Transform());

        /**
         * When a GameObject is instantiated it will be added to the current scene
         */
        if (SceneManager.hasScene()) {
            SceneManager.current().addGameObjects(this);
        }
    }

    /**
     * Called every frame.
     */
    public void update() {
        if (parent != null) {
            transform.position.x = parent.transform.position.x + transform.localPosition.x;
            transform.position.y = parent.transform.position.y + transform.localPosition.y;
        }
        for (int i = 0; i < components.size(); i++) {
            Component comp = components.get(i);
            comp.update();
        }
    }

    /**
     * Called every frame after update.
     */
    public void draw() {
        for (int i = 0; i < components.size(); i++) {
            Component comp = components.get(i);
            comp.draw();
        }
    }

    /**
     * Called when the game object is removed from the scene.
     */
    public void dispose() {
    }

    /**
     * Adds a component to the gameobject.
     * 
     * @param component The component to add
     * @return Returns the added component
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T addComponent(Component component) {
        component.gameObject = this;
        component.init();
        components.add(component);

        try {
            return (T) component.getClass().cast(component);
        } catch (ClassCastException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    /**
     * Removes a component from the gameobject.
     * 
     * @param componentClass The class of the component to remove
     */
    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            Component comp = components.get(i);
            if (componentClass.isAssignableFrom(comp.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    /**
     * Returns a component of the specified class.
     * 
     * @param componentClass The class of the component to get
     * @return Returns the component or null if not found
     */
    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            Component comp = components.get(i);
            if (componentClass.isAssignableFrom(comp.getClass())) {
                return componentClass.cast(comp);
            }
        }
        return null;
    }

    /**
     * Adds a gameobject as a child to the gameobject.
     * The child will automatically be assigned to the parent.
     * 
     * @param child
     */
    public void addChild(GameObject child) {
        child.parent = this;
        children.add(child);
    }

    /**
     * Returns a list of all child objects.
     * 
     * @return Returns a list of all child objects
     */
    public ArrayList<GameObject> getChildren() {
        return children;
    }

    /**
     * Returns a child object at the specified index.
     * 
     * @param index The index of the child object
     * @return Returns the child object
     */
    public GameObject getChild(int index) {
        return children.get(index);
    }

    /**
     * Returns the parent of the gameobject.
     * 
     * @return Returns the parent of the gameobject
     */
    public GameObject getParent() {
        return parent;
    }

    /**
     * Sets the parent of the gameobject.
     * 
     * When the parent is set, the position of the gameobject will be relative to
     * the parent.
     * 
     * Change the local psotion of the transform instead of the position, to move
     * the gameobject.
     * 
     * @see Transform#localPosition
     * 
     * @param parent
     */
    public void setParent(GameObject parent) {
        parent.addChild(this);
    }

}
