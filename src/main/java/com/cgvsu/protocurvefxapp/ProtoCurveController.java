package com.cgvsu.protocurvefxapp;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ProtoCurveController {

    @FXML
    AnchorPane anchorPane;
    @FXML
    private Canvas canvas;

    private final ArrayList<Point2D> points = new ArrayList<>();

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        canvas.setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY -> handlePrimaryClick(canvas.getGraphicsContext2D(), event);
                case SECONDARY -> handleSecondaryClick(canvas.getGraphicsContext2D(), event);
            }
        });
    }

    private void handlePrimaryClick(GraphicsContext graphicsContext, MouseEvent event) {
        final Point2D clickPoint = new Point2D(event.getX(), event.getY());
        points.add(clickPoint);
        draw(graphicsContext);
    }

    private void handleSecondaryClick(GraphicsContext graphicsContext, MouseEvent event) {
        Point2D clickPoint = new Point2D(event.getX(), event.getY());
        final int DELETE_RADIUS = 10;
        int closestPointIndex = -1;
        double closestDistance = DELETE_RADIUS;

        for (int i = 0; i < points.size(); i++) {
            double distance = points.get(i).distance(clickPoint);

            if (distance <= closestDistance) {
                closestDistance = distance;
                closestPointIndex = i;
            }
        }

        if (closestPointIndex != -1) {
            points.remove(closestPointIndex);
            draw(graphicsContext);
        }
    }

    private void draw(GraphicsContext graphicsContext) {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Контрольный многоугольник
        graphicsContext.setStroke(Color.LIGHTGRAY);
        for (int i = 1; i < points.size(); i++) {
            Point2D firstPoint = points.get(i - 1);
            Point2D secondPoint = points.get(i);
            graphicsContext.strokeLine(
                    firstPoint.getX(), firstPoint.getY(),
                    secondPoint.getX(), secondPoint.getY());
        }

        // Контрольные точки
        final int POINT_RADIUS = 3;
        graphicsContext.setFill(Color.RED);
        for (Point2D point : points) {
            graphicsContext.fillOval(
                    point.getX() - POINT_RADIUS,
                    point.getY() - POINT_RADIUS,
                    2 * POINT_RADIUS,
                    2 * POINT_RADIUS);
        }

        drawBezierCurve(graphicsContext);
    }

    private void drawBezierCurve(GraphicsContext graphicsContext) {
        if (points.size() < 2) {
            return;
        }

        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.setLineWidth(2);

        Point2D previousPoint = calculateBezierPoint(0);
        final int STEPS = 1000;

        for (int i = 1; i <= STEPS; i++) {
            double t = (double) i / STEPS;
            Point2D currentPoint = calculateBezierPoint(t);
            graphicsContext.strokeLine(
                    previousPoint.getX(), previousPoint.getY(),
                    currentPoint.getX(), currentPoint.getY());
            previousPoint = currentPoint;
        }

        graphicsContext.setLineWidth(1);
    }

    private Point2D calculateBezierPoint(double t) {
        int degree = points.size() - 1;
        double x = 0;
        double y = 0;

        for (int i = 0; i <= degree; i++) {
            double coefficient = combination(degree, i)
                    * Math.pow(t, i)
                    * Math.pow(1 - t, degree - i);

            x += coefficient * points.get(i).getX();
            y += coefficient * points.get(i).getY();
        }

        return new Point2D(x, y);
    }

    private double combination(int n, int k) {
        double result = 1;

        for (int i = 1; i <= k; i++) {
            result = result * (n - i + 1) / i;
        }

        return result;
    }
}
