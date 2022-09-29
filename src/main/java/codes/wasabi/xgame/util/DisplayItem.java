package codes.wasabi.xgame.util;

import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import codes.wasabi.xplug.lib.adventure.text.format.TextColor;
import codes.wasabi.xplug.lib.adventure.text.format.TextDecoration;
import codes.wasabi.xplug.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
import codes.wasabi.xplug.lib.matlib.MaterialLib;
import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class DisplayItem {

    private static Component prepareComponent(Component c) {
        if (c == null) return null;
        c = c.colorIfAbsent(NamedTextColor.WHITE);
        if (c.decoration(TextDecoration.ITALIC).equals(TextDecoration.State.NOT_SET)) {
            return c.decoration(TextDecoration.ITALIC, false);
        } else {
            return c;
        }
    }

    private static Component prepareComponent(String s) {
        if (s == null) return null;
        return prepareComponent(LegacyComponentSerializer.legacySection().deserialize(s));
    }

    public static ItemStack create(MetaMaterial material, @Nullable Component name, int count, @Nullable List<Component> lore, boolean enchanted) {
        ItemStack is = new ItemStack(material.getBukkitMaterial(), count);
        material.apply(is);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
            if (enchanted) meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
            if (lore != null) {
                LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
                meta.setLore(lore.stream().map((Component c) -> serializer.serialize(prepareComponent(c))).collect(Collectors.toList()));
            } else {
                meta.setLore(null);
            }
            if (name != null) {
                meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(prepareComponent(name)));
            } else {
                meta.setDisplayName(null);
            }
            is.setItemMeta(meta);
        }
        return is;
    }

    public static ItemStack create(MetaMaterial material, @Nullable String name, @Nullable TextColor color, int count, @Nullable List<String> lore, boolean enchanted) {
        Component nameComponent;
        if (name == null) {
            nameComponent = null;
        } else {
            nameComponent = prepareComponent(name);
            if (color != null) nameComponent = nameComponent.color(color);
        }
        List<Component> loreComponents;
        if (lore == null) {
            loreComponents = null;
        } else {
            loreComponents = lore.stream().map(DisplayItem::prepareComponent).collect(Collectors.toList());
        }
        return create(material, nameComponent, count, loreComponents, enchanted);
    }

    @Contract(" -> new")
    public static Builder builder() {
        return new Builder();
    }

    @Contract("!null -> new; null -> fail")
    public static Builder builder(MetaMaterial mm) {
        return builder().material(mm);
    }

    @Contract("!null -> new; null -> fail")
    public static Builder builder(String material) {
        return builder().material(material);
    }

    public static class Builder {

        private MetaMaterial metaMaterial = null;
        private Component name = null;
        private TextColor lastColor = null;
        private int count = 1;
        private List<Component> lore = null;
        private boolean enchanted;

        @Contract("!null -> this; null -> fail")
        public Builder material(MetaMaterial mm) {
            this.metaMaterial = Objects.requireNonNull(mm);
            return this;
        }

        @Contract("!null -> this; null -> fail")
        public Builder material(String material) {
            this.metaMaterial = MaterialLib.getMaterial(Objects.requireNonNull(material));
            return this;
        }

        @Contract("_ -> this")
        public Builder name(@Nullable Component name) {
            this.name = (name == null ? null : name.color(lastColor));
            return this;
        }

        @Contract("_, _ -> this")
        public Builder name(@Nullable String name, @Nullable TextColor tc) {
            if (name == null) {
                this.name = null;
            } else {
                this.name = Component.text(name);
                if (tc != null) {
                    this.lastColor = tc;
                }
                this.name = this.name.color(this.lastColor);
            }
            return this;
        }

        @Contract("_ -> this")
        public Builder name(@Nullable String name) {
            return this.name(name, null);
        }

        @Contract("_ -> this")
        public Builder color(@Nullable TextColor color) {
            if (name != null) {
                name = name.color(color);
            }
            this.lastColor = color;
            return this;
        }

        @Contract("_ -> this")
        public Builder count(int count) {
            this.count = count;
            return this;
        }

        @Contract("_ -> this")
        public Builder lore(@Nullable Collection<Component> lore) {
            if (lore == null) {
                this.lore = null;
            } else {
                this.lore = new ArrayList<>(lore);
            }
            return this;
        }

        @Contract("_ -> this")
        public Builder lore(@Nullable List<String> lore) {
            if (lore == null) {
                this.lore = null;
            } else {
                this.lore = lore.stream().map(Component::text).collect(Collectors.toCollection(ArrayList::new));
            }
            return this;
        }

        @Contract("_ -> this")
        public Builder addLore(@NotNull Component line) {
            if (this.lore == null) {
                this.lore = new ArrayList<>();
            }
            this.lore.add(line);
            return this;
        }

        @Contract("_ -> this")
        public Builder addLore(@NotNull String lore) {
            return addLore(Component.text(lore));
        }

        @Contract("_ -> this")
        public Builder enchanted(boolean enchanted) {
            this.enchanted = enchanted;
            return this;
        }

        @Contract(" -> this")
        public Builder enchant() {
            this.enchanted = true;
            return this;
        }

        @Contract(" -> new")
        public ItemStack build() {
            return DisplayItem.create(
                    metaMaterial == null ? MaterialLib.getMaterial("STONE") : metaMaterial,
                    name,
                    count,
                    lore,
                    enchanted
            );
        }

    }

}
