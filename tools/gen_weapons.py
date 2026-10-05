#!/usr/bin/env python3
"""Generates the extra Morrowind weapons from the table below. Run from the repo root:  python tools/gen_weapons.py

Writes   src/main/java/com/minewind/MorrowindWeapons.java            (always regenerated)
Adds     models/item/<id>.json and textures/item/<id>.png            (only if missing, never overwrites your art)
Merges   lang/en_us.json names and tags/item/swords.json             (adds missing entries, keeps everything else)
To add a weapon, add (tier, kind) to ROWS and re-run. Stats are rough approximations of Morrowind; tune the tables.
"""
import json, os, struct, zlib

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, '..', 'src', 'main')
ASSETS = os.path.join(SRC, 'resources', 'assets', 'minewind')
SKIP = {'iron_dagger', 'steel_longsword', 'steel_claymore', 'ebony_broadsword'}  # registered in ModItems

# kind: (display, damage, attack speed modifier, reach bonus, base durability, axe?)
KINDS = {
    'dagger': ('Dagger', 1, -1.5, -0.5, 150, False), 'shortsword': ('Shortsword', 2, -1.8, -0.25, 250, False),
    'longsword': ('Longsword', 3, -2.4, 0.0, 350, False), 'claymore': ('Claymore', 5, -3.0, 0.75, 400, False),
    'waraxe': ('War Axe', 4, -3.0, 0.0, 300, True), 'battleaxe': ('Battle Axe', 6, -3.2, 0.5, 400, True),
    'mace': ('Mace', 3, -2.6, 0.0, 350, False), 'warhammer': ('Warhammer', 7, -3.3, 0.5, 450, False),
    'club': ('Club', 2, -2.4, 0.0, 250, False), 'spear': ('Spear', 3, -2.6, 1.0, 300, False),
    'halberd': ('Halberd', 6, -3.1, 1.25, 400, False), 'staff': ('Staff', 1, -2.2, 1.0, 200, False),
}
# tier: (name, ToolMaterials constant, damage bonus, durability multiplier, rarity, icon colour)
TIERS = {
    'wooden': ('Wooden', 'WOOD', 0, 0.6, 'COMMON', (140, 100, 60)), 'chitin': ('Chitin', 'STONE', 0, 0.7, 'COMMON', (190, 170, 120)),
    'iron': ('Iron', 'IRON', 0, 1.0, 'COMMON', (150, 150, 160)), 'steel': ('Steel', 'IRON', 1, 1.4, 'COMMON', (190, 195, 205)),
    'silver': ('Silver', 'IRON', 1, 1.2, 'UNCOMMON', (225, 230, 240)), 'dwarven': ('Dwarven', 'IRON', 1, 2.0, 'UNCOMMON', (200, 150, 70)),
    'nordic': ('Nordic', 'IRON', 1, 1.8, 'UNCOMMON', (150, 170, 190)), 'orcish': ('Orcish', 'IRON', 3, 2.2, 'UNCOMMON', (70, 110, 70)),
    'ebony': ('Ebony', 'DIAMOND', 2, 3.0, 'RARE', (40, 40, 50)), 'glass': ('Glass', 'DIAMOND', 3, 2.5, 'RARE', (120, 220, 160)),
    'daedric': ('Daedric', 'DIAMOND', 4, 4.0, 'EPIC', (170, 40, 40)),
}
ROWS = [
    ('steel', 'dagger'), ('silver', 'dagger'), ('dwarven', 'dagger'), ('ebony', 'dagger'), ('glass', 'dagger'), ('daedric', 'dagger'),
    ('chitin', 'shortsword'), ('steel', 'shortsword'), ('silver', 'shortsword'), ('dwarven', 'shortsword'),
    ('iron', 'longsword'), ('silver', 'longsword'), ('ebony', 'longsword'), ('glass', 'longsword'), ('daedric', 'longsword'),
    ('dwarven', 'claymore'), ('nordic', 'claymore'), ('daedric', 'claymore'),
    ('iron', 'waraxe'), ('steel', 'waraxe'), ('silver', 'waraxe'), ('steel', 'battleaxe'), ('nordic', 'battleaxe'), ('orcish', 'battleaxe'),
    ('iron', 'mace'), ('silver', 'mace'), ('iron', 'warhammer'), ('steel', 'warhammer'), ('dwarven', 'warhammer'), ('daedric', 'warhammer'),
    ('wooden', 'club'), ('wooden', 'staff'),
    ('chitin', 'spear'), ('iron', 'spear'), ('steel', 'spear'), ('silver', 'spear'), ('dwarven', 'spear'), ('ebony', 'spear'), ('daedric', 'spear'),
    ('steel', 'halberd'), ('daedric', 'halberd'),
]

def png(path, pixels):
    rows = b''.join(b'\0' + b''.join(bytes(p) for p in row) for row in pixels)
    ch = lambda t, d: struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    open(path, 'wb').write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', 16, 16, 8, 6, 0, 0, 0)) + ch(b'IDAT', zlib.compress(rows)) + ch(b'IEND', b''))

def icon(kind, col):
    px = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    brown, gold = (110, 70, 30, 255), (200, 170, 60, 255)
    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16: px[y][x] = c
    def diag(a, b, c, thick=False):
        for i in range(a, b):
            put(i, 15 - i, c)
            if thick: put(i - 1, 15 - i, c)
    if kind in ('dagger', 'shortsword', 'longsword', 'claymore'):
        end = {'dagger': 9, 'shortsword': 11, 'longsword': 13, 'claymore': 14}[kind]
        diag(1, 4, brown); diag(5, end, col + (255,), True)
        for x, y in [(4, 10), (5, 9), (3, 9), (4, 8), (6, 11)]: put(x, y, gold)
    elif kind in ('waraxe', 'battleaxe', 'mace', 'warhammer', 'club', 'staff'):
        diag(2, 14 if kind == 'staff' else 12, brown, kind in ('club',))
        if kind in ('waraxe', 'battleaxe'):
            for x, y in [(9, 4), (10, 3), (11, 3), (10, 4), (11, 4), (9, 3), (12, 4), (10, 5)]: put(x, y, col + (255,))
        elif kind in ('mace', 'warhammer'):
            for dx in range(-1, 2):
                for dy in range(-1, 2): put(11 + dx, 4 + dy, col + (255,))
    else:  # spear, halberd
        diag(1, 13, brown)
        for x, y in [(13, 2), (12, 3), (14, 1), (14, 2), (13, 1)]: put(x, y, col + (255,))
        if kind == 'halberd':
            for x, y in [(9, 4), (10, 4), (9, 5), (8, 5), (10, 5)]: put(x, y, col + (255,))
    return px

def main():
    os.makedirs(os.path.join(ASSETS, 'models', 'item'), exist_ok=True)
    os.makedirs(os.path.join(ASSETS, 'textures', 'item'), exist_ok=True)
    entries, names, ids = [], {}, []
    for tier, kind in ROWS:
        wid = f'{tier}_{kind}'
        if wid in SKIP: continue
        tname, mat, bonus, dmult, rarity, col = TIERS[tier]
        kname, dmg, speed, reach, dur, axe = KINDS[kind]
        names[f'item.minewind.{wid}'] = f'{tname} {kname}'
        ids.append(wid)
        entries.append(f'        add("{wid}", ToolMaterials.{mat}, {str(axe).lower()}, {dmg + bonus}, {speed}f, {reach}, {int(dur * dmult)}, Rarity.{rarity});')
        mp = os.path.join(ASSETS, 'models', 'item', wid + '.json')
        if not os.path.exists(mp):
            open(mp, 'w').write(json.dumps({"parent": "minecraft:item/handheld", "textures": {"layer0": f"minewind:item/{wid}"}}, indent=2) + '\n')
        tp = os.path.join(ASSETS, 'textures', 'item', wid + '.png')
        if not os.path.exists(tp): png(tp, icon(kind, col))

    java = '''package com.minewind;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;

/** GENERATED by tools/gen_weapons.py - edit the table there, not this file. Stats are rough Morrowind approximations. */
public final class MorrowindWeapons {
    public static final List<Item> ALL = new ArrayList<>();

    static {
%s
    }

    private static void add(String id, ToolMaterial material, boolean axe, int damage, float speed, double reach, int durability, Rarity rarity) {
        AttributeModifiersComponent base = axe
                ? AxeItem.createAttributeModifiers(material, damage, speed)
                : SwordItem.createAttributeModifiers(material, damage, speed);
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        for (AttributeModifiersComponent.Entry entry : base.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        if (reach != 0.0) {
            builder.add(
                    EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                    new EntityAttributeModifier(Identifier.of(MinewindMod.MOD_ID, id + "_reach"), reach, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND);
        }
        Item.Settings settings = new Item.Settings().maxDamage(durability).rarity(rarity).attributeModifiers(builder.build());
        Item item = axe ? new AxeItem(material, settings) : new SwordItem(material, settings);
        ALL.add(Registry.register(Registries.ITEM, Identifier.of(MinewindMod.MOD_ID, id), item));
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> ALL.forEach(entries::add));
    }

    private MorrowindWeapons() {}
}
''' % '\n'.join(entries)
    open(os.path.join(SRC, 'java', 'com', 'minewind', 'MorrowindWeapons.java'), 'w').write(java)

    lp = os.path.join(ASSETS, 'lang', 'en_us.json')
    lang = json.load(open(lp)) if os.path.exists(lp) else {}
    for k, v in names.items(): lang.setdefault(k, v)
    os.makedirs(os.path.dirname(lp), exist_ok=True)
    open(lp, 'w').write(json.dumps(lang, indent=2) + '\n')

    tp = os.path.join(SRC, 'resources', 'data', 'minecraft', 'tags', 'item', 'swords.json')
    tag = json.load(open(tp)) if os.path.exists(tp) else {"replace": False, "values": []}
    for wid in ids:
        if f'minewind:{wid}' not in tag['values']: tag['values'].append(f'minewind:{wid}')
    os.makedirs(os.path.dirname(tp), exist_ok=True)
    open(tp, 'w').write(json.dumps(tag, indent=2) + '\n')
    print(f'{len(ids)} weapons generated')

main()
