package arm.fpgrowth;

import arm.IConditionalItem;

public class FPConditionalItem implements IConditionalItem {
    String name;
    String value;

    public FPConditionalItem(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s=%s", name, value);
    }

    @Override
    public String getItemName() {
        return this.name;
    }

    @Override
    public String getItemValue() {
        return this.value;
    }
}
