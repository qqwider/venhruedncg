package me.qwider.sundlc.render.gui;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.settings.*;
import me.qwider.sundlc.render.MSDFRenderer;
import me.qwider.sundlc.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import java.util.List;

public class Panel {
    public final Category category;
    public final Draggable drag;
    private boolean open = true;

    private Module activeModule = null;
    private float sidePanelY = 0;

    public Panel(Category category, float x, float y) {
        this.category = category;
        this.drag = new Draggable("panel_" + category.name(), x, y, 100, 16);
    }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public void render(DrawContext context, Matrix4f matrix, int mouseX, int mouseY, float anim) {
        drag.update(mouseX, mouseY);
        float x = drag.x, y = drag.y, w = drag.width, h = drag.height;
        List<Module> modules = ModuleManager.getByCategory(category);
        int alpha = (int) (255 * anim);
        int accent = (alpha << 24) | 0x8877FF;

        // 1. Отрисовка основной панели категорий
        float mainHeight = h + (open ? modules.size() * 16 + 3 : 0);
        RenderUtils.drawRoundedRect(context, x, y, w, mainHeight, 6, (alpha << 24) | 0x141415);
        RenderUtils.drawRoundedRect(context, x, y, w, h, 6, (alpha << 24) | 0x1D1B1F);
        MSDFRenderer.drawString(matrix, category.name, x + (w - MSDFRenderer.getStringWidth(category.name, 9)) / 2f, y + 10.5f, 9, (alpha << 24) | 0xFFFFFF);

        if (open) {
            float currentY = y + h + 2;
            for (Module m : modules) {
                boolean hovered = isHovered(mouseX, mouseY, x + 3, currentY, w - 6, 14);

                // Цвет фона зависит от состояния
                int modBg;
                if (m == activeModule) {
                    modBg = 0x2D2B33;
                } else if (m.isEnabled()) {
                    modBg = 0x24222A;
                } else if (hovered) {
                    modBg = 0x28262E;
                } else {
                    modBg = 0x1A1A1B;
                }

                RenderUtils.drawRoundedRect(context, x + 3, currentY, w - 6, 14, 4, (alpha << 24) | modBg);
                
                int textColor = m.isEnabled() ? 0xFFFFFFFF : 0xBBBBBB;
                
                // ЦЕНТРИРУЕМ НАЗВАНИЕ
                float textWidth = MSDFRenderer.getStringWidth(m.getName(), 7.5f);
                float textX = x + 3 + (w - 6 - textWidth) / 2f;
                MSDFRenderer.drawString(matrix, m.getName(), textX, currentY + 9.5f, 7.5f, (alpha << 24) | textColor);

                if (m == activeModule) {
                    sidePanelY = currentY;
                }

                currentY += 16;
            }
        }

        // 2. ОТРИСОВКА БОКОВОЙ ПАНЕЛИ
        if (activeModule != null) {
            activeModule.settingsAnim.update(activeModule.settingsOpen);
            float sAnim = (float) activeModule.settingsAnim.getValue();

            if (sAnim > 0.001f) {
                renderSettings(context, matrix, mouseX, mouseY, sAnim * anim);
            } else if (!activeModule.settingsOpen) {
                activeModule = null;
            }
        }
    }

    private void renderSettings(DrawContext context, Matrix4f matrix, int mouseX, int mouseY, float combinedAnim) {
        float w = 120;
        float x = drag.x + drag.width + 5;
        float y = sidePanelY;

        int alpha = (int) (255 * combinedAnim);
        int accent = (alpha << 24) | 0x8877FF;

        // Расчет динамической высоты
        float totalH = 18;
        for (Setting s : activeModule.settings) {
            if (!s.isVisible()) continue; // Пропускаем невидимые настройки
            if (s instanceof ColorSetting cs && cs.opened) totalH += 80;
            else if (s instanceof ModeSetting ms && ms.opened) totalH += 16 + ms.modes.size() * 14;
            else totalH += 16;
        }
        totalH += 4;

        float animatedW = w * combinedAnim;

        context.enableScissor((int)x, (int)y, (int)(x + animatedW), (int)(y + totalH + 10));

        RenderUtils.drawRoundedRect(context, x, y, w, totalH, 6, (alpha << 24) | 0x141415);
        RenderUtils.drawRoundedRect(context, x, y, w, 16, 6, (alpha << 24) | 0x1D1B1F);
        
        // Название модуля по центру
        float titleWidth = MSDFRenderer.getStringWidth(activeModule.getName(), 8f);
        MSDFRenderer.drawString(matrix, activeModule.getName(), x + (w - titleWidth) / 2f, y + 10.5f, 8f, accent);

        float sY = y + 20;
        for (Setting s : activeModule.settings) {
            if (!s.isVisible()) continue; // Пропускаем невидимые настройки
            float sX = x + 6, sW = w - 12;
            
            if (s instanceof BooleanSetting bs) {
                boolean hovered = isHovered(mouseX, mouseY, sX, sY, sW, 14);
                int bg = hovered ? 0x252526 : 0x1D1B1F;
                RenderUtils.drawRoundedRect(context, sX, sY, sW, 14, 4, (alpha << 24) | bg);
                MSDFRenderer.drawString(matrix, s.name, sX + 6, sY + 9.5f, 7.5f, (alpha << 24) | 0xFFFFFF);
                
                // Переключатель справа
                float toggleX = sX + sW - 24;
                float toggleY = sY + 4;
                int toggleBg = bs.enabled ? (accent & 0x00FFFFFF) : 0x444444;
                RenderUtils.drawRoundedRect(context, toggleX, toggleY, 20, 6, 3, (alpha << 24) | toggleBg);
                float circleX = bs.enabled ? toggleX + 14 : toggleX + 2;
                RenderUtils.drawRoundedRect(context, circleX, toggleY, 6, 6, 3, 0xFFFFFFFF);
                
                sY += 16;
            }
            else if (s instanceof NumberSetting ns) {
                boolean hovered = isHovered(mouseX, mouseY, sX, sY, sW, 14);
                int bg = hovered ? 0x252526 : 0x1D1B1F;
                RenderUtils.drawRoundedRect(context, sX, sY, sW, 14, 4, (alpha << 24) | bg);
                
                if (ns.dragging) {
                    double diff = Math.min(sW, Math.max(0, mouseX - sX));
                    double newValue = ((diff / sW) * (ns.max - ns.min) + ns.min);
                    if (ns.isInteger) {
                        ns.value = Math.round(newValue);
                    } else {
                        ns.value = Math.round(newValue * 100.0) / 100.0;
                    }
                }
                
                float fillW = (float) ((ns.value - ns.min) / (ns.max - ns.min) * sW);
                RenderUtils.drawRoundedRect(context, sX, sY, fillW, 14, 4, (accent & 0x00FFFFFF) | (alpha / 3 << 24));
                
                MSDFRenderer.drawString(matrix, s.name, sX + 6, sY + 9.5f, 7.5f, (alpha << 24) | 0xFFFFFF);
                String valueStr = ns.isInteger ? String.valueOf((int)ns.value) : String.valueOf(ns.value);
                float valueWidth = MSDFRenderer.getStringWidth(valueStr, 7.5f);
                MSDFRenderer.drawString(matrix, valueStr, sX + sW - valueWidth - 6, sY + 9.5f, 7.5f, accent);
                
                sY += 16;
            }
            else if (s instanceof ModeSetting ms) {
                boolean hovered = isHovered(mouseX, mouseY, sX, sY, sW, 14);
                int bg = hovered ? 0x252526 : 0x1D1B1F;
                float hH = 14;
                float fullH = hH + (ms.opened ? ms.modes.size() * 14 + 2 : 0);
                
                RenderUtils.drawRoundedRect(context, sX, sY, sW, fullH, 4, (alpha << 24) | bg);
                MSDFRenderer.drawString(matrix, ms.name, sX + 6, sY + 9.5f, 7.5f, (alpha << 24) | 0xBBBBBB);
                
                String currentMode = ms.getMode();
                float modeWidth = MSDFRenderer.getStringWidth(currentMode, 7.5f);
                MSDFRenderer.drawString(matrix, currentMode, sX + sW - modeWidth - 6, sY + 9.5f, 7.5f, accent);
                
                if (ms.opened) {
                    float mY = sY + hH + 2;
                    for (int i = 0; i < ms.modes.size(); i++) {
                        boolean modeHovered = isHovered(mouseX, mouseY, sX + 4, mY, sW - 8, 12);
                        int modeBg = (i == ms.index) ? 0x2D2B33 : (modeHovered ? 0x252526 : 0x1A1A1B);
                        RenderUtils.drawRoundedRect(context, sX + 4, mY, sW - 8, 12, 3, (alpha << 24) | modeBg);
                        
                        int modeColor = (i == ms.index) ? accent : (alpha << 24) | 0xFFFFFF;
                        MSDFRenderer.drawString(matrix, ms.modes.get(i), sX + 10, mY + 8.5f, 7f, modeColor);
                        mY += 14;
                    }
                }
                sY += fullH + 2;
            }
            else if (s instanceof ColorSetting cs) {
                boolean hovered = isHovered(mouseX, mouseY, sX, sY, sW, 14);
                int bg = hovered ? 0x252526 : 0x1D1B1F;
                float hH = 14, pickH = 66, fullH = hH + (cs.opened ? pickH : 0);
                
                RenderUtils.drawRoundedRect(context, sX, sY, sW, fullH, 4, (alpha << 24) | bg);
                MSDFRenderer.drawString(matrix, s.name, sX + 6, sY + 9.5f, 7.5f, (alpha << 24) | 0xFFFFFF);
                
                // Индикатор цвета справа
                RenderUtils.drawRoundedRect(context, sX + sW - 18, sY + 4, 14, 6, 2, 0xFFFFFFFF);
                RenderUtils.drawRoundedRect(context, sX + sW - 17.5f, sY + 4.5f, 13, 5, 1.5f, (alpha << 24) | (cs.color & 0x00FFFFFF));
                
                if (cs.opened) {
                    float pickY = sY + 18, cX = sX + 6, sqS = 54;
                    int pureHue = java.awt.Color.HSBtoRGB(cs.hue, 1, 1);
                    context.fill((int)cX, (int)pickY, (int)(cX + sqS), (int)(pickY + sqS), 0xFF000000 | pureHue);
                    context.fillGradient((int)cX, (int)pickY, (int)(cX + sqS), (int)(pickY + sqS), 0xFFFFFFFF, 0x00FFFFFF);
                    context.fillGradient((int)cX, (int)pickY, (int)(cX + sqS), (int)(pickY + sqS), 0x00000000, 0xFF000000);
                    
                    float selX = cX + (cs.saturation * sqS), selY = pickY + ((1 - cs.brightness) * sqS);
                    RenderUtils.drawRoundedRect(context, selX - 2, selY - 2, 4, 4, 2, 0xFFFFFFFF);
                    RenderUtils.drawRoundedRect(context, selX - 1.5f, selY - 1.5f, 3, 3, 1.5f, 0xFF000000);
                    
                    float hX = cX + sqS + 6, hW = 8;
                    for (int i = 0; i < sqS; i++) {
                        int c = java.awt.Color.HSBtoRGB(i / sqS, 1, 1);
                        context.fill((int)hX, (int)(pickY + i), (int)(hX + hW), (int)(pickY + i + 1), 0xFF000000 | c);
                    }
                    RenderUtils.drawRoundedRect(context, hX - 1, pickY + (cs.hue * sqS) - 1.5f, hW + 2, 3, 1.5f, 0xFFFFFFFF);
                    
                    if (cs.draggingHSB) {
                        cs.saturation = MathHelper.clamp((float) (mouseX - cX) / sqS, 0, 1);
                        cs.brightness = MathHelper.clamp(1 - (float) (mouseY - pickY) / sqS, 0, 1);
                        cs.updateColor();
                    }
                    if (cs.draggingHue) {
                        cs.hue = MathHelper.clamp((float) (mouseY - pickY) / sqS, 0, 1);
                        cs.updateColor();
                    }
                }
                sY += fullH + 2;
            }
        }
        context.disableScissor();
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        drag.onMouseClick((int) mouseX, (int) mouseY, button);
        float x = drag.x, y = drag.y, w = drag.width;

        if (button == 1 && isHovered(mouseX, mouseY, x, y, w, 16)) {
            open = !open;
            me.qwider.sundlc.util.SoundUtil.playClick();
            return;
        }

        if (open) {
            float currentY = y + 16 + 2;
            for (Module m : ModuleManager.getByCategory(category)) {
                if (isHovered(mouseX, mouseY, x + 3, currentY, w - 6, 14)) {
                    if (button == 0) m.toggle();
                    if (button == 1) {
                        if (activeModule == m) {
                            m.settingsOpen = false;
                        } else {
                            if (activeModule != null) activeModule.settingsOpen = false;
                            activeModule = m;
                            activeModule.settingsOpen = true;
                            sidePanelY = currentY;
                        }
                        me.qwider.sundlc.util.SoundUtil.playClick();
                    }
                    return;
                }
                currentY += 16;
            }
        }

        // КЛИКИ В БОКОВОЙ ПАНЕЛИ
        if (activeModule != null && activeModule.settingsAnim.getValue() > 0.5) {
            float sX_base = drag.x + drag.width + 5 + 6;
            float sY_base = sidePanelY + 20;
            float sW = 108;
            boolean clickHandled = false;
            
            for (Setting s : activeModule.settings) {
                if (!s.isVisible()) continue; // Пропускаем невидимые настройки
                
                if (clickHandled) {
                    // Пропускаем остальные настройки, если клик уже обработан
                    if (s instanceof ModeSetting ms) {
                        sY_base += 14 + (ms.opened ? ms.modes.size() * 14 + 2 : 0) + 2;
                    } else if (s instanceof ColorSetting cs) {
                        sY_base += 14 + (cs.opened ? 66 : 0) + 2;
                    } else {
                        sY_base += 16;
                    }
                    continue;
                }
                
                if (s instanceof BooleanSetting bs) {
                    if (isHovered(mouseX, mouseY, sX_base, sY_base, sW, 14)) {
                        bs.enabled = !bs.enabled;
                        me.qwider.sundlc.util.SoundUtil.playClick();
                        clickHandled = true;
                    }
                    sY_base += 16;
                } else if (s instanceof NumberSetting ns) {
                    if (isHovered(mouseX, mouseY, sX_base, sY_base, sW, 14)) {
                        ns.dragging = true;
                        clickHandled = true;
                    }
                    sY_base += 16;
                } else if (s instanceof ModeSetting ms) {
                    if (isHovered(mouseX, mouseY, sX_base, sY_base, sW, 14)) {
                        ms.opened = !ms.opened;
                        me.qwider.sundlc.util.SoundUtil.playClick();
                        clickHandled = true;
                    } else if (ms.opened) {
                        float mY = sY_base + 14 + 2;
                        for (int i = 0; i < ms.modes.size(); i++) {
                            if (isHovered(mouseX, mouseY, sX_base + 4, mY, sW - 8, 12)) {
                                ms.setMode(i); // Используем новый метод
                                me.qwider.sundlc.util.SoundUtil.playClick();
                                clickHandled = true;
                                break;
                            }
                            mY += 14;
                        }
                    }
                    sY_base += 14 + (ms.opened ? ms.modes.size() * 14 + 2 : 0) + 2;
                } else if (s instanceof ColorSetting cs) {
                    if (isHovered(mouseX, mouseY, sX_base, sY_base, sW, 14)) {
                        cs.opened = !cs.opened;
                        me.qwider.sundlc.util.SoundUtil.playClick();
                        clickHandled = true;
                    } else if (cs.opened) {
                        if (isHovered(mouseX, mouseY, sX_base + 6, sY_base + 18, 54, 54)) {
                            cs.draggingHSB = true;
                            clickHandled = true;
                        }
                        if (isHovered(mouseX, mouseY, sX_base + 66, sY_base + 18, 8, 54)) {
                            cs.draggingHue = true;
                            clickHandled = true;
                        }
                    }
                    sY_base += 14 + (cs.opened ? 66 : 0) + 2;
                }
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        drag.onMouseRelease(button);
        if (activeModule != null) {
            for (Setting s : activeModule.settings) {
                if (s instanceof NumberSetting ns) ns.dragging = false;
                if (s instanceof ColorSetting cs) { cs.draggingHSB = false; cs.draggingHue = false; }
            }
        }
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
