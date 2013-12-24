package net.minecraft.entity.item;

import net.canarymod.api.CanaryDamageSource;
import net.canarymod.api.DamageType;
import net.canarymod.api.entity.hanging.CanaryItemFrame;
import net.canarymod.api.entity.hanging.HangingEntity;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.hook.entity.HangingEntityDestroyHook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class EntityItemFrame extends EntityHanging {

    public float e = 1.0F; // CanaryMod: private -> public

    public EntityItemFrame(World world) {
        super(world);
        this.entity = new CanaryItemFrame(this); // CanaryMod: Wrap Entity
    }

    public EntityItemFrame(World world, int i0, int i1, int i2, int i3) {
        super(world, i0, i1, i2, i3);
        this.a(i3);
        this.entity = new CanaryItemFrame(this); // CanaryMod: Wrap Entity
    }

    protected void c() {
        this.z().a(2, 5);
        this.z().a(3, Byte.valueOf((byte) 0));
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else if (this.j() != null) {
            if (!this.p.E) {
                this.b(damagesource.j(), false);
                this.a((ItemStack) null);
            }

            return true;
        }
        else {
            return super.a(damagesource, f0);
        }
    }

    public int f() {
        return 9;
    }

    public int i() {
        return 9;
    }

    public void b(Entity entity) {
        this.b(entity, true);
    }

    public void b(Entity entity, boolean flag0) {
        //CanaryMod start
        HangingEntityDestroyHook hook = null;
        boolean isPlayer = false;
        if (entity instanceof EntityPlayer) {
            isPlayer = true;
            hook = (HangingEntityDestroyHook) new HangingEntityDestroyHook((HangingEntity) this.getCanaryEntity(), (Player) entity.getCanaryEntity(), CanaryDamageSource.getDamageSourceFromType(DamageType.GENERIC)).call();
        }
        else {
            hook = (HangingEntityDestroyHook) new HangingEntityDestroyHook((HangingEntity) this.getCanaryEntity(), null, CanaryDamageSource.getDamageSourceFromType(DamageType.GENERIC)).call();
        }
        if (hook.isCanceled()) {
            return;
        }
        //CanaryMod end

        ItemStack itemstack = this.j();

        if (entity instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entity;

            if (entityplayer.bF.d) {
                this.b(itemstack);
                return;
            }
        }

        if (flag0) {
            this.a(new ItemStack(Items.bD), 0.0F);
        }

        if (itemstack != null && this.aa.nextFloat() < this.e) {
            itemstack = itemstack.m();
            this.b(itemstack);
            this.a(itemstack, 0.0F);
        }
    }

    private void b(ItemStack itemstack) {
        if (itemstack != null) {
            if (itemstack.b() == Items.aY) {
                MapData mapdata = ((ItemMap) itemstack.b()).a(itemstack, this.p);

                mapdata.g.remove("frame-" + this.y());
            }

            itemstack.a((EntityItemFrame) null);
        }
    }

    public ItemStack j() {
        return this.z().f(2);
    }

    public void a(ItemStack itemstack) {
        if (itemstack != null) {
            itemstack = itemstack.m();
            itemstack.b = 1;
            itemstack.a(this);
        }

        this.z().b(2, itemstack);
        this.z().h(2);
    }

    public int k() {
        return this.z().a(3);
    }

    public void c(int i0) {
        this.z().b(3, Byte.valueOf((byte) (i0 % 4)));
    }

    public void b(NBTTagCompound nbttagcompound) {
        if (this.j() != null) {
            nbttagcompound.a("Item", (NBTBase) this.j().b(new NBTTagCompound()));
            nbttagcompound.a("ItemRotation", (byte) this.k());
            nbttagcompound.a("ItemDropChance", this.e);
        }

        super.b(nbttagcompound);
    }

    public void a(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = nbttagcompound.m("Item");

        if (nbttagcompound1 != null && !nbttagcompound1.d()) {
            this.a(ItemStack.a(nbttagcompound1));
            this.c(nbttagcompound.d("ItemRotation"));
            if (nbttagcompound.b("ItemDropChance", 99)) {
                this.e = nbttagcompound.h("ItemDropChance");
            }
        }

        super.a(nbttagcompound);
    }

    public boolean c(EntityPlayer entityplayer) {
        if (this.j() == null) {
            ItemStack itemstack = entityplayer.be();

            if (itemstack != null && !this.p.E) {
                this.a(itemstack);
                if (!entityplayer.bF.d && --itemstack.b <= 0) {
                    entityplayer.bn.a(entityplayer.bn.c, (ItemStack) null);
                }
            }
        }
        else if (!this.p.E) {
            this.c(this.k() + 1);
        }

        return true;
    }
}
