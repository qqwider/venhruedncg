package me.qwider.sundlc.util;

import java.util.UUID;

public record AltAccount(String name, UUID uuid, String accessToken, String type) {
    public boolean isMicrosoft() {
        return type.equalsIgnoreCase("msa");
    }
}