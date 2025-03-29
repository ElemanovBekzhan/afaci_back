package org.example.affaci.Models.Enum;

public enum Unit {
    g("Грамм"),
    mg("Милиграмм"),
    µg("Микрограмм"),
    IU("Международная единица");



    private final String name;

    Unit(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
