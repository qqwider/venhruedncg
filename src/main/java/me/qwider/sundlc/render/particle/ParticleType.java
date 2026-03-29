package me.qwider.sundlc.render.particle;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public enum ParticleType {
    STAR(Identifier.of("sundlc", "textures/particles/star.png")),
    HEART(Identifier.of("sundlc", "textures/particles/heart.png")),
    SNOW(Identifier.of("sundlc", "textures/particles/snow.png"));

    public final Identifier texture;
    public final RenderLayer renderLayer;
    
    ParticleType(Identifier texture) {
        this.texture = texture;
        this.renderLayer = RenderLayer.getEntityTranslucentEmissive(texture);
    }
}