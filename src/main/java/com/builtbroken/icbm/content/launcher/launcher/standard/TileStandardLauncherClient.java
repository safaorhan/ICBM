package com.builtbroken.icbm.content.launcher.launcher.standard;

import com.builtbroken.icbm.content.Assets;
import com.builtbroken.icbm.content.crafting.missile.MissileModuleBuilder;
import com.builtbroken.icbm.content.crafting.missile.casing.Missile;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/13/2016.
 */
public class TileStandardLauncherClient extends TileStandardLauncher
{
    private Missile missile;
    private AxisAlignedBB renderBounds;

    @Override
    public Tile newTile()
    {
        return new TileStandardLauncherClient();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        if (renderBounds == null)
        {
            renderBounds = AxisAlignedBB.getBoundingBox(x(), y(), z(), z() + 1, y() + 1, z() + 1);
        }
        return renderBounds;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos center, float frame, int pass)
    {
        if (missile != null)
        {
            //Render launcher
            GL11.glPushMatrix();
            Pos pos = center;

            switch (ForgeDirection.getOrientation(getMetadata()))
            {
                case NORTH:
                    pos = pos.add(0.7, -0.5, 1.9);
                    break;
                case SOUTH:
                    pos = pos.add(0.7, -0.5, 3.86);
                    break;
                case EAST:
                    pos = pos.add(1.7, -0.5, 2.86);
                    break;
                case WEST:
                    pos = pos.add(-0.3, -0.5, 2.86);
                    break;
            }
            GL11.glTranslatef(pos.xf(), pos.yf(), pos.zf());
            GL11.glRotatef(45f, 0, 1, 0);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(Assets.GREY_FAKE_TEXTURE);
            Assets.STANDARD_MISSILE_MODEL.renderAll();
            GL11.glPopMatrix();
        }
        else if (recipe != null)
        {
            //Render launcher
            GL11.glPushMatrix();
            Pos pos = center;

            switch (ForgeDirection.getOrientation(getMetadata()))
            {
                case NORTH:
                    pos = pos.add(0.7, -0.5, 1.9);
                    break;
                case SOUTH:
                    pos = pos.add(0.7, -0.5, 3.86);
                    break;
                case EAST:
                    pos = pos.add(1.7, -0.5, 2.86);
                    break;
                case WEST:
                    pos = pos.add(-0.3, -0.5, 2.86);
                    break;
            }
            GL11.glTranslatef(pos.xf(), pos.yf(), pos.zf());
            GL11.glRotatef(45f, 0, 1, 0);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(Assets.GREY_FAKE_TEXTURE);
            //TODO render crafting progress
            //TODO render ghost of missile frame
            Assets.STANDARD_MISSILE_MODEL_2.renderAll();
            GL11.glPopMatrix();
        }
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        byte type = buf.readByte();
        if (type == 0)
        {
            ItemStack missileStack = ByteBufUtils.readItemStack(buf);
            missile = MissileModuleBuilder.INSTANCE.buildMissile(missileStack);
        }
        else if (type == 1)
        {
            this.missile = null;
            if (recipe == null)
            {
                recipe = new StandardMissileCrafting();
            }
            recipe.readBytes(buf);
        }
        else if (type == 2 || type == 3)
        {
            this.missile = null;
            this.recipe = null;
            this.isCrafting = false;
        }

        if (missile != null || recipe != null)
        {
            //TODO modified to fit missile rotation
            renderBounds = new Cube(-1, 0, -1, 2, 8, 2).add(x(), y(), z()).toAABB();
        }
    }
}
