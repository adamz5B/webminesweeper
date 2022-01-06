package pl.adamzylinski.yam.models;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * Player parameters: number of lives, number of set flags and revealed fields.
 * And also score at the end of the game.
 */
public class PlayerInfo implements JsonObjectifyI {
    private int lives;
    private int flags;
    private int revealedFields;
    private double score = 0.0;

    public PlayerInfo() {
        this.flags = 0;
        this.lives = 5;
        this.revealedFields = 0;
    }

    public void lifeLost() {
        lives--;
        if (lives < 0) {
            lives = 0;
        }
    }

    public int getLives() {
        return lives;
    }

    public void addFlag() {
        flags++;
    }

    public void remFlag() {
        flags--;
    }

    public int getFlags() {
        return flags;
    }

    public void addRevealedFields(int addedFieldsNumber) {
        revealedFields += addedFieldsNumber;
    }

    public int getRevealedFields() {
        return revealedFields;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Double getScore() {
        return score;
    }

    @Override
    public JsonObject getJsonObject() {
        return Json.createObjectBuilder().add("lives", lives).add("flags", flags).add("revealedFields", revealedFields)
                .add("score", score).build();
    }

}
