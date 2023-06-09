/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.ChatUtils;

@SearchTags({"kill potion", "KillerPotion", "killer potion", "KillingPotion",
	"killing potion", "InstantDeathPotion", "instant death potion"})
public final class KillPotionHack extends Hack
{
	private final EnumSetting<PotionType> potionType =
		new EnumSetting<>("药水类型", "要生成的药水的类型。",
			PotionType.values(), PotionType.SPLASH);
	
	public KillPotionHack()
	{
		super("杀戮神药");
		
		setCategory(Category.ITEMS);
		addSetting(potionType);
	}
	
	@Override
	public void onEnable()
	{
		// check gamemode
		if(!MC.player.getAbilities().creativeMode)
		{
			ChatUtils.error("只有创造模式可用");
			setEnabled(false);
			return;
		}
		
		// generate potion
		ItemStack stack = potionType.getSelected().createPotionStack();
		
		// give potion
		if(placeStackInHotbar(stack))
			ChatUtils.message("已生成.");
		else
			ChatUtils.error("请清空你的快捷栏中的一个槽。");
		
		setEnabled(false);
	}
	
	private boolean placeStackInHotbar(ItemStack stack)
	{
		for(int i = 0; i < 9; i++)
		{
			if(!MC.player.getInventory().getStack(i).isEmpty())
				continue;
			
			MC.player.networkHandler.sendPacket(
				new CreativeInventoryActionC2SPacket(36 + i, stack));
			return true;
		}
		
		return false;
	}
	
	private enum PotionType
	{
		NORMAL("普通", "药水", Items.POTION),
		
		SPLASH("喷溅", "喷溅药水", Items.SPLASH_POTION),
		
		LINGERING("滞留", "滞留药水", Items.LINGERING_POTION);
		
		// 不起作用
		// ARROW("箭", "箭", Items.TIPPED_ARROW);
		
		private final String name;
		private final String itemName;
		private final Item item;
		
		private PotionType(String name, String itemName, Item item)
		{
			this.name = name;
			this.itemName = itemName;
			this.item = item;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public ItemStack createPotionStack()
		{
			ItemStack stack = new ItemStack(item);
			
			NbtCompound effect = new NbtCompound();
			effect.putInt("Amplifier", 125);
			effect.putInt("Duration", 2000);
			effect.putInt("Id", 6);
			
			NbtList effects = new NbtList();
			effects.add(effect);
			
			NbtCompound nbt = new NbtCompound();
			nbt.put("CustomPotionEffects", effects);
			stack.setNbt(nbt);
			
			String name =
				"\u00a7f" + itemName + " of \u00a74\u00a7l瞬间死亡";
			stack.setCustomName(Text.literal(name));
			
			return stack;
		}
	}
}
