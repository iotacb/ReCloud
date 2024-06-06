package de.kostari.cloud.demo2;

import java.util.ArrayList;
import java.util.List;

public class BouncingBallsDemo {
    private ShapeRenderer renderer;
    private List<Ball> balls;

    public BouncingBallsDemo() {
        renderer = new ShapeRenderer();
        balls = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            balls.add(new Ball((float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1,
                    (float) Math.random() * 0.02f, (float) Math.random() * 0.02f, 0.05f));
        }
    }

    public void update() {
        for (Ball ball : balls) {
            ball.update();
        }
    }

    public void render() {
        float[] vertices = new float[balls.size() * 6];
        int index = 0;
        for (Ball ball : balls) {
            float[] ballVertices = ball.getVertices();
            System.arraycopy(ballVertices, 0, vertices, index, ballVertices.length);
            index += ballVertices.length;
        }
        renderer.render(vertices);
    }

    public void cleanup() {
        renderer.cleanup();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}

class Ball {
    private float x, y;
    private float vx, vy;
    private float radius;

    public Ball(float x, float y, float vx, float vy, float radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
    }

    public void update() {
        x += vx;
        y += vy;
        if (x - radius < -1 || x + radius > 1) {
            vx = -vx;
        }
        if (y - radius < -1 || y + radius > 1) {
            vy = -vy;
        }
    }

    public float[] getVertices() {
        return new float[] {
                x - radius, y - radius,
                x + radius, y - radius,
                x + radius, y + radius,
                x - radius, y - radius,
                x + radius, y + radius,
                x - radius, y + radius
        };
    }
}
