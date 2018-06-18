package org.enginehub.util.forge.dumper;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings({"unchecked"})
public class BlockRegistryDumper extends RegistryDumper<Block> {

    public BlockRegistryDumper(File file) {
        super(file);
    }

    @Override
    public void registerAdapters(GsonBuilder builder) {
        super.registerAdapters(builder);

        builder.registerTypeAdapter(Vec3i.class, new Vec3iAdapter());
        builder.registerTypeAdapter(Vec3d.class, new Vec3dAdapter());
    }

    @Override
    public IForgeRegistry<Block> getRegistry() {
        return ForgeRegistries.BLOCKS;
    }

    @Override
    public Comparator<Map<String, Object>> getComparator() {
        return new MapComparator();
    }

    @Override
    public List<Map<String, Object>> getProperties(Entry<ResourceLocation, Block> e) {
        Map<String, Object> map = new LinkedHashMap<>();
        Block b = e.getValue();
        map.put("legacyId", Block.getIdFromBlock(b));
        map.put("id", e.getKey().toString());
        map.put("unlocalizedName", b.getUnlocalizedName());
        map.put("localizedName", b.getLocalizedName());
        map.put("states", getStates(b));
        map.put("material", getMaterial(b));
        return Lists.newArrayList(map);
    }

    private Map<String, Map> getStates(Block b) {
        Map<String, Map> map = new LinkedHashMap<>();
        BlockStateContainer bs = b.getBlockState();
        Collection<IProperty<?>> props = bs.getProperties();
        for (IProperty prop : props) {
            map.put(prop.getName(), dataValues(prop));
        }

        return map;
    }

    private Map<String, List> dataValues(IProperty prop) {
        //BlockState bs = b.getBlockState();
        List<Object> valueList = new ArrayList<>();
        for (Comparable val : (Iterable<Comparable>) prop.getAllowedValues()) {
            valueList.add(prop.getName(val));
        }

        Map<String, List> dataMap = new HashMap<>();
        dataMap.put("values", valueList);
        return dataMap;
    }

    private Map<String, Object> getMaterial(Block b) {
        IBlockState bs = b.getDefaultState();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("powerSource", b.canProvidePower(bs));
        map.put("lightOpacity", b.getLightOpacity(bs));
        map.put("lightValue", b.getLightValue(bs));
        map.put("usingNeighborLight", b.getUseNeighborBrightness(bs));
        map.put("hardness", getField(b, Block.class, "blockHardness", "field_149782_v"));
        map.put("resistance", getField(b, Block.class, "blockResistance", "field_149781_w"));
        map.put("ticksRandomly", b.getTickRandomly());
        map.put("fullCube", b.isFullCube(bs));
        map.put("slipperiness", b.slipperiness);
        map.put("renderedAsNormalBlock", b.isFullBlock(bs));
        //map.put("solidFullCube", b.isSolidFullCube());
        Material m = b.getMaterial(bs);
        map.put("liquid", m.isLiquid());
        map.put("solid", m.isSolid());
        map.put("movementBlocker", m.blocksMovement());
        //map.put("blocksLight", m.blocksLight());
        map.put("burnable", m.getCanBurn());
        map.put("opaque", m.isOpaque());
        map.put("replacedDuringPlacement", m.isReplaceable());
        map.put("toolRequired", !m.isToolNotRequired());
        map.put("fragileWhenPushed", m.getMobilityFlag() == EnumPushReaction.DESTROY);
        map.put("unpushable", m.getMobilityFlag() == EnumPushReaction.BLOCK);
        map.put("adventureModeExempt", getField(m, Material.class, "isAdventureModeExempt", "field_85159_M"));
        //map.put("mapColor", rgb(m.getMaterialMapColor().colorValue));

        try {
            map.put("ambientOcclusionLightValue", bs.getAmbientOcclusionLightValue());
        } catch (NoSuchMethodError ignored) {
            map.put("ambientOcclusionLightValue", bs.isBlockNormalCube() ? 0.2F : 1.0F);
        }
        map.put("grassBlocking", false); // idk what this property was originally supposed to be...grass uses a combination of light values to check growth
        return map;
    }

    public static class Vec3iAdapter extends TypeAdapter<Vec3i> {
        @Override
        public Vec3i read(final JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
        @Override
        public void write(final JsonWriter out, final Vec3i vec) throws IOException {
            out.beginArray();
            out.value(vec.getX());
            out.value(vec.getY());
            out.value(vec.getZ());
            out.endArray();
        }
    }

    public static class Vec3dAdapter extends TypeAdapter<Vec3d> {
        @Override
        public Vec3d read(final JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
        @Override
        public void write(final JsonWriter out, final Vec3d vec) throws IOException {
            out.beginArray();
            out.value(vec.x);
            out.value(vec.y);
            out.value(vec.z);
            out.endArray();
        }
    }

    private static class MapComparator implements Comparator<Map<String, Object>> {
        @Override
        public int compare(Map<String, Object> a, Map<String, Object> b) {
            return ((Integer) a.get("legacyId")).compareTo((Integer) b.get("legacyId"));
        }
    }
}