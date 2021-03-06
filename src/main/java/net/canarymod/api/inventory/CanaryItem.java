package net.canarymod.api.inventory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.canarymod.api.attributes.AttributeModifier;
import net.canarymod.api.attributes.CanaryAttributeModifier;
import net.canarymod.api.nbt.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.Map;

/**
 * Item wrapper implementation
 *
 * @author Jason (darkdiplomat)
 */
public class CanaryItem implements Item {

    private ItemType type;
    private int slot = -1;
    private ItemStack item;

    private static int getItemId(ItemStack stack) {
        return net.minecraft.item.Item.b(stack.b());
    }

    private static net.minecraft.item.Item getItemFromId(int id) {
        return net.minecraft.item.Item.b(id);
    }

    /**
     * Constructs a new CanaryItem
     *
     * @param itemStack
     *         the native Minecraft item stack to wrap
     */
    public CanaryItem(ItemStack itemStack) {
        this.type = ItemType.fromIdAndData(getItemId(itemStack), itemStack.i());
        if (this.type == null) {
            // Seems to be an unregistered item type, go ahead an pass an new unnamed itemtype
            this.type = new ItemType(getItemId(itemStack), itemStack.i());
        }
        this.item = itemStack;
    }

    public CanaryItem(int itemId, int amount) {
        this.type = ItemType.fromId(itemId);
        if (this.type == null) {
            // Seems to be an unregistered item type, go ahead an pass an new unnamed itemtype
            this.type = new ItemType(itemId);
        }
        this.item = new ItemStack(getItemFromId(itemId), amount, 0);
    }

    public CanaryItem(int itemId, int amount, int damage) {
        this.item = new ItemStack(getItemFromId(itemId), amount, damage);
        this.type = ItemType.fromIdAndData(itemId, damage);
        if (this.type == null) {
            // Seems to be an unregistered item type, go ahead an pass an new unnamed itemtype
            this.type = new ItemType(itemId, damage);
        }
    }

    public CanaryItem(int itemId, int amount, int damage, int slot) {
        this.item = new ItemStack(getItemFromId(itemId), amount, damage);
        this.slot = slot;
        this.type = ItemType.fromIdAndData(itemId, damage);
        if (this.type == null) {
            // Seems to be an unregistered item type, go ahead an pass an new unnamed itemtype
            this.type = new ItemType(itemId, damage);
        }
    }

    public CanaryItem(ItemType type, int amount, int slot) {
        this.item = new ItemStack(getItemFromId(type.getId()), amount, type.getData());
        this.slot = slot;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return type.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(int id) {
        type = ItemType.fromIdAndData(id, type.getData());
        getHandle().a(getItemFromId(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDamage() {
        return item.i();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAmount() {
        return item.b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSlot() {
        return slot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAmount(int amount) {
        item.b = amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDamage(int damage) {
        item.b(damage);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxAmount() {
        return item.getBaseItem().getMaxStackSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxAmount(int amount) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDisplayName() {
        return item.s();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return item.q();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayName(String name) {
        item.c(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDisplayName() {
        getDataTag().getCompoundTag("display").remove("Name");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRepairCost() {
        return item.A();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRepairCost(int cost) {
        item.c(cost);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getLore() {
        if (!hasLore()) {
            return null;
        }
        ListTag<StringTag> lore = getDataTag().getCompoundTag("display").getListTag("Lore");
        String[] rt = new String[lore.size()];

        for (int index = 0; index < rt.length; index++) {
            rt[index] = ((CanaryStringTag) lore.get(index)).getValue();
        }
        return rt;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setLore(String... lore) {
        CompoundTag tag = getDataTag();

        if (tag == null) {
            tag = new CanaryCompoundTag();
            setDataTag(tag);
        }
        if (!tag.containsKey("display")) {
            tag.put("display", new CanaryCompoundTag());
        }
        CanaryListTag<StringTag> list = new CanaryListTag();

        for (String line : lore) {
            list.add(new CanaryStringTag(line));
        }
        tag.getCompoundTag("display").put("Lore", list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLore() {
        if (hasDataTag()) {
            if (getDataTag().containsKey("display")) {
                if (getDataTag().getCompoundTag("display").containsKey("Lore")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnchanted() {
        return item.w();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnchantable() {
        return item.v();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enchantment getEnchantment() {
        return getEnchantment(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enchantment getEnchantment(int index) {
        if (isEnchanted()) {
            int size = getHandle().p().c();

            if (index >= size) {
                index = 0;
            }
            CompoundTag tag = new CanaryCompoundTag((NBTTagCompound) getHandle().p().b(index));

            return new CanaryEnchantment(Enchantment.Type.fromId(tag.getShort("id")), tag.getShort("lvl"));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enchantment[] getEnchantments() {
        Enchantment[] enchantments = null;

        if (isEnchanted()) {
            int size = getHandle().p().c();

            enchantments = new Enchantment[size];
            CanaryListTag<CompoundTag> nbtTagList = new CanaryListTag<CompoundTag>(getHandle().p());

            for (int i = 0; i < size; i++) {
                CompoundTag tag = nbtTagList.get(i);
                enchantments[i] = new CanaryEnchantment(Enchantment.Type.fromId(tag.getShort("id")), tag.getShort("lvl"));
            }
        }
        return enchantments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEnchantments(Enchantment... enchantments) {
        if (enchantments != null) {
            for (Enchantment ench : enchantments) {
                getHandle().a(((CanaryEnchantment) ench).getHandle(), ench.getLevel());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnchantments(Enchantment... enchantments) {
        removeAllEnchantments();
        addEnchantments(enchantments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEnchantment(Enchantment enchantment) {
        Enchantment[] enchs = getEnchantments();
        removeAllEnchantments();
        for (Enchantment ench : enchs) {
            if (ench.getType() == enchantment.getType() && ench.getLevel() == enchantment.getLevel()) {
                continue;
            }
            getHandle().a(((CanaryEnchantment) ench).getHandle(), ench.getLevel());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllEnchantments() {
        if (isEnchanted()) {
            getDataTag().remove("ench");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDataTag() {
        return item.n();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompoundTag getDataTag() {
        return item.n() ? new CanaryCompoundTag(item.o()) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDataTag(CompoundTag tag) {
        getHandle().d(tag == null ? null : ((CanaryCompoundTag) tag).getHandle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMetaTag() {
        if (hasDataTag()) {
            return getDataTag().containsKey("Canary");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompoundTag getMetaTag() {
        CompoundTag dataTag = getDataTag();

        if (dataTag == null) {
            dataTag = new CanaryCompoundTag();
            setDataTag(dataTag);
        }
        if (!dataTag.containsKey("Canary")) {
            dataTag.put("Canary", new CanaryCompoundTag());
        }
        return dataTag.getCompoundTag("Canary");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompoundTag writeToTag(CompoundTag tag) {
        return new CanaryCompoundTag(getHandle().b(((CanaryCompoundTag) tag).getHandle()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFromTag(CompoundTag tag) {
        getHandle().c(((CanaryCompoundTag) tag).getHandle());
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributes() {
        Multimap rawAttributes = getHandle().B();
        Multimap<String, AttributeModifier> toRet = HashMultimap.create();
        for (Map.Entry entry : (Collection<Map.Entry>) rawAttributes.entries()) {
            toRet.put((String) entry.getKey(), ((net.minecraft.entity.ai.attributes.AttributeModifier) entry.getValue()).getWrapper());
        }
        return toRet;
    }

    @Override
    public void updateAttributes(Multimap<String, AttributeModifier> attributes) {
        Multimap rawAttributes = getHandle().B();
        rawAttributes.clear(); // Clear out the old stuff
        for (Map.Entry<String, AttributeModifier> entry : attributes.entries()) {
            rawAttributes.put(entry.getKey(), ((CanaryAttributeModifier) entry.getValue()).getNative());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equalsIgnoreSize(Item item) {
        return item == null ? false : this.getId() == item.getId() && this.getDamage() == item.getDamage() && (this.getDataTag() == null ? item.getDataTag() == null : this.getDataTag().equals(item.getDataTag()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseItem getBaseItem() {
        return item.getBaseItem();
    }

    /**
     * Gets the ItemStack being wrapped
     */
    public ItemStack getHandle() {
        return item;
    }

    public static Item[] stackArrayToItemArray(ItemStack[] stackarray) {
        CanaryItem[] items = new CanaryItem[stackarray.length];

        for (int index = 0; index < stackarray.length; index++) {
            if (stackarray[index] != null) {
                items[index] = nativeToItem(stackarray[index]);
                items[index].setSlot(index);
            }
        }
        return items;
    }

    public static ItemStack[] itemArrayToStackArray(Item[] itemarray) {
        ItemStack[] stacks = new ItemStack[itemarray.length];

        for (int index = 0; index < itemarray.length; index++) {
            if (itemarray[index] != null) {
                stacks[index] = itemToNative(itemarray[index]);
            }
        }
        return stacks;
    }

    /**
     * Safely converts an {@link net.canarymod.api.inventory.Item} to an ItemStack
     */
    public static ItemStack itemToNative(Item item){
        if(item != null && item instanceof CanaryItem){
            return ((CanaryItem)item).getHandle();
        }
        return null;
    }

    /**
     * Safely converts an ItemStack to an {@link net.canarymod.api.inventory.Item}
     */
    public static CanaryItem nativeToItem(ItemStack itemstack){
        if(itemstack != null){
            return itemstack.getCanaryItem();
        }
        return null;
    }

    @Override
    public Item clone() {
        return getHandle().k().getCanaryItem();
    }

    /**
     * Returns a String value representing this object
     *
     * @return String representation of this object
     */
    @Override
    public String toString() {
        return String.format("Item[id=%d, amount=%d, slot=%d, damage=%d, Name=%s, isEnchanted=%b, hasLore=%b]", getId(), getAmount(), slot, getDamage(), getDisplayName(), isEnchanted(), hasLore());
    }

    /**
     * Tests the given object to see if it equals this object
     *
     * @param obj
     *         the object to test
     *
     * @return true if the two objects match
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemStack) {
            return ItemStack.b(item, (ItemStack) obj);
        }
        else if (obj instanceof CanaryItem) {
            return ItemStack.b(item, ((CanaryItem) obj).getHandle());
        }
        return false;
    }

    /**
     * Returns a semi-unique hashcode for this object
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = 97 * hash + getId();
        hash = 97 * hash + getAmount();
        hash = 97 * hash + slot;
        hash = 97 * hash + getDamage();
        return hash;
    }
}
