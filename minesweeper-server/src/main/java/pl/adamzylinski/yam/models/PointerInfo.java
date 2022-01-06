package pl.adamzylinski.yam.models;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/** Inforation needed for transfering pointer position of another player */
public class PointerInfo implements JsonObjectifyI {
    private XYcoord<Integer> pointerPos;
    private XYcoord<Integer> field;

    public PointerInfo(int fieldX, int fieldY, int pointerX, int pointerY) {
        this(new XYcoord<>(fieldX, fieldY), new XYcoord<>(pointerX, pointerY));
    }

    public PointerInfo(XYcoord<Integer> field, XYcoord<Integer> pointer) {
        this.field = field;
        this.pointerPos = pointer;
    }

    public PointerInfo() {
        pointerPos = new XYcoord<>(0, 0);
        field = new XYcoord<>(0, 0);
    }

    public void setPointerPos(XYcoord<Integer> pointerPos) {
        this.pointerPos = pointerPos;
    }

    public XYcoord<Integer> getPointerPos() {
        return pointerPos;
    }

    public XYcoord<Integer> getField() {
        return field;
    }

    public void setField(XYcoord<Integer> field) {
        this.field = field;
    }

    @Override
    public JsonObject getJsonObject() {
        return Json.createObjectBuilder()
                .add("pointerPos",
                        Json.createObjectBuilder().add("x", pointerPos.getX()).add("y", pointerPos.getY()).build())
                .add("field", Json.createObjectBuilder().add("x", field.getX()).add("y", field.getY()).build()).build();
    }

}
