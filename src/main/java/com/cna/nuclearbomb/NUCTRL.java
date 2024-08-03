package com.cna.nuclearbomb;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class NUCTRL implements CommandExecutor {
    private Nuclearbomb thisplugin=null;
    @Override
    public boolean onCommand(CommandSender commandSender,Command command,String label,String[] args) {
        if(commandSender instanceof Player sender){
            ItemStack InHand=sender.getInventory().getItemInMainHand();
            if(InHand!=null&&InHand.getType()==Material.FILLED_MAP){

                sender.getInventory().setItemInMainHand(null);

                ItemStack ControlPaper=new ItemStack(Material.PAPER);
                ItemMeta meta=ControlPaper.getItemMeta();
                //注册物品

                NamespacedKey metakey=new NamespacedKey(Nuclearbomb.plugin,"nuctrlpaper_ctrltext");
                //设置插件专有物品命名空间

                meta.getPersistentDataContainer().set(
                        metakey,
                        PersistentDataType.STRING,
                        (args[0]+","+args[1]+","+args[2]+","+args[3]+","+args[4])
                        //0到1到2:目标xyz坐标
                        //3:上升到高度
                        //4:是否启用自毁
                );
                meta.setDisplayName("nuctrlpaper");
                //设置物品meta的标签

                ControlPaper.setItemMeta(meta);

                sender.getInventory().setItemInMainHand(ControlPaper);
                //sender.getInventory().setItem(1,Nuclearbomb.MissileDevice);

                sender.sendMessage("导弹控制图生成完成...");
            }else{
                sender.sendMessage("您的手中需要有一张定位地图才能生成导弹控制图！");
            }
        }
        return true;
    }
}
