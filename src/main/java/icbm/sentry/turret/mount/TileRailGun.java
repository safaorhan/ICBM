package icbm.sentry.turret.mount;

import icbm.Reference;
import icbm.sentry.ICBMSentry;
import icbm.sentry.ProjectileType;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.multiblock.link.IMultiBlock;
import calclavia.lib.prefab.tile.IRedstoneReceptor;

/** Railgun
 * 
 * @author Calclavia */
public class TileRailGun extends TileMountableTurret implements IRedstoneReceptor, IMultiBlock
{
    private int gunChargingTicks = 0;

    private boolean redstonePowerOn = false;
    /** Is current ammo antimatter */
    private boolean isAntimatter;

    private float explosionSize;

    private int explosionDepth;

    /** A counter used client side for the smoke and streaming effects of the Railgun after a shot. */
    private int endTicks = 0;

    public TileRailGun()
    {
        this.baseFiringDelay = 80;
        this.minFiringDelay = 50;
        this.getPitchServo().setLimits(60, -60);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (this.getPlatform() != null)
        {
            if (this.redstonePowerOn)
            {
                this.tryActivateWeapon();
            }

            if (this.gunChargingTicks > 0)
            {
                this.gunChargingTicks++;

                if (this.gunChargingTicks >= this.getFireDelay())
                {
                    this.onFire();
                    this.gunChargingTicks = 0;
                }
            }

            if (this.worldObj.isRemote && this.endTicks-- > 0)
            {
                MovingObjectPosition objectMouseOver = this.rayTrace(2000);

                if (objectMouseOver != null && objectMouseOver.hitVec != null)
                {
                    this.drawParticleStreamTo(new Vector3(objectMouseOver.hitVec));
                }
            }
        }
    }

    public void tryActivateWeapon()
    {
        if (this.canActivateWeapon() && this.gunChargingTicks == 0)
        {
            this.onWeaponActivated();
        }
    }

    @SuppressWarnings("unchecked")
    public void onFire()
    {
        if (!this.worldObj.isRemote)
        {
            while (this.explosionDepth > 0)
            {
                MovingObjectPosition objectMouseOver = this.rayTrace(2000);

                if (objectMouseOver != null)
                {

                    if (this.isAntimatter)
                    {
                        /** Remove Redmatter Explosions. */
                        int radius = 50;
                        AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(objectMouseOver.blockX - radius, objectMouseOver.blockY - radius, objectMouseOver.blockZ - radius, objectMouseOver.blockX + radius, objectMouseOver.blockY + radius, objectMouseOver.blockZ + radius);
                        List<Entity> missilesNearby = worldObj.getEntitiesWithinAABB(Entity.class, bounds);

                        // for (Entity entity : missilesNearby)
                        // {
                        // if (entity instanceof IExplosive)
                        // {
                        // entity.setDead();
                        // }
                        // }
                    }

                    Vector3 blockPosition = new Vector3(objectMouseOver.hitVec);

                    int blockID = blockPosition.getBlockID(this.worldObj);
                    Block block = Block.blocksList[blockID];

                    // Any hardness under zero is unbreakable
                    if (block != null && block.getBlockHardness(this.worldObj, blockPosition.intX(), blockPosition.intY(), blockPosition.intZ()) != -1)
                    {
                        this.worldObj.setBlock(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ, 0, 0, 2);
                    }

                    Entity responsibleEntity = this.entityFake != null ? this.entityFake.riddenByEntity : null;
                    this.worldObj.newExplosion(responsibleEntity, blockPosition.x, blockPosition.y, blockPosition.z, explosionSize, true, true);
                }

                this.explosionDepth--;
            }
        }
    }

    public void renderShot(Vector3 target)
    {
        this.endTicks = 20;
    }

    public void playFiringSound()
    {
        this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, Reference.PREFIX + "railgun", 5F, 1F);
    }

    @Override
    public Vector3[] getMultiBlockVectors()
    {
        return new Vector3[] { new Vector3(0, 1, 0) };
    }

    @Override
    public Vector3 pos()
    {
        return new Vector3(this).add(new Vector3(0.5, 1.5, 0.5));
    }

    @Override
    public void onPowerOn()
    {
        this.redstonePowerOn = true;
    }

    @Override
    public void onPowerOff()
    {
        this.redstonePowerOn = false;
    }

    public long getFiringRequest()
    {
        return 10000;
    }

    @Override
    public void onWeaponActivated()
    {
        super.onWeaponActivated();
        this.gunChargingTicks = 1;
        this.redstonePowerOn = false;
        this.isAntimatter = false;
        ItemStack ammoStack = this.getPlatform().hasAmmunition(ProjectileType.RAILGUN);

        if (ammoStack != null)
        {
            if (ammoStack.equals(ICBMSentry.antimatterBullet) && this.getPlatform().useAmmunition(ammoStack))
            {
                this.isAntimatter = true;
            }
            else
            {
                this.getPlatform().useAmmunition(ammoStack);
            }
        }

        // TODO: Somehow this energy request method does not work.
        // this.getPlatform().provideElectricity(this.getFiringRequest(), true);
        this.getPlatform().setEnergy(ForgeDirection.UNKNOWN, 0);

        this.explosionSize = 5f;
        this.explosionDepth = 5;

        if (this.isAntimatter)
        {
            this.explosionSize = 8f;
            this.explosionDepth = 10;
        }

        this.playFiringSound();
    }

    public boolean canActivateWeapon()
    {
        if (this.getPlatform() != null)
        {
            if (this.getPlatform().hasAmmunition(ProjectileType.RAILGUN) != null)
            {
                if (this.getPlatform().energy.getEnergy() >= this.getFiringRequest())
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int getMaxHealth()
    {
        return 450;
    }

}