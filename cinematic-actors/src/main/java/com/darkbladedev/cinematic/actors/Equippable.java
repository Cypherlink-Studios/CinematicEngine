package com.darkbladedev.cinematic.actors;

public interface Equippable {
    void setEquipment(EquipmentSlot slot, String itemKey);

    String getEquipment(EquipmentSlot slot);
}
