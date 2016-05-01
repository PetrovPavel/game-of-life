package com.ppetrov.game.viewer;

import com.ppetrov.game.model.Map;
import javafx.scene.layout.VBox;
import rx.Observable;

public class BrushPane extends VBox {

    private FieldCanvas brushCanvas;

    public BrushPane() {
        createBrushPane();
    }

    private void createBrushPane() {
        this.brushCanvas = new FieldCanvas();
        this.brushCanvas.setPrefSize(100, 100);
        this.brushCanvas.setMap(new Map(new Boolean[][]{
                {false, false, false, false, false},
                {false, false, false, false, false},
                {false, false, true, false, false},
                {false, false, false, false, false},
                {false, false, false, false, false}
        }));
        getChildren().add(this.brushCanvas);
    }

    public Observable<Map> getMapChanges() {
        return this.brushCanvas.getMapChanges();
    }

}
