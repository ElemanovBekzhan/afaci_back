package org.example.affaci.Models.Enum;

public enum Mineral {
    Ca("Кальций"),
    Na("Натрий"),
    K("Калий"),
    P("Фосфор"),
    Mn("Марганец"),
    Zn("Цинк"),
    Se("Скандий"),
    Cu("Медь"),
    Fe("Железо"),
    I("Йод"),
    B("Бор"),
    Li("Литий"),
    Al("Алюминий"),
    Mg("Магний"),
    V("Ванадий"),
    Ni("Нитрий"),
    Co("Ковальт"),
    Cr("Хром"),
    Sn("Олово"),
    Ba("Барий");



    private final String name;

    Mineral(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
