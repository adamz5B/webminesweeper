package pl.adamzylinski.yam.models;

import jakarta.json.JsonObject;

/** Interface for providing {@link JsonObject} representation of an object. */
public interface JsonObjectifyI {
    /**
     * Gets representation of object as a {@link JsonObject}
     * 
     * @return a {@link JsonObject}
     */
    JsonObject getJsonObject();
}
