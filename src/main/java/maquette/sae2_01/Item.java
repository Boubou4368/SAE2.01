package maquette.sae2_01;

public class Item {
    public ItemType type;

    public Item(ItemType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Item{type=" + type + "}";
    }
}