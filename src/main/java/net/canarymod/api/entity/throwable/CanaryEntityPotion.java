package net.canarymod.api.entity.throwable;

import net.canarymod.api.entity.EntityType;
import net.canarymod.api.potion.PotionType;

/**
 * EntityPotion wrapper implementation
 *
 * @author Jason (darkdiplomat)
 */
public class CanaryEntityPotion extends CanaryEntityThrowable implements EntityPotion {

    /**
     * Constructs a new wrapper for EntityPotion
     *
     * @param entity
     *         the EntityPotion to be wrapped
     */
    public CanaryEntityPotion(net.minecraft.entity.projectile.EntityPotion entity) {
        super(entity);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENTITYPOTION;
    }

    @Override
    public String getFqName() {
        return "Potion";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getPotionTypeId() {
        return (short) getHandle().o();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PotionType getPotionType() {
        return PotionType.fromTypeID(getPotionTypeId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPotionTypeId(short typeId) {
        getHandle().a(typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPotionType(PotionType type) {
        getHandle().a(type.getTypeId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public net.minecraft.entity.projectile.EntityPotion getHandle() {
        return (net.minecraft.entity.projectile.EntityPotion) entity;
    }

}
