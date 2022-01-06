package pl.adamzylinski.yam.game;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import pl.adamzylinski.yam.models.JsonObjectifyI;

/**
 * Class representing board field model.
 */
public class BoardField implements JsonObjectifyI {
    private String value;
    private boolean checked;
    private boolean flag;
    private String ownerId;

    public BoardField() {
        value = "0";
        checked = flag = false;
        ownerId = "";
    }

    public BoardField setValue(String value) {
        this.value = value;
        return this;
    }

    public String getValue() {
        return value;
    }

    public BoardField setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public boolean isChecked() {
        return checked;
    }

    public BoardField setFlag(boolean flag) {
        this.flag = flag;
        return this;
    }

    public boolean isFlag() {
        return flag;
    }

    public BoardField setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public JsonObject getJsonObject() {
        return Json.createObjectBuilder().add("value", value).add("checked", checked).add("flag", flag)
                .add("owner", ownerId).build();
    }
}
