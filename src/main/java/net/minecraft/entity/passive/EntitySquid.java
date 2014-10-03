package net.minecraft.entity.passive;

import net.canarymod.api.entity.living.animal.CanarySquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;


public class EntitySquid extends EntityWaterMob {

    public float a;
    public float b;
    public float c;
    public float bi;
    public float bj;
    public float bk;
    public float bl;
    public float bm;
    private float bn;
    private float bo;
    private float bp;
    private float bq;
    private float br;
    private float bs;
   
    public EntitySquid(World world) {
        super(world);
        this.a(0.95F, 0.95F);
        this.V.setSeed((long) (1 + this.F()));
        this.bo = 1.0F / (this.V.nextFloat() + 1.0F) * 0.2F;
        this.i.a(0, new EntitySquid.AIMoveRandom());
        this.entity = new CanarySquid(this); // CanaryMod: Wrap Entity
    }

    protected void aW() {
        super.aW();
        this.a(SharedMonsterAttributes.a).a(10.0D);
    }

    public float aR() {
        return this.K * 0.5F;
    }

    protected String z() {
        return null;
    }

    protected String bn() {
        return null;
    }

    protected String bo() {
        return null;
    }

    protected float bA() {
        return 0.4F;
    }

    protected Item A() {
        return null;
    }

    protected boolean r_() {
        return false;
    }

    protected void b(boolean flag0, int i0) {
        int i1 = this.V.nextInt(3 + i0) + 1;

        for (int i2 = 0; i2 < i1; ++i2) {
            this.a(new ItemStack(Items.aW, 1, EnumDyeColor.BLACK.b()), 0.0F);
        }

    }

    public boolean V() {
        return this.o.a(this.aQ().b(0.0D, -0.6000000238418579D, 0.0D), Material.h, (Entity) this);
    }

    public void m() {
        super.m();
        this.b = this.a;
        this.bi = this.c;
        this.bk = this.bj;
        this.bm = this.bl;
        this.bj += this.bo;
        if ((double) this.bj > 6.283185307179586D) {
            if (this.o.D) {
                this.bj = 6.2831855F;
            } else {
                this.bj = (float) ((double) this.bj - 6.283185307179586D);
                if (this.V.nextInt(10) == 0) {
                    this.bo = 1.0F / (this.V.nextFloat() + 1.0F) * 0.2F;
                }

                this.o.a((Entity) this, (byte) 19);
            }
        }

        if (this.Y) {
            float f0;

            if (this.bj < 3.1415927F) {
                f0 = this.bj / 3.1415927F;
                this.bl = MathHelper.a(f0 * f0 * 3.1415927F) * 3.1415927F * 0.25F;
                if ((double) f0 > 0.75D) {
                    this.bn = 1.0F;
                    this.bp = 1.0F;
                } else {
                    this.bp *= 0.8F;
                }
            } else {
                this.bl = 0.0F;
                this.bn *= 0.9F;
                this.bp *= 0.99F;
            }

            if (!this.o.D) {
                this.v = (double) (this.bq * this.bn);
                this.w = (double) (this.br * this.bn);
                this.x = (double) (this.bs * this.bn);
            }

            f0 = MathHelper.a(this.v * this.v + this.x * this.x);
            this.aG += (-((float) Math.atan2(this.v, this.x)) * 180.0F / 3.1415927F - this.aG) * 0.1F;
            this.y = this.aG;
            this.c = (float) ((double) this.c + 3.141592653589793D * (double) this.bp * 1.5D);
            this.a += (-((float) Math.atan2((double) f0, this.w)) * 180.0F / 3.1415927F - this.a) * 0.1F;
        } else {
            this.bl = MathHelper.e(MathHelper.a(this.bj)) * 3.1415927F * 0.25F;
            if (!this.o.D) {
                this.v = 0.0D;
                this.w -= 0.08D;
                this.w *= 0.9800000190734863D;
                this.x = 0.0D;
            }

            this.a = (float) ((double) this.a + (double) (-90.0F - this.a) * 0.02D);
        }

    }

    public void g(float f0, float f1) {
        this.d(this.v, this.w, this.x);
    }

    public boolean bQ() {
        return this.t > 45.0D && this.t < 63.0D && super.bQ();
    }

    public void b(float f0, float f1, float f2) {
        this.bq = f0;
        this.br = f1;
        this.bs = f2;
    }

    public boolean n() {
        return this.bq != 0.0F || this.br != 0.0F || this.bs != 0.0F;
    }

    class AIMoveRandom extends EntityAIBase {

        private EntitySquid a = EntitySquid.this;
      
        public boolean a() {
            return true;
        }

        public void e() {
            int i0 = this.a.bg();

            if (i0 > 100) {
                this.a.b(0.0F, 0.0F, 0.0F);
            } else if (this.a.bb().nextInt(50) == 0 || !this.a.Y || !this.a.n()) {
                float f0 = this.a.bb().nextFloat() * 3.1415927F * 2.0F;
                float f1 = MathHelper.b(f0) * 0.2F;
                float f2 = -0.1F + this.a.bb().nextFloat() * 0.2F;
                float f3 = MathHelper.a(f0) * 0.2F;

                this.a.b(f1, f2, f3);
            }

        }
    }
}
