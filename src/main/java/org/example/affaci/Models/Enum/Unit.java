package org.example.affaci.Models.Enum;

public enum Unit {
    g("Грамм"),
    mg("Миллиграмм"),
    µg("Микрограмм"),
    IU("Международная единица"),
    kcal("Ккал");



    private final String name;

    Unit(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
